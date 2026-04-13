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
import java.util.List;
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
        ErrorResponse error = ErrorResponse.of("AUTHENTICATION_FAILED", exception.getMessage());
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
        List<String> details = exception.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .toList();

        String message = details.isEmpty()
                ? "バリデーションエラー"
                : String.join("; ", details);

        ErrorResponse error = ErrorResponse.of("VALIDATION_ERROR", message, details);
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }
}
