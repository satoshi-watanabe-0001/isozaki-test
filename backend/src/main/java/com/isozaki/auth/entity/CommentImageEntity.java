/**
 * コメント画像エンティティクラス
 *
 * <p>コメントに紐づく画像情報をPostgreSQLに永続化するためのJPAエンティティ。
 * PENDING/CONFIRMEDステータスで画像のライフサイクルを管理する。</p>
 *
 * @since 1.4
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
 * コメント画像情報を保持するエンティティ
 *
 * <p>画像ID（UUIDv7）、コメントID、S3キー、ステータス（PENDING/CONFIRMED）、
 * アップロードユーザID、作成日時を管理する。</p>
 *
 * @since 1.4
 */
@Entity
@Table(name = "comment_images")
public class CommentImageEntity extends PanacheEntityBase {

    /**
     * 画像ID（UUIDv7形式、アプリケーション側で生成）
     */
    @Id
    @Column(name = "image_id", nullable = false, columnDefinition = "uuid")
    public UUID imageId;

    /**
     * コメントID（CONFIRMED時に設定、PENDING時はnull）
     */
    @Column(name = "comment_id", columnDefinition = "uuid")
    public UUID commentId;

    /**
     * S3オブジェクトキー（originals/プレフィックス付き）
     */
    @Column(name = "s3_key", length = 500, nullable = false)
    public String s3Key;

    /**
     * 画像ステータス（PENDING: アップロード中、CONFIRMED: コメント紐付け済み）
     */
    @Column(name = "status", length = 20, nullable = false)
    public String status;

    /**
     * アップロードしたユーザID
     */
    @Column(name = "uploaded_by", nullable = false, columnDefinition = "uuid")
    public UUID uploadedBy;

    /**
     * レコード作成日時
     */
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
