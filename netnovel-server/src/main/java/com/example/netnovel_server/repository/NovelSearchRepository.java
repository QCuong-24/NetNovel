package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NovelSearchRepository extends JpaRepository<Novel, Long> {

    @Query(
        value = """
            select
                n.id as novelId,
                (
                    case when :q = '' then 0
                         when lower(n.title) = lower(:q) then 120
                         when lower(n.title) like lower(concat(:q, '%')) then 90
                         when lower(n.title) like lower(concat('%', :q, '%')) then 65
                         else similarity(lower(n.title), lower(:q)) * 55
                    end
                    + case when :q <> '' and lower(n.author) = lower(:q) then 45
                           when :q <> '' and lower(n.author) like lower(concat(:q, '%')) then 35
                           when :q <> '' and lower(n.author) like lower(concat('%', :q, '%')) then 25
                           else case when :q = '' then 0 else similarity(lower(n.author), lower(:q)) * 20 end
                      end
                    + case when :q <> '' and exists (
                            select 1
                            from novel_genres ng_score
                            join genres g_score on g_score.id = ng_score.genre_id
                            where ng_score.novel_id = n.id
                              and lower(g_score.name) like lower(concat('%', :q, '%'))
                        ) then 18 else 0 end
                    + ln(coalesce(n.views, 0) + 1) * 1.0
                    + ln(coalesce(n.likes, 0) + 1) * 2.0
                    + ln(coalesce(n.follows, 0) + 1) * 3.0
                ) as score
            from novels n
            where (:status = '' or n.status = :status)
              and (:genre = '' or exists (
                    select 1
                    from novel_genres ng_filter
                    join genres g_filter on g_filter.id = ng_filter.genre_id
                    where ng_filter.novel_id = n.id
                      and lower(trim(g_filter.name)) = lower(trim(:genre))
                ))
              and (:q = ''
                   or lower(n.title) like lower(concat('%', :q, '%'))
                   or lower(n.author) like lower(concat('%', :q, '%'))
                   or similarity(lower(n.title), lower(:q)) >= 0.18
                   or similarity(lower(n.author), lower(:q)) >= 0.18
                   or exists (
                        select 1
                        from novel_genres ng_query
                        join genres g_query on g_query.id = ng_query.genre_id
                        where ng_query.novel_id = n.id
                          and lower(g_query.name) like lower(concat('%', :q, '%'))
                   )
                   )
            order by
                case when :sort = 'latest' then extract(epoch from n.update_at) end desc,
                case when :sort = 'popular' then (coalesce(n.views, 0) + coalesce(n.likes, 0) * 3 + coalesce(n.follows, 0) * 5) end desc,
                score desc,
                n.update_at desc
            """,
        countQuery = """
            select count(*)
            from novels n
            where (:status = '' or n.status = :status)
              and (:genre = '' or exists (
                    select 1
                    from novel_genres ng_filter
                    join genres g_filter on g_filter.id = ng_filter.genre_id
                    where ng_filter.novel_id = n.id
                      and lower(trim(g_filter.name)) = lower(trim(:genre))
                ))
              and (:q = ''
                   or lower(n.title) like lower(concat('%', :q, '%'))
                   or lower(n.author) like lower(concat('%', :q, '%'))
                   or similarity(lower(n.title), lower(:q)) >= 0.18
                   or similarity(lower(n.author), lower(:q)) >= 0.18
                   or exists (
                        select 1
                        from novel_genres ng_query
                        join genres g_query on g_query.id = ng_query.genre_id
                        where ng_query.novel_id = n.id
                          and lower(g_query.name) like lower(concat('%', :q, '%'))
                   )
                   )
            """,
        nativeQuery = true
    )
    Page<NovelSearchProjection> searchNovels(
        @Param("q") String query,
        @Param("status") String status,
        @Param("genre") String genre,
        @Param("sort") String sort,
        Pageable pageable
    );

    @Query(
        value = """
            select
                candidate.id as novelId,
                (
                    coalesce(genre_scores.match_count, 0) * 12.0
                    + coalesce(tag_scores.match_count, 0) * 7.0
                    + case when lower(candidate.author) = lower(source.author) then 5.0 else 0.0 end
                    + case when candidate.status = source.status then 2.0 else 0.0 end
                    + ln(coalesce(candidate.views, 0) + 1) * 0.5
                    + ln(coalesce(candidate.likes, 0) + 1) * 1.0
                    + ln(coalesce(candidate.follows, 0) + 1) * 1.5
                ) as score
            from novels source
            join novels candidate on candidate.id <> source.id
            left join (
                select ng_candidate.novel_id, count(*) as match_count
                from novel_genres ng_source
                join novel_genres ng_candidate on ng_candidate.genre_id = ng_source.genre_id
                where ng_source.novel_id = :novelId
                  and ng_candidate.novel_id <> :novelId
                group by ng_candidate.novel_id
            ) genre_scores on genre_scores.novel_id = candidate.id
            left join (
                select nt_candidate.novel_id, count(*) as match_count
                from novel_tags nt_source
                join novel_tags nt_candidate on nt_candidate.tag_id = nt_source.tag_id
                where nt_source.novel_id = :novelId
                  and nt_candidate.novel_id <> :novelId
                group by nt_candidate.novel_id
            ) tag_scores on tag_scores.novel_id = candidate.id
            where source.id = :novelId
              and (
                  coalesce(genre_scores.match_count, 0) > 0
                  or
                  coalesce(tag_scores.match_count, 0) > 0
                  or lower(candidate.author) = lower(source.author)
                  or candidate.status = source.status
              )
            order by score desc, candidate.update_at desc
            """,
        countQuery = """
            select count(*)
            from novels source
            join novels candidate on candidate.id <> source.id
            left join (
                select ng_candidate.novel_id, count(*) as match_count
                from novel_genres ng_source
                join novel_genres ng_candidate on ng_candidate.genre_id = ng_source.genre_id
                where ng_source.novel_id = :novelId
                  and ng_candidate.novel_id <> :novelId
                group by ng_candidate.novel_id
            ) genre_scores on genre_scores.novel_id = candidate.id
            left join (
                select nt_candidate.novel_id, count(*) as match_count
                from novel_tags nt_source
                join novel_tags nt_candidate on nt_candidate.tag_id = nt_source.tag_id
                where nt_source.novel_id = :novelId
                  and nt_candidate.novel_id <> :novelId
                group by nt_candidate.novel_id
            ) tag_scores on tag_scores.novel_id = candidate.id
            where source.id = :novelId
              and (
                  coalesce(genre_scores.match_count, 0) > 0
                  or
                  coalesce(tag_scores.match_count, 0) > 0
                  or lower(candidate.author) = lower(source.author)
                  or candidate.status = source.status
              )
            """,
        nativeQuery = true
    )
    Page<NovelSearchProjection> findSimilarNovels(@Param("novelId") Long novelId, Pageable pageable);

    @Query(
        value = """
            select suggestion_type as type, suggestion_id as id, suggestion_label as label
            from (
                select
                    'NOVEL' as suggestion_type,
                    n.id as suggestion_id,
                    n.title as suggestion_label,
                    case when lower(n.title) like lower(concat(:q, '%')) then 100
                         when lower(n.title) like lower(concat('%', :q, '%')) then 70
                         else similarity(lower(n.title), lower(:q)) * 60
                    end as score
                from novels n
                where :q <> ''
                  and (lower(n.title) like lower(concat('%', :q, '%'))
                       or similarity(lower(n.title), lower(:q)) >= 0.18)

                union all

                select
                    'AUTHOR' as suggestion_type,
                    null as suggestion_id,
                    n.author as suggestion_label,
                    case when lower(n.author) like lower(concat(:q, '%')) then 85
                         when lower(n.author) like lower(concat('%', :q, '%')) then 55
                         else similarity(lower(n.author), lower(:q)) * 45
                    end as score
                from novels n
                where :q <> ''
                  and n.author is not null
                  and (lower(n.author) like lower(concat('%', :q, '%'))
                       or similarity(lower(n.author), lower(:q)) >= 0.18)
                group by n.author

                union all

                select
                    'GENRE' as suggestion_type,
                    g.id as suggestion_id,
                    g.name as suggestion_label,
                    case when lower(g.name) like lower(concat(:q, '%')) then 78
                         when lower(g.name) like lower(concat('%', :q, '%')) then 48
                         else similarity(lower(g.name), lower(:q)) * 38
                    end as score
                from genres g
                where :q <> ''
                  and (lower(g.name) like lower(concat('%', :q, '%'))
                       or similarity(lower(g.name), lower(:q)) >= 0.18)
            ) suggestions
            order by score desc, suggestion_label asc
            limit :limit
            """,
        nativeQuery = true
    )
    List<SearchSuggestionProjection> findSuggestions(@Param("q") String query, @Param("limit") int limit);
}
