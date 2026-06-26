package com.example.netnovel_server.chatbot.service.embedding;

import com.example.netnovel_server.chatbot.config.ChatbotEmbeddingProperties;
import com.example.netnovel_server.chatbot.dto.EmbeddingResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class ChatbotEmbeddingClient {

    private final ChatbotEmbeddingProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatbotEmbeddingClient(ChatbotEmbeddingProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    }

    public List<List<Double>> embedPassages(List<String> texts) {
        return embed(texts, "passage");
    }

    public List<Double> embedQuery(String text) {
        List<List<Double>> embeddings = embed(List.of(text), "query");
        return embeddings.isEmpty() ? List.of() : embeddings.getFirst();
    }

    private List<List<Double>> embed(List<String> texts, String inputType) {
        EmbeddingResponseDTO response = postEmbeddings(requestJson(texts, inputType));

        if (response == null || response.getEmbeddings() == null) {
            return List.of();
        }

        if (response.getDimension() > 0 && response.getDimension() != properties.dimension()) {
            throw new IllegalStateException("Embedding dimension mismatch. Expected "
                + properties.dimension() + " but service returned " + response.getDimension());
        }

        return response.getEmbeddings();
    }

    private String requestJson(List<String> texts, String inputType) {
        try {
            return objectMapper.writeValueAsString(Map.of("texts", texts, "inputType", inputType));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize embedding request", exception);
        }
    }

    private EmbeddingResponseDTO postEmbeddings(String requestJson) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(properties.baseUrl() + "/v1/embeddings"))
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(Duration.ofSeconds(60))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestJson))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Embedding service returned HTTP "
                    + response.statusCode() + ": " + response.body());
            }
            return objectMapper.readValue(response.body(), EmbeddingResponseDTO.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not call embedding service", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Embedding service call was interrupted", exception);
        }
    }
}

