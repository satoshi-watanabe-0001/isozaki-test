# Devinテストアプリケーション

Quarkus バックエンドと Next.js フロントエンドで構成されたモノレポ構成のコミュニティプラットフォームです。

## 概要

本リポジトリはモノレポ構成を採用しており、以下のサービスで構成されています。

| サービス | ディレクトリ | 技術スタック |
|---|---|---|
| Backend | `backend/` | Quarkus（Java 17）、Gradle |
| Frontend | `frontend/` | Next.js 16（TypeScript、Tailwind CSS） |
| Image Processor | `image-processor/` | Node.js 22、sharp（画像リサイズ・WebP変換） |
| Lambda Template | `lambda/` | AWS SAM テンプレート（デプロイスケルトン） |

### インフラストラクチャ

- **データベース**: PostgreSQL 16（UTF-8）
- **セッション管理**: Redis 7
- **オブジェクトストレージ**: MinIO（S3互換、画像保存用）
- **画像処理**: image-processor（Node.js + sharp、WebP変換・リサイズ）
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
- アーティスト一覧API（50音順ソート）
- コミュニティTOP情報集約API（画像・キャンペーン・お知らせ）
- スレッドCRUD API（一覧・詳細・作成・コメント追加、カーソルベースページング）
- 画像アップロードAPI（Pre-signed URL発行、PENDING/CONFIRMED管理、定期クリーンアップ）

### Frontend

- トップページに「ようこそEntm-Cloneへ」メッセージと「アーティスト一覧」リンクを表示
- 全ページ共通ヘッダー（「Devin-Test」タイトル表示）
  - 未ログイン時：「ログイン」ボタンを表示
  - ログイン済み時：ユーザーIDとユーザー名を表示、ログアウトボタンを表示
  - サブページでの「戻る」ボタン対応
- ログインモーダル（メールアドレス・パスワード入力、クローズボタン・オーバーレイクリック対応）
- 認証状態管理（AuthContext）
  - `sessionStorage`を使用したブラウザでのセッション保持
  - ページ再アクセス時にバックエンドAPIでセッション有効性を検証
  - ログアウト時にバックエンドAPIでRedisセッションを削除
- アーティスト一覧ページ（`/artists`）— **SSR（Server Component）**
  - サーバサイドでバックエンドAPIからアーティスト一覧を取得
  - 2列グリッドで50音順に表示
  - 各アーティストカードからコミュニティTOPページへのリンク
  - 「And more...」表示
  - ストリーミングSSR時のローディングスピナー表示（`loading.tsx`）
- コミュニティTOPページ（`/community/{artistId}`）— **SSR（Server + Client Component）**
  - サーバサイドでバックエンドAPIからコミュニティTOP情報を取得（Server Component）
  - アーティスト名表示（先頭）
  - カルーセル画像（Embla Carousel、正方形、横スワイプ切替、最大3件、インジケーター付き）— Client Component
  - メニュー領域（4列: プロフィール・イベント・キャンペーン・スレッド・お知らせ・公式ページ）
  - キャンペーン領域（スマホ横幅対応の正方形画像を横スクロール、スクロールバー非表示、最大3件）
  - お知らせ領域（新着順でタイトル表示、最大5件）
  - ストリーミングSSR時のローディングスピナー表示（`loading.tsx`）
- スレッド一覧ページ（`/community/{artistId}/threads`）
  - スレッドタイトル、作成ユーザ名、最新コメント、書き込み日時を表示
  - 最新書き込み日時の降順ソート、ページング対応（20件/ページ）
  - ログイン済みユーザのスレッド作成機能（FAB + モーダル）
  - ログイン促進ダイアログ
  - ローディングスピナー表示
- スレッド詳細ページ（`/community/{artistId}/threads/{threadId}`）
  - スレッドタイトル、作成者、コメント一覧表示
  - 「もっと見る」ボタンによるカーソルベースページング（10件/ページ）
  - 画像付きコメント対応（X(Twitter)風グリッドレイアウト、react-image-galleryライトボックス）
  - ログイン済みユーザのコメント追加機能（FAB + モーダル、画像添付対応）
  - EXIF回転情報の自動補正（exifr + Canvas API）
  - ローディングスピナー表示
- 共通404エラーページ（「404 Not Found」「ページが見つかりません」表示）
- 共通エラーページ（「エラーが発生しました」「TOPページへ戻る」リンク表示）
- ヘルスチェック状況表示ページ（`/test`）
- Next.js の rewrites 機能による Backend へのAPIプロキシ

### 画像処理

