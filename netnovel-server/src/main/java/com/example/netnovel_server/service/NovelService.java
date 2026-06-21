package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.NovelCreateDTO;
import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.entity.Genre;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.NovelFollow;
import com.example.netnovel_server.entity.Status;
import com.example.netnovel_server.entity.Tag;
import com.example.netnovel_server.entity.UserEventType;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.NovelMapper;
import com.example.netnovel_server.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NovelService {

    private final NovelRepository novelRepository;
    private final GenreRepository genreRepository;
    private final TagRepository tagRepository;
    private final NovelFollowRepository novelFollowRepository;
    private final NotificationService notificationService;
    private final NovelChapterInfoService novelChapterInfoService;
    private final UserEventService userEventService;

    public NovelService(
        NovelRepository novelRepository,
        GenreRepository genreRepository,
        TagRepository tagRepository,
        NovelFollowRepository novelFollowRepository,
        NotificationService notificationService,
        NovelChapterInfoService novelChapterInfoService,
        UserEventService userEventService
    ) {
        this.novelRepository = novelRepository;
        this.genreRepository = genreRepository;
        this.tagRepository = tagRepository;
        this.novelFollowRepository = novelFollowRepository;
        this.notificationService = notificationService;
        this.novelChapterInfoService = novelChapterInfoService;
        this.userEventService = userEventService;
    }

    @Transactional(readOnly = true)
    public Page<NovelDTO> getNovels(Pageable pageable) {
        return novelRepository.findAll(pageable).map(NovelMapper::toDTO);
    }

    @Transactional
    public NovelDTO getNovel(Long novelId) {
        Novel novel = findNovel(novelId);
        userEventService.recordForCurrentUser(UserEventType.VIEW_NOVEL, novel);
        return NovelMapper.toDTO(novel);
    }

    @Transactional(readOnly = true)
    public Page<NovelDTO> searchByTitle(String title, Pageable pageable) {
        return novelRepository.findByTitleContainingIgnoreCase(title, pageable).map(NovelMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<NovelDTO> getUpdatedNovels(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return novelRepository.findByUpdateAtBetweenOrderByUpdateAtDesc(start, end, pageable).map(NovelMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<NovelDTO> getLatestUpdatedNovels(Pageable pageable) {
        return novelRepository.findAllByOrderByUpdateAtDesc(pageable).map(NovelMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<NovelDTO> getCompletedNovels(Pageable pageable) {
        return novelRepository.findByStatusOrderByUpdateAtDesc(Status.COMPLETED, pageable).map(NovelMapper::toDTO);
    }

    @Transactional
    public NovelDTO createNovel(NovelCreateDTO request) {
        if (novelRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new DuplicateResourceException("Novel title already exists");
        }

        Set<Genre> genres = resolveGenres(request.getGenres());
        Set<Tag> tags = resolveTags(request.getTags());
        validateStatus(request.getStatus());

        Novel novel = NovelMapper.toEntity(request, genres, tags);
        Novel savedNovel = novelRepository.save(novel);
        novelChapterInfoService.refresh(savedNovel.getId());
        return NovelMapper.toDTO(savedNovel);
    }

    @Transactional
    public NovelDTO updateNovel(Long novelId, NovelCreateDTO request) {
        Novel novel = findNovel(novelId);

        if (novelRepository.existsByTitleIgnoreCaseAndIdNot(request.getTitle(), novelId)) {
            throw new DuplicateResourceException("Novel title already exists");
        }

        novel.setTitle(request.getTitle());
        novel.setAuthor(request.getAuthor());
        novel.setDescription(request.getDescription());
        novel.setCoverImageUrl(request.getCoverImageUrl());
        novel.setCoverImagePublicId(request.getCoverImagePublicId());
        novel.setStatus(validateStatus(request.getStatus()));
        novel.setGenres(resolveGenres(request.getGenres()));
        novel.setTags(resolveTags(request.getTags()));

        return NovelMapper.toDTO(novelRepository.save(novel));
    }

    @Transactional
    public void deleteNovel(Long novelId) {
        Novel novel = findNovel(novelId);
        notificationService.createNotifications(
            novelFollowRepository.findByNovelId(novelId).stream()
                .map(NovelFollow::getUser)
                .toList(),
            NotificationService.TYPE_NOVEL_DELETED,
            "Novel deleted: " + novel.getTitle(),
            "A novel you followed has been deleted.",
            "/api/novels"
        );
        novelRepository.delete(novel);
    }

    private Novel findNovel(Long novelId) {
        return novelRepository.findById(novelId)
            .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));
    }

    private Set<Genre> resolveGenres(Set<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) {
            return Collections.emptySet();
        }

        return genreNames.stream()
            .map(this::findGenreByName)
            .collect(Collectors.toSet());
    }

    private Genre findGenreByName(String genreName) {
        if (genreName == null || genreName.isBlank()) {
            throw new BadRequestException("Genre name is required");
        }

        return genreRepository.findByNameIgnoreCase(genreName.trim())
            .orElseThrow(() -> new BadRequestException("Genre does not exist: " + genreName));
    }

    private Set<Tag> resolveTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptySet();
        }

        return tagNames.stream()
            .map(this::findTagByName)
            .collect(Collectors.toSet());
    }

    private Tag findTagByName(String tagName) {
        if (tagName == null || tagName.isBlank()) {
            throw new BadRequestException("Tag name is required");
        }

        return tagRepository.findByNameIgnoreCase(tagName.trim())
            .orElseThrow(() -> new BadRequestException("Tag does not exist: " + tagName));
    }

    private Status validateStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new BadRequestException("Novel status is required");
        }

        try {
            return Status.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid novel status: " + status);
        }
    }

}
