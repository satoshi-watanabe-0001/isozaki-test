/**
 * スレッドコメントリポジトリクラス
 *
 * <p>thread_commentsテーブルへのデータアクセスを提供するリポジトリ。
 * スレッドIDによるコメント検索をサポートする。</p>
 *
 * @since 1.3
 */

package com.isozaki.auth.repository;

import com.isozaki.auth.entity.ThreadCommentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

/**
 * スレッドコメントデータアクセスリポジトリ
 *
 * <p>スレッドに紐づくコメントの検索・件数取得・最新コメント取得を行う。</p>
 *
 * @since 1.3
 */
@ApplicationScoped
public class ThreadCommentRepository implements PanacheRepositoryBase<ThreadCommentEntity, Integer> {

    /**
     * 指定スレッドのコメント一覧を取得する（作成日時降順、ページング）
     *
     * @param threadId スレッドID
     * @param page     ページ番号（0始まり）
     * @param size     1ページあたりの件数
     * @return コメントエンティティのリスト
     */
    public List<ThreadCommentEntity> findByThreadId(int threadId, int page, int size) {
        return find("threadId", Sort.by("createdAt").descending(), threadId)
                .page(page, size)
                .list();
    }

    /**
     * 指定スレッドのコメント総数を取得する
     *
     * @param threadId スレッドID
     * @return コメント件数
     */
    public long countByThreadId(int threadId) {
        return count("threadId", threadId);
    }

    /**
     * 指定スレッドの最新コメントを取得する
     *
     * @param threadId スレッドID
     * @return 最新コメント（存在しない場合はOptional.empty）
     */
    public Optional<ThreadCommentEntity> findLatestByThreadId(int threadId) {
        return find("threadId", Sort.by("createdAt").descending(), threadId)
                .firstResultOptional();
    }
}
