package com.example.netnovel_server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private UserDTO user;

    private String accessToken;

    private String refreshToken;

    private String message;
}
