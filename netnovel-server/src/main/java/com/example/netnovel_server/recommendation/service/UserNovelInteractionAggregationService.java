package com.example.netnovel_server.recommendation.service;

import com.example.netnovel_server.entity.Bookmark;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.NovelFollow;
import com.example.netnovel_server.entity.NovelLike;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.entity.UserEventType;
import com.example.netnovel_server.recommendation.dto.UserNovelEventAggregate;
import com.example.netnovel_server.recommendation.dto.UserNovelInteractionRebuildDTO;
import com.example.netnovel_server.recommendation.dto.UserNovelInteractionDTO;
import com.example.netnovel_server.recommendation.entity.UserNovelInteraction;
import com.example.netnovel_server.recommendation.entity.UserNovelInteractionId;
import com.example.netnovel_server.recommendation.mapper.UserNovelInteractionMapper;
import com.example.netnovel_server.recommendation.repository.UserNovelInteractionRepository;
import com.example.netnovel_server.recommendation.repository.UserNovelInteractionSourceRepository;
import com.example.netnovel_server.repository.BookmarkRepository;
import com.example.netnovel_server.repository.NovelFollowRepository;
import com.example.netnovel_server.repository.NovelLikeRepository;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserNovelInteractionAggregationService {

    private static final EnumSet<UserEventType> POSITIVE_NOVEL_EVENT_TYPES = EnumSet.of(
        UserEventType.VIEW_NOVEL,
        UserEventType.VIEW_CHAPTER,
        UserEventType.CREATE_COMMENT,
        UserEventType.REPLY_COMMENT
    );

    private final UserNovelInteractionSourceRepository sourceRepository;
    private final UserNovelInteractionRepository interactionRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final NovelFollowRepository novelFollowRepository;
    private final NovelLikeRepository novelLikeRepository;
    private final BookmarkRepository bookmarkRepository;

    public UserNovelInteractionAggregationService(
        UserNovelInteractionSourceRepository sourceRepository,
        UserNovelInteractionRepository interactionRepository,
        UserRepository userRepository,
        NovelRepository novelRepository,
        NovelFollowRepository novelFollowRepository,
        NovelLikeRepository novelLikeRepository,
        BookmarkRepository bookmarkRepository
    ) {
        this.sourceRepository = sourceRepository;
        this.interactionRepository = interactionRepository;
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
        this.novelFollowRepository = novelFollowRepository;
        this.novelLikeRepository = novelLikeRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @Transactional
    public UserNovelInteractionRebuildDTO rebuild() {
        Map<InteractionKey, InteractionAccumulator> interactions = new HashMap<>();

        sourceRepository.aggregatePositiveNovelEvents(POSITIVE_NOVEL_EVENT_TYPES)
            .forEach(event -> applyEventAggregate(interactions, event));
        novelFollowRepository.findAll().forEach(follow -> applyFollow(interactions, follow));
        novelLikeRepository.findAll().forEach(like -> applyLike(interactions, like));
        bookmarkRepository.findAll().stream()
            .filter(bookmark -> bookmark.getNovel() != null)
            .forEach(bookmark -> applyBookmark(interactions, bookmark));

        LocalDateTime rebuiltAt = LocalDateTime.now();
        List<UserNovelInteraction> rebuiltInteractions = interactions.values().stream()
            .map(interaction -> interaction.toEntity(rebuiltAt))
            .toList();

        interactionRepository.deleteAllInBatch();
        interactionRepository.saveAll(rebuiltInteractions);

        return new UserNovelInteractionRebuildDTO(rebuiltInteractions.size(), rebuiltAt);
    }

    @Transactional(readOnly = true)
    public Page<UserNovelInteractionDTO> getInteractions(Long userId, Pageable pageable) {
        Page<UserNovelInteraction> interactions = userId == null
            ? interactionRepository.findAllByOrderByInteractionScoreDesc(pageable)
            : interactionRepository.findByUserIdOrderByInteractionScoreDesc(userId, pageable);

        return interactions.map(UserNovelInteractionMapper::toDTO);
    }

    private void applyEventAggregate(Map<InteractionKey, InteractionAccumulator> interactions, UserNovelEventAggregate event) {
        InteractionAccumulator interaction = getOrCreate(interactions, event.userId(), event.novelId());
        interaction.viewNovelCount = event.viewNovelCount();
        interaction.viewChapterCount = event.viewChapterCount();
        interaction.commentCount = event.commentCount();
        interaction.replyCount = event.replyCount();
        interaction.includeInteractionTime(event.firstInteractedAt());
        interaction.includeInteractionTime(event.lastInteractedAt());
    }

    private void applyFollow(Map<InteractionKey, InteractionAccumulator> interactions, NovelFollow follow) {
        InteractionAccumulator interaction = getOrCreate(interactions, follow.getUser().getId(), follow.getNovel().getId());
        interaction.followed = true;
        interaction.includeInteractionTime(follow.getFollowedAt());
    }

    private void applyLike(Map<InteractionKey, InteractionAccumulator> interactions, NovelLike like) {
        InteractionAccumulator interaction = getOrCreate(interactions, like.getUser().getId(), like.getNovel().getId());
        interaction.liked = true;
        interaction.includeInteractionTime(like.getLikedAt());
    }

    private void applyBookmark(Map<InteractionKey, InteractionAccumulator> interactions, Bookmark bookmark) {
        InteractionAccumulator interaction = getOrCreate(interactions, bookmark.getUser().getId(), bookmark.getNovel().getId());
        interaction.bookmarked = true;
        interaction.includeInteractionTime(bookmark.getCreatedAt());
    }

    private InteractionAccumulator getOrCreate(Map<InteractionKey, InteractionAccumulator> interactions, Long userId, Long novelId) {
        return interactions.computeIfAbsent(new InteractionKey(userId, novelId), InteractionAccumulator::new);
    }

    private final class InteractionAccumulator {
        private final InteractionKey key;
        private Long viewNovelCount = 0L;
        private Long viewChapterCount = 0L;
        private Long commentCount = 0L;
        private Long replyCount = 0L;
        private boolean followed;
        private boolean liked;
        private boolean bookmarked;
        private LocalDateTime firstInteractedAt;
        private LocalDateTime lastInteractedAt;

        private InteractionAccumulator(InteractionKey key) {
            this.key = key;
        }

        private void includeInteractionTime(LocalDateTime interactionTime) {
            if (interactionTime == null) {
                return;
            }
            if (firstInteractedAt == null || interactionTime.isBefore(firstInteractedAt)) {
                firstInteractedAt = interactionTime;
            }
            if (lastInteractedAt == null || interactionTime.isAfter(lastInteractedAt)) {
                lastInteractedAt = interactionTime;
            }
        }

        private UserNovelInteraction toEntity(LocalDateTime rebuiltAt) {
            User user = userRepository.getReferenceById(key.userId());
            Novel novel = novelRepository.getReferenceById(key.novelId());
            double interactionScore = (viewNovelCount > 0 ? 1 : 0)
                + viewChapterCount * 2
                + (commentCount + replyCount) * 3
                + (bookmarked ? 5 : 0)
                + (followed ? 6 : 0)
                + (liked ? 7 : 0);

            return UserNovelInteraction.builder()
                .id(new UserNovelInteractionId(key.userId(), key.novelId()))
                .user(user)
                .novel(novel)
                .viewNovelCount(viewNovelCount)
                .viewChapterCount(viewChapterCount)
                .commentCount(commentCount)
                .replyCount(replyCount)
                .followed(followed)
                .liked(liked)
                .bookmarked(bookmarked)
                .interactionScore(interactionScore)
                .firstInteractedAt(firstInteractedAt)
                .lastInteractedAt(lastInteractedAt)
                .calculatedAt(rebuiltAt)
                .build();
        }
    }

    private record InteractionKey(Long userId, Long novelId) {
    }
}
