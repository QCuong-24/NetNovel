package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.NotificationDTO;
import com.example.netnovel_server.entity.Notification;
import com.example.netnovel_server.entity.User;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        User user = notification.getUser();

        return NotificationDTO.builder()
            .notificationId(notification.getId())
            .userId(user != null ? user.getId() : null)
            .type(notification.getType())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .link(notification.getLink())
            .isRead(notification.getIsRead())
            .createdAt(notification.getCreatedAt())
            .build();
    }

    public static Notification toEntity(NotificationDTO dto, User user) {
        if (dto == null) {
            return null;
        }

        return Notification.builder()
            .id(dto.getNotificationId())
            .user(user)
            .type(dto.getType())
            .title(dto.getTitle())
            .message(dto.getMessage())
            .link(dto.getLink())
            .isRead(dto.getIsRead())
            .build();
    }

}
