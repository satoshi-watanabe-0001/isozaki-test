/**
 * アーティストエンティティクラス
 *
 * <p>アーティスト情報をPostgreSQLに永続化するためのJPAエンティティ。
 * アーティストの英名を主キーとし、URLパスとしても利用可能にする。</p>
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

/**
 * アーティスト情報を保持するエンティティ
 *
 * <p>アーティストID（英名文字列）、アーティスト名（日本語対応）、
 * ソート用読み仮名（ひらがな）を管理する。</p>
 *
 * @since 1.1
 */
@Entity
@Table(name = "artists")
public class ArtistEntity extends PanacheEntityBase {

    /**
     * アーティストID（英名文字列、URLパスとして利用可能）
     */
    @Id
    @Column(name = "artist_id", nullable = false, length = 100)
    public String artistId;

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
     * アイコン画像のURL（フロントエンド静的ファイルのパス）
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
