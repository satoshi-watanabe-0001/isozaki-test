/**
 * LoginRequestの単体テスト
 *
 * <p>LoginRequest recordのコンストラクタとアクセサをテストする。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * LoginRequestのテストクラス
 */
@DisplayName("LoginRequest テスト")
class LoginRequestTest {

    @Test
    @DisplayName("コンストラクタで値が正しく設定されること")
    void shouldCreateWithConstructor() {
        // When
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        // Then
        assertEquals("test@example.com", request.email());
        assertEquals("password123", request.password());
    }

    @Test
    @DisplayName("同じ値を持つインスタンスが等価であること")
    void shouldBeEqualForSameValues() {
        // Given
        LoginRequest request1 = new LoginRequest("test@example.com", "password123");
        LoginRequest request2 = new LoginRequest("test@example.com", "password123");

        // Then
        assertEquals(request1, request2);
    }
}
