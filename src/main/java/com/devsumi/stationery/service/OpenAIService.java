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
    
    public String generateText(String prompt) {
        // 実際はAzure OpenAI API呼び出し。ここではダミーレスポンス
        return String.format(
            "【AI推薦結果】\n" +
            "お客様のご要望「%s」に基づき、以下の商品をお勧めいたします：\n\n" +
            "1. 高級ボールペンセット - ビジネスシーンに最適\n" +
            "2. ゲルインクボールペン - 滑らかな書き心地\n" +
            "3. 多機能ペン - 効率的な作業をサポート\n\n" +
            "これらの商品は、お客様のニーズに合わせて厳選されています。",
            prompt.length() > 100 ? prompt.substring(0, 100) + "..." : prompt
        );
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public String getDeployment() {
        return deployment;
    }
}
