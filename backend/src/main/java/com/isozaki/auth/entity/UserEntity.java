/**
 * ユーザエンティティクラス
 *
 * <p>ユーザ情報をPostgreSQLに永続化するためのJPAエンティティ。
 * UUIDv7を主キーとし、メールアドレスの一意性を保証する。</p>
 *
 * @since 1.0
 */

package com.isozaki.auth.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * ユーザ情報を保持するエンティティ
 *
 * <p>ユーザID（UUIDv7）、ユーザ名（日本語対応）、メールアドレス（重複不可）、
 * パスワード（bcryptハッシュ）を管理する。</p>
 *
 * @since 1.0
 */
@Entity
@Table(name = "users")
public class UserEntity extends PanacheEntityBase {

    /**
     * ユーザID（UUIDv7形式）
     */
    @Id
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    public UUID userId;

    /**
     * ユーザ名（日本語入力可）
     */
    @Column(name = "username", length = 255, nullable = false)
    public String username;

    /**
     * メールアドレス（重複不可）
     */
    @Column(name = "email", length = 255, nullable = false, unique = true)
    public String email;

    /**
     * bcryptでハッシュ化されたパスワード
     */
    @Column(name = "password_hash", length = 255, nullable = false)
    public String passwordHash;

    /**
     * レコード作成日時
     */
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    /**
     * レコード更新日時
     */
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}
