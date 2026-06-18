package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {

    private String username;

    private String email;

    private String password;

    private String profilePictureUrl;
}
