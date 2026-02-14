package com.devsumi.stationery.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class OpenAIService {
    private final String endpoint;
    private final String apiKey;
    private final String deployment;

    public OpenAIService() {
        this.endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
        this.apiKey = System.getenv("AZURE_OPENAI_API_KEY");
        this.deployment = System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME");
        
        if (endpoint == null || apiKey == null || deployment == null) {
            throw new IllegalStateException(
                "Azure OpenAI environment variables are not set: " +
                "AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY, AZURE_OPENAI_DEPLOYMENT_NAME"
            );
        }
    }

    public float[] createEmbedding(String text) {
        // 実際はAPI呼び出し。ここではダミー
        float[] dummy = new float[1536];
        Arrays.fill(dummy, 0.01f);
        return dummy;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public String getDeployment() {
        return deployment;
    }
}
