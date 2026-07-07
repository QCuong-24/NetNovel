package com.example.netnovel_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationCreateDTO {

    @Size(max = 64, message = "Notification type must not exceed 64 characters")
    private String type;

    @NotBlank(message = "Notification title is required")
    @Size(max = 255, message = "Notification title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Notification message is required")
    @Size(max = 2000, message = "Notification message must not exceed 2000 characters")
    private String message;

    @Size(max = 1000, message = "Notification link must not exceed 1000 characters")
    private String link;
}
