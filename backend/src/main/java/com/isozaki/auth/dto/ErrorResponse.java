/**
 * エラーレスポンスDTOクラス
 *
 * <p>エラー発生時にクライアントへ返却するレスポンスボディを表現するDTO。
 * API規約に準拠した統一レスポンス構造を提供する。</p>
 *
 * @since 1.0
 */

package com.isozaki.auth.dto;

import java.time.Instant;
import java.util.List;

/**
 * エラーレスポンスのデータ転送オブジェクト
 *
 * <p>API規約に準拠した構造: {@code { "error": { "code", "message", "details" }, "meta": { "timestamp" } }}</p>
 *
 * @param error エラー詳細情報
 * @param meta  メタ情報（タイムスタンプ等）
 * @since 1.0
 */
public record ErrorResponse(
        ErrorDetail error,
        MetaInfo meta
) {

    /**
     * エラー詳細情報を保持するレコード
     *
     * @param code    エラーコード
     * @param message エラーメッセージ
     * @param details エラー詳細のリスト（空リスト可）
     */
    public record ErrorDetail(
            String code,
            String message,
            List<String> details
    ) {
    }

    /**
     * メタ情報を保持するレコード
     *
     * @param timestamp レスポンス生成時刻（ISO-8601形式）
     */
    public record MetaInfo(
            String timestamp
    ) {
    }

    /**
     * エラーコードとメッセージからErrorResponseを生成するファクトリメソッド
     *
     * @param code    エラーコード
     * @param message エラーメッセージ
     * @return ErrorResponseインスタンス
     */
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(
                new ErrorDetail(code, message, List.of()),
                new MetaInfo(Instant.now().toString())
        );
    }

    /**
     * エラーコード、メッセージ、詳細リストからErrorResponseを生成するファクトリメソッド
     *
     * @param code    エラーコード
     * @param message エラーメッセージ
     * @param details エラー詳細のリスト
     * @return ErrorResponseインスタンス
     */
    public static ErrorResponse of(String code, String message, List<String> details) {
        return new ErrorResponse(
                new ErrorDetail(code, message, details),
                new MetaInfo(Instant.now().toString())
        );
    }
}
