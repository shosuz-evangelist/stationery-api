# Stationery API

文房具ECサイト向けバックエンドAPI。セマンティック検索とAI推薦機能を提供。

## 技術スタック

- **Backend**: Spring Boot 3.x (Java 17)
- **Database**: Azure PostgreSQL 16 + pgvector
- **AI**: ハイブリッドAI基盤（FPT AI Factory / Azure OpenAI）
- **Deploy**: Zeabur

## 主要機能

- 商品CRUD API
- セマンティック検索（pgvector）
- AI Routing Engine（FPT/Azure切替）
- AI Agent推薦機能（RAG）

## 開発方法

このプロジェクトは **Specification-Driven Development (SDD)** で開発されています。

詳細な仕様は [Constitution.md](./Constitution.md) を参照してください。

## セットアップ

### 環境変数

```
DATABASE_URL=postgresql://user:password@host:5432/stationery_db
AI_PROVIDER=fpt
FPT_AI_ENDPOINT=https://...
FPT_AI_API_KEY=xxx
AZURE_OPENAI_ENDPOINT=https://...
AZURE_OPENAI_API_KEY=xxx
```

### ローカル実行

```bash
./mvnw spring-boot:run
```

### Zeabur デプロイ

1. GitHub リポジトリを Zeabur に接続
2. 環境変数を設定
3. デプロイ

## API ドキュメント

起動後、Swagger UI でAPIを確認できます:

```
http://localhost:8080/swagger-ui.html
```

## ライセンス

MIT
