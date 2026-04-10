/**
 * AuthenticationExceptionの単体テスト
 *
 * <p>認証例外クラスのコンストラクタとメッセージ取得をテストする。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * AuthenticationExceptionのテストクラス
 */
@DisplayName("AuthenticationException テスト")
class AuthenticationExceptionTest {

    @Test
    @DisplayName("エラーメッセージが正しく設定されること")
    void shouldSetMessageCorrectly() {
        // Given
        String message = "メールアドレスまたはパスワードが正しくありません";

        // When
        AuthenticationException exception = new AuthenticationException(message);

        // Then
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("RuntimeExceptionを継承していること")
    void shouldExtendRuntimeException() {
        // When
        AuthenticationException exception = new AuthenticationException("test");

        // Then
        assertInstanceOf(RuntimeException.class, exception);
    }
}
