/**
 * 画像リソースクラス
 *
 * <p>画像アップロード機能のエンドポイントを提供するJAX-RSリソース。
 * Pre-signed URL生成APIを公開する。</p>
 *
 * @since 1.4
 */

package com.isozaki.auth.resource;

import com.isozaki.auth.dto.UploadUrlRequest;
import com.isozaki.auth.dto.UploadUrlResponse;
import com.isozaki.auth.dto.UploadUrlItem;
import com.isozaki.auth.service.ImageService;
import com.isozaki.auth.service.SessionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * 画像APIのエンドポイントを提供するリソース
 *
 * <p>コミュニティスレッドのコメント画像アップロード用
 * Pre-signed URL生成を提供する。セッション認証が必要。</p>
 *
 * @since 1.4
 */
@Path("/api/v1/community/{artistId}/threads/{threadId}/upload-urls")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ImageResource {

    private final ImageService imageService;
    private final SessionService sessionService;

    /**
     * 画像サービスとセッションサービスを注入してリソースを初期化する
     *
     * @param imageService   画像サービス
     * @param sessionService セッション管理サービス
     */
    @Inject
    public ImageResource(ImageService imageService, SessionService sessionService) {
        this.imageService = imageService;
        this.sessionService = sessionService;
    }

    /**
     * Pre-signed URL生成エンドポイント
     *
     * <p>画像アップロード用のPre-signed URLを生成する。
     * 最大4ファイルまでのファイル名を受け取り、
     * それぞれのPre-signed URLと画像IDを返却する。
     * セッション認証が必要。</p>
     *
     * @param artistId アーティストID
     * @param threadId スレッドID
     * @param request  Pre-signed URL取得リクエスト
     * @return 200 OK: Pre-signed URL情報、401 Unauthorized: 認証失敗、
     *         400 Bad Request: リクエスト不正
     */
    @POST
    public Response generateUploadUrls(
            @PathParam("artistId") String artistId,
            @PathParam("threadId") String threadId,
            UploadUrlRequest request) {

        // セッション認証
        if (request.sessionId() == null || request.sessionId().isBlank()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String userId = sessionService.getUserIdBySession(request.sessionId());
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // ファイル名バリデーション
        if (request.fileNames() == null || request.fileNames().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (request.fileNames().size() > 4) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            UUID userUuid = UUID.fromString(userId);
            List<UploadUrlItem> items = imageService.generateUploadUrls(
                    request.fileNames(), userUuid);
            return Response.ok(new UploadUrlResponse(items)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
