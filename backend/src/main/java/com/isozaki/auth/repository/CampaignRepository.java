/**
 * キャンペーンリポジトリクラス
 *
 * <p>キャンペーンエンティティに対するデータアクセス操作を提供する。
 * Panacheリポジトリパターンを使用してPostgreSQLへのCRUD操作を行う。</p>
 *
 * @since 1.2
 */

package com.isozaki.auth.repository;

import com.isozaki.auth.entity.CampaignEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * キャンペーンデータへのアクセスを提供するリポジトリ
 *
 * @since 1.2
 */
@ApplicationScoped
public class CampaignRepository implements PanacheRepositoryBase<CampaignEntity, Integer> {

    /**
     * 指定アーティストのキャンペーンを表示順で取得する（最大3件）
     *
     * <p>display_orderカラムの昇順でソートし、先頭3件を返却する。</p>
     *
     * @param artistId アーティストID
     * @return 表示順にソートされたキャンペーンエンティティのリスト（最大3件）
     */
    public List<CampaignEntity> findByArtistIdOrderByDisplayOrder(String artistId) {
        return find("artistId = ?1 ORDER BY displayOrder ASC", artistId)
                .page(0, 3)
                .list();
    }
}
