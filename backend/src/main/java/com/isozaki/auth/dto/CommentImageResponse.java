package com.isozaki.auth.dto;

/**
 * コメント画像レスポンスDTO
 *
 * <p>コメントに紐づく画像情報をフロントエンドに返却するDTO。
 * サムネイルURLと表示用URLを含む。</p>
 *
 * @param imageId      画像ID（UUIDv7文字列）
 * @param thumbnailUrl サムネイル画像URL（400px幅WebP）
 * @param displayUrl   表示用画像URL（1200px幅WebP）
 * @since 1.4
 */
public record CommentImageResponse(
        String imageId,
        String thumbnailUrl,
        String displayUrl
) {
}
