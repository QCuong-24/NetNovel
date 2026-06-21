package com.example.netnovel_server.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    private Long userId;

    private String username;

    private String profilePictureUrl;

    private String[] roles;

    private LocalDateTime createAt;
}
