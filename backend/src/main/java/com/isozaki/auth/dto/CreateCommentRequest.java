package com.isozaki.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * コメント追加リクエストDTO
 *
 * <p>スレッドへのコメント追加時にフロントエンドから送信されるリクエスト。
 * コメント内容（最大200文字）と任意の画像IDリスト（最大4件）を含む。</p>
 *
 * @param content   コメント内容（必須、最大200文字）
 * @param sessionId セッションID（認証用）
 * @param imageIds  画像IDリスト（任意、最大4件、Pre-signed URL取得時に発行されたID）
 * @since 1.3
 */
public record CreateCommentRequest(
        @NotBlank(message = "コメントは必須です")
        @Size(max = 200, message = "コメントは200文字以内で入力してください")
        String content,

        @NotBlank(message = "セッションIDは必須です")
        String sessionId,

        List<String> imageIds
) {
}
