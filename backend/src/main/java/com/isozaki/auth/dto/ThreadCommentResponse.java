package com.isozaki.auth.dto;

import java.time.Instant;
import java.util.List;

/**
 * スレッドコメントレスポンスDTO
 *
 * <p>スレッド詳細画面で表示するコメント情報を保持するDTO。
 * コメントに紐づく画像情報も含む。</p>
 *
 * @param commentId         コメントID（UUIDv7文字列）
 * @param content           コメント内容
 * @param createdByUsername コメント作成ユーザ名
 * @param createdAt         コメント作成日時
 * @param images            コメントに紐づく画像リスト（存在しない場合は空リスト）
 * @since 1.3
 */
public record ThreadCommentResponse(
        String commentId,
        String content,
        String createdByUsername,
        Instant createdAt,
        List<CommentImageResponse> images
) {
}
