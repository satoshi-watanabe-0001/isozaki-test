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
 * @since 1.0
 */
public class LoginRequest {

    /**
     * ログイン用メールアドレス
     */
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    /**
     * ログイン用パスワード
     */
    @NotBlank(message = "パスワードは必須です")
    private String password;

    /**
     * デフォルトコンストラクタ
     */
    public LoginRequest() {
    }

    /**
     * 全フィールドを指定するコンストラクタ
     *
     * @param email    メールアドレス
     * @param password パスワード
     */
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * メールアドレスを取得する
     *
     * @return メールアドレス
     */
    public String getEmail() {
        return email;
    }

    /**
     * メールアドレスを設定する
     *
     * @param email メールアドレス
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * パスワードを取得する
     *
     * @return パスワード
     */
    public String getPassword() {
        return password;
    }

    /**
     * パスワードを設定する
     *
     * @param password パスワード
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
