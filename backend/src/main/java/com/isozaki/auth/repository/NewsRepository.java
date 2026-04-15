/**
 * お知らせリポジトリクラス
 *
 * <p>お知らせエンティティに対するデータアクセス操作を提供する。
 * Panacheリポジトリパターンを使用してPostgreSQLへのCRUD操作を行う。</p>
 *
 * @since 1.2
 */

package com.isozaki.auth.repository;

import com.isozaki.auth.entity.NewsEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * お知らせデータへのアクセスを提供するリポジトリ
 *
 * @since 1.2
 */
@ApplicationScoped
public class NewsRepository implements PanacheRepositoryBase<NewsEntity, Integer> {

    /**
     * 指定アーティストのお知らせを新着順で取得する（最大5件）
     *
     * <p>published_atカラムの降順でソートし、先頭5件を返却する。</p>
     *
     * @param artistId アーティストID
     * @return 新着順にソートされたお知らせエンティティのリスト（最大5件）
     */
    public List<NewsEntity> findByArtistIdOrderByPublishedAtDesc(String artistId) {
        return find("artistId = ?1 ORDER BY publishedAt DESC", artistId)
                .page(0, 5)
                .list();
    }
}
