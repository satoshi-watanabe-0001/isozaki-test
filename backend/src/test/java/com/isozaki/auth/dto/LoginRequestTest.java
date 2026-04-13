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

    /**
     * 【テスト対象】LoginRequest コンストラクタ
     * 【テストケース】メールアドレスとパスワードを指定して生成する
     * 【期待結果】各フィールドに指定した値が設定される
     * 【ビジネス要件】ログインリクエストのデータ保持
     */
    @Test
    @DisplayName("コンストラクタで値が正しく設定されること")
    void shouldCreateWithConstructor() {
        // When
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        // Then
        assertEquals("test@example.com", request.email());
        assertEquals("password123", request.password());
    }

    /**
     * 【テスト対象】LoginRequest#equals
     * 【テストケース】同じ値を持つ2つのインスタンスを比較する
     * 【期待結果】equalsがtrueを返す
     * 【ビジネス要件】recordの等価性保証
     */
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
