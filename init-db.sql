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
    -- アーティストID（UUIDv7形式、PostgreSQL UUID型、プライマリキー）
    artist_id UUID NOT NULL PRIMARY KEY,

    -- アーティスト名（日本語入力可、UTF-8対応）
    name VARCHAR(255) NOT NULL UNIQUE,

    -- ソート用読み仮名（ひらがな、50音順ソートに使用）
    name_kana VARCHAR(255) NOT NULL,

    -- アイコン画像のURL（ダミー画像使用）
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
    (CAST('01908b7e-2001-7000-8000-000000000001' AS uuid), 'あいみょん', 'あいみょん', 'https://placehold.co/150x150?text=A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (CAST('01908b7e-2002-7000-8000-000000000002' AS uuid), 'いきものがかり', 'いきものがかり', 'https://placehold.co/150x150?text=I', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (CAST('01908b7e-2003-7000-8000-000000000003' AS uuid), 'ウルフルズ', 'うるふるず', 'https://placehold.co/150x150?text=U', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (CAST('01908b7e-2004-7000-8000-000000000004' AS uuid), 'EXILE', 'えぐざいる', 'https://placehold.co/150x150?text=E', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (CAST('01908b7e-2005-7000-8000-000000000005' AS uuid), '大塚愛', 'おおつかあい', 'https://placehold.co/150x150?text=O', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (CAST('01908b7e-2006-7000-8000-000000000006' AS uuid), '嵐', 'あらし', 'https://placehold.co/150x150?text=AR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (CAST('01908b7e-2007-7000-8000-000000000007' AS uuid), 'GLAY', 'ぐれい', 'https://placehold.co/150x150?text=GL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (CAST('01908b7e-2008-7000-8000-000000000008' AS uuid), 'サザンオールスターズ', 'さざんおーるすたーず', 'https://placehold.co/150x150?text=SA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (CAST('01908b7e-2009-7000-8000-000000000009' AS uuid), 'スピッツ', 'すぴっつ', 'https://placehold.co/150x150?text=SP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (CAST('01908b7e-200a-7000-8000-00000000000a' AS uuid), 'DREAMS COME TRUE', 'どりーむずかむとぅるー', 'https://placehold.co/150x150?text=DC', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;
