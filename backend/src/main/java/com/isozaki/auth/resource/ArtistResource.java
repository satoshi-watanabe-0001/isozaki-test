/**
 * アーティストリソースクラス
 *
 * <p>アーティスト一覧エンドポイントを提供するJAX-RSリソース。
 * 50音順にソートされたアーティスト一覧をJSON形式で返却する。</p>
 *
 * @since 1.1
 */

package com.isozaki.auth.resource;

import com.isozaki.auth.dto.ArtistResponse;
import com.isozaki.auth.service.ArtistService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * アーティストAPIのエンドポイントを提供するリソース
 *
 * <p>GET /api/v1/artists で50音順にソートされたアーティスト一覧を返却する。</p>
 *
 * @since 1.1
 */
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtistResource {

    private final ArtistService artistService;

    /**
     * アーティストサービスを注入してリソースを初期化する
     *
     * @param artistService アーティストサービス
     */
    @Inject
    public ArtistResource(ArtistService artistService) {
        this.artistService = artistService;
    }

    /**
     * アーティスト一覧取得エンドポイント
     *
     * <p>50音順にソートされた全アーティストの一覧を返却する。
     * 各アーティストにはID、名前、読み仮名、アイコンURLが含まれる。</p>
     *
     * @return 200 OK: アーティスト一覧（JSON配列）
     */
    @GET
    @Path("/artists")
    public Response getArtists() {
        List<ArtistResponse> artists = artistService.getAllArtists();
        return Response.ok(artists).build();
    }
}
