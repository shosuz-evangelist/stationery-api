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
    
    public String generateText(String prompt) {
        // 実際はFPT AI API呼び出し。ここではダミーレスポンス
        return String.format(
            "【FPT AI推薦結果】\n" +
            "お客様のご要望に基づき、最適な商品を提案いたします：\n\n" +
            "• プレミアムボールペン\n" +
            "• 高機能ノート\n" +
            "• デザイン文具セット\n\n" +
            "詳細はお問い合わせください。"
        );
    }
}
