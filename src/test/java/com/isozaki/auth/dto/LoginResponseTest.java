/**
 * LoginResponseの単体テスト
 *
 * <p>LoginResponse recordのコンストラクタとアクセサをテストする。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * LoginResponseのテストクラス
 */
@DisplayName("LoginResponse テスト")
class LoginResponseTest {

    @Test
    @DisplayName("コンストラクタで値が正しく設定されること")
    void shouldCreateWithConstructor() {
        // When
        LoginResponse response = new LoginResponse("session-123", "user-456", "テストユーザ");

        // Then
        assertEquals("session-123", response.sessionId());
        assertEquals("user-456", response.userId());
        assertEquals("テストユーザ", response.username());
    }

    @Test
    @DisplayName("同じ値を持つインスタンスが等価であること")
    void shouldBeEqualForSameValues() {
        // Given
        LoginResponse response1 = new LoginResponse("session-123", "user-456", "テストユーザ");
        LoginResponse response2 = new LoginResponse("session-123", "user-456", "テストユーザ");

        // Then
        assertEquals(response1, response2);
    }
}
