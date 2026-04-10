/**
 * 認証例外クラス
 *
 * <p>ログイン認証に失敗した場合にスローされる例外。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.exception;

/**
 * 認証失敗時にスローされる例外
 *
 * @since 1.0
 */
public class AuthenticationException extends RuntimeException {

    /**
     * エラーメッセージを指定して例外を生成する
     *
     * @param message エラーメッセージ
     */
    public AuthenticationException(String message) {
        super(message);
    }
}
