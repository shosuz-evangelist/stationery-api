package com.devsumi.stationery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AIRoutingEngine {
    @Value("${AI_PROVIDER:azure}")
    private String provider;

    private final OpenAIService openAIService;
    private final FPTAIService fptAIService;

    public AIRoutingEngine(OpenAIService openAIService, FPTAIService fptAIService) {
        this.openAIService = openAIService;
        this.fptAIService = fptAIService;
    }

    public float[] createEmbedding(String text) {
        return switch (provider) {
            case "fpt" -> fptAIService.createEmbedding(text);
            case "azure" -> openAIService.createEmbedding(text);
            // 将来拡張用
            case "gcp", "aws" -> throw new UnsupportedOperationException("Not implemented");
            default -> openAIService.createEmbedding(text);
        };
    }
}
