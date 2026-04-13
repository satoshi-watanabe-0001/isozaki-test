/**
 * PasswordServiceの単体テスト
 *
 * <p>bcryptによるパスワードハッシュ化・検証ロジックの正常系・異常系をテストする。</p>
 *
 * @since 1.0
 */

package com.isozaki.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PasswordServiceのテストクラス
 */
@DisplayName("PasswordService テスト")
class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Nested
    @DisplayName("hashPassword テスト")
    class HashPasswordTests {

        /**
         * 【テスト対象】PasswordService#hashPassword
         * 【テストケース】平文パスワードをハッシュ化する
         * 【期待結果】nullでないハッシュ文字列が返却される
         * 【ビジネス要件】パスワードの安全な保存
         */
        @Test
        @DisplayName("パスワードをハッシュ化できること")
        void shouldHashPasswordSuccessfully() {
            // Given
            String plainPassword = "password123";

            // When
            String hashed = passwordService.hashPassword(plainPassword);

            // Then
            assertNotNull(hashed);
            assertNotEquals(plainPassword, hashed);
        }

        /**
         * 【テスト対象】PasswordService#hashPassword
         * 【テストケース】同じパスワードを2回ハッシュ化する
         * 【期待結果】異なるハッシュ値が生成される
         * 【ビジネス要件】bcryptのソルトによるセキュリティ確保
         */
        @Test
        @DisplayName("同じパスワードでも異なるハッシュ値が生成されること")
        void shouldGenerateDifferentHashesForSamePassword() {
            // Given
            String plainPassword = "password123";

            // When
            String hash1 = passwordService.hashPassword(plainPassword);
            String hash2 = passwordService.hashPassword(plainPassword);

            // Then
            assertNotEquals(hash1, hash2);
        }

        /**
         * 【テスト対象】PasswordService#hashPassword
         * 【テストケース】日本語パスワードをハッシュ化する
         * 【期待結果】nullでないハッシュ文字列が返却される
         * 【ビジネス要件】日本語入力対応
         */
        @Test
        @DisplayName("日本語パスワードをハッシュ化できること")
        void shouldHashJapanesePassword() {
            // Given
            String plainPassword = "パスワード123";

            // When
            String hashed = passwordService.hashPassword(plainPassword);

            // Then
            assertNotNull(hashed);
            assertNotEquals(plainPassword, hashed);
        }
    }

    @Nested
    @DisplayName("verifyPassword テスト")
    class VerifyPasswordTests {

        /**
         * 【テスト対象】PasswordService#verifyPassword
         * 【テストケース】正しいパスワードで検証する
         * 【期待結果】trueが返却される
         * 【ビジネス要件】ログイン時のパスワード検証
         */
        @Test
        @DisplayName("正しいパスワードで検証が成功すること")
        void shouldVerifyCorrectPassword() {
            // Given
            String plainPassword = "password123";
            String hashed = passwordService.hashPassword(plainPassword);

            // When
            boolean result = passwordService.verifyPassword(plainPassword, hashed);

            // Then
            assertTrue(result);
        }

        /**
         * 【テスト対象】PasswordService#verifyPassword
         * 【テストケース】誤ったパスワードで検証する
         * 【期待結果】falseが返却される
         * 【ビジネス要件】不正パスワードの拒否
         */
        @Test
        @DisplayName("誤ったパスワードで検証が失敗すること")
        void shouldRejectIncorrectPassword() {
            // Given
            String plainPassword = "password123";
            String hashed = passwordService.hashPassword(plainPassword);

            // When
            boolean result = passwordService.verifyPassword("wrongpassword", hashed);

            // Then
            assertFalse(result);
        }

        /**
         * 【テスト対象】PasswordService#verifyPassword
         * 【テストケース】空文字列のパスワードで検証する
         * 【期待結果】falseが返却される
         * 【ビジネス要件】空パスワードの拒否
         */
        @Test
        @DisplayName("空文字列のパスワードで検証が失敗すること")
        void shouldRejectEmptyPassword() {
            // Given
            String plainPassword = "password123";
            String hashed = passwordService.hashPassword(plainPassword);

            // When
            boolean result = passwordService.verifyPassword("", hashed);

            // Then
            assertFalse(result);
        }
    }
}
