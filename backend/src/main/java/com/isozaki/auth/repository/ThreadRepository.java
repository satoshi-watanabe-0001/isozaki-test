/**
 * スレッドリポジトリクラス
 *
 * <p>threadsテーブルへのデータアクセスを提供するリポジトリ。
 * アーティストIDによるスレッド検索をサポートする。
 * ユーザ名はLEFT JOINで取得し、N+1問題を回避する。
 * クエリ結果はProjection DTOにマッピングする。</p>
 *
 * @since 1.3
 */

package com.isozaki.auth.repository;

import com.isozaki.auth.dto.ThreadDetailProjection;
import com.isozaki.auth.dto.ThreadListProjection;
import com.isozaki.auth.entity.ThreadEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/**
 * スレッドデータアクセスリポジトリ
 *
 * <p>アーティストに紐づくスレッドの検索・件数取得を行う。
 * スレッド一覧取得時はusersテーブルとLEFT JOINし、ユーザ名を同時に取得する。
 * 最新コメント情報はthreadsテーブルの非正規化カラムから取得する。
 * クエリ結果はSELECT NEWでProjection DTOに直接マッピングする。</p>
 *
 * @since 1.3
 */
@ApplicationScoped
public class ThreadRepository implements PanacheRepositoryBase<ThreadEntity, UUID> {

    /**
     * 指定アーティストのスレッド一覧をユーザ名付きで取得する（JPQL JOIN）
     *
     * <p>usersテーブルとLEFT JOINし、スレッド作成者のユーザ名を同時に取得する。
     * 非正規化カラム（latestCommentContent, latestCommentAt）から最新コメント情報を取得する。
     * 最新コメント日時の降順（コメントなしは作成日時で代替）でソートする。
     * 結果はThreadListProjection DTOに直接マッピングされる。</p>
     *
     * @param artistId アーティストID
     * @param page     ページ番号（0始まり）
     * @param size     1ページあたりの件数
     * @return ThreadListProjectionのリスト
     */
    public List<ThreadListProjection> findByArtistIdWithUsername(
            String artistId, int page, int size) {
        return getEntityManager().createQuery(
                "SELECT NEW com.isozaki.auth.dto.ThreadListProjection("
                + "t.threadId, t.title, COALESCE(u.username, '不明なユーザ'), "
                + "t.latestCommentContent, t.latestCommentAt, t.createdAt) "
                + "FROM ThreadEntity t "
                + "LEFT JOIN UserEntity u ON t.createdBy = u.userId "
                + "WHERE t.artistId = :artistId "
                + "ORDER BY COALESCE(t.latestCommentAt, t.createdAt) DESC",
                ThreadListProjection.class)
                .setParameter("artistId", artistId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * 指定スレッドの詳細情報をユーザ名付きで取得する（JPQL JOIN）
     *
     * <p>usersテーブルとLEFT JOINし、スレッド作成者のユーザ名を同時に取得する。
     * アーティストIDの整合性チェックも同時に行う。
     * 結果はThreadDetailProjection DTOに直接マッピングされる。</p>
     *
     * @param threadId スレッドID
     * @param artistId アーティストID
     * @return ThreadDetailProjection（存在しない場合はnull）
     */
    public ThreadDetailProjection findByIdAndArtistIdWithUsername(
            UUID threadId, String artistId) {
        List<ThreadDetailProjection> results = getEntityManager().createQuery(
                "SELECT NEW com.isozaki.auth.dto.ThreadDetailProjection("
                + "t.threadId, t.title, COALESCE(u.username, '不明なユーザ'), t.createdAt) "
                + "FROM ThreadEntity t "
                + "LEFT JOIN UserEntity u ON t.createdBy = u.userId "
                + "WHERE t.threadId = :threadId AND t.artistId = :artistId",
                ThreadDetailProjection.class)
                .setParameter("threadId", threadId)
                .setParameter("artistId", artistId)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
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
