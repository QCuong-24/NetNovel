package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.*;
import com.example.netnovel_server.entity.*;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.exception.UnauthorizedException;
import com.example.netnovel_server.mapper.UserMapper;
import com.example.netnovel_server.repository.RefreshTokenRepository;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.TokenHashUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthService(
        UserRepository userRepository,
        RefreshTokenRepository refreshTokenRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        GoogleTokenVerifier googleTokenVerifier
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .profilePictureUrl(request.getProfilePictureUrl())
            .provider(AuthProvider.LOCAL)
            .roles(Set.of(Role.USER))
            .build();

        return buildAuthResponse(userRepository.save(user), "Register successfully");
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getProvider() != AuthProvider.LOCAL || user.getPassword() == null) {
            throw new BadRequestException("This account uses Google login");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return buildAuthResponse(user, "Login successfully");
    }

    @Transactional
    public AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO request) {
        GoogleTokenVerifier.GoogleUserInfo googleUser = googleTokenVerifier.verify(request.getIdToken());

        User user = userRepository.findByEmail(googleUser.email())
            .map(existingUser -> handleExistingGoogleUser(existingUser, googleUser))
            .orElseGet(() -> createGoogleUser(googleUser));

        return buildAuthResponse(user, "Login with Google successfully");
    }

    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String tokenHash = TokenHashUtils.sha256(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        if (Boolean.TRUE.equals(storedToken.getRevoked()) || storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        storedToken.setRevoked(true);
        User user = storedToken.getUser();
        return buildAuthResponse(user, "Refresh token successfully");
    }

    @Transactional
    public void logout(RefreshTokenRequestDTO request) {
        String tokenHash = TokenHashUtils.sha256(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(Long userId) {
        return userRepository.findById(userId)
            .map(UserMapper::toDTO)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private User handleExistingGoogleUser(User user, GoogleTokenVerifier.GoogleUserInfo googleUser) {
        if (user.getProvider() == AuthProvider.LOCAL) {
            throw new BadRequestException("This email is already registered with local login");
        }

        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderId(googleUser.providerId());
        user.setProfilePictureUrl(googleUser.picture());
        return userRepository.save(user);
    }

    private User createGoogleUser(GoogleTokenVerifier.GoogleUserInfo googleUser) {
        User user = User.builder()
            .username(buildGoogleUsername(googleUser))
            .email(googleUser.email())
            .profilePictureUrl(googleUser.picture())
            .provider(AuthProvider.GOOGLE)
            .providerId(googleUser.providerId())
            .roles(Set.of(Role.USER))
            .build();

        return userRepository.save(user);
    }

    private String buildGoogleUsername(GoogleTokenVerifier.GoogleUserInfo googleUser) {
        String baseName = googleUser.name() != null && !googleUser.name().isBlank()
            ? googleUser.name().trim()
            : googleUser.email().substring(0, googleUser.email().indexOf("@"));
        String username = baseName.replaceAll("\\s+", "_");

        if (!userRepository.existsByUsername(username)) {
            return username;
        }

        return username + "_" + googleUser.providerId().substring(0, Math.min(8, googleUser.providerId().length()));
    }

    private AuthResponseDTO buildAuthResponse(User user, String message) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveRefreshToken(user, refreshToken);

        return AuthResponseDTO.builder()
            .user(UserMapper.toDTO(user))
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .message(message)
            .build();
    }

    private void saveRefreshToken(User user, String refreshToken) {
        RefreshToken token = RefreshToken.builder()
            .user(user)
            .tokenHash(TokenHashUtils.sha256(refreshToken))
            .expiresAt(LocalDateTime.ofInstant(jwtService.extractExpiration(refreshToken), ZoneOffset.UTC))
            .revoked(false)
            .build();

        refreshTokenRepository.save(token);
    }
}
