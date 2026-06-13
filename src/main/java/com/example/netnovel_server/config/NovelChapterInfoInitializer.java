package com.example.netnovel_server.config;

import com.example.netnovel_server.service.NovelChapterInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NovelChapterInfoInitializer {

    private static final Logger log = LoggerFactory.getLogger(NovelChapterInfoInitializer.class);

    @Bean
    public ApplicationRunner initializeNovelChapterInfos(NovelChapterInfoService novelChapterInfoService) {
        return args -> {
            try {
                novelChapterInfoService.refreshAll();
                log.info("Novel chapter info records are ready.");
            } catch (Exception exception) {
                log.warn("Could not initialize novel chapter info records.", exception);
            }
        };
    }
}