- MinIO Webhook通知によるイベント駆動型画像処理
- アップロード画像のWebP変換・リサイズ（sharp使用）
  - サムネイル（400px幅）
  - 表示用（1200px幅）
- 単一コードベース・デュアルエントリーポイント構成
  - `localServer.js`: ローカル開発用（Express + MinIO Webhook）
  - `lambdaHandler.js`: AWS Lambda用（S3イベント）

## データ構造

### users

| カラム名 | 型 | 説明 |
|---|---|---|
| user_id | UUID | ユーザID（UUIDv7、主キー） |
| username | VARCHAR(255) | ユーザ名（日本語入力可） |
| email | VARCHAR(255) | メールアドレス（重複不可） |
| password_hash | VARCHAR(255) | bcryptハッシュ化パスワード |
| created_at | TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | 更新日時 |

### artists

| カラム名 | 型 | 説明 |
|---|---|---|
| artist_id | VARCHAR(100) | アーティストID（英名文字列、主キー） |
| name | VARCHAR(255) | アーティスト名 |
| name_kana | VARCHAR(255) | ソート用読み仮名（ひらがな） |
| icon_url | VARCHAR(500) | アイコン画像URL |
| created_at | TIMESTAMP | 作成日時 |
| updated_at | TIMESTAMP | 更新日時 |

### artist_images

| カラム名 | 型 | 説明 |
|---|---|---|
| image_id | SERIAL | 画像ID（主キー） |
| artist_id | VARCHAR(100) | アーティストID（外部キー） |
| image_url | VARCHAR(500) | 画像URL |
| display_order | INT | 表示順 |
| created_at | TIMESTAMP | 作成日時 |

### campaigns

| カラム名 | 型 | 説明 |
|---|---|---|
| campaign_id | SERIAL | キャンペーンID（主キー） |
| artist_id | VARCHAR(100) | アーティストID（外部キー） |
| title | VARCHAR(255) | キャンペーンタイトル |
| image_url | VARCHAR(500) | キャンペーン画像URL |
| display_order | INT | 表示順 |
| created_at | TIMESTAMP | 作成日時 |

### news

| カラム名 | 型 | 説明 |
|---|---|---|
| news_id | SERIAL | お知らせID（主キー） |
| artist_id | VARCHAR(100) | アーティストID（外部キー） |
| title | VARCHAR(500) | お知らせタイトル |
| published_at | TIMESTAMP | 公開日時 |
| created_at | TIMESTAMP | 作成日時 |

### threads

| カラム名 | 型 | 説明 |
|---|---|---|
| thread_id | UUID | スレッドID（UUIDv7、主キー） |
| artist_id | VARCHAR(100) | アーティストID（外部キー） |
| title | VARCHAR(50) | スレッドタイトル |
| created_by | UUID | 作成者ユーザID（外部キー） |
| created_at | TIMESTAMP | 作成日時 |
| latest_comment_content | VARCHAR(200) | 最新コメント内容（非正規化） |
| latest_comment_at | TIMESTAMP | 最新コメント日時（非正規化） |

### thread_comments

| カラム名 | 型 | 説明 |
|---|---|---|
| comment_id | UUID | コメントID（UUIDv7、主キー） |
| thread_id | UUID | スレッドID（外部キー） |
| content | VARCHAR(200) | コメント内容 |
| created_by | UUID | 作成者ユーザID（外部キー） |
| created_at | TIMESTAMP | 作成日時 |

### comment_images

| カラム名 | 型 | 説明 |
|---|---|---|
| image_id | UUID | 画像ID（UUIDv7、主キー） |
| comment_id | UUID | コメントID（外部キー、NULL=PENDING） |
| s3_key | VARCHAR(500) | S3オブジェクトキー |
| status | VARCHAR(20) | ステータス（PENDING/CONFIRMED） |
| uploaded_by | UUID | アップロードユーザID（外部キー） |
| created_at | TIMESTAMP | 作成日時 |

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
docker compose up -d --build --wait backend frontend image-processor
docker compose up minio-init
```

> **注意:** `minio-init`はone-shotコンテナ（初期設定完了後に終了）のため、`--wait`フラグと分離して実行します。

### サービス構成

| サービス | ポート | 説明 |
|---|---|---|
| backend | 8080 | Quarkusアプリケーション |
| frontend | 3000 | Next.jsフロントエンド |
| postgres | 5432 | PostgreSQLデータベース |
| redis | 6379 | Redisセッションストア |
| minio | 9000, 9001 | MinIOオブジェクトストレージ（API: 9000、コンソール: 9001） |
| minio-init | - | MinIOバケット・Webhook初期設定（one-shot） |
| image-processor | 3001 | 画像リサイズ・WebP変換サービス |

### サービス依存関係

```
postgres, redis, minio → backend → frontend
minio → minio-init
minio → image-processor
```

- `backend` は `postgres`、`redis`、`minio` の正常起動を待ってから起動
- `frontend` は `backend` のヘルスチェック完了を待ってから起動
- `minio-init` は `minio` の正常起動を待ってバケット・Webhook設定を実行後に終了
- `image-processor` は `minio` の正常起動を待ってから起動

### 環境変数

Frontend の Backend 接続先は以下の環境変数で制御されます:

| 環境変数 | 用途 | デフォルト値 |
|---|---|---|
| `NEXT_PUBLIC_BACKEND_URL` | クライアントサイド（ブラウザ）からのAPI呼び出し | `http://localhost:8080` |
| `BACKEND_URL` | サーバサイド（SSR）からのAPI呼び出し | `http://localhost:8080` |

