/**
 * コミュニティサービスクラス
 *
 * <p>コミュニティTOPページに必要なデータの取得ロジックを担当するサービス。
 * アーティスト情報、カルーセル画像、キャンペーン、お知らせを集約して返却する。</p>
 *
 * @since 1.2
 */

package com.isozaki.auth.service;

import com.isozaki.auth.dto.ArtistImageResponse;
import com.isozaki.auth.dto.CampaignResponse;
import com.isozaki.auth.dto.CommunityTopResponse;
import com.isozaki.auth.dto.NewsResponse;
import com.isozaki.auth.entity.ArtistEntity;
import com.isozaki.auth.entity.ArtistImageEntity;
import com.isozaki.auth.entity.CampaignEntity;
import com.isozaki.auth.entity.NewsEntity;
import com.isozaki.auth.repository.ArtistImageRepository;
import com.isozaki.auth.repository.ArtistRepository;
import com.isozaki.auth.repository.CampaignRepository;
import com.isozaki.auth.repository.NewsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * コミュニティTOPページのデータ取得ロジックを提供するサービス
 *
 * <p>アーティストリポジトリ、画像リポジトリ、キャンペーンリポジトリ、
 * お知らせリポジトリからデータを取得し、コミュニティTOPレスポンスDTOに集約する。</p>
 *
 * @since 1.2
 */
@ApplicationScoped
public class CommunityService {

    private final ArtistRepository artistRepository;
    private final ArtistImageRepository artistImageRepository;
    private final CampaignRepository campaignRepository;
    private final NewsRepository newsRepository;

    /**
     * 各リポジトリを注入してサービスを初期化する
     *
     * @param artistRepository      アーティストリポジトリ
     * @param artistImageRepository アーティスト画像リポジトリ
     * @param campaignRepository    キャンペーンリポジトリ
     * @param newsRepository        お知らせリポジトリ
     */
    @Inject
    public CommunityService(
            ArtistRepository artistRepository,
            ArtistImageRepository artistImageRepository,
            CampaignRepository campaignRepository,
            NewsRepository newsRepository) {
        this.artistRepository = artistRepository;
        this.artistImageRepository = artistImageRepository;
        this.campaignRepository = campaignRepository;
        this.newsRepository = newsRepository;
    }

    /**
     * 指定アーティストのコミュニティTOPページ情報を取得する
     *
     * <p>アーティスト基本情報、カルーセル画像（最大3件）、
     * キャンペーン（最大3件）、お知らせ（最大5件、新着順）を集約して返却する。</p>
     *
     * @param artistId アーティストID
     * @return コミュニティTOPレスポンスDTO（アーティストが存在しない場合はOptional.empty）
     */
    public Optional<CommunityTopResponse> getCommunityTop(String artistId) {
        ArtistEntity artist = artistRepository.findById(artistId);
        if (artist == null) {
            return Optional.empty();
        }

        List<ArtistImageResponse> images = artistImageRepository
                .findByArtistIdOrderByDisplayOrder(artistId)
                .stream()
                .map(this::toImageResponse)
                .toList();

        List<CampaignResponse> campaigns = campaignRepository
                .findByArtistIdOrderByDisplayOrder(artistId)
                .stream()
                .map(this::toCampaignResponse)
                .toList();

        List<NewsResponse> news = newsRepository
                .findByArtistIdOrderByPublishedAtDesc(artistId)
                .stream()
                .map(this::toNewsResponse)
                .toList();

        return Optional.of(new CommunityTopResponse(
                artist.artistId,
                artist.name,
                images,
                campaigns,
                news
        ));
    }

    /**
     * アーティスト画像エンティティをレスポンスDTOに変換する
     *
     * @param entity 変換対象のアーティスト画像エンティティ
     * @return 変換後のアーティスト画像レスポンスDTO
     */
    private ArtistImageResponse toImageResponse(ArtistImageEntity entity) {
        return new ArtistImageResponse(
                entity.imageId,
                entity.imageUrl,
                entity.displayOrder
        );
    }

    /**
     * キャンペーンエンティティをレスポンスDTOに変換する
     *
     * @param entity 変換対象のキャンペーンエンティティ
     * @return 変換後のキャンペーンレスポンスDTO
     */
    private CampaignResponse toCampaignResponse(CampaignEntity entity) {
        return new CampaignResponse(
                entity.campaignId,
                entity.title,
                entity.imageUrl
        );
    }

    /**
     * お知らせエンティティをレスポンスDTOに変換する
     *
     * @param entity 変換対象のお知らせエンティティ
     * @return 変換後のお知らせレスポンスDTO
     */
    private NewsResponse toNewsResponse(NewsEntity entity) {
        return new NewsResponse(
                entity.newsId,
                entity.title,
                entity.publishedAt
        );
    }
}
