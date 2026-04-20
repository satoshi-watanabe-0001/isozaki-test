package com.isozaki.auth.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * スレッド詳細クエリ結果のプロジェクションDTO
 *
 * <p>JPQL SELECT NEWで使用するDTO。
 * ThreadRepository#findByIdAndArtistIdWithUsernameの戻り値として使用する。</p>
 *
 * @param threadId  スレッドID
 * @param title     スレッドタイトル
 * @param username  スレッド作成ユーザ名
 * @param createdAt スレッド作成日時
 * @since 1.3
 */
public record ThreadDetailProjection(
        UUID threadId,
        String title,
        String username,
        Instant createdAt
) {
}
