/**
 * セッションリソースクラス
 *
 * <p>セッションの検証・削除エンドポイントを提供するJAX-RSリソース。
 * フロントエンドからのセッション有効性確認およびログアウト処理に使用する。</p>
 *
 * @since 1.1
 */

package com.isozaki.auth.resource;

import com.isozaki.auth.service.SessionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * セッション管理APIのエンドポイントを提供するリソース
 *
 * <p>GET /api/v1/session/{sessionId} でセッションの有効性を確認し、
 * DELETE /api/v1/session/{sessionId} でセッションを削除する。</p>
 *
 * @since 1.1
 */
@Path("/api/v1/session")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionResource {

    private final SessionService sessionService;

    /**
     * セッションサービスを注入してリソースを初期化する
     *
     * @param sessionService セッション管理サービス
     */
    @Inject
    public SessionResource(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * セッション検証エンドポイント
     *
     * <p>指定されたセッションIDがRedisに存在し、有効であるかを確認する。
     * 有効な場合はユーザIDを含むレスポンスを返し、無効な場合は404を返す。</p>
     *
     * @param sessionId 検証対象のセッションID
     * @return 200 OK: セッション有効（ユーザID含む）、404 Not Found: セッション無効
     */
    @GET
    @Path("/{sessionId}")
    public Response validateSession(@PathParam("sessionId") String sessionId) {
        String userId = sessionService.getUserIdBySession(sessionId);
        if (userId == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(new SessionValidationResponse(sessionId, userId)).build();
    }

    /**
     * セッション削除エンドポイント（ログアウト）
     *
     * <p>指定されたセッションIDをRedisから削除する。
     * ログアウト時にフロントエンドから呼び出される。</p>
     *
     * @param sessionId 削除対象のセッションID
     * @return 204 No Content: 削除成功
     */
    @DELETE
    @Path("/{sessionId}")
    public Response deleteSession(@PathParam("sessionId") String sessionId) {
        sessionService.invalidateSession(sessionId);
        return Response.noContent().build();
    }

    /**
     * セッション検証レスポンスのDTO
     *
     * @param sessionId セッションID
     * @param userId    ユーザID
     */
    public record SessionValidationResponse(
            String sessionId,
            String userId
    ) {
    }
}
