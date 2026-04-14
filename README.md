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

- JSON形式のログインエンドポイント（`POST /api/v1/login`）
- メールアドレスとパスワードによるユーザ認証
- bcrypt（コストファクタ12）によるパスワードハッシュ化
- Redis によるセッション管理（TTL: 30分）
- UUIDv7 によるユーザID生成
- ヘルスチェックエンドポイント（`/q/health`、`/q/health/live`、`/q/health/ready`）
- JSON形式のログ出力
- アクセスログ

### Frontend

- トップページに「frontendテストページ」メッセージを表示
- 全ページ共通ヘッダー（「Devin-Test」タイトル表示）
  - 未ログイン時：「ログイン」ボタンを表示
  - ログイン済み時：ユーザーIDとユーザー名を表示、ログアウトボタンを表示
- ログインモーダル（メールアドレス・パスワード入力、クローズボタン・オーバーレイクリック対応）
- 認証状態管理（AuthContext）
  - `sessionStorage`を使用したブラウザでのセッション保持
  - ページ再アクセス時にバックエンドAPIでセッション有効性を検証
  - ログアウト時にバックエンドAPIでRedisセッションを削除
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

### Backend E2Eテスト

Docker Compose 環境で Backend API の E2E テストを実行します:

```shell
docker compose up -d --build
chmod +x ./e2e/backend/e2e-test.sh
./e2e/backend/e2e-test.sh
docker compose down -v
```

テストレポートは `e2e/backend/reports/` ディレクトリに出力されます。

### Frontend統合テスト（Playwright）

Playwright を使用したフロントエンド統合テストを実行します。
MSW（Mock Service Worker）によるAPIモック化により、バックエンド不要で実行可能です。

```shell
# フロントエンドのビルド・起動
cd frontend
npm install
npm run build
npm run start &

# テスト実行
cd ../e2e/frontend
npm install
npx playwright install chromium
npx playwright test
```

#### デバイス別テスト実行

```shell
# PC（Desktop Chrome）のみ
npm run test:pc

# Android（Pixel 7）のみ
npm run test:android

# iPhone（iPhone 14）のみ
npm run test:iphone
```

テストレポートは `e2e/frontend/reports/` ディレクトリに出力されます。
HTMLレポートの表示:

```shell
npm run report
```

#### テスト対象デバイス

| プロジェクト名 | デバイス | ビューポート | ブラウザ |
|---|---|---|---|
| PC（Desktop Chrome） | デスクトップ | 1280x720 | Chromium |
| Android（Pixel 7） | Android | 393x851 | Chromium |
| iPhone（iPhone 14） | iPhone | 390x844 | WebKit |

## APIエンドポイント

### ログイン

```
POST /api/v1/login
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

### セッション検証

```
GET /api/v1/session/{sessionId}
```

セッションIDの有効性をRedisで確認する。フロントエンドがページ再アクセス時にセッションがバックエンド側で保持されているかチェックするために使用する。

**成功レスポンス（200 OK）:**

```json
{
  "sessionId": "セッションID",
  "userId": "ユーザのUUID"
}
```

**セッション無効レスポンス（404 Not Found）:**

レスポンスボディなし

### セッション削除（ログアウト）

```
DELETE /api/v1/session/{sessionId}
```

Redisからセッションを削除する。フロントエンドのログアウト処理から呼び出される。

**成功レスポンス（204 No Content）:**

レスポンスボディなし

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
│       │   │   ├── LoginResource.java
│       │   │   └── SessionResource.java
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
│   └── src/
│       ├── app/
│       │   ├── layout.tsx          # AuthProvider・Header組み込み
│       │   ├── page.tsx            # トップページ（frontendテストページ）
│       │   ├── page.test.tsx       # トップページ単体テスト
│       │   └── test/
│       │       └── page.tsx        # ヘルスチェック表示ページ
│       ├── components/
│       │   ├── Header.tsx          # 共通ヘッダーコンポーネント
│       │   ├── Header.test.tsx     # ヘッダー単体テスト
│       │   ├── LoginModal.tsx      # ログインモーダルコンポーネント
│       │   └── LoginModal.test.tsx # ログインモーダル単体テスト
│       └── contexts/
│           └── AuthContext.tsx     # 認証コンテキスト・プロバイダー
├── e2e/                            # E2E・統合テスト
│   ├── backend/                   # Backend E2Eテスト
│   │   └── e2e-test.sh            # curl + jq によるAPIテスト
│   └── frontend/                  # Frontend統合テスト（Playwright + MSW）
│       ├── playwright.config.ts   # Playwright設定（PC/Android/iPhone）
│       ├── msw/                   # MSWモックハンドラー
│       │   ├── handlers.ts
│       │   └── setup.ts
│       └── tests/                 # テストファイル
│           ├── top-page.spec.ts
│           ├── login-modal.spec.ts
│           ├── auth-session.spec.ts
│           └── header-cross-page.spec.ts
├── docker-compose.yml
├── init-db.sql
└── .github/workflows/
    ├── unit-test.yml               # 単体テスト・カバレッジ検証
    ├── e2e-test.yml                # Backend E2Eテスト（Docker環境）
    └── frontend-integration-test.yml # Frontend統合テスト（Playwright）
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
