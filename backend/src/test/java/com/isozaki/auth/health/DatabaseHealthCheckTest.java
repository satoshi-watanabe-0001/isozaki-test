/**
 * DatabaseHealthCheckの単体テスト
 *
 * <p>データベースヘルスチェックの正常系・異常系をテストする。
 * DataSourceはモックを使用する。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.health;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * DatabaseHealthCheckのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseHealthCheck テスト")
class DatabaseHealthCheckTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private DatabaseHealthCheck healthCheck;

    @BeforeEach
    void setUp() {
        healthCheck = new DatabaseHealthCheck(dataSource);
    }

    @Nested
    @DisplayName("call 正常系テスト")
    class CallSuccessTests {

        @Test
        @DisplayName("データベース接続が有効な場合、UPが返されること")
        void shouldReturnUpWhenConnectionIsValid() throws SQLException {
            // Given
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(5)).thenReturn(true);

            // When
            HealthCheckResponse response = healthCheck.call();

            // Then
            assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
            assertEquals("データベース接続", response.getName());
        }
    }

    @Nested
    @DisplayName("call 異常系テスト")
    class CallFailureTests {

        @Test
        @DisplayName("データベース接続が無効な場合、DOWNが返されること")
        void shouldReturnDownWhenConnectionIsInvalid() throws SQLException {
            // Given
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isValid(5)).thenReturn(false);

            // When
            HealthCheckResponse response = healthCheck.call();

            // Then
            assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
            assertEquals("データベース接続", response.getName());
        }

        @Test
        @DisplayName("データベース接続で例外が発生した場合、DOWNが返されること")
        void shouldReturnDownWhenExceptionOccurs() throws SQLException {
            // Given
            when(dataSource.getConnection()).thenThrow(new SQLException("接続失敗"));

            // When
            HealthCheckResponse response = healthCheck.call();

            // Then
            assertEquals(HealthCheckResponse.Status.DOWN, response.getStatus());
            assertEquals("データベース接続", response.getName());
        }
    }
}
