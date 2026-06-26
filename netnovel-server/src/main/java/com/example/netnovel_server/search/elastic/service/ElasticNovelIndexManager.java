package com.example.netnovel_server.search.elastic.service;

import com.example.netnovel_server.exception.SearchUnavailableException;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@ConditionalOnProperty(prefix = "app.search.elastic", name = "enabled", havingValue = "true")
public class ElasticNovelIndexManager {

    private final RestClient restClient;
    private final String novelIndexName;

    public ElasticNovelIndexManager(
        RestClient restClient,
        @Value("${app.search.elastic.novel-index}") String novelIndexName
    ) {
        this.restClient = restClient;
        this.novelIndexName = novelIndexName;
    }

    public String getNovelIndexName() {
        return novelIndexName;
    }

    public void ensureNovelIndex() {
        if (indexExists()) {
            return;
        }

        Request request = new Request("PUT", "/" + novelIndexName);
        request.setJsonEntity(novelIndexMapping());
        try {
            restClient.performRequest(request);
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not create Elasticsearch novel index: " + novelIndexName, exception);
        }
    }

    public boolean indexExists() {
        try {
            restClient.performRequest(new Request("HEAD", "/" + novelIndexName));
            return true;
        } catch (ResponseException exception) {
            return exception.getResponse().getStatusLine().getStatusCode() != 404;
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not connect to Elasticsearch", exception);
        }
    }

    public void deleteNovelIndexIfExists() {
        if (!indexExists()) {
            return;
        }

        try {
            restClient.performRequest(new Request("DELETE", "/" + novelIndexName));
        } catch (IOException exception) {
            throw new SearchUnavailableException("Could not delete Elasticsearch novel index: " + novelIndexName, exception);
        }
    }

    private String novelIndexMapping() {
        return """
            {
              "settings": {
                "analysis": {
                  "analyzer": {
                    "folding_text": {
                      "tokenizer": "standard",
                      "filter": ["lowercase", "asciifolding"]
                    },
                    "autocomplete": {
                      "tokenizer": "autocomplete_tokenizer",
                      "filter": ["lowercase", "asciifolding"]
                    }
                  },
                  "tokenizer": {
                    "autocomplete_tokenizer": {
                      "type": "edge_ngram",
                      "min_gram": 2,
                      "max_gram": 20,
                      "token_chars": ["letter", "digit"]
                    }
                  }
                }
              },
              "mappings": {
                "properties": {
                  "novelId": { "type": "long" },
                  "title": {
                    "type": "text",
                    "analyzer": "folding_text",
                    "fields": {
                      "keyword": { "type": "keyword" },
                      "suggest": {
                        "type": "text",
                        "analyzer": "autocomplete",
                        "search_analyzer": "folding_text"
                      }
                    }
                  },
                  "author": {
                    "type": "text",
                    "analyzer": "folding_text",
                    "fields": {
                      "keyword": { "type": "keyword" },
                      "suggest": {
                        "type": "text",
                        "analyzer": "autocomplete",
                        "search_analyzer": "folding_text"
                      }
                    }
                  },
                  "description": { "type": "text", "analyzer": "folding_text" },
                  "genres": { "type": "keyword" },
                  "tags": { "type": "keyword" },
                  "status": { "type": "keyword" },
                  "views": { "type": "long" },
                  "follows": { "type": "long" },
                  "likes": { "type": "long" },
                  "chapterCount": { "type": "integer" },
                  "latestChapterNumber": { "type": "integer" },
                  "lastChapterUpdatedAt": { "type": "date" },
                  "createdAt": { "type": "date" },
                  "updatedAt": { "type": "date" },
                  "crawled": { "type": "boolean" },
                  "sourceName": { "type": "keyword" },
                  "sourceNovelUrl": { "type": "keyword" },
                  "popularityScore": { "type": "double" },
                  "freshnessScore": { "type": "double" },
                  "recommendationText": { "type": "text", "analyzer": "folding_text" }
                }
              }
            }
            """;
    }
}
