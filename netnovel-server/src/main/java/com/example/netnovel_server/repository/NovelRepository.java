package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {

    Page<Novel> findByStatus(Status status, Pageable pageable);

    Page<Novel> findByStatusOrderByUpdateAtDesc(Status status, Pageable pageable);

    Page<Novel> findAllByOrderByUpdateAtDesc(Pageable pageable);

    Page<Novel> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Novel> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);

    Page<Novel> findByUpdateAtBetweenOrderByUpdateAtDesc(
        LocalDateTime start,
        LocalDateTime end,
        Pageable pageable
    );

    List<Novel> findTop10ByOrderByViewsDesc();

    List<Novel> findTop10ByOrderByFollowsDesc();

    List<Novel> findTop10ByOrderByLikesDesc();

    List<Novel> findByGenresId(Long genreId);

    List<Novel> findByTagsId(Long tagId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update Novel novel
        set novel.updateAt = :updatedAt
        where novel.id = :novelId
          and novel.updateAt < :updatedAt
        """)
    void advanceUpdateAt(@Param("novelId") Long novelId, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Novel novel set novel.views = novel.views + 1 where novel.id = :novelId")
    void incrementViews(@Param("novelId") Long novelId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Novel novel set novel.follows = novel.follows + 1 where novel.id = :novelId")
    void incrementFollows(@Param("novelId") Long novelId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Novel novel set novel.follows = case when novel.follows > 0 then novel.follows - 1 else 0 end where novel.id = :novelId")
    void decrementFollows(@Param("novelId") Long novelId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Novel novel set novel.likes = novel.likes + 1 where novel.id = :novelId")
    void incrementLikes(@Param("novelId") Long novelId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Novel novel set novel.likes = case when novel.likes > 0 then novel.likes - 1 else 0 end where novel.id = :novelId")
    void decrementLikes(@Param("novelId") Long novelId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Novel novel set novel.bookmarks = novel.bookmarks + 1 where novel.id = :novelId")
    void incrementBookmarks(@Param("novelId") Long novelId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Novel novel set novel.bookmarks = case when novel.bookmarks > 0 then novel.bookmarks - 1 else 0 end where novel.id = :novelId")
    void decrementBookmarks(@Param("novelId") Long novelId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update Novel novel
        set novel.bookmarks = case
            when novel.bookmarks > :amount then novel.bookmarks - :amount
            else 0
        end
        where novel.id = :novelId
        """)
    void decrementBookmarksBy(@Param("novelId") Long novelId, @Param("amount") long amount);
}
