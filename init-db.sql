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

-- ============================================================================
-- artist_imagesテーブルの作成（カルーセル用画像）
-- ============================================================================

CREATE TABLE IF NOT EXISTS artist_images (
    -- 画像ID（連番）
    image_id SERIAL PRIMARY KEY,

    -- アーティストID（外部キー）
    artist_id VARCHAR(100) NOT NULL REFERENCES artists(artist_id),

    -- 画像URL（フロントエンド静的ファイルのパス）
    image_url VARCHAR(500) NOT NULL,

    -- 表示順（昇順で表示）
    display_order INT NOT NULL DEFAULT 0,

    -- レコード作成日時
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- アーティストID検索用インデックス
CREATE INDEX IF NOT EXISTS idx_artist_images_artist_id ON artist_images (artist_id);

-- テスト用カルーセル画像データ（各アーティスト3件ずつ）
INSERT INTO artist_images (artist_id, image_url, display_order, created_at) VALUES
    ('aimyon', '/images/artists/aimyon.svg', 1, CURRENT_TIMESTAMP),
    ('aimyon', '/images/artists/aimyon.svg', 2, CURRENT_TIMESTAMP),
    ('aimyon', '/images/artists/aimyon.svg', 3, CURRENT_TIMESTAMP),
    ('arashi', '/images/artists/arashi.svg', 1, CURRENT_TIMESTAMP),
    ('arashi', '/images/artists/arashi.svg', 2, CURRENT_TIMESTAMP),
    ('arashi', '/images/artists/arashi.svg', 3, CURRENT_TIMESTAMP),
    ('ikimonogakari', '/images/artists/ikimonogakari.svg', 1, CURRENT_TIMESTAMP),
    ('ikimonogakari', '/images/artists/ikimonogakari.svg', 2, CURRENT_TIMESTAMP),
    ('ikimonogakari', '/images/artists/ikimonogakari.svg', 3, CURRENT_TIMESTAMP),
    ('spitz', '/images/artists/spitz.svg', 1, CURRENT_TIMESTAMP),
    ('spitz', '/images/artists/spitz.svg', 2, CURRENT_TIMESTAMP),
    ('spitz', '/images/artists/spitz.svg', 3, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- campaignsテーブルの作成（キャンペーン情報）
-- ============================================================================

CREATE TABLE IF NOT EXISTS campaigns (
    -- キャンペーンID（連番）
    campaign_id SERIAL PRIMARY KEY,

    -- アーティストID（外部キー）
    artist_id VARCHAR(100) NOT NULL REFERENCES artists(artist_id),

    -- キャンペーンタイトル
    title VARCHAR(255) NOT NULL,

    -- キャンペーン画像URL（フロントエンド静的ファイルのパス）
    image_url VARCHAR(500) NOT NULL,

    -- 表示順（昇順で表示）
    display_order INT NOT NULL DEFAULT 0,

    -- レコード作成日時
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- アーティストID検索用インデックス
CREATE INDEX IF NOT EXISTS idx_campaigns_artist_id ON campaigns (artist_id);

-- テスト用キャンペーンデータ（各アーティスト3件ずつ）
INSERT INTO campaigns (artist_id, title, image_url, display_order, created_at) VALUES
    ('aimyon', 'ライブツアー2025', '/images/campaigns/default.svg', 1, CURRENT_TIMESTAMP),
    ('aimyon', 'ニューアルバム発売記念', '/images/campaigns/default.svg', 2, CURRENT_TIMESTAMP),
    ('aimyon', 'ファンクラブ限定イベント', '/images/campaigns/default.svg', 3, CURRENT_TIMESTAMP),
    ('arashi', 'アニバーサリーフェス', '/images/campaigns/default.svg', 1, CURRENT_TIMESTAMP),
    ('arashi', 'メンバーソロ企画', '/images/campaigns/default.svg', 2, CURRENT_TIMESTAMP),
    ('arashi', 'グッズプレゼント', '/images/campaigns/default.svg', 3, CURRENT_TIMESTAMP),
    ('spitz', '結成記念ライブ', '/images/campaigns/default.svg', 1, CURRENT_TIMESTAMP),
    ('spitz', 'ベストアルバム投票', '/images/campaigns/default.svg', 2, CURRENT_TIMESTAMP),
    ('spitz', 'スペシャルコラボ', '/images/campaigns/default.svg', 3, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- newsテーブルの作成（お知らせ情報）
-- ============================================================================

CREATE TABLE IF NOT EXISTS news (
    -- お知らせID（連番）
    news_id SERIAL PRIMARY KEY,

    -- アーティストID（外部キー）
    artist_id VARCHAR(100) NOT NULL REFERENCES artists(artist_id),

    -- お知らせタイトル
    title VARCHAR(500) NOT NULL,

    -- 公開日時（新着順ソートに使用）
    published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- レコード作成日時
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- アーティストID・公開日時検索用インデックス
CREATE INDEX IF NOT EXISTS idx_news_artist_published ON news (artist_id, published_at DESC);

-- テスト用お知らせデータ（各アーティスト5件ずつ）
INSERT INTO news (artist_id, title, published_at, created_at) VALUES
    ('aimyon', 'ニューシングル「風になりたい」リリース決定', '2025-04-10 10:00:00', CURRENT_TIMESTAMP),
    ('aimyon', '全国ツアー2025 追加公演決定', '2025-04-08 12:00:00', CURRENT_TIMESTAMP),
    ('aimyon', 'テレビ出演情報（4月）', '2025-04-05 09:00:00', CURRENT_TIMESTAMP),
    ('aimyon', 'オフィシャルグッズ新商品のお知らせ', '2025-04-01 15:00:00', CURRENT_TIMESTAMP),
    ('aimyon', 'ファンクラブ会員限定イベント開催', '2025-03-28 11:00:00', CURRENT_TIMESTAMP),
    ('arashi', 'デジタルコンテンツ配信開始', '2025-04-12 10:00:00', CURRENT_TIMESTAMP),
    ('arashi', 'メンバー出演ドラマ情報', '2025-04-09 14:00:00', CURRENT_TIMESTAMP),
    ('arashi', '記念グッズ受注販売開始', '2025-04-06 10:00:00', CURRENT_TIMESTAMP),
    ('arashi', 'ファンミーティング開催決定', '2025-04-03 12:00:00', CURRENT_TIMESTAMP),
    ('arashi', 'オフィシャルサイトリニューアル', '2025-03-30 09:00:00', CURRENT_TIMESTAMP),
    ('spitz', '新曲「青い車（Re-recording）」配信開始', '2025-04-11 10:00:00', CURRENT_TIMESTAMP),
    ('spitz', 'ライブDVD/Blu-ray発売決定', '2025-04-07 11:00:00', CURRENT_TIMESTAMP),
    ('spitz', 'ラジオ出演情報', '2025-04-04 09:00:00', CURRENT_TIMESTAMP),
    ('spitz', 'SPITZ JAMBOREE TOUR 開催', '2025-04-02 15:00:00', CURRENT_TIMESTAMP),
    ('spitz', 'ファンクラブ更新特典のご案内', '2025-03-29 10:00:00', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;
