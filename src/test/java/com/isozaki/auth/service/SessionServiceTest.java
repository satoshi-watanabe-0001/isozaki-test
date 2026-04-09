/**
 * SessionServiceの単体テスト
 *
 * <p>Redisセッション管理ロジックの正常系・異常系をテストする。
 * RedisDataSourceはモックを使用する。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.service;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SessionServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService テスト")
class SessionServiceTest {

    @Mock
    private RedisDataSource redisDataSource;

    @Mock
    private ValueCommands<String, String> valueCommands;

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        when(redisDataSource.value(String.class, String.class)).thenReturn(valueCommands);
        sessionService = new SessionService(redisDataSource);
    }

    @Nested
    @DisplayName("createSession テスト")
    class CreateSessionTests {

        @Test
        @DisplayName("セッションが正常に作成されること")
        void shouldCreateSessionSuccessfully() {
            // Given
            String userId = "01908b7e-1234-7000-8000-000000000001";

            // When
            String sessionId = sessionService.createSession(userId);

            // Then
            assertNotNull(sessionId);
            verify(valueCommands).setex(startsWith("session:"), eq(1800L), eq(userId));
        }

        @Test
        @DisplayName("異なるセッションIDが毎回生成されること")
        void shouldGenerateUniqueSessionIds() {
            // Given
            String userId = "01908b7e-1234-7000-8000-000000000001";

            // When
            String sessionId1 = sessionService.createSession(userId);
            String sessionId2 = sessionService.createSession(userId);

            // Then
            assertNotNull(sessionId1);
            assertNotNull(sessionId2);
        }
    }

    @Nested
    @DisplayName("getUserIdBySession テスト")
    class GetUserIdBySessionTests {

        @Test
        @DisplayName("有効なセッションIDでユーザIDが取得できること")
        void shouldReturnUserIdForValidSession() {
            // Given
            String sessionId = "test-session-id";
            String expectedUserId = "01908b7e-1234-7000-8000-000000000001";
            when(valueCommands.get("session:" + sessionId)).thenReturn(expectedUserId);

            // When
            String result = sessionService.getUserIdBySession(sessionId);

            // Then
            assertEquals(expectedUserId, result);
            verify(valueCommands).get("session:" + sessionId);
        }

        @Test
        @DisplayName("無効なセッションIDでnullが返されること")
        void shouldReturnNullForInvalidSession() {
            // Given
            String sessionId = "invalid-session-id";
            when(valueCommands.get("session:" + sessionId)).thenReturn(null);

            // When
            String result = sessionService.getUserIdBySession(sessionId);

            // Then
            assertNull(result);
            verify(valueCommands).get("session:" + sessionId);
        }
    }

    @Nested
    @DisplayName("invalidateSession テスト")
    class InvalidateSessionTests {

        @Test
        @DisplayName("セッションが正常に削除されること")
        void shouldInvalidateSessionSuccessfully() {
            // Given
            String sessionId = "test-session-id";

            // When
            sessionService.invalidateSession(sessionId);

            // Then
            verify(valueCommands).getdel("session:" + sessionId);
        }
    }
}
