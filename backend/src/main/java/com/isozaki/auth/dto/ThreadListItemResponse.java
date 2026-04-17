package com.isozaki.auth.dto;

import java.time.Instant;

/**
 * スレッド一覧レスポンスDTO
 *
 * <p>スレッド一覧画面で表示するスレッド情報を保持するDTO。
 * スレッドタイトル、作成ユーザ名、最新コメント情報を含む。</p>
 *
 * @param threadId          スレッドID（UUIDv7文字列）
 * @param title             スレッドタイトル
 * @param createdByUsername スレッド作成ユーザ名
 * @param latestComment     最新コメント内容（存在しない場合はnull）
 * @param latestCommentAt   最新コメント日時（存在しない場合はnull）
 * @since 1.3
 */
public record ThreadListItemResponse(
        String threadId,
        String title,
        String createdByUsername,
        String latestComment,
        Instant latestCommentAt
) {
}
