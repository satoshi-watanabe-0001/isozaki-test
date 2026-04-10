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

    @Test
    @DisplayName("UUIDv7が生成されること")
    void shouldGenerateUuidV7() {
        // When
        String uuid = uuidService.generateUuidV7();

        // Then
        assertNotNull(uuid);
        assertTrue(uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    }

    @Test
    @DisplayName("生成されるUUIDが毎回異なること")
    void shouldGenerateUniqueUuids() {
        // When
        String uuid1 = uuidService.generateUuidV7();
        String uuid2 = uuidService.generateUuidV7();

        // Then
        assertNotEquals(uuid1, uuid2);
    }

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
