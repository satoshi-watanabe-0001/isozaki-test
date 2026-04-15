/**
 * アーティスト画像エンティティクラス
 *
 * <p>アーティストのカルーセル表示用画像情報をPostgreSQLに永続化するためのJPAエンティティ。
 * アーティストごとに複数の画像を保持し、表示順で管理する。</p>
 *
 * @since 1.2
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

/**
 * アーティスト画像情報を保持するエンティティ
 *
 * <p>カルーセル表示用の画像URL、表示順を管理する。</p>
 *
 * @since 1.2
 */
@Entity
@Table(name = "artist_images")
public class ArtistImageEntity extends PanacheEntityBase {

    /**
     * 画像ID（自動採番）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    public Integer imageId;

    /**
     * アーティストID（外部キー）
     */
    @Column(name = "artist_id", nullable = false, length = 100)
    public String artistId;

    /**
     * 画像URL（フロントエンド静的ファイルのパス）
     */
    @Column(name = "image_url", nullable = false, length = 500)
    public String imageUrl;

    /**
     * 表示順（昇順で表示）
     */
    @Column(name = "display_order", nullable = false)
    public Integer displayOrder;

    /**
     * レコード作成日時
     */
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
