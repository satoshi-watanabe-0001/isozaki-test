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
 * @since 1.0
 */
public class ErrorResponse {

    /**
     * エラーコード
     */
    private String errorCode;

    /**
     * エラーメッセージ
     */
    private String message;

    /**
     * デフォルトコンストラクタ
     */
    public ErrorResponse() {
    }

    /**
     * 全フィールドを指定するコンストラクタ
     *
     * @param errorCode エラーコード
     * @param message   エラーメッセージ
     */
    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * エラーコードを取得する
     *
     * @return エラーコード
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * エラーコードを設定する
     *
     * @param errorCode エラーコード
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * エラーメッセージを取得する
     *
     * @return エラーメッセージ
     */
    public String getMessage() {
        return message;
    }

    /**
     * エラーメッセージを設定する
     *
     * @param message エラーメッセージ
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
