package com.example.netnovel_server.search.elastic.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@ConditionalOnProperty(prefix = "app.search.elastic", name = "enabled", havingValue = "true")
public class ElasticSearchConfig {

    @Bean(destroyMethod = "close")
    public RestClient elasticsearchRestClient(@Value("${app.search.elastic.url}") String elasticsearchUrl) {
        URI uri = URI.create(elasticsearchUrl);
        String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
        int port = uri.getPort() == -1 ? defaultPort(scheme) : uri.getPort();
        return RestClient.builder(new HttpHost(uri.getHost(), port, scheme)).build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient elasticsearchRestClient) {
        return new RestClientTransport(elasticsearchRestClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport elasticsearchTransport) {
        return new ElasticsearchClient(elasticsearchTransport);
    }

    private int defaultPort(String scheme) {
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }
}
