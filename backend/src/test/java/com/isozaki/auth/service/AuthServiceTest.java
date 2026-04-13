/**
 * AuthServiceの単体テスト
 *
 * <p>ログイン認証ロジックの正常系・異常系をテストする。
 * UserRepository、PasswordService、SessionServiceはモックを使用する。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.service;

import com.isozaki.auth.dto.LoginRequest;
import com.isozaki.auth.dto.LoginResponse;
import com.isozaki.auth.entity.UserEntity;
import com.isozaki.auth.exception.AuthenticationException;
import com.isozaki.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService テスト")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private SessionService sessionService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordService, sessionService);
    }

    /**
     * テスト用のUserEntityを生成するヘルパーメソッド
     *
     * @return テスト用ユーザエンティティ
     */
    private UserEntity createTestUser() {
        UserEntity user = new UserEntity();
        user.userId = UUID.fromString("01908b7e-1234-7000-8000-000000000001");
        user.username = "テストユーザ";
        user.email = "test@example.com";
        user.passwordHash = "$2a$12$hashedpassword";
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }

    @Nested
    @DisplayName("login 正常系テスト")
    class LoginSuccessTests {

        /**
         * 【テスト対象】AuthService#login
         * 【テストケース】正しいメールアドレスとパスワードでログインする
         * 【期待結果】セッションID・ユーザID・ユーザ名を含むLoginResponseが返却される
         * 【ビジネス要件】ユーザ認証機能 - 正常ログイン
         */
        @Test
        @DisplayName("正しい認証情報でログインが成功すること")
        void shouldLoginSuccessfully() {
            // Given
            LoginRequest request = new LoginRequest("test@example.com", "password123");
            UserEntity user = createTestUser();
            String expectedSessionId = "session-uuid-123";

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordService.verifyPassword("password123", user.passwordHash)).thenReturn(true);
            when(sessionService.createSession(user.userId.toString())).thenReturn(expectedSessionId);

            // When
            LoginResponse response = authService.login(request);

            // Then
            assertNotNull(response);
            assertEquals(expectedSessionId, response.sessionId());
            assertEquals(user.userId.toString(), response.userId());
            assertEquals(user.username, response.username());

            verify(userRepository).findByEmail("test@example.com");
            verify(passwordService).verifyPassword("password123", user.passwordHash);
            verify(sessionService).createSession(user.userId.toString());
        }
    }

    @Nested
    @DisplayName("login 異常系テスト")
    class LoginFailureTests {

        /**
         * 【テスト対象】AuthService#login
         * 【テストケース】未登録のメールアドレスでログインを試みる
         * 【期待結果】AuthenticationExceptionがスローされる
         * 【ビジネス要件】ユーザ認証機能 - 不正なメールアドレスの拒否
         */
        @Test
        @DisplayName("未登録のメールアドレスでAuthenticationExceptionがスローされること")
        void shouldThrowExceptionWhenEmailNotFound() {
            // Given
            LoginRequest request = new LoginRequest("unknown@example.com", "password123");
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            // When & Then
            AuthenticationException exception = assertThrows(
                    AuthenticationException.class,
                    () -> authService.login(request)
            );

            assertEquals("メールアドレスまたはパスワードが正しくありません", exception.getMessage());
            verify(userRepository).findByEmail("unknown@example.com");
            verify(passwordService, never()).verifyPassword(anyString(), anyString());
            verify(sessionService, never()).createSession(anyString());
        }

        /**
         * 【テスト対象】AuthService#login
         * 【テストケース】誤ったパスワードでログインを試みる
         * 【期待結果】AuthenticationExceptionがスローされる
         * 【ビジネス要件】ユーザ認証機能 - 不正なパスワードの拒否
         */
        @Test
        @DisplayName("パスワード不一致でAuthenticationExceptionがスローされること")
        void shouldThrowExceptionWhenPasswordDoesNotMatch() {
            // Given
            LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
            UserEntity user = createTestUser();

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordService.verifyPassword("wrongpassword", user.passwordHash)).thenReturn(false);

            // When & Then
            AuthenticationException exception = assertThrows(
                    AuthenticationException.class,
                    () -> authService.login(request)
            );

            assertEquals("メールアドレスまたはパスワードが正しくありません", exception.getMessage());
            verify(userRepository).findByEmail("test@example.com");
            verify(passwordService).verifyPassword("wrongpassword", user.passwordHash);
            verify(sessionService, never()).createSession(anyString());
        }
    }
}
