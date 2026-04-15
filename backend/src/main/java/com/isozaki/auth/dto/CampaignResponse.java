/**
 * キャンペーンレスポンスDTOクラス
 *
 * <p>キャンペーン情報を保持するDTO。</p>
 *
 * @since 1.2
 */

package com.isozaki.auth.dto;

/**
 * キャンペーンのレスポンスデータ転送オブジェクト
 *
 * @param campaignId キャンペーンID
 * @param title      キャンペーンタイトル
 * @param imageUrl   キャンペーン画像URL
 * @since 1.2
 */
public record CampaignResponse(
        Integer campaignId,
        String title,
        String imageUrl
) {
}
