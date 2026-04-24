package com.isozaki.auth.dto;

import java.util.List;

/**
 * Pre-signed URLレスポンスDTO
 *
 * <p>複数画像のアップロード用Pre-signed URLをまとめて返却する。</p>
 *
 * @param uploads Pre-signed URL情報のリスト
 * @since 1.4
 */
public record UploadUrlResponse(
        List<UploadUrlItem> uploads
) {
}
