/**
 * ログインリソースクラス
 *
 * <p>ログインエンドポイントを提供するJAX-RSリソース。
 * JSON形式のリクエストを受け取り、認証処理を実行する。</p>
 *
 * @since 1.0
 */

package com.isozaki.auth.resource;

import com.isozaki.auth.dto.LoginRequest;
import com.isozaki.auth.dto.LoginResponse;
import com.isozaki.auth.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * ログインAPIのエンドポイントを提供するリソース
 *
 * <p>POST /api/v1/login でJSON形式のメールアドレスとパスワードを受け取り、
 * DBのユーザデータと突合してログイン処理を行う。</p>
 *
 * @since 1.0
 */
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoginResource {

    private final AuthService authService;

    /**
     * 認証サービスを注入してリソースを初期化する
     *
     * @param authService 認証サービス
     */
    @Inject
    public LoginResource(AuthService authService) {
        this.authService = authService;
    }

    /**
     * ログインエンドポイント
     *
     * <p>JSON形式でメールアドレスとパスワードを受け取り、
     * DBのユーザデータと突合した上でログインを行う。
     * ログイン成功時はRedisにセッションを保存し、セッション情報を返却する。</p>
     *
     * @param request ログインリクエスト（メールアドレス・パスワード）
     * @return 200 OK: ログイン成功（セッションID・ユーザ情報）、401 Unauthorized: 認証失敗
     */
    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Response.ok(response).build();
    }
}
