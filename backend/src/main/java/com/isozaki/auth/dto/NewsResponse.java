/**
 * お知らせレスポンスDTOクラス
 *
 * <p>お知らせ情報のタイトルと公開日時を保持するDTO。</p>
 *
 * @since 1.2
 */

package com.isozaki.auth.dto;

import java.time.Instant;

/**
 * お知らせのレスポンスデータ転送オブジェクト
 *
 * @param newsId      お知らせID
 * @param title       お知らせタイトル
 * @param publishedAt 公開日時
 * @since 1.2
 */
public record NewsResponse(
        Integer newsId,
        String title,
        Instant publishedAt
) {
}
