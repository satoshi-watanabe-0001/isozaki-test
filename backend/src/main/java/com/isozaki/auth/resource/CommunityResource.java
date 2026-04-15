/**
 * コミュニティリソースクラス
 *
 * <p>コミュニティTOPページのエンドポイントを提供するJAX-RSリソース。
 * アーティストIDを指定してコミュニティTOPページ情報をJSON形式で返却する。</p>
 *
 * @since 1.2
 */

package com.isozaki.auth.resource;

import com.isozaki.auth.dto.CommunityTopResponse;
import com.isozaki.auth.service.CommunityService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;

/**
 * コミュニティAPIのエンドポイントを提供するリソース
 *
 * <p>GET /api/v1/community/{artistId} でコミュニティTOPページ情報を返却する。</p>
 *
 * @since 1.2
 */
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommunityResource {

    private final CommunityService communityService;

    /**
     * コミュニティサービスを注入してリソースを初期化する
     *
     * @param communityService コミュニティサービス
     */
    @Inject
    public CommunityResource(CommunityService communityService) {
        this.communityService = communityService;
    }

    /**
     * コミュニティTOPページ情報取得エンドポイント
     *
     * <p>指定されたアーティストIDに対応するコミュニティTOPページ情報を返却する。
     * アーティストが存在しない場合は404エラーを返却する。</p>
     *
     * @param artistId アーティストID（URLパスパラメータ）
     * @return 200 OK: コミュニティTOPページ情報（JSON）
     *         404 Not Found: アーティストが存在しない場合
     */
    @GET
    @Path("/community/{artistId}")
    public Response getCommunityTop(@PathParam("artistId") String artistId) {
        Optional<CommunityTopResponse> communityTop = communityService.getCommunityTop(artistId);

        if (communityTop.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":{\"code\":\"ARTIST_NOT_FOUND\",\"message\":\"アーティストが見つかりません\"}}")
                    .build();
        }

        return Response.ok(communityTop.get()).build();
    }
}
