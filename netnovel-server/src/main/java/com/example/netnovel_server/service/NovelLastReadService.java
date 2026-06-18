package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.NovelLastReadDTO;
import com.example.netnovel_server.dto.NovelLastReadUpdateDTO;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.NovelLastRead;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.NovelLastReadMapper;
import com.example.netnovel_server.repository.ChapterRepository;
import com.example.netnovel_server.repository.NovelLastReadRepository;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NovelLastReadService {

    private final NovelLastReadRepository novelLastReadRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;

    public NovelLastReadService(
        NovelLastReadRepository novelLastReadRepository,
        UserRepository userRepository,
        NovelRepository novelRepository,
        ChapterRepository chapterRepository
    ) {
        this.novelLastReadRepository = novelLastReadRepository;
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
    }

    @Transactional(readOnly = true)
    public Page<NovelLastReadDTO> getMyLastReads(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return novelLastReadRepository.findByUserIdOrderByLastReadAtDesc(userId, pageable)
            .map(NovelLastReadMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public NovelLastReadDTO getMyNovelLastRead(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return novelLastReadRepository.findByUserIdAndNovelId(userId, novelId)
            .map(NovelLastReadMapper::toDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Novel last read not found"));
    }

    @Transactional
    public NovelLastReadDTO updateMyLastRead(NovelLastReadUpdateDTO request) {
        if (request == null || request.getChapterId() == null) {
            throw new BadRequestException("chapterId is required");
        }

        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        User user = findUser(userId);
        Chapter chapter = findChapter(request.getChapterId());
        Novel novel = chapter.getNovel();

        if (request.getNovelId() != null && !request.getNovelId().equals(novel.getId())) {
            throw new BadRequestException("Chapter does not belong to the provided novel");
        }

        NovelLastRead lastRead = novelLastReadRepository.findByUserIdAndNovelId(userId, novel.getId())
            .orElseGet(() -> NovelLastReadMapper.toEntity(user, novel, chapter));

        lastRead.setUser(user);
        lastRead.setNovel(novel);
        lastRead.setChapter(chapter);
        lastRead.setLastReadAt(LocalDateTime.now());

        return NovelLastReadMapper.toDTO(novelLastReadRepository.save(lastRead));
    }

    @Transactional
    public NovelLastReadDTO updateMyNovelLastRead(Long novelId, Long chapterId) {
        Novel novel = findNovel(novelId);
        Chapter chapter = findChapter(chapterId);
        if (!chapter.getNovel().getId().equals(novel.getId())) {
            throw new BadRequestException("Chapter does not belong to the novel");
        }
        return updateMyLastRead(NovelLastReadUpdateDTO.builder()
            .novelId(novelId)
            .chapterId(chapterId)
            .build());
    }

    @Transactional
    public void deleteMyNovelLastRead(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        if (novelLastReadRepository.findByUserIdAndNovelId(userId, novelId).isEmpty()) {
            throw new ResourceNotFoundException("Novel last read not found");
        }
        novelLastReadRepository.deleteByUserIdAndNovelId(userId, novelId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Novel findNovel(Long novelId) {
        return novelRepository.findById(novelId)
            .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));
    }

    private Chapter findChapter(Long chapterId) {
        return chapterRepository.findById(chapterId)
            .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
    }
}
