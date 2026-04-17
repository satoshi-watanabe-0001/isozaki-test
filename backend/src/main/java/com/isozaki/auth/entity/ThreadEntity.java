/**
 * スレッドエンティティクラス
 *
 * <p>スレッド情報をPostgreSQLに永続化するためのJPAエンティティ。
 * アーティストコミュニティに紐づくスレッドを管理する。</p>
 *
 * @since 1.3
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
 * スレッド情報を保持するエンティティ
 *
 * <p>スレッドID（UUIDv7）、アーティストID、タイトル（最大50文字）、
 * 作成者ユーザID、作成日時を管理する。</p>
 *
 * @since 1.3
 */
@Entity
@Table(name = "threads")
public class ThreadEntity extends PanacheEntityBase {

    /**
     * スレッドID（UUIDv7形式、アプリケーション側で生成）
     */
    @Id
    @Column(name = "thread_id", nullable = false, columnDefinition = "uuid")
    public UUID threadId;

    /**
     * アーティストID（外部キー）
     */
    @Column(name = "artist_id", length = 100, nullable = false)
    public String artistId;

    /**
     * スレッドタイトル（最大50文字、改行不可）
     */
    @Column(name = "title", length = 50, nullable = false)
    public String title;

    /**
     * スレッド作成者のユーザID
     */
    @Column(name = "created_by", nullable = false, columnDefinition = "uuid")
    public UUID createdBy;

    /**
     * レコード作成日時
     */
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
