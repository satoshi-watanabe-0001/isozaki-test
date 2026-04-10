/**
 * UserRepositoryの単体テスト
 *
 * <p>ユーザリポジトリのデータアクセスロジックをテストする。
 * Panacheの内部メソッドはモック・スパイを使用する。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.repository;

import com.isozaki.auth.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserRepositoryのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository テスト")
class UserRepositoryTest {

    @Spy
    private UserRepository userRepository;

    @Mock
    private PanacheQuery<UserEntity> panacheQuery;

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
    @DisplayName("findByEmail テスト")
    class FindByEmailTests {

        @Test
        @DisplayName("存在するメールアドレスでユーザが取得できること")
        void shouldReturnUserWhenEmailExists() {
            // Given
            UserEntity expectedUser = createTestUser();
            doReturn(panacheQuery).when(userRepository).find("email", "test@example.com");
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.of(expectedUser));

            // When
            Optional<UserEntity> result = userRepository.findByEmail("test@example.com");

            // Then
            assertTrue(result.isPresent());
            assertEquals(expectedUser.userId, result.get().userId);
            assertEquals("test@example.com", result.get().email);
            assertEquals("テストユーザ", result.get().username);
            verify(userRepository).find("email", "test@example.com");
            verify(panacheQuery).firstResultOptional();
        }

        @Test
        @DisplayName("存在しないメールアドレスで空のOptionalが返されること")
        void shouldReturnEmptyWhenEmailDoesNotExist() {
            // Given
            doReturn(panacheQuery).when(userRepository).find("email", "unknown@example.com");
            when(panacheQuery.firstResultOptional()).thenReturn(Optional.empty());

            // When
            Optional<UserEntity> result = userRepository.findByEmail("unknown@example.com");

            // Then
            assertFalse(result.isPresent());
            verify(userRepository).find("email", "unknown@example.com");
            verify(panacheQuery).firstResultOptional();
        }
    }

    @Nested
    @DisplayName("existsByEmail テスト")
    class ExistsByEmailTests {

        @Test
        @DisplayName("登録済みメールアドレスでtrueが返されること")
        void shouldReturnTrueWhenEmailExists() {
            // Given
            doReturn(1L).when(userRepository).count("email", "test@example.com");

            // When
            boolean result = userRepository.existsByEmail("test@example.com");

            // Then
            assertTrue(result);
            verify(userRepository).count("email", "test@example.com");
        }

        @Test
        @DisplayName("未登録メールアドレスでfalseが返されること")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // Given
            doReturn(0L).when(userRepository).count("email", "unknown@example.com");

            // When
            boolean result = userRepository.existsByEmail("unknown@example.com");

            // Then
            assertFalse(result);
            verify(userRepository).count("email", "unknown@example.com");
        }

        @Test
        @DisplayName("同じメールアドレスが複数件ある場合でもtrueが返されること")
        void shouldReturnTrueWhenMultipleUsersExist() {
            // Given
            doReturn(2L).when(userRepository).count("email", "duplicate@example.com");

            // When
            boolean result = userRepository.existsByEmail("duplicate@example.com");

            // Then
            assertTrue(result);
            verify(userRepository).count("email", "duplicate@example.com");
        }
    }
}
