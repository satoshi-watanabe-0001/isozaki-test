package com.isozaki.auth.dto;

import java.time.Instant;

/**
 * スレッドコメントレスポンスDTO
 *
 * <p>スレッド詳細画面で表示するコメント情報を保持するDTO。</p>
 *
 * @param commentId         コメントID（UUIDv7文字列）
 * @param content           コメント内容
 * @param createdByUsername コメント作成ユーザ名
 * @param createdAt         コメント作成日時
 * @since 1.3
 */
public record ThreadCommentResponse(
        String commentId,
        String content,
        String createdByUsername,
        Instant createdAt
) {
}
