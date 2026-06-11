package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.UserDTO;
import com.example.netnovel_server.entity.Role;
import com.example.netnovel_server.entity.User;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .profilePictureUrl(user.getProfilePictureUrl())
            .roles(toRoleNames(user.getRoles()))
            .provider(user.getProvider() != null ? user.getProvider().name() : null)
            .createAt(user.getCreateAt())
            .build();
    }

    public static User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
            .id(dto.getUserId())
            .username(dto.getUsername())
            .email(dto.getEmail())
            .profilePictureUrl(dto.getProfilePictureUrl())
            .roles(toRoles(dto.getRoles()))
            .build();
    }

    private static String[] toRoleNames(Set<Role> roles) {
        if (roles == null) {
            return new String[0];
        }

        return roles.stream()
            .map(Role::name)
            .toArray(String[]::new);
    }

    private static Set<Role> toRoles(String[] roles) {
        if (roles == null) {
            return Set.of();
        }

        return Arrays.stream(roles)
            .filter(role -> role != null && !role.isBlank())
            .map(role -> Role.valueOf(role.trim().toUpperCase()))
            .collect(Collectors.toSet());
    }
}
