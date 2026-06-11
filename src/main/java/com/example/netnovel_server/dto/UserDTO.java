package com.example.netnovel_server.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long userId;

    private String username;

    private String email;

    private String profilePictureUrl;

    private String[] roles;

    private String provider;

    private LocalDateTime createAt;
}
