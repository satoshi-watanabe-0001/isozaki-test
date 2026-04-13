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

    /**
     * 【テスト対象】LoginResponse コンストラクタ
     * 【テストケース】セッションID・ユーザID・ユーザ名を指定して生成する
     * 【期待結果】各フィールドに指定した値が設定される
     * 【ビジネス要件】ログイン成功レスポンスのデータ保持
     */
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

    /**
     * 【テスト対象】LoginResponse#equals
     * 【テストケース】同じ値を持つ2つのインスタンスを比較する
     * 【期待結果】equalsがtrueを返す
     * 【ビジネス要件】recordの等価性保証
     */
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
