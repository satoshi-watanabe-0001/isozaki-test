/**
 * ErrorResponseの単体テスト
 *
 * <p>ErrorResponse DTOのgetter/setter/コンストラクタをテストする。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * ErrorResponseのテストクラス
 */
@DisplayName("ErrorResponse テスト")
class ErrorResponseTest {

    @Test
    @DisplayName("デフォルトコンストラクタでnullが設定されること")
    void shouldCreateWithDefaultConstructor() {
        // When
        ErrorResponse response = new ErrorResponse();

        // Then
        assertNull(response.getErrorCode());
        assertNull(response.getMessage());
    }

    @Test
    @DisplayName("全引数コンストラクタで値が正しく設定されること")
    void shouldCreateWithAllArgsConstructor() {
        // When
        ErrorResponse response = new ErrorResponse("AUTH_FAILED", "認証に失敗しました");

        // Then
        assertEquals("AUTH_FAILED", response.getErrorCode());
        assertEquals("認証に失敗しました", response.getMessage());
    }

    @Test
    @DisplayName("setterで値が正しく設定されること")
    void shouldSetValuesCorrectly() {
        // Given
        ErrorResponse response = new ErrorResponse();

        // When
        response.setErrorCode("VALIDATION_ERROR");
        response.setMessage("バリデーションエラー");

        // Then
        assertEquals("VALIDATION_ERROR", response.getErrorCode());
        assertEquals("バリデーションエラー", response.getMessage());
    }
}
