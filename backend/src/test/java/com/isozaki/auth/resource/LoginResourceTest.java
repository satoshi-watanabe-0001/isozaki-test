/**
 * LoginResourceの単体テスト
 *
 * <p>ログインエンドポイントのリクエスト処理とレスポンス生成をテストする。
 * AuthServiceはモックを使用する。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.resource;

import com.isozaki.auth.dto.LoginRequest;
import com.isozaki.auth.dto.LoginResponse;
import com.isozaki.auth.service.AuthService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LoginResourceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginResource テスト")
class LoginResourceTest {

    @Mock
    private AuthService authService;

    private LoginResource loginResource;

    @BeforeEach
    void setUp() {
        loginResource = new LoginResource(authService);
    }

    @Test
    @DisplayName("ログインが成功した場合、200 OKとレスポンスが返されること")
    void shouldReturnOkWithLoginResponse() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        LoginResponse expectedResponse = new LoginResponse("session-123", "user-456", "テストユーザ");
        when(authService.login(request)).thenReturn(expectedResponse);

        // When
        Response response = loginResource.login(request);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        LoginResponse actualResponse = (LoginResponse) response.getEntity();
        assertNotNull(actualResponse);
        assertEquals("session-123", actualResponse.sessionId());
        assertEquals("user-456", actualResponse.userId());
        assertEquals("テストユーザ", actualResponse.username());
        verify(authService).login(request);
    }
}
