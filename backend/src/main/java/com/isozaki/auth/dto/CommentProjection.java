package com.isozaki.auth.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * コメントクエリ結果のプロジェクションDTO
 *
 * <p>JPQL SELECT NEWで使用するDTO。
 * ThreadCommentRepository#findByThreadIdWithUsernameの戻り値として使用する。</p>
 *
 * @param commentId コメントID
 * @param content   コメント内容
 * @param username  コメント作成ユーザ名
 * @param createdAt コメント作成日時
 * @since 1.3
 */
public record CommentProjection(
        UUID commentId,
        String content,
        String username,
        Instant createdAt
) {
}
