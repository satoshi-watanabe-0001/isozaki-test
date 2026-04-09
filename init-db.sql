-- ============================================================================
-- Script: init-db.sql
-- Description: PostgreSQLデータベース初期化スクリプト
-- Dependencies: なし
-- Notes: Docker起動時にデータベースとテーブルを初期化する
--        文字コードはutf8mb4相当（UTF-8）を使用
-- ============================================================================

-- usersテーブルの作成
CREATE TABLE IF NOT EXISTS users (
    -- ユーザID（UUIDv7形式、PostgreSQL UUID型、プライマリキー）
    user_id UUID NOT NULL PRIMARY KEY,

    -- ユーザ名（日本語入力可、UTF-8対応）
    username VARCHAR(255) NOT NULL,

    -- メールアドレス（重複不可、ユニーク制約）
    email VARCHAR(255) NOT NULL UNIQUE,

    -- bcryptでハッシュ化されたパスワード
    password_hash VARCHAR(255) NOT NULL,

    -- レコード作成日時
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- レコード更新日時
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- メールアドレス検索用インデックス
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- テスト用ユーザデータ（パスワード: password123）
INSERT INTO users (user_id, username, email, password_hash, created_at, updated_at)
VALUES (
    CAST('01908b7e-1234-7000-8000-000000000001' AS uuid),
    'テストユーザ',
    'test@example.com',
    '$2a$12$LJ3m4ys3uz2YHjYhBMUL5u0GNf9SmFRNkPlCz.Nzh5131MKmVaKMG',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;
