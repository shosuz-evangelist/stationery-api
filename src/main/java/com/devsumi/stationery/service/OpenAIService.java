package com.devsumi.stationery.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class OpenAIService {
    private final String endpoint;
    private final String apiKey;
    private final String embeddingDeployment;
    private final String chatDeployment;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAIService() {
        this.endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
        this.apiKey = System.getenv("AZURE_OPENAI_API_KEY");
        this.embeddingDeployment = System.getenv("AZURE_OPENAI_EMBEDDING_DEPLOYMENT");
        this.chatDeployment = System.getenv("AZURE_OPENAI_CHAT_DEPLOYMENT");
        
        if (endpoint == null || apiKey == null) {
            throw new IllegalStateException(
                "Azure OpenAI environment variables are not set: " +
                "AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY"
            );
        }
        
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public float[] createEmbedding(String text) {
        try {
            String url = endpoint + "/openai/deployments/" + embeddingDeployment + "/embeddings?api-version=2024-02-01";
            
            String requestBody = objectMapper.writeValueAsString(Map.of("input", text));
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Azure OpenAI API error: " + response.statusCode() + " - " + response.body());
            }
            
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode embeddingNode = root.path("data").get(0).path("embedding");
            
            float[] embedding = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                embedding[i] = (float) embeddingNode.get(i).asDouble();
            }
            
            return embedding;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create embedding: " + e.getMessage(), e);
        }
    }

    public String generateText(String prompt) {
        try {
            String url = endpoint + "/openai/deployments/" + chatDeployment + "/chat/completions?api-version=2024-02-01";
            
            Map<String, Object> requestBody = Map.of(
                "messages", List.of(
                    Map.of("role", "system", "content", "あなたは文房具の専門家です。お客様に最適な商品を推薦してください。"),
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 500,
                "temperature", 0.7
            );
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Azure OpenAI API error: " + response.statusCode() + " - " + response.body());
            }
            
            JsonNode root = objectMapper.readTree(response.body());
            return root.path("choices").get(0).path("message").path("content").asText();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate text: " + e.getMessage(), e);
        }
    }

    public String getEndpoint() { return endpoint; }
    public String getEmbeddingDeployment() { return embeddingDeployment; }
    public String getChatDeployment() { return chatDeployment; }
}
