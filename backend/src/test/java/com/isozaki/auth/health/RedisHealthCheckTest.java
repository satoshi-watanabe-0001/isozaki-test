/**
 * RedisHealthCheckの単体テスト
 *
 * <p>Redisヘルスチェックの正常系・異常系をテストする。
 * RedisDataSourceはモックを使用する。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.health;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * RedisHealthCheckのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RedisHealthCheck テスト")
class RedisHealthCheckTest {

    @Mock
    private RedisDataSource redisDataSource;

    @Mock
    private ValueCommands<String, String> valueCommands;

    private RedisHealthCheck healthCheck;

    @BeforeEach
    void setUp() {
        healthCheck = new RedisHealthCheck(redisDataSource);
    }

    @Nested
    @DisplayName("call 正常系テスト")
    class CallSuccessTests {

        /**
         * 【テスト対象】RedisHealthCheck#call
         * 【テストケース】Redis接続が正常な場合
         * 【期待結果】ステータスUPが返却される
         * 【ビジネス要件】Redis接続の正常性確認
         */
        @Test
        @DisplayName("Redis接続が正常な場合、UPが返されること")
        void shouldReturnUpWhenRedisIsAvailable() {
            // Given
            when(redisDataSource.value(String.class, String.class)).thenReturn(valueCommands);
            when(valueCommands.get("health-check-ping")).thenReturn(null);

            // When
            HealthCheckResponse response = healthCheck.call();

            // Then
            assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
            assertEquals("Redis接続", response.getName());
        }
    }

    @Nested
    @DisplayName("call 異常系テスト")
    class CallFailureTests {

        /**
         * 【テスト対象】RedisHealthCheck#call
         * 【テストケース】Redis接続で例外が発生する
         * 【期待結果】ステータスDOWNが返却される
         * 【ビジネス要件】Redis障害時の異常検知
         */
        @Test
        @DisplayName("Redis接続で例外が発生した場合、DOWNが返されること")
        void shouldReturnDownWhenExceptionOccurs() {
            // Given
            when(redisDataSource.value(String.class, String.class))
                    .thenThrow(new RuntimeException("Redis接続失敗"));

            // When
            HealthCheckResponse response = healthCheck.call();

            // Then
            assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
            assertEquals("Redis接続", response.getName());
        }
    }
}
