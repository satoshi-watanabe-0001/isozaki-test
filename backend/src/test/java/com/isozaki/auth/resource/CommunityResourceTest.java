/**
 * CommunityResourceの単体テスト
 *
 * <p>コミュニティTOPページ取得エンドポイントのリクエスト処理とレスポンス生成をテストする。
 * CommunityServiceはモックを使用する。</p>
 *
 * @since 1.2
 */

package com.isozaki.auth.resource;

import com.isozaki.auth.dto.ArtistImageResponse;
import com.isozaki.auth.dto.CampaignResponse;
import com.isozaki.auth.dto.CommunityTopResponse;
import com.isozaki.auth.dto.NewsResponse;
import com.isozaki.auth.service.CommunityService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CommunityResourceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityResource テスト")
class CommunityResourceTest {

    @Mock
    private CommunityService communityService;

    private CommunityResource communityResource;

    @BeforeEach
    void setUp() {
        communityResource = new CommunityResource(communityService);
    }

    /**
     * 【テスト対象】CommunityResource#getCommunityTop
     * 【テストケース】アーティストが存在する場合のコミュニティTOP取得
     * 【期待結果】HTTP 200 OKとコミュニティTOP情報が返却される
     * 【ビジネス要件】コミュニティTOP API - 正常系
     */
    @Test
    @DisplayName("アーティストが存在する場合、200 OKとコミュニティTOP情報が返されること")
    void shouldReturnOkWithCommunityTop() {
        // Given: コミュニティTOP情報が存在する
        CommunityTopResponse expectedResponse = new CommunityTopResponse(
                "aimyon",
                "あいみょん",
                List.of(
                        new ArtistImageResponse(1, "/images/artists/aimyon.svg", 1),
                        new ArtistImageResponse(2, "/images/artists/aimyon.svg", 2)
                ),
                List.of(
                        new CampaignResponse(1, "ライブツアー2025", "/images/campaigns/default.svg")
                ),
                List.of(
                        new NewsResponse(1, "ニューシングルリリース決定", Instant.parse("2025-04-10T10:00:00Z"))
                )
        );
        when(communityService.getCommunityTop("aimyon")).thenReturn(Optional.of(expectedResponse));

        // When: コミュニティTOP取得APIを実行
        Response response = communityResource.getCommunityTop("aimyon");

        // Then: HTTP 200 OKと正しいコミュニティTOP情報が返却される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        CommunityTopResponse body = (CommunityTopResponse) response.getEntity();
        assertNotNull(body);
        assertEquals("aimyon", body.artistId());
        assertEquals("あいみょん", body.name());
        assertEquals(2, body.images().size());
        assertEquals(1, body.campaigns().size());
        assertEquals(1, body.news().size());
        verify(communityService).getCommunityTop("aimyon");
    }

    /**
     * 【テスト対象】CommunityResource#getCommunityTop
     * 【テストケース】アーティストが存在しない場合
     * 【期待結果】HTTP 404 Not Foundが返却される
     * 【ビジネス要件】コミュニティTOP API - アーティスト不在
     */
    @Test
    @DisplayName("アーティストが存在しない場合、404 Not Foundが返されること")
    void shouldReturnNotFoundWhenArtistNotExists() {
        // Given: アーティストが存在しない
        when(communityService.getCommunityTop("unknown")).thenReturn(Optional.empty());

        // When: 存在しないアーティストIDでコミュニティTOP取得APIを実行
        Response response = communityResource.getCommunityTop("unknown");

        // Then: HTTP 404 Not Foundが返却される
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(communityService).getCommunityTop("unknown");
    }
}
