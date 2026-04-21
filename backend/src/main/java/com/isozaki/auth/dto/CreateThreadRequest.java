package com.isozaki.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * スレッド作成リクエストDTO
 *
 * <p>スレッド作成時にフロントエンドから送信されるリクエスト。
 * タイトル（最大50文字）と初回コメント（最大200文字）を含む。</p>
 *
 * @param title     スレッドタイトル（必須、最大50文字）
 * @param comment   初回コメント（必須、最大200文字）
 * @param sessionId セッションID（認証用）
 * @since 1.3
 */
public record CreateThreadRequest(
        @NotBlank(message = "タイトルは必須です")
        @Size(max = 50, message = "タイトルは50文字以内で入力してください")
        String title,

        @NotBlank(message = "コメントは必須です")
        @Size(max = 200, message = "コメントは200文字以内で入力してください")
        String comment,

        @NotBlank(message = "セッションIDは必須です")
        String sessionId
) {
}
