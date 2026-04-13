/**
 * UuidServiceの単体テスト
 *
 * <p>UUIDv7生成ロジックの正常系をテストする。</p>
 *
 * @since 1.0
 */

package com.isozaki.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UuidServiceのテストクラス
 */
@DisplayName("UuidService テスト")
class UuidServiceTest {

    private UuidService uuidService;

    @BeforeEach
    void setUp() {
        uuidService = new UuidService();
    }

    /**
     * 【テスト対象】UuidService#generateUuidV7
     * 【テストケース】UUIDv7を生成する
     * 【期待結果】UUIDv7形式の文字列が返却される
     * 【ビジネス要件】ユーザIDの一意性保証
     */
    @Test
    @DisplayName("UUIDv7が生成されること")
    void shouldGenerateUuidV7() {
        // When
        String uuid = uuidService.generateUuidV7();

        // Then
        assertNotNull(uuid);
        assertTrue(uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    /**
     * 【テスト対象】UuidService#generateUuidV7
     * 【テストケース】複数回UUIDを生成する
     * 【期待結果】異なるUUIDが毎回生成される
     * 【ビジネス要件】UUIDの一意性保証
     */
    @Test
    @DisplayName("生成されるUUIDが毎回異なること")
    void shouldGenerateUniqueUuids() {
        // When
        String uuid1 = uuidService.generateUuidV7();
        String uuid2 = uuidService.generateUuidV7();

        // Then
        assertNotEquals(uuid1, uuid2);
    }

    /**
     * 【テスト対象】UuidService#generateUuidV7
     * 【テストケース】生成されたUUIDの文字列長を確認する
     * 【期待結果】UUID形式の36文字である
     * 【ビジネス要件】UUID形式の正当性
     */
    @Test
    @DisplayName("UUIDが正しい文字列長であること")
    void shouldGenerateCorrectLengthUuid() {
        // When
        String uuid = uuidService.generateUuidV7();

        // Then
        // UUID形式: 8-4-4-4-12 = 36文字
        assertTrue(uuid.length() == 36);
    }
}
