package com.example.netnovel_server.utility;

import com.example.netnovel_server.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.valueOf(authentication.getName()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    public static Long getCurrentUserIdOrThrow() {
        return getCurrentUserId()
            .orElseThrow(() -> new UnauthorizedException("Authentication required"));
    }

    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Set<String> roleNames = Arrays.stream(roles)
            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
            .collect(Collectors.toSet());

        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(roleNames::contains);
    }
}
