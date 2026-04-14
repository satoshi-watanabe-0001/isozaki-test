/**
 * アーティストエンティティクラス
 *
 * <p>アーティスト情報をPostgreSQLに永続化するためのJPAエンティティ。
 * UUIDv7を主キーとし、アーティスト名の一意性を保証する。</p>
 *
 * @since 1.1
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
 * アーティスト情報を保持するエンティティ
 *
 * <p>アーティストID（UUIDv7）、アーティスト名（日本語対応）、
 * ソート用読み仮名（ひらがな）を管理する。</p>
 *
 * @since 1.1
 */
@Entity
@Table(name = "artists")
public class ArtistEntity extends PanacheEntityBase {

    /**
     * アーティストID（UUIDv7形式）
     */
    @Id
    @Column(name = "artist_id", nullable = false, columnDefinition = "uuid")
    public UUID artistId;

    /**
     * アーティスト名（日本語入力可）
     */
    @Column(name = "name", length = 255, nullable = false, unique = true)
    public String name;

    /**
     * ソート用読み仮名（ひらがな、50音順ソートに使用）
     */
    @Column(name = "name_kana", length = 255, nullable = false)
    public String nameKana;

    /**
     * アイコン画像のURL
     */
    @Column(name = "icon_url", length = 500)
    public String iconUrl;

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
