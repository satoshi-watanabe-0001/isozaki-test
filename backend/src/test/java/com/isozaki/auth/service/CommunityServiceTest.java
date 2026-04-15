/**
 * CommunityServiceの単体テスト
 *
 * <p>コミュニティTOPページのデータ取得ロジックをテストする。
 * 各リポジトリはモックを使用する。</p>
 *
 * @since 1.2
 */

package com.isozaki.auth.service;

import com.isozaki.auth.dto.CommunityTopResponse;
import com.isozaki.auth.entity.ArtistEntity;
import com.isozaki.auth.entity.ArtistImageEntity;
import com.isozaki.auth.entity.CampaignEntity;
import com.isozaki.auth.entity.NewsEntity;
import com.isozaki.auth.repository.ArtistImageRepository;
import com.isozaki.auth.repository.ArtistRepository;
import com.isozaki.auth.repository.CampaignRepository;
import com.isozaki.auth.repository.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CommunityServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityService テスト")
class CommunityServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ArtistImageRepository artistImageRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private NewsRepository newsRepository;

    private CommunityService communityService;

    @BeforeEach
    void setUp() {
        communityService = new CommunityService(
                artistRepository, artistImageRepository, campaignRepository, newsRepository);
    }

    /**
     * テスト用のArtistEntityを生成するヘルパーメソッド
     */
    private ArtistEntity createArtistEntity(String id, String name) {
        ArtistEntity entity = new ArtistEntity();
        entity.artistId = id;
        entity.name = name;
        entity.nameKana = name;
        entity.iconUrl = "/images/artists/" + id + ".svg";
        entity.createdAt = Instant.now();
        entity.updatedAt = Instant.now();
        return entity;
    }

    /**
     * テスト用のArtistImageEntityを生成するヘルパーメソッド
     */
    private ArtistImageEntity createImageEntity(Integer id, String artistId, String imageUrl, Integer order) {
        ArtistImageEntity entity = new ArtistImageEntity();
        entity.imageId = id;
        entity.artistId = artistId;
        entity.imageUrl = imageUrl;
        entity.displayOrder = order;
        entity.createdAt = Instant.now();
        return entity;
    }

    /**
     * テスト用のCampaignEntityを生成するヘルパーメソッド
     */
    private CampaignEntity createCampaignEntity(Integer id, String artistId, String title, Integer order) {
        CampaignEntity entity = new CampaignEntity();
        entity.campaignId = id;
        entity.artistId = artistId;
        entity.title = title;
        entity.imageUrl = "/images/campaigns/default.svg";
        entity.displayOrder = order;
        entity.createdAt = Instant.now();
        return entity;
    }

    /**
     * テスト用のNewsEntityを生成するヘルパーメソッド
     */
    private NewsEntity createNewsEntity(Integer id, String artistId, String title, Instant publishedAt) {
        NewsEntity entity = new NewsEntity();
        entity.newsId = id;
        entity.artistId = artistId;
        entity.title = title;
        entity.publishedAt = publishedAt;
        entity.createdAt = Instant.now();
        return entity;
    }

    /**
     * 【テスト対象】CommunityService#getCommunityTop
     * 【テストケース】アーティストが存在し、全データがある場合
     * 【期待結果】画像・キャンペーン・お知らせを含むコミュニティTOP情報が返却される
     * 【ビジネス要件】コミュニティTOP取得 - 正常系
     */
    @Test
    @DisplayName("全データがある場合、コミュニティTOP情報が正しく返されること")
    void shouldReturnCommunityTopWithAllData() {
        // Given: アーティスト・画像・キャンペーン・お知らせが存在する
        String artistId = "aimyon";
        ArtistEntity artist = createArtistEntity(artistId, "あいみょん");
        when(artistRepository.findById(artistId)).thenReturn(artist);

        List<ArtistImageEntity> images = List.of(
                createImageEntity(1, artistId, "/images/artists/aimyon.svg", 1),
                createImageEntity(2, artistId, "/images/artists/aimyon.svg", 2),
                createImageEntity(3, artistId, "/images/artists/aimyon.svg", 3)
        );
        when(artistImageRepository.findByArtistIdOrderByDisplayOrder(artistId)).thenReturn(images);

        List<CampaignEntity> campaigns = List.of(
                createCampaignEntity(1, artistId, "ライブツアー2025", 1),
                createCampaignEntity(2, artistId, "ニューアルバム発売記念", 2)
        );
        when(campaignRepository.findByArtistIdOrderByDisplayOrder(artistId)).thenReturn(campaigns);

        List<NewsEntity> news = List.of(
                createNewsEntity(1, artistId, "ニューシングルリリース決定", Instant.parse("2025-04-10T10:00:00Z")),
                createNewsEntity(2, artistId, "全国ツアー追加公演決定", Instant.parse("2025-04-08T12:00:00Z"))
        );
        when(newsRepository.findByArtistIdOrderByPublishedAtDesc(artistId)).thenReturn(news);

        // When: コミュニティTOP取得を実行
        Optional<CommunityTopResponse> result = communityService.getCommunityTop(artistId);

        // Then: 全データが正しく集約されたレスポンスが返却される
        assertTrue(result.isPresent());
        CommunityTopResponse response = result.get();
        assertEquals("aimyon", response.artistId());
        assertEquals("あいみょん", response.name());
        assertEquals(3, response.images().size());
        assertEquals(2, response.campaigns().size());
        assertEquals(2, response.news().size());
        assertEquals("ライブツアー2025", response.campaigns().get(0).title());
        assertEquals("ニューシングルリリース決定", response.news().get(0).title());
        verify(artistRepository).findById(artistId);
        verify(artistImageRepository).findByArtistIdOrderByDisplayOrder(artistId);
        verify(campaignRepository).findByArtistIdOrderByDisplayOrder(artistId);
        verify(newsRepository).findByArtistIdOrderByPublishedAtDesc(artistId);
    }

    /**
     * 【テスト対象】CommunityService#getCommunityTop
     * 【テストケース】アーティストが存在しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】コミュニティTOP取得 - アーティスト不在
     */
    @Test
    @DisplayName("アーティストが存在しない場合、emptyが返されること")
    void shouldReturnEmptyWhenArtistNotFound() {
        // Given: アーティストが存在しない
        when(artistRepository.findById("unknown")).thenReturn(null);

        // When: 存在しないアーティストIDでコミュニティTOP取得を実行
        Optional<CommunityTopResponse> result = communityService.getCommunityTop("unknown");

        // Then: Optional.emptyが返却される
        assertFalse(result.isPresent());
        verify(artistRepository).findById("unknown");
    }

    /**
     * 【テスト対象】CommunityService#getCommunityTop
     * 【テストケース】画像・キャンペーン・お知らせがない場合
     * 【期待結果】空のリストを含むコミュニティTOP情報が返却される
     * 【ビジネス要件】コミュニティTOP取得 - 関連データなし
     */
    @Test
    @DisplayName("関連データがない場合、空リストを含むレスポンスが返されること")
    void shouldReturnCommunityTopWithEmptyRelatedData() {
        // Given: アーティストは存在するが、関連データがない
        String artistId = "glay";
        ArtistEntity artist = createArtistEntity(artistId, "GLAY");
        when(artistRepository.findById(artistId)).thenReturn(artist);
        when(artistImageRepository.findByArtistIdOrderByDisplayOrder(artistId)).thenReturn(Collections.emptyList());
        when(campaignRepository.findByArtistIdOrderByDisplayOrder(artistId)).thenReturn(Collections.emptyList());
        when(newsRepository.findByArtistIdOrderByPublishedAtDesc(artistId)).thenReturn(Collections.emptyList());

        // When: コミュニティTOP取得を実行
        Optional<CommunityTopResponse> result = communityService.getCommunityTop(artistId);

        // Then: 空リストを含むレスポンスが返却される
        assertTrue(result.isPresent());
        CommunityTopResponse response = result.get();
        assertEquals("glay", response.artistId());
        assertEquals("GLAY", response.name());
        assertNotNull(response.images());
        assertEquals(0, response.images().size());
        assertNotNull(response.campaigns());
        assertEquals(0, response.campaigns().size());
        assertNotNull(response.news());
        assertEquals(0, response.news().size());
    }
}
