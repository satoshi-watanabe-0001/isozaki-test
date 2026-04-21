package com.isozaki.auth.dto;

import java.time.Instant;
import java.util.List;

/**
 * スレッド詳細レスポンスDTO
 *
 * <p>スレッド詳細画面で表示するスレッド情報とコメント一覧を保持するDTO。</p>
 *
 * @param threadId          スレッドID（UUIDv7文字列）
 * @param title             スレッドタイトル
 * @param createdByUsername スレッド作成ユーザ名
 * @param createdAt         スレッド作成日時
 * @param comments          コメント一覧
 * @param totalComments     総コメント数
 * @param page              現在のページ番号（1始まり）
 * @param size              1ページあたりの件数
 * @param totalPages        総ページ数
 * @since 1.3
 */
public record ThreadDetailResponse(
        String threadId,
        String title,
        String createdByUsername,
        Instant createdAt,
        List<ThreadCommentResponse> comments,
        long totalComments,
        int page,
        int size,
        int totalPages
) {
}
