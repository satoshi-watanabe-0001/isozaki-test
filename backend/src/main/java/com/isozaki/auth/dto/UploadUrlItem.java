package com.isozaki.auth.dto;

/**
 * Pre-signed URLレスポンスの個別画像情報DTO
 *
 * <p>各画像ごとのアップロード用Pre-signed URLと画像IDを返却する。</p>
 *
 * @param imageId    画像ID（UUIDv7文字列）
 * @param uploadUrl  Pre-signed URL（PUT用）
 * @param s3Key      S3オブジェクトキー
 * @since 1.4
 */
public record UploadUrlItem(
        String imageId,
        String uploadUrl,
        String s3Key
) {
}
