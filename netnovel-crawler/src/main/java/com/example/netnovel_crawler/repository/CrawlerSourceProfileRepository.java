package com.example.netnovel_crawler.repository;

import com.example.netnovel_crawler.profile.CrawlerSourceProfile;
import com.example.netnovel_crawler.profile.SourceProfileValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrawlerSourceProfileRepository extends JpaRepository<CrawlerSourceProfile, Long> {

    Optional<CrawlerSourceProfile> findFirstByDomainAndEnabledTrueOrderByVersionDesc(String domain);

    Optional<CrawlerSourceProfile> findFirstByDomainAndEnabledTrueAndValidationStatusOrderByVersionDesc(
        String domain,
        SourceProfileValidationStatus validationStatus
    );

    boolean existsByDomainAndVersion(String domain, Integer version);

    List<CrawlerSourceProfile> findByValidationStatus(SourceProfileValidationStatus validationStatus);
}
