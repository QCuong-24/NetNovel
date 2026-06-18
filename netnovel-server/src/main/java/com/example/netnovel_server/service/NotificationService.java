package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.NotificationDTO;
import com.example.netnovel_server.entity.Notification;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.NotificationMapper;
import com.example.netnovel_server.repository.NotificationRepository;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;

@Service
public class NotificationService {

    public static final String TYPE_WELCOME = "WELCOME";
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
    public List<NotificationDTO> getMyNotifications() {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(NotificationMapper::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getMyUnreadNotifications() {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false).stream()
            .map(NotificationMapper::toDTO)
            .toList();
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
    public void createNotifications(Collection<User> users, String type, String title, String message, String link) {
        for (User user : users) {
            createNotification(user, type, title, message, link);
        }
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
