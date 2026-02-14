package com.devsumi.stationery.service;

import org.springframework.stereotype.Service;

@Service
public class FPTAIService {
    public float[] createEmbedding(String text) {
        // 実際はAPI呼び出し。ここではダミー
        float[] dummy = new float[1536];
        java.util.Arrays.fill(dummy, 0.02f);
        return dummy;
    }
}
