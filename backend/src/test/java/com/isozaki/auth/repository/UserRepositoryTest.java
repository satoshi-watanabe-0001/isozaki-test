/**
 * UserRepositoryの結合テスト
 *
 * <p>H2インメモリデータベースを使用して、ユーザリポジトリの
 * データアクセスロジックを検証する。</p>
 *
 * @since 1.0
 */
package com.isozaki.auth.repository;

import com.isozaki.auth.entity.UserEntity;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UserRepositoryの結合テストクラス
 *
 * <p>H2データベースと結合し、実際のJPAクエリの動作を検証する。</p>
 */
@QuarkusTest
@DisplayName("UserRepository 結合テスト")
class UserRepositoryTest {

    @Inject
    UserRepository userRepository;

    /**
     * RedisDataSourceをモック化してRedis接続エラーを回避する
     */
    @InjectMock
    RedisDataSource redisDataSource;

    /**
     * 各テスト前にデータベースをクリーンアップする
     */
    @BeforeEach
    @Transactional
    void setUp() {
        userRepository.deleteAll();
    }

    /**
     * テスト用のUserEntityを生成し永続化するヘルパーメソッド
     *
     * @param email メールアドレス
     * @param username ユーザ名
     * @return 永続化されたユーザエンティティ
     */
    private UserEntity createAndPersistUser(String email, String username) {
        UserEntity user = new UserEntity();
        user.userId = UUID.fromString("01908b7e-1234-7000-8000-000000000001");
        user.username = username;
        user.email = email;
        user.passwordHash = "$2a$12$hashedpassword";
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        userRepository.persist(user);
        return user;
    }

    /**
     * 【テスト対象】UserRepository#findByEmail
     * 【テストケース】存在するメールアドレスで検索する
     * 【期待結果】対応するユーザエンティティが返却される
     * 【ビジネス要件】ログイン時のユーザ検索
     */
    @Test
    @DisplayName("findByEmail: 存在するメールアドレスでユーザが取得できること")
    @Transactional
    void findByEmail_shouldReturnUserWhenEmailExists() {
        // Given
        UserEntity persistedUser = createAndPersistUser("test@example.com", "テストユーザ");

        // When
        Optional<UserEntity> result = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals(persistedUser.userId, result.get().userId);
        assertEquals("test@example.com", result.get().email);
        assertEquals("テストユーザ", result.get().username);
    }

    /**
     * 【テスト対象】UserRepository#findByEmail
     * 【テストケース】存在しないメールアドレスで検索する
     * 【期待結果】空のOptionalが返却される
     * 【ビジネス要件】未登録ユーザの検索失敗ハンドリング
     */
    @Test
    @DisplayName("findByEmail: 存在しないメールアドレスで空のOptionalが返されること")
    @Transactional
    void findByEmail_shouldReturnEmptyWhenEmailDoesNotExist() {
        // Given
        createAndPersistUser("test@example.com", "テストユーザ");

        // When
        Optional<UserEntity> result = userRepository.findByEmail("unknown@example.com");

        // Then
        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】UserRepository#existsByEmail
     * 【テストケース】登録済みメールアドレスで存在確認する
     * 【期待結果】trueが返却される
     * 【ビジネス要件】メールアドレス重複チェック
     */
    @Test
    @DisplayName("existsByEmail: 登録済みメールアドレスでtrueが返されること")
    @Transactional
    void existsByEmail_shouldReturnTrueWhenEmailExists() {
        // Given
        createAndPersistUser("test@example.com", "テストユーザ");

        // When
        boolean result = userRepository.existsByEmail("test@example.com");

        // Then
        assertTrue(result);
    }

    /**
     * 【テスト対象】UserRepository#existsByEmail
     * 【テストケース】未登録メールアドレスで存在確認する
     * 【期待結果】falseが返却される
     * 【ビジネス要件】未登録メールアドレスの判定
     */
    @Test
    @DisplayName("existsByEmail: 未登録メールアドレスでfalseが返されること")
    @Transactional
    void existsByEmail_shouldReturnFalseWhenEmailDoesNotExist() {
        // Given
        createAndPersistUser("test@example.com", "テストユーザ");

        // When
        boolean result = userRepository.existsByEmail("unknown@example.com");

        // Then
        assertFalse(result);
    }

    /**
     * 【テスト対象】UserRepository#existsByEmail
     * 【テストケース】データが存在しない状態で存在確認する
     * 【期待結果】falseが返却される
     * 【ビジネス要件】空テーブルでの存在確認
     */
    @Test
    @DisplayName("existsByEmail: データが存在しない場合にfalseが返されること")
    @Transactional
    void existsByEmail_shouldReturnFalseWhenNoDataExists() {
        // When
        boolean result = userRepository.existsByEmail("test@example.com");

        // Then
        assertFalse(result);
    }
}
