package com.example.netnovel_server.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long notificationId;

    private Long userId;

    private String type;

    private String title;

    private String message;

    private String link;

    private Boolean isRead;

    private LocalDateTime createdAt;
}
