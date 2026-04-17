/**
 * ユーザリポジトリクラス
 *
 * <p>ユーザエンティティに対するデータアクセス操作を提供する。
 * Panacheリポジトリパターンを使用してPostgreSQLへのCRUD操作を行う。</p>
 *
 * @since 1.0
 */

package com.isozaki.auth.repository;

import com.isozaki.auth.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ユーザデータへのアクセスを提供するリポジトリ
 *
 * @since 1.0
 */
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, UUID> {

    /**
     * メールアドレスでユーザを検索する
     *
     * @param email 検索対象のメールアドレス（null不可）
     * @return ユーザが見つかった場合はOptionalに包まれたエンティティ、
     *         見つからない場合は空のOptional
     */
    public Optional<UserEntity> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    /**
     * 指定されたメールアドレスが既に登録されているか確認する
     *
     * @param email 確認対象のメールアドレス（null不可）
     * @return 既に登録されている場合はtrue
     */
    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    /**
     * ユーザIDでユーザを検索する
     *
     * @param userId 検索対象のユーザID（null不可）
     * @return ユーザが見つかった場合はOptionalに包まれたエンティティ、
     *         見つからない場合は空のOptional
     */
    public Optional<UserEntity> findByUserId(UUID userId) {
        return find("userId", userId).firstResultOptional();
    }

    /**
     * 複数ユーザIDからユーザ名を一括取得する（N+1問題対策）
     *
     * @param userIds 検索対象のユーザIDリスト
     * @return ユーザIDをキー、ユーザ名を値とするMap
     */
    public Map<UUID, String> findUsernamesByUserIds(List<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<UserEntity> users = list("userId in ?1", userIds);
        return users.stream()
                .collect(Collectors.toMap(u -> u.userId, u -> u.username));
    }
}
