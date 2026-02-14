package com.devsumi.stationery.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FPTAIService {
    private final String endpoint;
    private final String apiKey;

    public FPTAIService() {
        this.endpoint = System.getenv("FPT_AI_ENDPOINT");
        this.apiKey = System.getenv("FPT_AI_API_KEY");
        // FPT は後で実装するため、null チェックはしない
    }

    public float[] createEmbedding(String text) {
        // 実際はAPI呼び出し。ここではダミー
        float[] dummy = new float[1536];
        Arrays.fill(dummy, 0.02f);
        return dummy;
    }
}
