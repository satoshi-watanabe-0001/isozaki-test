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
