/**
 * ApplicationLivenessCheckの単体テスト
 *
 * <p>アプリケーションLivenessチェックをテストする。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.health;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ApplicationLivenessCheckのテストクラス
 */
@DisplayName("ApplicationLivenessCheck テスト")
class ApplicationLivenessCheckTest {

    private ApplicationLivenessCheck healthCheck;

    @BeforeEach
    void setUp() {
        healthCheck = new ApplicationLivenessCheck();
    }

    @Test
    @DisplayName("アプリケーションが稼働中の場合、UPが返されること")
    void shouldReturnUp() {
        // When
        HealthCheckResponse response = healthCheck.call();

        // Then
        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("アプリケーション稼働状態", response.getName());
    }
}
