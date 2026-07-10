package com.example.netnovel_crawler.source;

import com.example.netnovel_crawler.profile.CrawlerSourceProfile;
import com.example.netnovel_crawler.profile.SourceProfileValidationStatus;
import com.example.netnovel_crawler.repository.CrawlerSourceProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class SourceRegistry {

    private final List<CrawlerSource> sources;
    private final CrawlerSourceProfileRepository crawlerSourceProfileRepository;

    public SourceRegistry(
        @Value("${app.crawler.supported-sources:}") String configuredSources,
        CrawlerSourceProfileRepository crawlerSourceProfileRepository
    ) {
        this.sources = parseSources(configuredSources);
        this.crawlerSourceProfileRepository = crawlerSourceProfileRepository;
    }

    public Optional<CrawlerSource> resolve(String url) {
        String host = extractHost(url);
        if (host == null) {
            return Optional.empty();
        }

        Optional<CrawlerSource> configuredSource = sources.stream()
            .filter(source -> matchesDomain(host, source.domain()))
            .findFirst();
        if (configuredSource.isPresent()) {
            return configuredSource;
        }

        return resolveDynamicSource(host);
    }

    private Optional<CrawlerSource> resolveDynamicSource(String host) {
        for (String domain : domainCandidates(host)) {
            Optional<CrawlerSourceProfile> profile = crawlerSourceProfileRepository
                .findFirstByDomainAndEnabledTrueAndValidationStatusOrderByVersionDesc(
                    domain,
                    SourceProfileValidationStatus.VALID
                );
            if (profile.isPresent()) {
                return profile.map(this::toCrawlerSource);
            }
        }
        return Optional.empty();
    }

    private CrawlerSource toCrawlerSource(CrawlerSourceProfile profile) {
        return new CrawlerSource(
            profile.getSourceName(),
            profile.getDomain(),
            CrawlerEngine.valueOf(profile.getEngine().name()),
            profile.getId()
        );
    }

    private List<String> domainCandidates(String host) {
        List<String> candidates = new ArrayList<>();
        String current = host;
        while (current != null && !current.isBlank()) {
            candidates.add(current);
            int dotIndex = current.indexOf('.');
            if (dotIndex < 0) {
                break;
            }
            current = current.substring(dotIndex + 1);
        }
        return candidates;
    }

    private List<CrawlerSource> parseSources(String configuredSources) {
        if (configuredSources == null || configuredSources.isBlank()) {
            return List.of();
        }

        List<CrawlerSource> parsedSources = new ArrayList<>();
        for (String entry : configuredSources.split(";")) {
            String[] parts = entry.split("\\|");
            if (parts.length != 3) {
                continue;
            }

            String name = parts[0].trim();
            String domain = normalizeDomain(parts[1]);
            String engine = parts[2].trim().toUpperCase(Locale.ROOT);
            if (name.isBlank() || domain.isBlank()) {
                continue;
            }

            try {
                parsedSources.add(new CrawlerSource(name, domain, CrawlerEngine.valueOf(engine)));
            } catch (IllegalArgumentException ignored) {
                // Invalid source configs are ignored so one bad entry does not stop the worker.
            }
        }
        return List.copyOf(parsedSources);
    }

    private String extractHost(String url) {
        try {
            URI uri = new URI(url);
            return normalizeDomain(uri.getHost());
        } catch (URISyntaxException exception) {
            return null;
        }
    }

    private boolean matchesDomain(String host, String configuredDomain) {
        return host.equals(configuredDomain) || host.endsWith("." + configuredDomain);
    }

    private String normalizeDomain(String domain) {
        if (domain == null) {
            return "";
        }
        String normalized = domain.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("www.") ? normalized.substring(4) : normalized;
    }
}
