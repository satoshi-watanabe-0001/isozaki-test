/**
 * ログインレスポンスDTOクラス
 *
 * <p>ログイン成功時にクライアントへ返却するレスポンスボディを表現するDTO。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.dto;

/**
 * ログイン成功時のレスポンスデータ転送オブジェクト
 *
 * <p>セッションID、ユーザID、ユーザ名を含む。</p>
 *
 * @since 1.0
 */
public class LoginResponse {

    /**
     * セッションID
     */
    private String sessionId;

    /**
     * ユーザID（UUIDv7）
     */
    private String userId;

    /**
     * ユーザ名
     */
    private String username;

    /**
     * デフォルトコンストラクタ
     */
    public LoginResponse() {
    }

    /**
     * 全フィールドを指定するコンストラクタ
     *
     * @param sessionId セッションID
     * @param userId    ユーザID
     * @param username  ユーザ名
     */
    public LoginResponse(String sessionId, String userId, String username) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
    }

    /**
     * セッションIDを取得する
     *
     * @return セッションID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * セッションIDを設定する
     *
     * @param sessionId セッションID
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * ユーザIDを取得する
     *
     * @return ユーザID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * ユーザIDを設定する
     *
     * @param userId ユーザID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * ユーザ名を取得する
     *
     * @return ユーザ名
     */
    public String getUsername() {
        return username;
    }

    /**
     * ユーザ名を設定する
     *
     * @param username ユーザ名
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
