package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.NovelInteractionDTO;
import com.example.netnovel_server.entity.*;
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
    private final UserEventRepository userEventRepository;
    private final NovelFollowRepository novelFollowRepository;
    private final NovelLikeRepository novelLikeRepository;
    private final BookmarkRepository bookmarkRepository;

    public NovelInteractionService(
        NovelRepository novelRepository,
        UserRepository userRepository,
        NovelViewStatRepository novelViewStatRepository,
        NovelUserViewRepository novelUserViewRepository,
        UserEventRepository userEventRepository,
        NovelFollowRepository novelFollowRepository,
        NovelLikeRepository novelLikeRepository,
        BookmarkRepository bookmarkRepository
    ) {
        this.novelRepository = novelRepository;
        this.userRepository = userRepository;
        this.novelViewStatRepository = novelViewStatRepository;
        this.novelUserViewRepository = novelUserViewRepository;
        this.userEventRepository = userEventRepository;
        this.novelFollowRepository = novelFollowRepository;
        this.novelLikeRepository = novelLikeRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @Transactional
    public NovelInteractionDTO increaseView(Long novelId) {
        Novel novel = findNovel(novelId);
        Optional<User> user = SecurityUtils.getCurrentUserId().map(this::findUser);

        novelViewStatRepository.incrementViewCount(novelId, LocalDate.now());
        user.ifPresent(currentUser -> {
            novelUserViewRepository.incrementViewCount(novelId, currentUser.getId());
            userEventRepository.save(UserEvent.builder()
                .user(currentUser)
                .novel(novel)
                .eventType(UserEventType.VIEW_NOVEL)
                .build());
        });

        novel.setViews(safeIncrement(novel.getViews()));
        novelRepository.save(novel);

        return buildInteractionDTO(novel, user.map(User::getId));
    }

    @Transactional
    public NovelInteractionDTO toggleFollow(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        User user = findUser(userId);
        Novel novel = findNovel(novelId);

        Optional<NovelFollow> existingFollow = novelFollowRepository.findByUserIdAndNovelId(userId, novelId);
        if (existingFollow.isPresent()) {
            novelFollowRepository.delete(existingFollow.get());
            novel.setFollows(safeDecrement(novel.getFollows()));
        } else {
            NovelFollow follow = NovelFollow.builder()
                .user(user)
                .novel(novel)
                .build();
            novelFollowRepository.save(follow);
            novel.setFollows(safeIncrement(novel.getFollows()));
        }

        novelRepository.save(novel);
        return buildInteractionDTO(novel, Optional.of(userId));
    }

    @Transactional
    public NovelInteractionDTO toggleLike(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        User user = findUser(userId);
        Novel novel = findNovel(novelId);

        Optional<NovelLike> existingLike = novelLikeRepository.findByUserIdAndNovelId(userId, novelId);
        if (existingLike.isPresent()) {
            novelLikeRepository.delete(existingLike.get());
            novel.setLikes(safeDecrement(novel.getLikes()));
        } else {
            NovelLike like = NovelLike.builder()
                .user(user)
                .novel(novel)
                .build();
            novelLikeRepository.save(like);
            novel.setLikes(safeIncrement(novel.getLikes()));
        }

        novelRepository.save(novel);
        return buildInteractionDTO(novel, Optional.of(userId));
    }

    @Transactional
    public NovelInteractionDTO toggleBookmark(Long novelId) {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        User user = findUser(userId);
        Novel novel = findNovel(novelId);

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndNovelId(userId, novelId);
        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
            novel.setBookmarks(safeDecrement(novel.getBookmarks()));
        } else {
            Bookmark bookmark = Bookmark.builder()
                .user(user)
                .novel(novel)
                .build();
            bookmarkRepository.save(bookmark);
            novel.setBookmarks(safeIncrement(novel.getBookmarks()));
        }

        novelRepository.save(novel);
        return buildInteractionDTO(novel, Optional.of(userId));
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

    private Long safeIncrement(Long value) {
        return value == null ? 1L : value + 1;
    }

    private Long safeDecrement(Long value) {
        if (value == null || value <= 0) {
            return 0L;
        }
        return value - 1;
    }
}
