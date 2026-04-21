package com.isozaki.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * コメント追加リクエストDTO
 *
 * <p>スレッドへのコメント追加時にフロントエンドから送信されるリクエスト。
 * コメント内容（最大200文字）を含む。</p>
 *
 * @param content   コメント内容（必須、最大200文字）
 * @param sessionId セッションID（認証用）
 * @since 1.3
 */
public record CreateCommentRequest(
        @NotBlank(message = "コメントは必須です")
        @Size(max = 200, message = "コメントは200文字以内で入力してください")
        String content,

        @NotBlank(message = "セッションIDは必須です")
        String sessionId
) {
}
