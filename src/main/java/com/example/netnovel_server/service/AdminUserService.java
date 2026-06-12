package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.AdminUserCreateDTO;
import com.example.netnovel_server.dto.AdminUserUpdateDTO;
import com.example.netnovel_server.dto.UserDTO;
import com.example.netnovel_server.entity.AuthProvider;
import com.example.netnovel_server.entity.Role;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.ForbiddenException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.UserMapper;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public UserDTO getUser(Long userId) {
        return UserMapper.toDTO(findUser(userId));
    }

    @Transactional
    public UserDTO createUser(AdminUserCreateDTO request) {
        validateRequired(request);
        validateUniqueEmail(request.getEmail());
        validateUniqueUsername(request.getUsername());

        User user = User.builder()
            .username(request.getUsername().trim())
            .email(request.getEmail().trim())
            .password(passwordEncoder.encode(request.getPassword()))
            .profilePictureUrl(trimToNull(request.getProfilePictureUrl()))
            .profilePicturePublicId(trimToNull(request.getProfilePicturePublicId()))
            .provider(AuthProvider.LOCAL)
            .roles(resolveRoles(request.getRoles()))
            .build();

        return UserMapper.toDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO updateUser(Long userId, AdminUserUpdateDTO request) {
        if (request == null) {
            throw new BadRequestException("User update request is required");
        }

        User user = findUser(userId);

        if (request.getUsername() != null) {
            String username = requireNotBlank(request.getUsername(), "Username is required");
            validateUniqueUsername(username, userId);
            user.setUsername(username);
        }

        if (request.getEmail() != null) {
            String email = requireNotBlank(request.getEmail(), "Email is required");
            validateUniqueEmail(email, userId);
            user.setEmail(email);
        }

        if (request.getPassword() != null) {
            String password = requireNotBlank(request.getPassword(), "Password is required");
            user.setPassword(passwordEncoder.encode(password));
            user.setProvider(AuthProvider.LOCAL);
            user.setProviderId(null);
        }

        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(trimToNull(request.getProfilePictureUrl()));
        }

        if (request.getProfilePicturePublicId() != null) {
            user.setProfilePicturePublicId(trimToNull(request.getProfilePicturePublicId()));
        }

        if (request.getRoles() != null) {
            Set<Role> roles = resolveRoles(request.getRoles());
            preventSelfAdminRemoval(userId, roles);
            user.setRoles(roles);
        }

        return UserMapper.toDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        Long currentUserId = SecurityUtils.getCurrentUserIdOrThrow();
        if (currentUserId.equals(userId)) {
            throw new ForbiddenException("Admin cannot delete their own account");
        }

        User user = findUser(userId);
        userRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateRequired(AdminUserCreateDTO request) {
        if (request == null) {
            throw new BadRequestException("User create request is required");
        }
        requireNotBlank(request.getUsername(), "Username is required");
        requireNotBlank(request.getEmail(), "Email is required");
        requireNotBlank(request.getPassword(), "Password is required");
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email.trim())) {
            throw new DuplicateResourceException("Email already exists");
        }
    }

    private void validateUniqueEmail(String email, Long userId) {
        if (userRepository.existsByEmailAndIdNot(email.trim(), userId)) {
            throw new DuplicateResourceException("Email already exists");
        }
    }

    private void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(username.trim())) {
            throw new DuplicateResourceException("Username already exists");
        }
    }

    private void validateUniqueUsername(String username, Long userId) {
        if (userRepository.existsByUsernameAndIdNot(username.trim(), userId)) {
            throw new DuplicateResourceException("Username already exists");
        }
    }

    private Set<Role> resolveRoles(String[] roleNames) {
        if (roleNames == null || roleNames.length == 0) {
            return Set.of(Role.USER);
        }

        Set<Role> roles = Arrays.stream(roleNames)
            .map(role -> requireNotBlank(role, "Role is required"))
            .map(role -> {
                try {
                    return Role.valueOf(role.trim().toUpperCase());
                } catch (IllegalArgumentException exception) {
                    throw new BadRequestException("Invalid role: " + role);
                }
            })
            .collect(Collectors.toSet());

        roles.add(Role.USER);
        return roles;
    }

    private void preventSelfAdminRemoval(Long targetUserId, Set<Role> roles) {
        Long currentUserId = SecurityUtils.getCurrentUserIdOrThrow();
        if (currentUserId.equals(targetUserId) && !roles.contains(Role.ADMIN)) {
            throw new ForbiddenException("Admin cannot remove their own admin role");
        }
    }

    private String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
