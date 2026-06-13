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
                            from novel_tags nt_score
                            join tags t_score on t_score.id = nt_score.tag_id
                            where nt_score.novel_id = n.id
                              and lower(t_score.name) like lower(concat('%', :q, '%'))
                        ) then 18 else 0 end
                    + ln(coalesce(n.views, 0) + 1) * 1.0
                    + ln(coalesce(n.likes, 0) + 1) * 2.0
                    + ln(coalesce(n.follows, 0) + 1) * 3.0
                ) as score
            from novels n
            where (:status = '' or n.status = :status)
              and (:tag = '' or exists (
                    select 1
                    from novel_tags nt_filter
                    join tags t_filter on t_filter.id = nt_filter.tag_id
                    where nt_filter.novel_id = n.id
                      and lower(trim(t_filter.name)) = lower(trim(:tag))
                ))
              and (:q = ''
                   or lower(n.title) like lower(concat('%', :q, '%'))
                   or lower(n.author) like lower(concat('%', :q, '%'))
                   or similarity(lower(n.title), lower(:q)) >= 0.18
                   or similarity(lower(n.author), lower(:q)) >= 0.18
                   or exists (
                        select 1
                        from novel_tags nt_query
                        join tags t_query on t_query.id = nt_query.tag_id
                        where nt_query.novel_id = n.id
                          and lower(t_query.name) like lower(concat('%', :q, '%'))
                   ))
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
              and (:tag = '' or exists (
                    select 1
                    from novel_tags nt_filter
                    join tags t_filter on t_filter.id = nt_filter.tag_id
                    where nt_filter.novel_id = n.id
                      and lower(trim(t_filter.name)) = lower(trim(:tag))
                ))
              and (:q = ''
                   or lower(n.title) like lower(concat('%', :q, '%'))
                   or lower(n.author) like lower(concat('%', :q, '%'))
                   or similarity(lower(n.title), lower(:q)) >= 0.18
                   or similarity(lower(n.author), lower(:q)) >= 0.18
                   or exists (
                        select 1
                        from novel_tags nt_query
                        join tags t_query on t_query.id = nt_query.tag_id
                        where nt_query.novel_id = n.id
                          and lower(t_query.name) like lower(concat('%', :q, '%'))
                   ))
            """,
        nativeQuery = true
    )
    Page<NovelSearchProjection> searchNovels(
        @Param("q") String query,
        @Param("status") String status,
        @Param("tag") String tag,
        @Param("sort") String sort,
        Pageable pageable
    );

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
                    'TAG' as suggestion_type,
                    t.id as suggestion_id,
                    t.name as suggestion_label,
                    case when lower(t.name) like lower(concat(:q, '%')) then 75
                         when lower(t.name) like lower(concat('%', :q, '%')) then 45
                         else similarity(lower(t.name), lower(:q)) * 35
                    end as score
                from tags t
                where :q <> ''
                  and (lower(t.name) like lower(concat('%', :q, '%'))
                       or similarity(lower(t.name), lower(:q)) >= 0.18)
            ) suggestions
            order by score desc, suggestion_label asc
            limit :limit
            """,
        nativeQuery = true
    )
    List<SearchSuggestionProjection> findSuggestions(@Param("q") String query, @Param("limit") int limit);
}
