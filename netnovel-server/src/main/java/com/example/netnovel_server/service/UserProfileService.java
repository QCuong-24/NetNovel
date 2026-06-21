package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.UserProfileDTO;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.UserMapper;
import com.example.netnovel_server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        return userRepository.findById(userId)
            .map(UserMapper::toProfileDTO)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
