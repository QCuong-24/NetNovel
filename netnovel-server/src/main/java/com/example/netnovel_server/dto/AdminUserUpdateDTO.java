package com.example.netnovel_server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserUpdateDTO {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
    private String password;

    @Size(max = 1000, message = "Profile picture URL must not exceed 1000 characters")
    private String profilePictureUrl;

    @Size(max = 255, message = "Profile picture public id must not exceed 255 characters")
    private String profilePicturePublicId;

    private String[] roles;
}
