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

-- ============================================================================
-- threadsテーブルの作成（スレッド情報）
-- ============================================================================

CREATE TABLE IF NOT EXISTS threads (
    -- スレッドID（UUIDv7形式）
    thread_id UUID PRIMARY KEY,

    -- アーティストID（外部キー）
    artist_id VARCHAR(100) NOT NULL REFERENCES artists(artist_id),

    -- スレッドタイトル（最大50文字、改行不可）
    title VARCHAR(50) NOT NULL,

    -- スレッド作成者（外部キー → usersテーブル）
    created_by UUID NOT NULL REFERENCES users(user_id),

    -- レコード作成日時
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- アーティストID検索用インデックス
CREATE INDEX IF NOT EXISTS idx_threads_artist_id ON threads (artist_id);

-- ============================================================================
-- thread_commentsテーブルの作成（スレッドコメント情報）
-- ============================================================================

CREATE TABLE IF NOT EXISTS thread_comments (
    -- コメントID（UUIDv7形式）
    comment_id UUID PRIMARY KEY,

    -- スレッドID（外部キー）
    thread_id UUID NOT NULL REFERENCES threads(thread_id),

    -- コメント内容（最大200文字、改行可）
    content VARCHAR(200) NOT NULL,

    -- コメント作成者（外部キー → usersテーブル）
    created_by UUID NOT NULL REFERENCES users(user_id),

    -- レコード作成日時
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- スレッドID・作成日時検索用インデックス
CREATE INDEX IF NOT EXISTS idx_thread_comments_thread_id ON thread_comments (thread_id, created_at DESC);

-- テスト用追加ユーザデータ（スレッド投稿用）
INSERT INTO users (user_id, username, email, password_hash, created_at, updated_at)
VALUES (
    CAST('01908b7e-1234-7000-8000-000000000002' AS uuid),
    'サブユーザ',
    'sub@example.com',
    '$2a$12$7VmZoxX4W.QSvQelvMWgHOYRzd0BJzdUiuUOCrlkaedQC1.yte/y2',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- テスト用スレッドデータ（あいみょんコミュニティ、25件、UUIDv7形式）
INSERT INTO threads (thread_id, artist_id, title, created_by, created_at) VALUES
    (CAST('01970000-1000-7000-8000-000000000001' AS uuid), 'aimyon', 'ライブの感想を語ろう！', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-13 10:00:00'),
    (CAST('01970000-1000-7000-8000-000000000002' AS uuid), 'aimyon', 'おすすめの曲を教えてください', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-12 15:30:00'),
    (CAST('01970000-1000-7000-8000-000000000003' AS uuid), 'aimyon', '新曲「風になりたい」の感想', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-11 09:00:00'),
    (CAST('01970000-1000-7000-8000-000000000004' AS uuid), 'aimyon', 'ファンクラブイベント参加した方いますか？', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-10 20:00:00'),
    (CAST('01970000-1000-7000-8000-000000000005' AS uuid), 'aimyon', 'マリーゴールドが好きな人集合', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-09 14:00:00'),
    (CAST('01970000-1000-7000-8000-000000000006' AS uuid), 'aimyon', 'カラオケで歌いやすい曲は？', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-08 11:00:00'),
    (CAST('01970000-1000-7000-8000-000000000007' AS uuid), 'aimyon', 'ギター弾き語りしてる人いますか', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-07 16:30:00'),
    (CAST('01970000-1000-7000-8000-000000000008' AS uuid), 'aimyon', '歌詞の解釈について語りたい', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-06 08:00:00'),
    (CAST('01970000-1000-7000-8000-000000000009' AS uuid), 'aimyon', 'アルバムのおすすめ順番', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-05 19:00:00'),
    (CAST('01970000-1000-7000-8000-00000000000a' AS uuid), 'aimyon', 'ライブグッズの交換希望', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-04 13:00:00'),
    (CAST('01970000-1000-7000-8000-00000000000b' AS uuid), 'aimyon', 'あいみょんの魅力を語る', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-03 10:00:00'),
    (CAST('01970000-1000-7000-8000-00000000000c' AS uuid), 'aimyon', 'MVの考察スレ', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-02 22:00:00'),
    (CAST('01970000-1000-7000-8000-00000000000d' AS uuid), 'aimyon', '初めてライブに行く方へ', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-01 07:00:00'),
    (CAST('01970000-1000-7000-8000-00000000000e' AS uuid), 'aimyon', 'セットリスト予想', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-03-31 18:00:00'),
    (CAST('01970000-1000-7000-8000-00000000000f' AS uuid), 'aimyon', 'コラボ曲について', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-03-30 12:00:00'),
    (CAST('01970000-1000-7000-8000-000000000010' AS uuid), 'aimyon', 'ラジオ出演情報まとめ', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-03-29 09:00:00'),
    (CAST('01970000-1000-7000-8000-000000000011' AS uuid), 'aimyon', 'テレビ出演の感想', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-03-28 21:00:00'),
    (CAST('01970000-1000-7000-8000-000000000012' AS uuid), 'aimyon', 'あいみょん好きな有名人', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-03-27 15:00:00'),
    (CAST('01970000-1000-7000-8000-000000000013' AS uuid), 'aimyon', '弾き語りアレンジ共有', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-03-26 10:00:00'),
    (CAST('01970000-1000-7000-8000-000000000014' AS uuid), 'aimyon', 'ツアーの思い出を語ろう', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-03-25 14:00:00'),
    (CAST('01970000-1000-7000-8000-000000000015' AS uuid), 'aimyon', '雑談スレッド', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-03-24 08:00:00'),
    (CAST('01970000-1000-7000-8000-000000000016' AS uuid), 'aimyon', '歌詞ノートを見せ合おう', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-03-23 17:00:00'),
    (CAST('01970000-1000-7000-8000-000000000017' AS uuid), 'aimyon', 'フェス参戦報告', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-03-22 11:00:00'),
    (CAST('01970000-1000-7000-8000-000000000018' AS uuid), 'aimyon', 'あいみょん聖地巡礼', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-03-21 09:00:00'),
    (CAST('01970000-1000-7000-8000-000000000019' AS uuid), 'aimyon', '自己紹介スレッド', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-03-20 06:00:00')
ON CONFLICT DO NOTHING;

-- テスト用コメントデータ（スレッド1に15件のコメント、UUIDv7形式）
INSERT INTO thread_comments (comment_id, thread_id, content, created_by, created_at) VALUES
    (CAST('01970000-2000-7000-8000-000000000001' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), 'ライブ最高でした！特にアンコールが感動的でした。', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-13 10:05:00'),
    (CAST('01970000-2000-7000-8000-000000000002' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), '私も行きました！MCが面白かったですよね。', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-13 10:10:00'),
    (CAST('01970000-2000-7000-8000-000000000003' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), '次のツアーも絶対行きたい！チケット争奪戦頑張りましょう。', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-13 10:15:00'),
    (CAST('01970000-2000-7000-8000-000000000004' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), 'セットリストが神でした。新曲も良かったけど、定番曲の盛り上がりが最高。', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-13 10:20:00'),
    (CAST('01970000-2000-7000-8000-000000000005' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), '会場の雰囲気も最高でしたね。みんなで一体になって歌った瞬間が忘れられません。', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-13 10:25:00'),
    (CAST('01970000-2000-7000-8000-000000000006' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), 'グッズも可愛かったです。タオルとTシャツ買いました！', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-13 10:30:00'),
    (CAST('01970000-2000-7000-8000-000000000007' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), '初めてのライブだったけど、すごく楽しかった。ファンになって良かった。', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-13 10:35:00'),
    (CAST('01970000-2000-7000-8000-000000000008' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), '音響が良かった！特にバラード曲の時は鳥肌が立ちました。', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-13 10:40:00'),
    (CAST('01970000-2000-7000-8000-000000000009' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), 'アンコールで泣いてる人がたくさんいましたね。', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-13 10:45:00'),
    (CAST('01970000-2000-7000-8000-00000000000a' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), '地方公演にも行く予定の人いますか？', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-13 10:50:00'),
    (CAST('01970000-2000-7000-8000-00000000000b' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), '大阪公演行きます！楽しみ！', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-13 10:55:00'),
    (CAST('01970000-2000-7000-8000-00000000000c' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), '写真撮影OKの場所があって嬉しかったです。', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-13 11:00:00'),
    (CAST('01970000-2000-7000-8000-00000000000d' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), '来年もツアーあるといいな。毎年恒例にしてほしい。', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-13 11:05:00'),
    (CAST('01970000-2000-7000-8000-00000000000e' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), 'ライブ後のご飯も楽しみのひとつですよね。', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-13 11:10:00'),
    (CAST('01970000-2000-7000-8000-00000000000f' AS uuid), CAST('01970000-1000-7000-8000-000000000001' AS uuid), '最高の思い出になりました！みなさんありがとう！', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-13 11:15:00'),
    (CAST('01970000-2000-7000-8000-000000000010' AS uuid), CAST('01970000-1000-7000-8000-000000000002' AS uuid), '「マリーゴールド」は名曲ですよね。', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-12 15:35:00'),
    (CAST('01970000-2000-7000-8000-000000000011' AS uuid), CAST('01970000-1000-7000-8000-000000000002' AS uuid), '「裸の心」もおすすめです！歌詞がすごく良い。', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-12 16:00:00'),
    (CAST('01970000-2000-7000-8000-000000000012' AS uuid), CAST('01970000-1000-7000-8000-000000000002' AS uuid), '「ハルノヒ」聴いてみてください。映画の主題歌にもなった曲です。', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-12 16:30:00'),
    (CAST('01970000-2000-7000-8000-000000000013' AS uuid), CAST('01970000-1000-7000-8000-000000000003' AS uuid), '新曲すごく良かったです！リピートが止まらない。', CAST('01908b7e-1234-7000-8000-000000000002' AS uuid), '2025-04-11 09:30:00'),
    (CAST('01970000-2000-7000-8000-000000000014' AS uuid), CAST('01970000-1000-7000-8000-000000000003' AS uuid), 'MVの世界観も素敵でしたね。', CAST('01908b7e-1234-7000-8000-000000000001' AS uuid), '2025-04-11 10:00:00')
ON CONFLICT DO NOTHING;
