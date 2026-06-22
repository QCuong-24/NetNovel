package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.NovelInteractionDTO;
import com.example.netnovel_server.entity.*;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.repository.*;
import com.example.netnovel_server.utility.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class NovelInteractionService {

    private final NovelRepository novelRepository;
    private final UserRepository userRepository;
    private final NovelViewStatRepository novelViewStatRepository;
    private final NovelUserViewRepository novelUserViewRepository;
    private final ChapterRepository chapterRepository;
    private final UserEventService userEventService;
    private final NovelFollowRepository novelFollowRepository;
    private final NovelLikeRepository novelLikeRepository;
    private final BookmarkRepository bookmarkRepository;

    public NovelInteractionService(
        NovelRepository novelRepository,
        UserRepository userRepository,
        NovelViewStatRepository novelViewStatRepository,
        NovelUserViewRepository novelUserViewRepository,
        ChapterRepository chapterRepository,
        UserEventService userEventService,
        NovelFollowRepository novelFollowRepository,
        NovelLikeRepository novelLikeRepository,
        BookmarkRepository bookmarkRepository
    ) {
        this.novelRepository = novelRepository;
        this.userRepository = userRepository;
        this.novelViewStatRepository = novelViewStatRepository;
        this.novelUserViewRepository = novelUserViewRepository;
        this.chapterRepository = chapterRepository;
        this.userEventService = userEventService;
        this.novelFollowRepository = novelFollowRepository;
        this.novelLikeRepository = novelLikeRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @Transactional
    public NovelInteractionDTO increaseView(Long novelId, Long chapterId) {
        Novel novel = findNovel(novelId);
        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
        if (!chapter.getNovel().getId().equals(novelId)) {
            throw new BadRequestException("Chapter does not belong to novel");
        }

        Optional<User> user = SecurityUtils.getCurrentUserId().map(this::findUser);

        novelViewStatRepository.incrementViewCount(novelId, LocalDate.now());
        user.ifPresent(currentUser -> novelUserViewRepository.incrementViewCount(novelId, currentUser.getId()));
        userEventService.recordForCurrentUser(UserEventType.VIEW_CHAPTER, novel, chapter);
        novelRepository.incrementViews(novelId);
        return buildInteractionDTO(findNovel(novelId), user.map(User::getId));
    }

    @Transactional
    public void recordNovelView(Long novelId) {
        userEventService.recordForCurrentUser(UserEventType.VIEW_NOVEL, findNovel(novelId));
    }

    @Transactional
    public NovelInteractionDTO toggleFollow(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        User user = findUser(userId);
        Novel novel = findNovel(novelId);

        Optional<NovelFollow> existingFollow = novelFollowRepository.findByUserIdAndNovelId(userId, novelId);
        if (existingFollow.isPresent()) {
            novelFollowRepository.delete(existingFollow.get());
            novelRepository.decrementFollows(novelId);
            userEventService.recordForCurrentUser(UserEventType.UNFOLLOW_NOVEL, novel);
        } else {
            NovelFollow follow = NovelFollow.builder()
                .user(user)
                .novel(novel)
                .build();
            novelFollowRepository.save(follow);
            novelRepository.incrementFollows(novelId);
            userEventService.recordForCurrentUser(UserEventType.FOLLOW_NOVEL, novel);
        }

        return buildInteractionDTO(findNovel(novelId), Optional.of(userId));
    }

    @Transactional
    public NovelInteractionDTO toggleLike(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        User user = findUser(userId);
        Novel novel = findNovel(novelId);

        Optional<NovelLike> existingLike = novelLikeRepository.findByUserIdAndNovelId(userId, novelId);
        if (existingLike.isPresent()) {
            novelLikeRepository.delete(existingLike.get());
            novelRepository.decrementLikes(novelId);
            userEventService.recordForCurrentUser(UserEventType.UNLIKE_NOVEL, novel);
        } else {
            NovelLike like = NovelLike.builder()
                .user(user)
                .novel(novel)
                .build();
            novelLikeRepository.save(like);
            novelRepository.incrementLikes(novelId);
            userEventService.recordForCurrentUser(UserEventType.LIKE_NOVEL, novel);
        }

        return buildInteractionDTO(findNovel(novelId), Optional.of(userId));
    }

    @Transactional
    public NovelInteractionDTO toggleBookmark(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        User user = findUser(userId);
        Novel novel = findNovel(novelId);

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndNovelId(userId, novelId);
        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
            novelRepository.decrementBookmarks(novelId);
            userEventService.recordForCurrentUser(UserEventType.UNBOOKMARK_NOVEL, novel);
        } else {
            Bookmark bookmark = Bookmark.builder()
                .user(user)
                .novel(novel)
                .build();
            bookmarkRepository.save(bookmark);
            novelRepository.incrementBookmarks(novelId);
            userEventService.recordForCurrentUser(UserEventType.BOOKMARK_NOVEL, novel);
        }

        return buildInteractionDTO(findNovel(novelId), Optional.of(userId));
    }

    @Transactional(readOnly = true)
    public NovelInteractionDTO getMyInteraction(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        Novel novel = findNovel(novelId);
        return buildInteractionDTO(novel, Optional.of(userId));
    }

    private NovelInteractionDTO buildInteractionDTO(Novel novel, Optional<Long> userId) {
        boolean followed = userId
            .map(id -> novelFollowRepository.existsByUserIdAndNovelId(id, novel.getId()))
            .orElse(false);
        boolean liked = userId
            .map(id -> novelLikeRepository.existsByUserIdAndNovelId(id, novel.getId()))
            .orElse(false);
        boolean bookmarked = userId
            .map(id -> bookmarkRepository.existsByUserIdAndNovelId(id, novel.getId()))
            .orElse(false);

        return NovelInteractionDTO.builder()
            .novelId(novel.getId())
            .followed(followed)
            .liked(liked)
            .bookmarked(bookmarked)
            .views(novel.getViews())
            .follows(novel.getFollows())
            .likes(novel.getLikes())
            .bookmarks(novel.getBookmarks())
            .build();
    }

    private Novel findNovel(Long novelId) {
        return novelRepository.findById(novelId)
            .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

}
