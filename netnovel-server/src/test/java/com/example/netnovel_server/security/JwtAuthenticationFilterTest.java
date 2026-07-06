package com.example.netnovel_server.security;

import com.example.netnovel_server.service.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    /*
     * JWT filter scope:
     * - Verifies where tokens are read from: Authorization Bearer header and notification-stream access_token query.
     * - Verifies successful tokens populate SecurityContext with the user's authorities.
     * - Verifies invalid tokens clear SecurityContext but still continue the filter chain.
     */

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // Test cases for JWT authentication filter
    @Test
    void bearerTokenAuthenticatesUserAndContinuesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/bookmarks");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer access-token");
        UserDetails userDetails = User.withUsername("7")
            .password("")
            .roles("MANAGER")
            .build();

        when(jwtService.extractUserId("access-token")).thenReturn(7L);
        when(userDetailsService.loadUserById(7L)).thenReturn(userDetails);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("7", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_MANAGER".equals(authority.getAuthority())));
        verify(filterChain).doFilter(request, response);
    }

    // Test cases for notification stream access token
    @Test
    void notificationStreamCanReadAccessTokenFromQueryParam() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/notifications/stream");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setServletPath("/api/notifications/stream");
        request.addParameter("access_token", "stream-token");
        UserDetails userDetails = User.withUsername("8")
            .password("")
            .roles("USER")
            .build();

        when(jwtService.extractUserId("stream-token")).thenReturn(8L);
        when(userDetailsService.loadUserById(8L)).thenReturn(userDetails);

        filter.doFilter(request, response, filterChain);

        assertEquals("8", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    // Test cases for invalid token handling
    @Test
    void invalidTokenClearsSecurityContextAndContinuesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/bookmarks");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer broken-token");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("old", null));

        when(jwtService.extractUserId("broken-token")).thenThrow(new IllegalArgumentException("bad token"));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserById(7L);
        verify(filterChain).doFilter(request, response);
    }

    // Test cases for missing token handling
    @Test
    void missingTokenSkipsJwtLookupAndContinuesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/bookmarks");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userDetailsService);
        verify(filterChain).doFilter(request, response);
    }
}
