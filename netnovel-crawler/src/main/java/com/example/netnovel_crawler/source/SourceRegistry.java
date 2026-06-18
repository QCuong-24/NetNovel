package com.example.netnovel_crawler.source;

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

    public SourceRegistry(@Value("${app.crawler.supported-sources:}") String configuredSources) {
        this.sources = parseSources(configuredSources);
    }

    public Optional<CrawlerSource> resolve(String url) {
        String host = extractHost(url);
        if (host == null) {
            return Optional.empty();
        }

        return sources.stream()
            .filter(source -> matchesDomain(host, source.domain()))
            .findFirst();
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
