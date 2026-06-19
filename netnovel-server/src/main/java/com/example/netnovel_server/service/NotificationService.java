package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.NotificationDTO;
import com.example.netnovel_server.dto.NotificationCreateDTO;
import com.example.netnovel_server.entity.Notification;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.NotificationMapper;
import com.example.netnovel_server.repository.NotificationRepository;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;

@Service
public class NotificationService {

    public static final String TYPE_WELCOME = "WELCOME";
    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_ADMIN = "ADMIN";
    public static final String TYPE_COMMENT_REPLY = "COMMENT_REPLY";
    public static final String TYPE_NOVEL_DELETED = "NOVEL_DELETED";
    // Reserved for later async/batched follower notifications.
    public static final String TYPE_NEW_CHAPTER = "NEW_CHAPTER";
    public static final String TYPE_CHAPTER_UPDATED = "CHAPTER_UPDATED";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationSseService notificationSseService;

    public NotificationService(
        NotificationRepository notificationRepository,
        UserRepository userRepository,
        NotificationSseService notificationSseService
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationSseService = notificationSseService;
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getMyNotifications(Boolean isRead, String type, Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return notificationRepository.findMyNotifications(userId, isRead, normalizeType(type), pageable)
            .map(NotificationMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getMyUnreadNotifications(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable)
            .map(NotificationMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public long countMyUnreadNotifications() {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setIsRead(true);
        return NotificationMapper.toDTO(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllMyNotifications() {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        notificationRepository.deleteByUserId(userId);
    }

    public SseEmitter subscribe() {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return notificationSseService.subscribe(userId);
    }

    @Transactional
    public NotificationDTO createNotification(User user, String type, String title, String message, String link) {
        Notification notification = Notification.builder()
            .user(user)
            .type(type)
            .title(title)
            .message(message)
            .link(link)
            .isRead(false)
            .build();

        NotificationDTO dto = NotificationMapper.toDTO(notificationRepository.save(notification));
        notificationSseService.sendToUser(user.getId(), dto);
        return dto;
    }

    @Transactional
    public NotificationDTO sendAdminNotificationToUser(Long userId, NotificationCreateDTO request) {
        User user = findUser(userId);
        return createNotification(
            user,
            normalizeCreateType(request),
            requireText(request != null ? request.getTitle() : null, "Notification title is required"),
            requireText(request != null ? request.getMessage() : null, "Notification message is required"),
            request != null ? normalizeNullableText(request.getLink()) : null
        );
    }

    @Transactional
    public void createNotifications(Collection<User> users, String type, String title, String message, String link) {
        for (User user : users) {
            createNotification(user, type, title, message, link);
        }
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        return type.trim().toUpperCase();
    }

    private String normalizeCreateType(NotificationCreateDTO request) {
        if (request == null || request.getType() == null || request.getType().isBlank()) {
            return TYPE_ADMIN;
        }
        return request.getType().trim().toUpperCase();
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
