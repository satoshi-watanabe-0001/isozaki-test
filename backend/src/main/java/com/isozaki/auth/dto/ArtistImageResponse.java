/**
 * アーティスト画像レスポンスDTOクラス
 *
 * <p>カルーセル表示用の画像URLと表示順を保持するDTO。</p>
 *
 * @since 1.2
 */

package com.isozaki.auth.dto;

/**
 * アーティスト画像のレスポンスデータ転送オブジェクト
 *
 * @param imageId      画像ID
 * @param imageUrl     画像URL
 * @param displayOrder 表示順
 * @since 1.2
 */
public record ArtistImageResponse(
        Integer imageId,
        String imageUrl,
        Integer displayOrder
) {
}
