# ユーザ認証アプリケーション

Quarkus バックエンドと Next.js フロントエンドで構成されたモノレポ構成のユーザ認証アプリケーションです。

## 概要

本リポジトリはモノレポ構成を採用しており、以下の2つのサービスで構成されています。

| サービス | ディレクトリ | 技術スタック |
|---|---|---|
| Backend | `backend/` | Quarkus（Java 17）、Gradle |
| Frontend | `frontend/` | Next.js 16（TypeScript、Tailwind CSS） |

### インフラストラクチャ

- **データベース**: PostgreSQL 16（UTF-8）
- **セッション管理**: Redis 7
- **コンテナ**: Docker（マルチステージビルド）
- **オーケストレーション**: Docker Compose

## 機能

### Backend

- JSON形式のログインエンドポイント（`POST /api/login`）
- メールアドレスとパスワードによるユーザ認証
- bcrypt（コストファクタ12）によるパスワードハッシュ化
- Redis によるセッション管理（TTL: 30分）
- UUIDv7 によるユーザID生成
- ヘルスチェックエンドポイント（`/q/health`、`/q/health/live`、`/q/health/ready`）
- JSON形式のログ出力
- アクセスログ

### Frontend

- ヘルスチェック状況表示ページ（`/test`）
- Next.js の rewrites 機能による Backend へのAPIプロキシ

## ユーザデータ構造

| カラム名 | 型 | 説明 |
|---|---|---|
| user_id | UUID | ユーザID（UUIDv7、主キー） |
| username | VARCHAR(255) | ユーザ名（日本語入力可） |
| email | VARCHAR(255) | メールアドレス（重複不可） |
| password_hash | VARCHAR(255) | bcryptハッシュ化パスワード |
| created_at | TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | 更新日時 |

## 開発環境でのアプリケーション起動

### Backend（開発モード）

開発モード（ライブコーディング対応）で起動するには以下を実行します:

```shell
cd backend
./gradlew quarkusDev
```

> **注意:** 開発モードではDev UIが利用可能です: <http://localhost:8080/q/dev/>

### Frontend（開発モード）

```shell
cd frontend
npm install
npm run dev
```

フロントエンドは <http://localhost:3000> で起動します。

## Docker環境での起動

Docker Compose を使用して全サービスを一括起動できます:

```shell
docker compose up --build
```

### サービス構成

| サービス | ポート | 説明 |
|---|---|---|
| backend | 8080 | Quarkusアプリケーション |
| frontend | 3000 | Next.jsフロントエンド |
| postgres | 5432 | PostgreSQLデータベース |
| redis | 6379 | Redisセッションストア |

### サービス依存関係

```
postgres, redis → backend → frontend
```

- `backend` は `postgres` と `redis` の正常起動を待ってから起動
- `frontend` は `backend` のヘルスチェック完了を待ってから起動

### 環境変数

Frontend の Backend 接続先は `BACKEND_INTERNAL_URL` ビルド引数で制御されます（デフォルト: `http://localhost:8080`）。Docker Compose 環境では `http://backend:8080` が設定されます。

> **注意:** `BACKEND_INTERNAL_URL` は Next.js のビルド時にベイクされるため、変更時は `docker compose up --build` での再ビルドが必要です。

## ビルド

### Backend

```shell
cd backend
./gradlew build
```

`backend/build/quarkus-app/` ディレクトリに `quarkus-run.jar` が生成されます。

uber-jar としてビルドする場合:

```shell
cd backend
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

### Frontend

```shell
cd frontend
npm install
npm run build
```

## テスト

### Backend 単体テスト

```shell
cd backend
./gradlew test
```

カバレッジレポートの生成:

```shell
cd backend
./gradlew jacocoTestReport
```

カバレッジレポートは `backend/build/reports/jacoco/test/html/index.html` で確認できます。

カバレッジ検証（C1カバレッジ90%以上）:

```shell
cd backend
./gradlew jacocoTestCoverageVerification
```

### E2Eテスト

Docker Compose 環境で E2E テストを実行します:

```shell
docker compose up -d --build
chmod +x ./e2e/e2e-test.sh
./e2e/e2e-test.sh
docker compose down -v
```

テストレポートは `e2e/reports/` ディレクトリに出力されます。

## APIエンドポイント

### ログイン

```
POST /api/login
Content-Type: application/json
```

**リクエスト:**

```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

