/**
 * スレッドリポジトリクラス
 *
 * <p>threadsテーブルへのデータアクセスを提供するリポジトリ。
 * アーティストIDによるスレッド検索をサポートする。</p>
 *
 * @since 1.3
 */

package com.isozaki.auth.repository;

import com.isozaki.auth.entity.ThreadEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * スレッドデータアクセスリポジトリ
 *
 * <p>アーティストに紐づくスレッドの検索・件数取得を行う。
 * 最新コメント日時の降順ソートはサービス層で実施する。</p>
 *
 * @since 1.3
 */
@ApplicationScoped
public class ThreadRepository implements PanacheRepositoryBase<ThreadEntity, Integer> {

    /**
     * 指定アーティストのスレッド一覧を取得する（作成日時降順）
     *
     * @param artistId アーティストID
     * @param page     ページ番号（0始まり）
     * @param size     1ページあたりの件数
     * @return スレッドエンティティのリスト
     */
    public List<ThreadEntity> findByArtistId(String artistId, int page, int size) {
        return find("artistId", Sort.by("createdAt").descending(), artistId)
                .page(page, size)
                .list();
    }

    /**
     * 指定アーティストのスレッド総数を取得する
     *
     * @param artistId アーティストID
     * @return スレッド件数
     */
    public long countByArtistId(String artistId) {
        return count("artistId", artistId);
    }
}
