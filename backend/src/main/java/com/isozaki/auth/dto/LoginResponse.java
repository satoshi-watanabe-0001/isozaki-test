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
 * @param sessionId セッションID
 * @param userId    ユーザID（UUIDv7）
 * @param username  ユーザ名
 * @since 1.0
 */
public record LoginResponse(
        String sessionId,
        String userId,
        String username
) {
}
