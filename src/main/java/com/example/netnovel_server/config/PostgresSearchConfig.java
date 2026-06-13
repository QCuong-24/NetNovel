package com.example.netnovel_server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PostgresSearchConfig {

    private static final Logger log = LoggerFactory.getLogger(PostgresSearchConfig.class);

    @Bean
    public ApplicationRunner postgresSearchInitializer(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("create extension if not exists pg_trgm");
                jdbcTemplate.execute("create index if not exists idx_novels_title_trgm on novels using gin (title gin_trgm_ops)");
                jdbcTemplate.execute("create index if not exists idx_novels_author_trgm on novels using gin (author gin_trgm_ops)");
                jdbcTemplate.execute("create index if not exists idx_tags_name_trgm on tags using gin (name gin_trgm_ops)");
                jdbcTemplate.execute("create index if not exists idx_novels_status_update_at on novels (status, update_at desc)");
                jdbcTemplate.execute("create index if not exists idx_novels_popularity on novels (views desc, follows desc, likes desc)");
                log.info("PostgreSQL search extensions and indexes are ready.");
            } catch (Exception exception) {
                log.warn(
                    "Could not initialize PostgreSQL search extensions/indexes. Search queries using pg_trgm may fail until pg_trgm is enabled.",
                    exception
                );
            }
        };
    }
}
