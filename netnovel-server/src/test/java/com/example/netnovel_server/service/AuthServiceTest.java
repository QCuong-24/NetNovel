package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.AuthResponseDTO;
import com.example.netnovel_server.dto.LoginRequestDTO;
import com.example.netnovel_server.dto.RefreshTokenRequestDTO;
import com.example.netnovel_server.dto.RegisterRequestDTO;
import com.example.netnovel_server.entity.AuthProvider;
import com.example.netnovel_server.entity.RefreshToken;
import com.example.netnovel_server.entity.Role;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.UnauthorizedException;
import com.example.netnovel_server.repository.RefreshTokenRepository;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.TokenHashUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    /*
     * Unit scope:
     * - AuthService business rules only.
     * - All persistence, password hashing, JWT creation, Google verification, and notifications are mocked.
     * - No Spring context or database is started.
     */

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @Mock
    private NotificationService notificationService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            userRepository,
            refreshTokenRepository,
            passwordEncoder,
            jwtService,
            googleTokenVerifier,
            notificationService
        );
    }

    @Test
    void registerCreatesLocalUserAndReturnsTokens() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
            .username("reader")
            .email("reader@example.com")
            .password("plain-password")
            .profilePictureUrl("https://example.com/avatar.png")
            .build();
        User savedUser = localUser(10L, "reader@example.com", "encoded-password");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        stubTokenGeneration(savedUser);

        AuthResponseDTO response = authService.register(request);

        assertEquals("Register successfully", response.getMessage());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("reader@example.com", response.getUser().getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User userToSave = userCaptor.getValue();
        assertEquals("encoded-password", userToSave.getPassword());
        assertEquals(AuthProvider.LOCAL, userToSave.getProvider());
        assertTrue(userToSave.getRoles().contains(Role.USER));

        verify(notificationService).createNotification(
            savedUser,
            NotificationService.TYPE_WELCOME,
            "Welcome to NetNovel",
            "Your account has been created successfully.",
            "/"
        );
        verifyRefreshTokenSaved(savedUser, "refresh-token");
    }

    // Registration guard rails: duplicate data should stop before encoding or saving.
    @Test
    void registerRejectsDuplicateEmailBeforeEncodingPassword() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
            .username("reader")
            .email("reader@example.com")
            .password("plain-password")
            .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    // Password-login flow: local account + matching password should issue and persist a refresh token.
    @Test
    void loginReturnsTokensForValidLocalCredentials() {
        LoginRequestDTO request = LoginRequestDTO.builder()
            .email("reader@example.com")
            .password("plain-password")
            .build();
        User user = localUser(20L, request.getEmail(), "encoded-password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        stubTokenGeneration(user);

        AuthResponseDTO response = authService.login(request);

        assertEquals("Login successfully", response.getMessage());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verifyRefreshTokenSaved(user, "refresh-token");
    }

    @Test
    void loginRejectsWrongPassword() {
        LoginRequestDTO request = LoginRequestDTO.builder()
            .email("reader@example.com")
            .password("wrong-password")
            .build();
        User user = localUser(20L, request.getEmail(), "encoded-password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));

        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginRejectsGoogleAccountForPasswordLogin() {
        LoginRequestDTO request = LoginRequestDTO.builder()
            .email("reader@example.com")
            .password("plain-password")
            .build();
        User googleUser = User.builder()
            .id(30L)
            .username("reader")
            .email(request.getEmail())
            .provider(AuthProvider.GOOGLE)
            .roles(Set.of(Role.USER))
            .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(googleUser));

        assertThrows(BadRequestException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(refreshTokenRepository, never()).save(any());
    }

    // Refresh-token flow: the stored token is single-use, then a new token pair is issued.
    @Test
    void refreshTokenRevokesStoredTokenAndIssuesNewTokens() {
        User user = localUser(40L, "reader@example.com", "encoded-password");
        RefreshToken storedToken = RefreshToken.builder()
            .user(user)
            .tokenHash(TokenHashUtils.sha256("old-refresh-token"))
            .expiresAt(LocalDateTime.now().plusDays(1))
            .revoked(false)
            .build();
        RefreshTokenRequestDTO request = RefreshTokenRequestDTO.builder()
            .refreshToken("old-refresh-token")
            .build();

        when(jwtService.isRefreshToken(request.getRefreshToken())).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(TokenHashUtils.sha256(request.getRefreshToken())))
            .thenReturn(Optional.of(storedToken));
        stubTokenGeneration(user);

        AuthResponseDTO response = authService.refreshToken(request);

        assertEquals("Refresh token successfully", response.getMessage());
        assertTrue(storedToken.getRevoked());
        verifyRefreshTokenSaved(user, "refresh-token");
    }

    @Test
    void refreshTokenRejectsAccessTokens() {
        RefreshTokenRequestDTO request = RefreshTokenRequestDTO.builder()
            .refreshToken("access-token")
            .build();

        when(jwtService.isRefreshToken(request.getRefreshToken())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(request));

        verify(refreshTokenRepository, never()).findByTokenHash(any());
    }

    // Helper methods for stubbing and verifying token generation and persistence.
    private void stubTokenGeneration(User user) {
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.extractExpiration("refresh-token")).thenReturn(Instant.now().plus(7, ChronoUnit.DAYS));
    }

    // Verify that a refresh token was saved with the correct user, hash, and expiration.
    private void verifyRefreshTokenSaved(User user, String refreshToken) {
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken savedToken = tokenCaptor.getValue();

        assertEquals(user, savedToken.getUser());
        assertEquals(TokenHashUtils.sha256(refreshToken), savedToken.getTokenHash());
        assertFalse(savedToken.getRevoked());
        assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    // Helper method to create a local user for testing purposes.
    private static User localUser(Long id, String email, String encodedPassword) {
        return User.builder()
            .id(id)
            .username("reader")
            .email(email)
            .password(encodedPassword)
            .provider(AuthProvider.LOCAL)
            .roles(Set.of(Role.USER))
            .build();
    }
}
