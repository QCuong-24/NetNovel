package com.example.netnovel_server.recommendation.repository;

import com.example.netnovel_server.entity.UserEventType;
import com.example.netnovel_server.recommendation.dto.UserNovelEventAggregate;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class UserNovelInteractionSourceRepository {

    private final EntityManager entityManager;

    public UserNovelInteractionSourceRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<UserNovelEventAggregate> aggregatePositiveNovelEvents(Collection<UserEventType> eventTypes) {
        return entityManager.createQuery("""
            select new com.example.netnovel_server.recommendation.dto.UserNovelEventAggregate(
                e.user.id,
                e.novel.id,
                sum(case when e.eventType = :viewNovel then 1 else 0 end),
                sum(case when e.eventType = :viewChapter then 1 else 0 end),
                sum(case when e.eventType = :createComment then 1 else 0 end),
                sum(case when e.eventType = :replyComment then 1 else 0 end),
                min(e.eventAt),
                max(e.eventAt)
            )
            from UserEvent e
            where e.novel is not null and e.eventType in :eventTypes
            group by e.user.id, e.novel.id
            """, UserNovelEventAggregate.class)
            .setParameter("eventTypes", eventTypes)
            .setParameter("viewNovel", UserEventType.VIEW_NOVEL)
            .setParameter("viewChapter", UserEventType.VIEW_CHAPTER)
            .setParameter("createComment", UserEventType.CREATE_COMMENT)
            .setParameter("replyComment", UserEventType.REPLY_COMMENT)
            .getResultList();
    }
}
