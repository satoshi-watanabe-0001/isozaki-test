/**
 * LoginRequestの単体テスト
 *
 * <p>LoginRequest DTOのgetter/setter/コンストラクタをテストする。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * LoginRequestのテストクラス
 */
@DisplayName("LoginRequest テスト")
class LoginRequestTest {

    @Test
    @DisplayName("デフォルトコンストラクタでnullが設定されること")
    void shouldCreateWithDefaultConstructor() {
        // When
        LoginRequest request = new LoginRequest();

        // Then
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    @DisplayName("全引数コンストラクタで値が正しく設定されること")
    void shouldCreateWithAllArgsConstructor() {
        // When
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        // Then
        assertEquals("test@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    @DisplayName("setterで値が正しく設定されること")
    void shouldSetValuesCorrectly() {
        // Given
        LoginRequest request = new LoginRequest();

        // When
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Then
        assertEquals("test@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }
}
