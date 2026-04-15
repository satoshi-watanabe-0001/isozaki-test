/**
 * コミュニティTOPレスポンスDTOクラス
 *
 * <p>コミュニティTOPページAPIのレスポンスとして使用するDTO。
 * アーティスト情報、カルーセル画像、キャンペーン、お知らせを含む。</p>
 *
 * @since 1.2
 */

package com.isozaki.auth.dto;

import java.util.List;

/**
 * コミュニティTOPページのレスポンスデータ転送オブジェクト
 *
 * @param artistId   アーティストID（英名文字列）
 * @param name       アーティスト名
 * @param images     カルーセル表示用画像リスト（最大3件）
 * @param campaigns  キャンペーンリスト（最大3件）
 * @param news       お知らせリスト（最大5件、新着順）
 * @since 1.2
 */
public record CommunityTopResponse(
        String artistId,
        String name,
        List<ArtistImageResponse> images,
        List<CampaignResponse> campaigns,
        List<NewsResponse> news
) {
}
