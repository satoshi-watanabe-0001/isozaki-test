/**
 * グローバル例外ハンドラ
 *
 * <p>アプリケーション全体の例外をキャッチし、適切なHTTPレスポンスに変換する。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.exception;

import com.isozaki.auth.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 * REST APIの例外をHTTPレスポンスにマッピングするハンドラ
 *
 * @since 1.0
 */
public class GlobalExceptionHandler {

    /**
     * 認証例外をHTTP 401レスポンスに変換する
     *
     * @param exception 認証例外
     * @return 401 Unauthorizedレスポンス
     */
    @ServerExceptionMapper
    public Response handleAuthenticationException(AuthenticationException exception) {
        ErrorResponse error = new ErrorResponse("AUTHENTICATION_FAILED", exception.getMessage());
        return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
    }

    /**
     * バリデーション例外をHTTP 400レスポンスに変換する
     *
     * @param exception バリデーション例外
     * @return 400 Bad Requestレスポンス
     */
    @ServerExceptionMapper
    public Response handleConstraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("バリデーションエラー");
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", message);
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }
}
