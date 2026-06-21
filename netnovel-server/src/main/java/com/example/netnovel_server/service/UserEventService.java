package com.example.netnovel_server.service;

import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.UserEvent;
import com.example.netnovel_server.entity.UserEventType;
import com.example.netnovel_server.repository.UserEventRepository;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserEventService {

    private final UserRepository userRepository;
    private final UserEventRepository userEventRepository;

    public UserEventService(UserRepository userRepository, UserEventRepository userEventRepository) {
        this.userRepository = userRepository;
        this.userEventRepository = userEventRepository;
    }

    @Transactional
    public void recordForCurrentUser(UserEventType eventType) {
        recordForCurrentUser(eventType, null, null);
    }

    @Transactional
    public void recordForCurrentUser(UserEventType eventType, Novel novel) {
        recordForCurrentUser(eventType, novel, null);
    }

    @Transactional
    public void recordForCurrentUser(UserEventType eventType, Novel novel, Chapter chapter) {
        SecurityUtils.getCurrentUserId().ifPresent(userId -> userRepository.findById(userId).ifPresent(user ->
            userEventRepository.save(UserEvent.builder()
                .user(user)
                .novel(novel)
                .chapter(chapter)
                .eventType(eventType)
                .build())
        ));
    }
}
