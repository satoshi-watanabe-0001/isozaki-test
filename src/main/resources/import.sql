-- ============================================================================
-- Script: import.sql
-- Description: 初期データ投入用SQLスクリプト
-- Dependencies: usersテーブル
-- Notes: テスト用の初期ユーザデータを投入する
--        パスワードは 'password123' をbcryptでハッシュ化した値
-- ============================================================================

-- テスト用ユーザデータ（パスワード: password123）
INSERT INTO users (user_id, username, email, password_hash, created_at, updated_at)
VALUES (
    '01908b7e-1234-7000-8000-000000000001',
    'テストユーザ',
    'test@example.com',
    '$2a$12$LJ3m4ys3uz2YHjYhBMUL5u0GNf9SmFRNkPlCz.Nzh5131MKmVaKMG',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