Docker Compose 環境では `BACKEND_URL` に `http://backend:8080` が設定されます。

> **注意:** `NEXT_PUBLIC_BACKEND_URL` は Next.js のビルド時にベイクされるため、変更時は `docker compose up --build` での再ビルドが必要です。`BACKEND_URL` はランタイム環境変数のため再ビルド不要です。

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
docker compose up -d --build --wait backend frontend image-processor
docker compose up minio-init
chmod +x ./e2e/backend/e2e-test.sh
./e2e/backend/e2e-test.sh
docker compose down -v
```

テストレポートは `e2e/backend/reports/` ディレクトリに出力されます。

### Frontend 単体テスト

```shell
cd frontend
npm install
npm run test
```

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

### アーティスト一覧

```
GET /api/v1/artists
```

アーティスト一覧を50音順で取得する。

**成功レスポンス（200 OK）:**

```json
[
  {
    "artistId": "aimyon",
    "name": "あいみょん",
    "nameKana": "あいみょん",
    "iconUrl": "/images/artists/aimyon.svg"
  }
]
```

### コミュニティTOP

```
GET /api/v1/community/{artistId}
```

アーティストのコミュニティTOP情報（画像・キャンペーン・お知らせ）を集約取得する。

**成功レスポンス（200 OK）:**

```json
{
  "artistId": "aimyon",
  "name": "あいみょん",
  "images": [
    { "imageId": 1, "imageUrl": "/images/artists/aimyon.svg", "displayOrder": 1 }
  ],
  "campaigns": [
    { "campaignId": 1, "title": "ライブツアー2025", "imageUrl": "/images/campaigns/default.svg" }
  ],
  "news": [
    { "newsId": 1, "title": "ニューシングルリリース決定", "publishedAt": "2025-04-10T10:00:00Z" }
  ]
}
```

**アーティスト未存在レスポンス（404 Not Found）:**

```json
{
  "error": {
    "code": "ARTIST_NOT_FOUND",
    "message": "アーティストが見つかりません"
  }
}
```

### スレッド一覧

```
GET /api/v1/community/{artistId}/threads?page=1&size=20
```

アーティストのスレッド一覧を最新書き込み順で取得する。

**成功レスポンス（200 OK）:**

```json
{
  "threads": [
    {
      "threadId": "01970000-1000-7000-8000-000000000001",
      "title": "ライブの感想を語ろう！",
      "createdByUsername": "テストユーザ",
      "latestComment": "最高の思い出になりました！",
      "latestCommentAt": "2025-04-13T11:15:00Z"
    }
  ],
  "totalPages": 2,
  "totalThreads": 25
}
```

### スレッド詳細

```
GET /api/v1/community/{artistId}/threads/{threadId}?page=1&size=10
GET /api/v1/community/{artistId}/threads/{threadId}?before={commentId}&size=10
```

スレッド詳細とコメント一覧を取得する。`before`パラメータでカーソルベースページングに対応。

### スレッド作成

```
POST /api/v1/community/{artistId}/threads
X-Session-Id: {sessionId}
Content-Type: application/json
```

**リクエスト:**

```json
{
  "title": "新しいスレッド"
}
```

### コメント追加

```
POST /api/v1/community/{artistId}/threads/{threadId}/comments
X-Session-Id: {sessionId}
Content-Type: application/json
```

**リクエスト:**

```json
{
  "content": "コメント本文",
  "imageIds": ["画像ID1", "画像ID2"]
}
```

### 画像アップロードURL取得

```
POST /api/v1/images/upload-urls
X-Session-Id: {sessionId}
Content-Type: application/json
```

**リクエスト:**

```json
{
  "items": [
    { "fileName": "photo.jpg", "contentType": "image/jpeg" }
  ]
}
```

**成功レスポンス（200 OK）:**

```json
{
  "items": [
    {
      "imageId": "画像ID",
      "uploadUrl": "Pre-signed URL",
      "s3Key": "originals/xxx.jpg"
    }
  ]
}
```

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
│       │   │   ├── ArtistImageResponse.java
│       │   │   ├── ArtistResponse.java
│       │   │   ├── CampaignResponse.java
│       │   │   ├── CommentImageResponse.java
│       │   │   ├── CommentProjection.java
│       │   │   ├── CommunityTopResponse.java
│       │   │   ├── CreateCommentRequest.java
│       │   │   ├── CreateThreadRequest.java
│       │   │   ├── ErrorResponse.java
│       │   │   ├── LoginRequest.java
│       │   │   ├── LoginResponse.java
│       │   │   ├── NewsResponse.java
│       │   │   ├── ThreadCommentResponse.java
│       │   │   ├── ThreadDetailProjection.java
│       │   │   ├── ThreadDetailResponse.java
│       │   │   ├── ThreadListItemResponse.java
│       │   │   ├── ThreadListProjection.java
│       │   │   ├── ThreadListResponse.java
│       │   │   ├── UploadUrlItem.java
│       │   │   ├── UploadUrlRequest.java
│       │   │   └── UploadUrlResponse.java
│       │   ├── entity/             # JPAエンティティ
│       │   │   ├── ArtistEntity.java
│       │   │   ├── ArtistImageEntity.java
│       │   │   ├── CampaignEntity.java
│       │   │   ├── CommentImageEntity.java
│       │   │   ├── NewsEntity.java
│       │   │   ├── ThreadCommentEntity.java
│       │   │   ├── ThreadEntity.java
│       │   │   └── UserEntity.java
│       │   ├── exception/          # 例外クラス
│       │   │   ├── AuthenticationException.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── health/             # ヘルスチェック
│       │   │   ├── ApplicationLivenessCheck.java
│       │   │   ├── DatabaseHealthCheck.java
│       │   │   └── RedisHealthCheck.java
│       │   ├── repository/         # リポジトリ
│       │   │   ├── ArtistImageRepository.java
│       │   │   ├── ArtistRepository.java
│       │   │   ├── CampaignRepository.java
│       │   │   ├── CommentImageRepository.java
│       │   │   ├── NewsRepository.java
│       │   │   ├── ThreadCommentRepository.java
│       │   │   ├── ThreadRepository.java
│       │   │   └── UserRepository.java
│       │   ├── resource/           # RESTリソース
│       │   │   ├── ArtistResource.java
│       │   │   ├── CommunityResource.java
│       │   │   ├── ImageResource.java
│       │   │   ├── LoginResource.java
│       │   │   ├── SessionResource.java
│       │   │   └── ThreadResource.java
│       │   └── service/            # ビジネスロジック
│       │       ├── ArtistService.java
│       │       ├── AuthService.java
│       │       ├── CommunityService.java
│       │       ├── ImageService.java
│       │       ├── PasswordService.java
│       │       ├── SessionService.java
│       │       ├── ThreadService.java
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
│       │   ├── page.tsx            # トップページ（ようこそEntm-Cloneへ）
│       │   ├── not-found.tsx       # 共通404エラーページ
│       │   ├── error.tsx           # 共通エラーページ
│       │   ├── artists/
│       │   │   ├── page.tsx        # アーティスト一覧ページ（SSR）
│       │   │   └── loading.tsx     # ローディングスピナー
│       │   ├── community/
│       │   │   └── [artistId]/
│       │   │       ├── page.tsx    # コミュニティTOPページ（SSR）
│       │   │       ├── loading.tsx # ローディングスピナー
│       │   │       └── threads/
│       │   │           ├── page.tsx        # スレッド一覧ページ
│       │   │           └── [threadId]/
│       │   │               └── page.tsx    # スレッド詳細ページ
│       │   └── test/
│       │       └── page.tsx        # ヘルスチェック表示ページ
│       ├── components/
│       │   ├── AddCommentModal.tsx  # コメント追加モーダル（画像添付対応）
│       │   ├── ArtistCard.tsx      # アーティストカードコンポーネント
│       │   ├── CommentImageGrid.tsx # X(Twitter)風画像グリッド
│       │   ├── CommunityTopContent.tsx # コミュニティTOPコンテンツ（Client Component）
│       │   ├── CreateThreadModal.tsx # スレッド作成モーダル
│       │   ├── Header.tsx          # 共通ヘッダーコンポーネント
│       │   ├── ImageLightbox.tsx   # 画像ライトボックス（react-image-gallery）
│       │   ├── LoadingSpinner.tsx  # ローディングスピナーコンポーネント
│       │   ├── LoginModal.tsx      # ログインモーダルコンポーネント
│       │   └── LoginPromptDialog.tsx # ログイン促進ダイアログ
│       ├── contexts/
│       │   └── AuthContext.tsx     # 認証コンテキスト・プロバイダー
│       ├── types/
│       │   ├── artist.ts           # アーティスト型定義
│       │   ├── community.ts       # コミュニティ型定義
│       │   └── thread.ts          # スレッド・コメント型定義
│       └── utils/
│           ├── dateFormat.ts      # 相対日時表示ユーティリティ
│           └── imageUtils.ts      # 画像EXIF処理ユーティリティ（exifr）
├── image-processor/                # 画像処理サービス（Node.js + sharp）
│   ├── Dockerfile                  # ローカル開発用
│   ├── Makefile                    # Lambda SAMビルド用
│   ├── package.json
│   ├── package-lambda.json         # Lambda用依存定義
│   └── src/
│       ├── imageProcessor.js       # 共通画像処理ロジック
│       ├── lambdaHandler.js        # Lambda用エントリーポイント
│       └── localServer.js          # ローカル用エントリーポイント
├── lambda/                         # AWS SAMテンプレート
│   ├── template.yaml               # Lambda関数定義
│   └── samconfig.toml              # SAMデプロイ設定
├── minio/                          # MinIO設定ファイル
│   └── cors.json                   # CORS設定
├── e2e/                            # E2E・統合テスト
│   ├── backend/                   # Backend E2Eテスト
│   │   └── e2e-test.sh            # curl + jq によるAPIテスト
│   └── frontend/                  # Frontend統合テスト（Playwright + MSW）
│       ├── playwright.config.ts   # Playwright設定（PC/Android/iPhone）
│       ├── msw/                   # MSWモックハンドラー
│       │   ├── handlers.ts
│       │   └── setup.ts
│       └── tests/                 # テストファイル
├── docker-compose.yml
├── init-db.sql                     # DB初期化（全テーブル・初期データ）
└── .github/workflows/
    ├── unit-test.yml               # Backend単体テスト・カバレッジ検証
    ├── e2e-test.yml                # Backend E2Eテスト（Docker環境）
    ├── frontend-test.yml           # フロントエンド単体テスト
    ├── frontend-integration-test.yml # Frontend統合テスト（Playwright）
    └── deploy-lambda.yml           # Lambda自動デプロイ（無効化済み）
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
- [S3 Presigner](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3-presign.html) - Pre-signed URL生成

### Frontend

- [Next.js](https://nextjs.org/) - Reactフレームワーク（SSR対応）
- [TypeScript](https://www.typescriptlang.org/) - 型安全な JavaScript
- [Tailwind CSS](https://tailwindcss.com/) - ユーティリティファーストCSS
- [Embla Carousel](https://www.embla-carousel.com/) - カルーセルコンポーネント
- [react-hot-toast](https://react-hot-toast.com/) - トースト通知
- [react-image-gallery](https://github.com/xiaolin/react-image-gallery) - 画像ギャラリー・ライトボックス
- [exifr](https://github.com/MikeKovarik/exifr) - EXIF回転情報取得
- [Radix UI](https://www.radix-ui.com/) - UIプリミティブ（Dialog等）

### 画像処理

- [sharp](https://sharp.pixelplumbing.com/) - 画像リサイズ・WebP変換
- [Express](https://expressjs.com/) - ローカルWebhookサーバ

### インフラ・テスト

- [Docker](https://www.docker.com/) - コンテナ化
- [Docker Compose](https://docs.docker.com/compose/) - オーケストレーション
- [MinIO](https://min.io/) - S3互換オブジェクトストレージ
- [Vitest](https://vitest.dev/) - フロントエンド単体テスト
- [Playwright](https://playwright.dev/) - フロントエンド統合テスト
- [MSW](https://mswjs.io/) - APIモック
- [JUnit 5](https://junit.org/junit5/) - バックエンド単体テスト
- [JaCoCo](https://www.jacoco.org/) - コードカバレッジ
