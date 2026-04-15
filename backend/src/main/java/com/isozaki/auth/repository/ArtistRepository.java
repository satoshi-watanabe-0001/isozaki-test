/**
 * アーティストリポジトリクラス
 *
 * <p>アーティストエンティティに対するデータアクセス操作を提供する。
 * Panacheリポジトリパターンを使用してPostgreSQLへのCRUD操作を行う。</p>
 *
 * @since 1.1
 */

package com.isozaki.auth.repository;

import com.isozaki.auth.entity.ArtistEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * アーティストデータへのアクセスを提供するリポジトリ
 *
 * @since 1.1
 */
@ApplicationScoped
public class ArtistRepository implements PanacheRepositoryBase<ArtistEntity, String> {

    /**
     * 全アーティストを読み仮名の50音順で取得する
     *
     * <p>name_kanaカラムの昇順でソートすることで、50音順の並びを実現する。</p>
     *
     * @return 50音順にソートされたアーティストエンティティのリスト
     */
    public List<ArtistEntity> findAllOrderByNameKana() {
        return list("ORDER BY nameKana ASC");
    }
}
