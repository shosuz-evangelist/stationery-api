# 文房具ECサイト - システム仕様書

## プロジェクト概要
文房具のオンラインショップ向けバックエンドAPI。セマンティック検索と AI 推薦機能を提供。

## 技術スタック
- **Backend**: Spring Boot 3.x (Java 17)
- **Database**: Azure PostgreSQL 16 + pgvector
- **AI**: ハイブリッドAI基盤（FPT AI Factory / Azure OpenAI）
- **Deploy**: Zeabur

## データベーススキーマ

### products テーブル（既存）
```sql
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    tags TEXT[],
    brand VARCHAR(100),
    stock INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    embedding VECTOR(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 既存のインデックス
CREATE INDEX idx_products_brand ON products(brand);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_tags ON products USING GIN(tags);
```

**データ状況**:
- 1000件の商品データ投入済み
- embedding カラムは NULL（API で生成予定）
- Azure Blob Storage の画像 URL 格納済み

## 必須機能

### 1. 商品CRUD API
- `GET /api/products` - 商品一覧取得
  - クエリパラメータ: `category`, `minPrice`, `maxPrice`, `limit`, `offset`
- `GET /api/products/{id}` - 商品詳細取得
- `POST /api/products` - 商品登録
- `PUT /api/products/{id}` - 商品更新
- `DELETE /api/products/{id}` - 商品削除

### 2. セマンティック検索 API
- `POST /api/products/search`
- リクエスト:
```json
{
  "query": "プレゼン用の高級ボールペン",
  "limit": 10,
  "threshold": 0.7
}
```
- 処理フロー:
  1. クエリを Embedding に変換（OpenAI text-embedding-3-small, 1536次元）
  2. 商品の `name + description` から embedding を生成（初回のみ、DB に保存）
  3. pgvector でコサイン類似度検索
```sql
SELECT *, 1 - (embedding <=> query_vector) AS similarity
FROM products
WHERE 1 - (embedding <=> query_vector) > threshold
ORDER BY embedding <=> query_vector
LIMIT limit;
```
  4. 類似度が threshold 以上の商品を返す
- レスポンス:
```json
{
  "results": [
    {
      "id": 1,
      "name": "モンブラン マイスターシュテュック",
      "description": "...",
      "price": 89800.00,
      "category": "ボールペン",
      "tags": ["高級", "ビジネス", "プレゼント"],
      "brand": "モンブラン",
      "stock": 25,
      "image_url": "https://stationeryimages2026.blob.core.windows.net/...",
      "similarity": 0.89
    }
  ]
}
```

### 3. Embedding 生成バッチ処理
- `POST /api/products/generate-embeddings`
- 処理フロー:
  1. `embedding IS NULL` の商品を取得
  2. 各商品の `name + " " + description` から embedding 生成
  3. DB に保存（`UPDATE products SET embedding = ? WHERE id = ?`）
- レスポンス:
```json
{
  "processed": 1000,
  "success": 998,
  "failed": 2
}
```

### 4. AI Routing Engine
- FPT AI Factory と Azure OpenAI を動的に切替
- 環境変数で制御:
```
AI_PROVIDER=fpt
FPT_AI_ENDPOINT=https://...
FPT_AI_API_KEY=xxx
AZURE_OPENAI_ENDPOINT=https://...
AZURE_OPENAI_API_KEY=xxx
AZURE_OPENAI_DEPLOYMENT_NAME=text-embedding-3-small
```
- 実装:
```java
@Service
public class AIRoutingEngine {
    @Value("${AI_PROVIDER:azure}")
    private String provider;
    
    public float[] createEmbedding(String text) {
        if ("fpt".equals(provider)) {
            return fptAIService.createEmbedding(text);
        } else {
            return azureOpenAIService.createEmbedding(text);
        }
    }
}
```

### 5. AI Agent 推薦機能
- `POST /api/recommendations`
- リクエスト:
```json
{
  "userId": 123,
  "context": "ビジネス向け文房具"
}
```
- 処理フロー:
  1. ユーザーの購買履歴を取得（今回はモック: `["ボールペン", "ノート"]`）
  2. `context` からセマンティック検索で類似商品取得（上位5件）
  3. 購買履歴 + 検索結果を RAG プロンプトに渡す:
```
あなたは文房具専門のECサイトのAIアシスタントです。
以下のユーザー情報に基づいて、おすすめ商品を3つ提案してください。

【ユーザーの購買履歴】
- ボールペン（ジェットストリーム）
- ノート（モレスキン）

【検索結果（類似商品）】
1. モンブラン マイスターシュテュック（高級ボールペン）
2. パーカー ソネット（ビジネス向けボールペン）
...

【推薦条件】
- コンテキスト: ビジネス向け文房具
- 予算: 5,000円〜30,000円

JSON形式で回答してください:
{
  "recommendations": [
    {"product_id": 1, "reason": "..."},
    {"product_id": 2, "reason": "..."},
    {"product_id": 3, "reason": "..."}
  ]
}
```
  4. AI から推薦理由を取得
  5. レスポンス:
```json
{
  "recommendations": [
    {
      "product": {
        "id": 1,
        "name": "モンブラン マイスターシュテュック",
        "price": 89800.00,
        "image_url": "https://..."
      },
      "reason": "あなたは過去にビジネス用ボールペンを購入されており、高級感のあるモンブランは重要な商談やプレゼンテーションで信頼感を与えます。"
    }
  ]
}
```

## 非機能要件
- **レスポンスタイム**: セマンティック検索 < 1秒
- **同時接続**: 100リクエスト/秒
- **データ件数**: 1000件の商品データ

## 実装制約
- **必ず pgvector を使用**すること
- **Embedding は OpenAI text-embedding-3-small（1536次元）**
- **AI Provider 切替は実行時に可能**であること（環境変数 `AI_PROVIDER`）
- **Swagger UI を有効化**すること
- **CORS 設定を追加**すること（モバイルアプリからのアクセス）
- **DATABASE_URL 環境変数から接続文字列を取得**すること

## API エンドポイント一覧
```
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
POST   /api/products/search
POST   /api/products/generate-embeddings
POST   /api/recommendations
GET    /swagger-ui.html
```
