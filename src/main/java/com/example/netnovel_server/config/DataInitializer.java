package com.example.netnovel_server.config;

import com.example.netnovel_server.entity.AuthProvider;
import com.example.netnovel_server.entity.Role;
import com.example.netnovel_server.entity.Tag;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.repository.TagRepository;
import com.example.netnovel_server.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    private static final String[] DEFAULT_TAGS = {
        "Fantasy",
        "Sci-Fi",
        "Romance",
        "Mystery",
        "Thriller",
        "Horror",
        "Historical",
        "Adventure",
        "Comedy",
        "Drama",
        "Action",
        "Slice of Life",
        "Supernatural",
        "Psychological",
        "Tragedy",
        "Mythology",
        "Wuxia",
        "Xianxia",
        "Crawled"
    };

    @Bean
    public CommandLineRunner seedData(
        UserRepository userRepository,
        TagRepository tagRepository,
        PasswordEncoder passwordEncoder
    ) {
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

            for (String tagName : DEFAULT_TAGS) {
                createTagIfMissing(tagRepository, tagName);
            }
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

    private void createTagIfMissing(TagRepository tagRepository, String name) {
        if (tagRepository.existsByName(name)) {
            return;
        }

        tagRepository.save(Tag.builder().name(name).build());
    }
}
