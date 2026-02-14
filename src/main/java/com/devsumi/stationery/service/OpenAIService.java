package com.devsumi.stationery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class OpenAIService {
    @Value("${AZURE_OPENAI_ENDPOINT}")
    private String endpoint;
    @Value("${AZURE_OPENAI_API_KEY}")
    private String apiKey;
    @Value("${AZURE_OPENAI_DEPLOYMENT_NAME}")
    private String deployment;

    public float[] createEmbedding(String text) {
        // 実際はAPI呼び出し。ここではダミー
        float[] dummy = new float[1536];
        Arrays.fill(dummy, 0.01f);
        return dummy;
    }
}
