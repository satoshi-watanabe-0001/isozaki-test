/**
 * お知らせエンティティクラス
 *
 * <p>アーティストに紐づくお知らせ情報をPostgreSQLに永続化するためのJPAエンティティ。
 * お知らせのタイトル、公開日時を管理する。</p>
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
 * お知らせ情報を保持するエンティティ
 *
 * <p>お知らせタイトル、公開日時を管理する。</p>
 *
 * @since 1.2
 */
@Entity
@Table(name = "news")
public class NewsEntity extends PanacheEntityBase {

    /**
     * お知らせID（自動採番）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    public Integer newsId;

    /**
     * アーティストID（外部キー）
     */
    @Column(name = "artist_id", nullable = false, length = 100)
    public String artistId;

    /**
     * お知らせタイトル
     */
    @Column(name = "title", nullable = false, length = 500)
    public String title;

    /**
     * 公開日時（新着順ソートに使用）
     */
    @Column(name = "published_at", nullable = false)
    public Instant publishedAt;

    /**
     * レコード作成日時
     */
    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
