package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.NovelUserView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NovelUserViewRepository extends JpaRepository<NovelUserView, Long> {

    void deleteByNovelId(Long novelId);

    @Modifying
    @Query(
        value = """
            insert into novel_user_views (novel_id, user_id, view_count)
            values (:novelId, :userId, 1)
            on conflict (user_id, novel_id)
            do update set view_count = novel_user_views.view_count + 1
            """,
        nativeQuery = true
    )
    void incrementViewCount(@Param("novelId") Long novelId, @Param("userId") Long userId);
}
