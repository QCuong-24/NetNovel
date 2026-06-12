package com.example.netnovel_server.config;

import com.example.netnovel_server.entity.AuthProvider;
import com.example.netnovel_server.entity.Role;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfMissing(
                userRepository,
                passwordEncoder,
                "manager",
                "manager@netnovel.local",
                "12345678",
                Set.of(Role.USER, Role.MANAGER)
            );

            createUserIfMissing(
                userRepository,
                passwordEncoder,
                "admin",
                "admin@netnovel.local",
                "12345678",
                Set.of(Role.USER, Role.MANAGER, Role.ADMIN)
            );

            createUserIfMissing(
                userRepository,
                passwordEncoder,
                "user",
                "user@netnovel.local",
                "12345678",
                Set.of(Role.USER)
            );
        };
    }

    private void createUserIfMissing(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        String username,
        String email,
        String password,
        Set<Role> roles
    ) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User user = User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .provider(AuthProvider.LOCAL)
            .roles(roles)
            .build();

        userRepository.save(user);
    }
}
