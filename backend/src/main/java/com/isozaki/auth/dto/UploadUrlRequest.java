package com.isozaki.auth.dto;

import java.util.List;

/**
 * Pre-signed URL取得リクエストDTO
 *
 * <p>画像アップロード用のPre-signed URL生成を要求するリクエスト。
 * ファイル名リストとセッションIDを含む。</p>
 *
 * @param fileNames  アップロード対象のファイル名リスト（最大4件）
 * @param sessionId  セッションID（認証用）
 * @since 1.4
 */
public record UploadUrlRequest(
        List<String> fileNames,
        String sessionId
) {
}
