package com.devsumi.stationery.service;

import org.springframework.stereotype.Service;

@Service
public class AIRoutingEngine {
    private final String provider;
    private final OpenAIService openAIService;
    private final FPTAIService fptAIService;

    public AIRoutingEngine(OpenAIService openAIService, FPTAIService fptAIService) {
        this.openAIService = openAIService;
        this.fptAIService = fptAIService;
        this.provider = System.getenv().getOrDefault("AI_PROVIDER", "azure");
    }

    public float[] createEmbedding(String text) {
        return switch (provider.toLowerCase()) {
            case "fpt" -> fptAIService.createEmbedding(text);
            case "azure" -> openAIService.createEmbedding(text);
            case "gcp", "aws" -> throw new UnsupportedOperationException("Not implemented: " + provider);
            default -> openAIService.createEmbedding(text);
        };
    }
    
    public String getCurrentProvider() {
        return provider;
    }
}
