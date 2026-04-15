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
    '$2a$12$7VmZoxX4W.QSvQelvMWgHOYRzd0BJzdUiuUOCrlkaedQC1.yte/y2',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash;

-- ============================================================================
-- artistsテーブルの作成
-- ============================================================================

CREATE TABLE IF NOT EXISTS artists (
    -- アーティストID（英名文字列、URLパスとして利用可能、プライマリキー）
    artist_id VARCHAR(100) NOT NULL PRIMARY KEY,

    -- アーティスト名（日本語入力可、UTF-8対応）
    name VARCHAR(255) NOT NULL UNIQUE,

    -- ソート用読み仮名（ひらがな、50音順ソートに使用）
    name_kana VARCHAR(255) NOT NULL,

    -- アイコン画像のURL（フロントエンド静的ファイルのパス）
    icon_url VARCHAR(500),

    -- レコード作成日時
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- レコード更新日時
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 読み仮名検索・ソート用インデックス
CREATE INDEX IF NOT EXISTS idx_artists_name_kana ON artists (name_kana);

-- テスト用アーティストデータ（50音順で表示されるようname_kanaを設定）
INSERT INTO artists (artist_id, name, name_kana, icon_url, created_at, updated_at) VALUES
    ('aimyon', 'あいみょん', 'あいみょん', '/images/artists/aimyon.svg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ikimonogakari', 'いきものがかり', 'いきものがかり', '/images/artists/ikimonogakari.svg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ulfuls', 'ウルフルズ', 'うるふるず', '/images/artists/ulfuls.svg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('exile', 'EXILE', 'えぐざいる', '/images/artists/exile.svg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('otsuka-ai', '大塚愛', 'おおつかあい', '/images/artists/otsuka-ai.svg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('arashi', '嵐', 'あらし', '/images/artists/arashi.svg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('glay', 'GLAY', 'ぐれい', '/images/artists/glay.svg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('southern-all-stars', 'サザンオールスターズ', 'さざんおーるすたーず', '/images/artists/southern-all-stars.svg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('spitz', 'スピッツ', 'すぴっつ', '/images/artists/spitz.svg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('dreams-come-true', 'DREAMS COME TRUE', 'どりーむずかむとぅるー', '/images/artists/dreams-come-true.svg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;
