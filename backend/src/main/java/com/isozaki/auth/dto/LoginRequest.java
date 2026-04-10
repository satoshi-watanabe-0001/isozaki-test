/**
 * ログインリクエストDTOクラス
 *
 * <p>ログインエンドポイントへのリクエストボディを表現するDTO。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * ログインリクエストのデータ転送オブジェクト
 *
 * <p>JSON形式でメールアドレスとパスワードを受け取る。</p>
 *
 * @param email    ログイン用メールアドレス
 * @param password ログイン用パスワード
 * @since 1.0
 */
public record LoginRequest(
        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "メールアドレスの形式が正しくありません")
        String email,

        @NotBlank(message = "パスワードは必須です")
        String password
) {
}
