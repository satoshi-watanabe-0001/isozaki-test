package com.isozaki.auth.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * スレッド一覧クエリ結果のプロジェクションDTO
 *
 * <p>JPQL SELECT NEWで使用するDTO。
 * ThreadRepository#findByArtistIdWithUsernameの戻り値として使用する。</p>
 *
 * @param threadId             スレッドID
 * @param title                スレッドタイトル
 * @param username             スレッド作成ユーザ名
 * @param latestCommentContent 最新コメント内容（null可）
 * @param latestCommentAt      最新コメント日時（null可）
 * @param createdAt            スレッド作成日時
 * @since 1.3
 */
public record ThreadListProjection(
        UUID threadId,
        String title,
        String username,
        String latestCommentContent,
        Instant latestCommentAt,
        Instant createdAt
) {
}
