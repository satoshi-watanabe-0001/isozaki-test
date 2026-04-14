/**
 * SessionResourceの単体テスト
 *
 * <p>セッション検証・削除エンドポイントのリクエスト処理とレスポンス生成をテストする。
 * SessionServiceはモックを使用する。</p>
 *
 * @since 1.1
 */

package com.isozaki.auth.resource;

import com.isozaki.auth.resource.SessionResource.SessionValidationResponse;
import com.isozaki.auth.service.SessionService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SessionResourceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionResource テスト")
class SessionResourceTest {

    @Mock
    private SessionService sessionService;

    private SessionResource sessionResource;

    @BeforeEach
    void setUp() {
        sessionResource = new SessionResource(sessionService);
    }

    @Nested
    @DisplayName("validateSession テスト")
    class ValidateSessionTests {

        /**
         * 【テスト対象】SessionResource#validateSession
         * 【テストケース】有効なセッションIDでセッションを検証する
         * 【期待結果】HTTP 200 OKとSessionValidationResponseが返却される
         * 【ビジネス要件】セッション検証API - 有効セッション
         */
        @Test
        @DisplayName("有効なセッションIDの場合、200 OKとレスポンスが返されること")
        void shouldReturnOkForValidSession() {
            // Given
            String sessionId = "valid-session-id";
            String userId = "01908b7e-1234-7000-8000-000000000001";
            when(sessionService.getUserIdBySession(sessionId)).thenReturn(userId);

            // When
            Response response = sessionResource.validateSession(sessionId);

            // Then
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            SessionValidationResponse body = (SessionValidationResponse) response.getEntity();
            assertNotNull(body);
            assertEquals(sessionId, body.sessionId());
            assertEquals(userId, body.userId());
            verify(sessionService).getUserIdBySession(sessionId);
        }

        /**
         * 【テスト対象】SessionResource#validateSession
         * 【テストケース】無効なセッションIDでセッションを検証する
         * 【期待結果】HTTP 404 Not Foundが返却される
         * 【ビジネス要件】セッション検証API - 無効セッション
         */
        @Test
        @DisplayName("無効なセッションIDの場合、404 Not Foundが返されること")
        void shouldReturnNotFoundForInvalidSession() {
            // Given
            String sessionId = "invalid-session-id";
            when(sessionService.getUserIdBySession(sessionId)).thenReturn(null);

            // When
            Response response = sessionResource.validateSession(sessionId);

            // Then
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            verify(sessionService).getUserIdBySession(sessionId);
        }
    }

    @Nested
    @DisplayName("deleteSession テスト")
    class DeleteSessionTests {

        /**
         * 【テスト対象】SessionResource#deleteSession
         * 【テストケース】セッションIDを指定して削除する
         * 【期待結果】HTTP 204 No Contentが返却される
         * 【ビジネス要件】ログアウトAPI - セッション削除
         */
        @Test
        @DisplayName("セッション削除が成功した場合、204 No Contentが返されること")
        void shouldReturnNoContentOnDelete() {
            // Given
            String sessionId = "session-to-delete";

            // When
            Response response = sessionResource.deleteSession(sessionId);

            // Then
            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
            verify(sessionService).invalidateSession(sessionId);
        }
    }
}
