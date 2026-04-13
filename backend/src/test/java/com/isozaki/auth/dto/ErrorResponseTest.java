/**
 * ErrorResponseの単体テスト
 *
 * <p>ErrorResponse recordのファクトリメソッドとアクセサをテストする。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ErrorResponseのテストクラス
 */
@DisplayName("ErrorResponse テスト")
class ErrorResponseTest {

    /**
     * 【テスト対象】ErrorResponse#of(String, String)
     * 【テストケース】エラーコードとメッセージを指定して生成する
     * 【期待結果】error.code, error.message, meta.timestampが正しく設定される
     * 【ビジネス要件】API規約準拠のエラーレスポンス生成
     */
    @Test
    @DisplayName("ファクトリメソッドでエラーレスポンスが正しく生成されること")
    void shouldCreateWithFactoryMethod() {
        // When
        ErrorResponse response = ErrorResponse.of("AUTH_FAILED", "認証に失敗しました");

        // Then
        assertNotNull(response.error());
        assertEquals("AUTH_FAILED", response.error().code());
        assertEquals("認証に失敗しました", response.error().message());
        assertTrue(response.error().details().isEmpty());
        assertNotNull(response.meta());
        assertNotNull(response.meta().timestamp());
    }

    /**
     * 【テスト対象】ErrorResponse#of(String, String, List)
     * 【テストケース】エラーコード、メッセージ、詳細リストを指定して生成する
     * 【期待結果】error.detailsに指定したリストが設定される
     * 【ビジネス要件】バリデーションエラーの詳細情報伝達
     */
    @Test
    @DisplayName("詳細リスト付きでエラーレスポンスが正しく生成されること")
    void shouldCreateWithDetails() {
        // Given
        List<String> details = List.of("メールアドレスは必須です", "パスワードは必須です");

        // When
        ErrorResponse response = ErrorResponse.of("VALIDATION_ERROR", "バリデーションエラー", details);

        // Then
        assertNotNull(response.error());
        assertEquals("VALIDATION_ERROR", response.error().code());
        assertEquals("バリデーションエラー", response.error().message());
        assertEquals(2, response.error().details().size());
        assertEquals("メールアドレスは必須です", response.error().details().get(0));
        assertEquals("パスワードは必須です", response.error().details().get(1));
    }

    /**
     * 【テスト対象】ErrorResponse#meta
     * 【テストケース】生成時のタイムスタンプを確認する
     * 【期待結果】ISO-8601形式のタイムスタンプが設定される
     * 【ビジネス要件】エラー発生時刻の記録
     */
    @Test
    @DisplayName("メタ情報にタイムスタンプが設定されること")
    void shouldContainTimestamp() {
        // When
        ErrorResponse response = ErrorResponse.of("ERROR", "テスト");

        // Then
        assertNotNull(response.meta().timestamp());
        assertTrue(response.meta().timestamp().contains("T"));
    }
}
