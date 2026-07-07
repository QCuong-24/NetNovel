package com.example.netnovel_server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleLoginRequestDTO {

    @NotBlank(message = "Google id token is required")
    private String idToken;
}
