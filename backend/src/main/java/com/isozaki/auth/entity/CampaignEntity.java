/**
 * キャンペーンエンティティクラス
 *
 * <p>アーティストに紐づくキャンペーン情報をPostgreSQLに永続化するためのJPAエンティティ。
 * キャンペーンのタイトル、画像URL、表示順を管理する。</p>
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
 * キャンペーン情報を保持するエンティティ
 *
 * <p>キャンペーンタイトル、画像URL、表示順を管理する。</p>
 *
 * @since 1.2
 */
@Entity
@Table(name = "campaigns")
public class CampaignEntity extends PanacheEntityBase {

    /**
     * キャンペーンID（自動採番）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_id")
    public Integer campaignId;

    /**
     * アーティストID（外部キー）
     */
    @Column(name = "artist_id", nullable = false, length = 100)
    public String artistId;

    /**
     * キャンペーンタイトル
     */
    @Column(name = "title", nullable = false, length = 255)
    public String title;

    /**
     * キャンペーン画像URL（フロントエンド静的ファイルのパス）
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
