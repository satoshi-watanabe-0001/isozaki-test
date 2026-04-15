/**
 * ArtistResourceの単体テスト
 *
 * <p>アーティスト一覧取得エンドポイントのリクエスト処理とレスポンス生成をテストする。
 * ArtistServiceはモックを使用する。</p>
 *
 * @since 1.1
 */

package com.isozaki.auth.resource;

import com.isozaki.auth.dto.ArtistResponse;
import com.isozaki.auth.service.ArtistService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ArtistResourceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistResource テスト")
class ArtistResourceTest {

    @Mock
    private ArtistService artistService;

    private ArtistResource artistResource;

    @BeforeEach
    void setUp() {
        artistResource = new ArtistResource(artistService);
    }

    /**
     * 【テスト対象】ArtistResource#getArtists
     * 【テストケース】アーティストが存在する場合の一覧取得
     * 【期待結果】HTTP 200 OKとアーティスト一覧が返却される
     * 【ビジネス要件】アーティスト一覧API - 正常系
     */
    @Test
    @DisplayName("アーティストが存在する場合、200 OKと一覧が返されること")
    @SuppressWarnings("unchecked")
    void shouldReturnOkWithArtistList() {
        // Given: 50音順にソートされたアーティスト一覧
        List<ArtistResponse> expectedArtists = List.of(
                new ArtistResponse("aimyon", "あいみょん", "あいみょん", "/images/artists/aimyon.svg"),
                new ArtistResponse("arashi", "嵐", "あらし", "/images/artists/arashi.svg"),
                new ArtistResponse("ikimonogakari", "いきものがかり", "いきものがかり", "/images/artists/ikimonogakari.svg")
        );
        when(artistService.getAllArtists()).thenReturn(expectedArtists);

        // When: アーティスト一覧取得APIを実行
        Response response = artistResource.getArtists();

        // Then: HTTP 200 OKと正しいアーティスト一覧が返却される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<ArtistResponse> body = (List<ArtistResponse>) response.getEntity();
        assertNotNull(body);
        assertEquals(3, body.size());
        assertEquals("あいみょん", body.get(0).name());
        assertEquals("嵐", body.get(1).name());
        assertEquals("いきものがかり", body.get(2).name());
        verify(artistService).getAllArtists();
    }

    /**
     * 【テスト対象】ArtistResource#getArtists
     * 【テストケース】アーティストが存在しない場合の一覧取得
     * 【期待結果】HTTP 200 OKと空のリストが返却される
     * 【ビジネス要件】アーティスト一覧API - データなし
     */
    @Test
    @DisplayName("アーティストが存在しない場合、200 OKと空のリストが返されること")
    @SuppressWarnings("unchecked")
    void shouldReturnOkWithEmptyList() {
        // Given: アーティストが0件
        when(artistService.getAllArtists()).thenReturn(Collections.emptyList());

        // When: アーティスト一覧取得APIを実行
        Response response = artistResource.getArtists();

        // Then: HTTP 200 OKと空のリストが返却される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<ArtistResponse> body = (List<ArtistResponse>) response.getEntity();
        assertNotNull(body);
        assertEquals(0, body.size());
        verify(artistService).getAllArtists();
    }
}
