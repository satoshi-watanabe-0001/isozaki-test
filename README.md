# ユーザ認証アプリケーション

Quarkus フレームワークを使用したユーザ認証アプリケーションです。

## 概要

- **フレームワーク**: Quarkus（Java 17）
- **ビルドツール**: Gradle
- **データベース**: PostgreSQL 16（UTF-8）
- **セッション管理**: Redis 7
- **コンテナ**: Docker（マルチステージビルド）

## 機能

- JSON形式のログインエンドポイント（`POST /api/login`）
- メールアドレスとパスワードによるユーザ認証
- bcrypt（コストファクタ12）によるパスワードハッシュ化
- Redis によるセッション管理（TTL: 30分）
- UUIDv7 によるユーザID生成

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

開発モード（ライブコーディング対応）で起動するには以下を実行します:

```shell
./gradlew quarkusDev
```

> **注意:** 開発モードではDev UIが利用可能です: <http://localhost:8080/q/dev/>

## Docker環境での起動

Docker Compose を使用してアプリケーション、PostgreSQL、Redisを一括起動できます:

```shell
docker-compose up --build
```

### サービス構成

| サービス | ポート | 説明 |
|---|---|---|
| app | 8080 | Quarkusアプリケーション |
| postgres | 5432 | PostgreSQLデータベース |
| redis | 6379 | Redisセッションストア |

## ビルド

アプリケーションのビルド:

```shell
./gradlew build
```

`build/quarkus-app/` ディレクトリに `quarkus-run.jar` が生成されます。

uber-jar としてビルドする場合:

```shell
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

## テスト

単体テストの実行:

```shell
./gradlew test
```

カバレッジレポートの生成:

```shell
./gradlew jacocoTestReport
```

カバレッジレポートは `build/reports/jacoco/test/html/index.html` で確認できます。

カバレッジ検証（C1カバレッジ90%以上）:

```shell
./gradlew jacocoTestCoverageVerification
```

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

## プロジェクト構成

```
src/main/java/com/isozaki/auth/
├── dto/                    # データ転送オブジェクト
│   ├── ErrorResponse.java
│   ├── LoginRequest.java
│   └── LoginResponse.java
├── entity/                 # JPAエンティティ
│   └── UserEntity.java
├── exception/              # 例外クラス
│   ├── AuthenticationException.java
│   └── GlobalExceptionHandler.java
├── repository/             # リポジトリ
│   └── UserRepository.java
├── resource/               # RESTリソース
│   └── LoginResource.java
└── service/                # ビジネスロジック
    ├── AuthService.java
    ├── PasswordService.java
    ├── SessionService.java
    └── UuidService.java
```

## 技術スタック

- [Quarkus](https://quarkus.io/) - Javaフレームワーク
- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache) - ORM
- [REST Jackson](https://quarkus.io/guides/rest#json-serialisation) - JSONシリアライゼーション
- [Hibernate Validator](https://quarkus.io/guides/validation) - バリデーション
- [Redis Client](https://quarkus.io/guides/redis) - Redisクライアント
- [JDBC Driver - PostgreSQL](https://quarkus.io/guides/datasource) - PostgreSQLドライバ
