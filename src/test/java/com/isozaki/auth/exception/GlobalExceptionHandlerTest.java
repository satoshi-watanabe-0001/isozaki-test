/**
 * GlobalExceptionHandlerの単体テスト
 *
 * <p>例外ハンドラのHTTPレスポンス変換ロジックをテストする。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.exception;

import com.isozaki.auth.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * GlobalExceptionHandlerのテストクラス
 */
@DisplayName("GlobalExceptionHandler テスト")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("handleAuthenticationException テスト")
    class HandleAuthenticationExceptionTests {

        @Test
        @DisplayName("認証例外が401レスポンスに変換されること")
        void shouldReturn401ForAuthenticationException() {
            // Given
            AuthenticationException exception = new AuthenticationException("認証失敗");

            // When
            Response response = handler.handleAuthenticationException(exception);

            // Then
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
            ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
            assertNotNull(errorResponse);
            assertEquals("AUTHENTICATION_FAILED", errorResponse.errorCode());
            assertEquals("認証失敗", errorResponse.message());
        }
    }

    @Nested
    @DisplayName("handleConstraintViolation テスト")
    class HandleConstraintViolationTests {

        @Test
        @DisplayName("バリデーション例外が400レスポンスに変換されること")
        void shouldReturn400ForConstraintViolation() {
            // Given
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("メールアドレスは必須です");
            violations.add(violation);
            ConstraintViolationException exception = new ConstraintViolationException(violations);

            // When
            Response response = handler.handleConstraintViolation(exception);

            // Then
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
            assertNotNull(errorResponse);
            assertEquals("VALIDATION_ERROR", errorResponse.errorCode());
        }

        @Test
        @DisplayName("複数のバリデーションエラーがセミコロンで結合されること")
        void shouldConcatenateMultipleViolations() {
            // Given
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
            when(violation1.getMessage()).thenReturn("メールアドレスは必須です");
            ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
            when(violation2.getMessage()).thenReturn("パスワードは必須です");
            violations.add(violation1);
            violations.add(violation2);
            ConstraintViolationException exception = new ConstraintViolationException(violations);

            // When
            Response response = handler.handleConstraintViolation(exception);

            // Then
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
            assertNotNull(errorResponse);
            // メッセージに両方のエラーが含まれていることを確認
            String message = errorResponse.message();
            assertNotNull(message);
            assertTrue(message.contains("メールアドレスは必須です"),
                    "メッセージに'メールアドレスは必須です'が含まれていること");
            assertTrue(message.contains("パスワードは必須です"),
                    "メッセージに'パスワードは必須です'が含まれていること");
            assertTrue(message.contains("; "),
                    "メッセージがセミコロンで結合されていること");
        }

        @Test
        @DisplayName("空のバリデーション違反セットでデフォルトメッセージが設定されること")
        void shouldUseDefaultMessageForEmptyViolations() {
            // Given
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            ConstraintViolationException exception = new ConstraintViolationException(violations);

            // When
            Response response = handler.handleConstraintViolation(exception);

            // Then
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
            assertNotNull(errorResponse);
            assertEquals("バリデーションエラー", errorResponse.message());
        }
    }
}
