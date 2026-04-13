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

    /**
     * 【テスト対象】AuthenticationException コンストラクタ
     * 【テストケース】エラーメッセージを指定して例外を生成する
     * 【期待結果】getMessageで指定したメッセージが取得できる
     * 【ビジネス要件】認証失敗時のエラー情報伝達
     */
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

    /**
     * 【テスト対象】AuthenticationException 継承関係
     * 【テストケース】例外のインスタンス型を確認する
     * 【期待結果】RuntimeExceptionのサブクラスである
     * 【ビジネス要件】非検査例外としての認証エラー設計
     */
    @Test
    @DisplayName("RuntimeExceptionを継承していること")
    void shouldExtendRuntimeException() {
        // When
        AuthenticationException exception = new AuthenticationException("test");

        // Then
        assertInstanceOf(RuntimeException.class, exception);
    }
}
