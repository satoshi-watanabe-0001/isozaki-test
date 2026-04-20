/**
 * スレッドリソースクラス
 *
 * <p>スレッド機能のエンドポイントを提供するJAX-RSリソース。
 * スレッド一覧・詳細取得、スレッド作成、コメント追加のAPIを公開する。</p>
 *
 * @since 1.3
 */

package com.isozaki.auth.resource;

import com.isozaki.auth.dto.CreateCommentRequest;
import com.isozaki.auth.dto.CreateThreadRequest;
import com.isozaki.auth.dto.ThreadCommentResponse;
import com.isozaki.auth.dto.ThreadDetailResponse;
import com.isozaki.auth.dto.ThreadListResponse;
import com.isozaki.auth.service.ThreadService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;

/**
 * スレッドAPIのエンドポイントを提供するリソース
 *
 * <p>コミュニティに紐づくスレッドのCRUD操作を提供する。
 * GET操作は認証不要、POST操作はセッション認証が必要。</p>
 *
 * @since 1.3
 */
@Path("/api/v1/community/{artistId}/threads")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ThreadResource {

    private final ThreadService threadService;

    /**
     * スレッドサービスを注入してリソースを初期化する
     *
     * @param threadService スレッドサービス
     */
    @Inject
    public ThreadResource(ThreadService threadService) {
        this.threadService = threadService;
    }

    /**
     * スレッド一覧取得エンドポイント
     *
     * <p>指定アーティストのスレッド一覧をページング付きで返却する。
     * 最新コメント日時の降順でソートされる。認証不要。
     * sizeパラメータはサーバ側で上限100に制限される。</p>
     *
     * @param artistId アーティストID
     * @param page     ページ番号（1始まり、デフォルト1）
     * @param size     1ページあたりの件数（デフォルト20、上限100）
     * @return 200 OK: スレッド一覧、404 Not Found: アーティスト不在
     */
    @GET
    public Response getThreadList(
            @PathParam("artistId") String artistId,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        Optional<ThreadListResponse> result = threadService.getThreadList(artistId, page, size);

        if (result.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(result.get()).build();
    }

    /**
     * スレッド詳細取得エンドポイント
     *
     * <p>指定スレッドの詳細情報とコメント一覧をページング付きで返却する。
     * コメントは作成日時の降順で表示される。認証不要。
     * sizeパラメータはサーバ側で上限100に制限される。
     * beforeパラメータ（commentId）指定時はカーソルベースページングで
     * 重複取得を回避する。</p>
     *
     * @param artistId アーティストID
     * @param threadId スレッドID（UUIDv7文字列）
     * @param page     コメントページ番号（1始まり、デフォルト1、before未指定時に使用）
     * @param size     1ページあたりのコメント件数（デフォルト10、上限100）
     * @param before   カーソル用commentId（このIDより前のコメントを取得、省略時はオフセットベース）
     * @return 200 OK: スレッド詳細、404 Not Found: スレッド不在、
     *         400 Bad Request: 不正なUUID形式
     */
    @GET
    @Path("/{threadId}")
    public Response getThreadDetail(
            @PathParam("artistId") String artistId,
            @PathParam("threadId") String threadId,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("before") String before) {

        UUID threadUuid;
        try {
            threadUuid = UUID.fromString(threadId);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<ThreadDetailResponse> result;

        if (before != null && !before.isEmpty()) {
            // カーソルベースページング（commentId基準）
            UUID beforeCommentId;
            try {
                beforeCommentId = UUID.fromString(before);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            result = threadService.getThreadDetailBefore(
                    artistId, threadUuid, beforeCommentId, size);
        } else {
            // オフセットベースページング（初回取得時）
            result = threadService.getThreadDetail(artistId, threadUuid, page, size);
        }

        if (result.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(result.get()).build();
    }

    /**
     * スレッド作成エンドポイント
     *
     * <p>新しいスレッドを作成する。セッション認証が必要。
     * タイトル（最大50文字）と初回コメント（最大200文字）を受け取る。</p>
     *
     * @param artistId アーティストID
     * @param request  スレッド作成リクエスト
     * @return 201 Created: 作成成功、401 Unauthorized: 認証失敗、
     *         404 Not Found: アーティスト不在
     */
    @POST
    public Response createThread(
            @PathParam("artistId") String artistId,
            @Valid CreateThreadRequest request) {

        Optional<ThreadDetailResponse> result =
                threadService.createThread(artistId, request);

        if (result.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(result.get()).build();
    }

    /**
     * コメント追加エンドポイント
     *
     * <p>指定スレッドにコメントを追加する。セッション認証が必要。
     * コメント内容（最大200文字）を受け取る。</p>
     *
     * @param artistId アーティストID
     * @param threadId スレッドID（UUIDv7文字列）
     * @param request  コメント追加リクエスト
     * @return 201 Created: 追加成功、401 Unauthorized: 認証失敗、
     *         404 Not Found: スレッド不在、400 Bad Request: 不正なUUID形式
     */
    @POST
    @Path("/{threadId}/comments")
    public Response addComment(
            @PathParam("artistId") String artistId,
            @PathParam("threadId") String threadId,
            @Valid CreateCommentRequest request) {

        UUID threadUuid;
        try {
            threadUuid = UUID.fromString(threadId);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<ThreadCommentResponse> result =
                threadService.addComment(artistId, threadUuid, request);

        if (result.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(result.get()).build();
    }
}
