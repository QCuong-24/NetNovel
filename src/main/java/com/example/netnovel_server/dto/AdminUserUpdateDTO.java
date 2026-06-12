package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserUpdateDTO {

    private String username;

    private String email;

    private String password;

    private String profilePictureUrl;

    private String profilePicturePublicId;

    private String[] roles;
}
