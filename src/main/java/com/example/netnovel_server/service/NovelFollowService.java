package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.NovelFollowDTO;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.NovelFollowMapper;
import com.example.netnovel_server.repository.NovelFollowRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NovelFollowService {

    private final NovelFollowRepository novelFollowRepository;

    public NovelFollowService(NovelFollowRepository novelFollowRepository) {
        this.novelFollowRepository = novelFollowRepository;
    }

    @Transactional(readOnly = true)
    public Page<NovelFollowDTO> getMyFollowedNovels(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return novelFollowRepository.findByUserIdOrderByFollowedAtDesc(userId, pageable)
            .map(NovelFollowMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public NovelFollowDTO getMyFollowedNovel(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return novelFollowRepository.findByUserIdAndNovelId(userId, novelId)
            .map(NovelFollowMapper::toDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Novel follow not found"));
    }

    @Transactional(readOnly = true)
    public boolean existsMyFollowedNovel(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return novelFollowRepository.existsByUserIdAndNovelId(userId, novelId);
    }
}
