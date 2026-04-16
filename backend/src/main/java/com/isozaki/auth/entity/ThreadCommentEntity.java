/**
 * スレッドコメントエンティティクラス
 *
 * <p>スレッドに紐づくコメント情報をPostgreSQLに永続化するためのJPAエンティティ。
 * 各コメントはスレッドとユーザに紐づく。</p>
 *
 * @since 1.3
 */

package com.isozaki.auth.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * スレッドコメント情報を保持するエンティティ
 *
 * <p>コメントID（連番）、スレッドID、コメント内容（最大200文字）、
 * 作成者ユーザID、作成日時を管理する。</p>
 *
 * @since 1.3
 */
@Entity
@Table(name = "thread_comments")
public class ThreadCommentEntity extends PanacheEntityBase {

    /**
     * コメントID（連番、自動採番）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", nullable = false)
    public Integer commentId;

    /**
     * スレッドID（外部キー）
     */
    @Column(name = "thread_id", nullable = false)
    public Integer threadId;

    /**
     * コメント内容（最大200文字、改行可）
     */
    @Column(name = "content", length = 200, nullable = false)
    public String content;

    /**
     * コメント作成者のユーザID
     */
    @Column(name = "created_by", nullable = false, columnDefinition = "uuid")
    public UUID createdBy;

    /**
     * レコード作成日時
     */
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
