package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.AuthResponseDTO;
import com.example.netnovel_server.dto.UserDTO;
import com.example.netnovel_server.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    /*
     * Controller contract scope:
     * - Uses standalone MockMvc, not the full Spring context.
     * - Verifies HTTP route/body/status and that AuthService receives the expected DTO fields.
     * - Security filters are not loaded; /me authentication is supplied directly to the controller.
     */

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    void loginPostsCredentialsAndReturnsAuthResponse() throws Exception {
        when(authService.login(argThat(request ->
            "reader@example.com".equals(request.getEmail()) && "secret".equals(request.getPassword())
        ))).thenReturn(authResponse("Login successfully"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"reader@example.com\",\"password\":\"secret\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Login successfully"))
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.user.email").value("reader@example.com"));
    }

    @Test
    void registerPostsRegistrationPayloadAndReturnsAuthResponse() throws Exception {
        when(authService.register(argThat(request ->
            "reader".equals(request.getUsername())
                && "reader@example.com".equals(request.getEmail())
                && "secret".equals(request.getPassword())
        ))).thenReturn(authResponse("Register successfully"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"reader\",\"email\":\"reader@example.com\",\"password\":\"secret\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Register successfully"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void logoutPostsRefreshTokenAndReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"refresh-token\"}"))
            .andExpect(status().isNoContent());

        verify(authService).logout(argThat(request -> "refresh-token".equals(request.getRefreshToken())));
    }

    @Test
    void meReadsUserIdFromAuthenticationName() throws Exception {
        when(authService.getCurrentUser(7L)).thenReturn(user());

        mockMvc.perform(get("/api/auth/me")
                .principal(new TestingAuthenticationToken("7", null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(7))
            .andExpect(jsonPath("$.email").value("reader@example.com"));
    }

    private static AuthResponseDTO authResponse(String message) {
        return AuthResponseDTO.builder()
            .user(user())
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .message(message)
            .build();
    }

    private static UserDTO user() {
        return UserDTO.builder()
            .userId(7L)
            .username("reader")
            .email("reader@example.com")
            .build();
    }
}
