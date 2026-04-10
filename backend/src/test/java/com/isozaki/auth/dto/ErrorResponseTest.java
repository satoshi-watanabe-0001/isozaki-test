/**
 * ErrorResponseの単体テスト
 *
 * <p>ErrorResponse recordのコンストラクタとアクセサをテストする。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ErrorResponseのテストクラス
 */
@DisplayName("ErrorResponse テスト")
class ErrorResponseTest {

    @Test
    @DisplayName("コンストラクタで値が正しく設定されること")
    void shouldCreateWithConstructor() {
        // When
        ErrorResponse response = new ErrorResponse("AUTH_FAILED", "認証に失敗しました");

        // Then
        assertEquals("AUTH_FAILED", response.errorCode());
        assertEquals("認証に失敗しました", response.message());
    }

    @Test
    @DisplayName("同じ値を持つインスタンスが等価であること")
    void shouldBeEqualForSameValues() {
        // Given
        ErrorResponse response1 = new ErrorResponse("VALIDATION_ERROR", "バリデーションエラー");
        ErrorResponse response2 = new ErrorResponse("VALIDATION_ERROR", "バリデーションエラー");

        // Then
        assertEquals(response1, response2);
    }
}
