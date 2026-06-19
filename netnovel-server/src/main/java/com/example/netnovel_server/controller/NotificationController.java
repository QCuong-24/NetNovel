package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.NotificationCreateDTO;
import com.example.netnovel_server.dto.NotificationDTO;
import com.example.netnovel_server.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification inbox and SSE stream APIs")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get current user's notifications with optional filters")
    public ResponseEntity<Page<NotificationDTO>> getMyNotifications(
        @RequestParam(required = false) Boolean isRead,
        @RequestParam(required = false) String type,
        Pageable pageable
    ) {
        return ResponseEntity.ok(notificationService.getMyNotifications(isRead, type, pageable));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get current user's unread notifications")
    public ResponseEntity<Page<NotificationDTO>> getMyUnreadNotifications(Pageable pageable) {
        return ResponseEntity.ok(notificationService.getMyUnreadNotifications(pageable));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Count current user's unread notifications")
    public ResponseEntity<Map<String, Long>> countMyUnreadNotifications() {
        return ResponseEntity.ok(Map.of("count", notificationService.countMyUnreadNotifications()));
    }

    @GetMapping("/stream")
    @Operation(summary = "Subscribe to notification SSE stream")
    public SseEmitter stream() {
        return notificationService.subscribe();
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Delete all current user's notifications")
    public ResponseEntity<Void> deleteAllMyNotifications() {
        notificationService.deleteAllMyNotifications();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send a manual notification to a user")
    public ResponseEntity<NotificationDTO> sendNotificationToUser(
        @PathVariable Long userId,
        @RequestBody NotificationCreateDTO request
    ) {
        return ResponseEntity.ok(notificationService.sendAdminNotificationToUser(userId, request));
    }
}
