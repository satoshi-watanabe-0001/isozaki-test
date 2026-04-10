/**
 * エラーレスポンスDTOクラス
 *
 * <p>エラー発生時にクライアントへ返却するレスポンスボディを表現するDTO。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.dto;

/**
 * エラーレスポンスのデータ転送オブジェクト
 *
 * <p>エラーコードとメッセージを含む。</p>
 *
 * @param errorCode エラーコード
 * @param message   エラーメッセージ
 * @since 1.0
 */
public record ErrorResponse(
        String errorCode,
        String message
) {
}
