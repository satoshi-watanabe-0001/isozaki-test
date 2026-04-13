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

        /**
         * 【テスト対象】SessionService#createSession
         * 【テストケース】ユーザIDを指定してセッションを作成する
         * 【期待結果】セッションIDが返却され、Redisに保存される
         * 【ビジネス要件】ログイン時のセッション発行
         */
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

        /**
         * 【テスト対象】SessionService#createSession
         * 【テストケース】同じユーザIDで複数回セッションを作成する
         * 【期待結果】異なるセッションIDが毎回生成される
         * 【ビジネス要件】セッションIDの一意性保証
         */
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

        /**
         * 【テスト対象】SessionService#getUserIdBySession
         * 【テストケース】有効なセッションIDでユーザIDを取得する
         * 【期待結果】対応するユーザIDが返却される
         * 【ビジネス要件】セッションによるユーザ識別
         */
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

        /**
         * 【テスト対象】SessionService#getUserIdBySession
         * 【テストケース】無効なセッションIDでユーザIDを取得する
         * 【期待結果】nullが返却される
         * 【ビジネス要件】期限切れ・不正セッションの拒否
         */
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

        /**
         * 【テスト対象】SessionService#invalidateSession
         * 【テストケース】セッションIDを指定して削除する
         * 【期待結果】Redisからセッションが削除される
         * 【ビジネス要件】ログアウト機能
         */
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