**成功レスポンス（200 OK）:**

```json
{
  "sessionId": "生成されたセッションID",
  "userId": "ユーザのUUID",
  "username": "ユーザ名"
}
```

**認証失敗レスポンス（401 Unauthorized）:**

```json
{
  "errorCode": "AUTHENTICATION_FAILED",
  "message": "メールアドレスまたはパスワードが正しくありません"
}
```

**バリデーションエラーレスポンス（400 Bad Request）:**

```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "エラーメッセージ"
}
```

### ヘルスチェック

| エンドポイント | 説明 |
|---|---|
| `GET /q/health` | 全チェック統合 |
| `GET /q/health/live` | Livenessプローブ |
| `GET /q/health/ready` | Readinessプローブ（DB・Redis接続確認） |

## プロジェクト構成

```
.
├── backend/                        # Quarkus バックエンドサービス
│   ├── Dockerfile
│   ├── build.gradle
│   ├── gradlew
│   └── src/
│       ├── main/java/com/isozaki/auth/
│       │   ├── dto/                # データ転送オブジェクト（Java Record）
│       │   │   ├── ErrorResponse.java
│       │   │   ├── LoginRequest.java
│       │   │   └── LoginResponse.java
│       │   ├── entity/             # JPAエンティティ
│       │   │   └── UserEntity.java
│       │   ├── exception/          # 例外クラス
│       │   │   ├── AuthenticationException.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── health/             # ヘルスチェック
│       │   │   ├── ApplicationLivenessCheck.java
│       │   │   ├── DatabaseHealthCheck.java
│       │   │   └── RedisHealthCheck.java
│       │   ├── repository/         # リポジトリ
│       │   │   └── UserRepository.java
│       │   ├── resource/           # RESTリソース
│       │   │   └── LoginResource.java
│       │   └── service/            # ビジネスロジック
│       │       ├── AuthService.java
│       │       ├── PasswordService.java
│       │       ├── SessionService.java
│       │       └── UuidService.java
│       └── main/resources/
│           └── application.yaml
├── frontend/                       # Next.js フロントエンドサービス
│   ├── Dockerfile
│   ├── next.config.ts
│   ├── package.json
│   └── src/app/
│       ├── layout.tsx
│       ├── page.tsx
│       └── test/
│           └── page.tsx            # ヘルスチェック表示ページ
├── e2e/                            # E2Eテスト
│   └── e2e-test.sh
├── docker-compose.yml
├── init-db.sql
└── .github/workflows/
    ├── unit-test.yml               # 単体テスト・カバレッジ検証
    └── e2e-test.yml                # E2Eテスト（Docker環境）
```

## 技術スタック

### Backend

- [Quarkus](https://quarkus.io/) - Javaフレームワーク
- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache) - ORM
- [REST Jackson](https://quarkus.io/guides/rest#json-serialisation) - JSONシリアライゼーション
- [Hibernate Validator](https://quarkus.io/guides/validation) - バリデーション
- [Redis Client](https://quarkus.io/guides/redis) - Redisクライアント
- [JDBC Driver - PostgreSQL](https://quarkus.io/guides/datasource) - PostgreSQLドライバ
- [SmallRye Health](https://quarkus.io/guides/smallrye-health) - ヘルスチェック
- [Logging JSON](https://quarkus.io/guides/logging#json-logging) - JSONログ出力

### Frontend

- [Next.js](https://nextjs.org/) - Reactフレームワーク
- [TypeScript](https://www.typescriptlang.org/) - 型安全な JavaScript
- [Tailwind CSS](https://tailwindcss.com/) - ユーティリティファーストCSS
