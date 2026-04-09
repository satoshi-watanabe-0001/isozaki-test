/**
 * LoginResponseの単体テスト
 *
 * <p>LoginResponse DTOのgetter/setter/コンストラクタをテストする。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * LoginResponseのテストクラス
 */
@DisplayName("LoginResponse テスト")
class LoginResponseTest {

    @Test
    @DisplayName("デフォルトコンストラクタでnullが設定されること")
    void shouldCreateWithDefaultConstructor() {
        // When
        LoginResponse response = new LoginResponse();

        // Then
        assertNull(response.getSessionId());
        assertNull(response.getUserId());
        assertNull(response.getUsername());
    }

    @Test
    @DisplayName("全引数コンストラクタで値が正しく設定されること")
    void shouldCreateWithAllArgsConstructor() {
        // When
        LoginResponse response = new LoginResponse("session-123", "user-456", "テストユーザ");

        // Then
        assertEquals("session-123", response.getSessionId());
        assertEquals("user-456", response.getUserId());
        assertEquals("テストユーザ", response.getUsername());
    }

    @Test
    @DisplayName("setterで値が正しく設定されること")
    void shouldSetValuesCorrectly() {
        // Given
        LoginResponse response = new LoginResponse();

        // When
        response.setSessionId("session-123");
        response.setUserId("user-456");
        response.setUsername("テストユーザ");

        // Then
        assertEquals("session-123", response.getSessionId());
        assertEquals("user-456", response.getUserId());
        assertEquals("テストユーザ", response.getUsername());
    }
}
