package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.BookmarkCreateDTO;
import com.example.netnovel_server.dto.BookmarkDTO;
import com.example.netnovel_server.entity.Bookmark;
import com.example.netnovel_server.entity.Chapter;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.BookmarkMapper;
import com.example.netnovel_server.repository.BookmarkRepository;
import com.example.netnovel_server.repository.ChapterRepository;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.repository.UserRepository;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;

    public BookmarkService(
        BookmarkRepository bookmarkRepository,
        UserRepository userRepository,
        NovelRepository novelRepository,
        ChapterRepository chapterRepository
    ) {
        this.bookmarkRepository = bookmarkRepository;
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookmarkDTO> getMyBookmarks(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(BookmarkMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<BookmarkDTO> getMyNovelBookmarks(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return bookmarkRepository.findByUserIdAndNovelIsNotNullOrderByCreatedAtDesc(userId, pageable)
            .map(BookmarkMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<BookmarkDTO> getMyChapterBookmarks(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return bookmarkRepository.findByUserIdAndChapterIsNotNullOrderByCreatedAtDesc(userId, pageable)
            .map(BookmarkMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public BookmarkDTO getMyBookmark(Long bookmarkId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return BookmarkMapper.toDTO(findOwnedBookmark(bookmarkId, userId));
    }

    @Transactional(readOnly = true)
    public BookmarkDTO getMyNovelBookmark(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return bookmarkRepository.findByUserIdAndNovelId(userId, novelId)
            .map(BookmarkMapper::toDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Novel bookmark not found"));
    }

    @Transactional(readOnly = true)
    public BookmarkDTO getMyChapterBookmark(Long chapterId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return bookmarkRepository.findByUserIdAndChapterId(userId, chapterId)
            .map(BookmarkMapper::toDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Chapter bookmark not found"));
    }

    @Transactional(readOnly = true)
    public boolean existsMyNovelBookmark(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return bookmarkRepository.existsByUserIdAndNovelId(userId, novelId);
    }

    @Transactional(readOnly = true)
    public boolean existsMyChapterBookmark(Long chapterId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        return bookmarkRepository.existsByUserIdAndChapterId(userId, chapterId);
    }

    @Transactional
    public BookmarkDTO createBookmark(BookmarkCreateDTO request) {
        if (request == null) {
            throw new BadRequestException("Bookmark request is required");
        }

        boolean hasNovel = request.getNovelId() != null;
        boolean hasChapter = request.getChapterId() != null;
        if (hasNovel == hasChapter) {
            throw new BadRequestException("Provide exactly one of novelId or chapterId");
        }

        return hasNovel
            ? createNovelBookmark(request.getNovelId())
            : createChapterBookmark(request.getChapterId());
    }

    @Transactional
    public BookmarkDTO createNovelBookmark(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        User user = findUser(userId);
        Novel novel = findNovel(novelId);

        if (bookmarkRepository.existsByUserIdAndNovelId(userId, novelId)) {
            throw new DuplicateResourceException("Novel bookmark already exists");
        }

        Bookmark bookmark = Bookmark.builder()
            .user(user)
            .novel(novel)
            .build();

        return BookmarkMapper.toDTO(bookmarkRepository.save(bookmark));
    }

    @Transactional
    public BookmarkDTO createChapterBookmark(Long chapterId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        User user = findUser(userId);
        Chapter chapter = findChapter(chapterId);

        if (bookmarkRepository.existsByUserIdAndChapterId(userId, chapterId)) {
            throw new DuplicateResourceException("Chapter bookmark already exists");
        }

        Bookmark bookmark = Bookmark.builder()
            .user(user)
            .chapter(chapter)
            .build();

        return BookmarkMapper.toDTO(bookmarkRepository.save(bookmark));
    }

    @Transactional
    public void deleteMyBookmark(Long bookmarkId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        bookmarkRepository.delete(findOwnedBookmark(bookmarkId, userId));
    }

    @Transactional
    public void deleteMyNovelBookmark(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        if (!bookmarkRepository.existsByUserIdAndNovelId(userId, novelId)) {
            throw new ResourceNotFoundException("Novel bookmark not found");
        }
        bookmarkRepository.deleteByUserIdAndNovelId(userId, novelId);
    }

    @Transactional
    public void deleteMyChapterBookmark(Long chapterId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        if (!bookmarkRepository.existsByUserIdAndChapterId(userId, chapterId)) {
            throw new ResourceNotFoundException("Chapter bookmark not found");
        }
        bookmarkRepository.deleteByUserIdAndChapterId(userId, chapterId);
    }

    private Bookmark findOwnedBookmark(Long bookmarkId, Long userId) {
        return bookmarkRepository.findByIdAndUserId(bookmarkId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found"));
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
