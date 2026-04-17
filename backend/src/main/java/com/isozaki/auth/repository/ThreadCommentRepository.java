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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * スレッドコメントデータアクセスリポジトリ
 *
 * <p>スレッドに紐づくコメントの検索・件数取得・最新コメント取得を行う。</p>
 *
 * @since 1.3
 */
@ApplicationScoped
public class ThreadCommentRepository implements PanacheRepositoryBase<ThreadCommentEntity, UUID> {

    /**
     * 指定スレッドのコメント一覧を取得する（作成日時降順、ページング）
     *
     * @param threadId スレッドID
     * @param page     ページ番号（0始まり）
     * @param size     1ページあたりの件数
     * @return コメントエンティティのリスト
     */
    public List<ThreadCommentEntity> findByThreadId(UUID threadId, int page, int size) {
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
    public long countByThreadId(UUID threadId) {
        return count("threadId", threadId);
    }

    /**
     * 指定スレッドの最新コメントを取得する
     *
     * @param threadId スレッドID
     * @return 最新コメント（存在しない場合はOptional.empty）
     */
    public Optional<ThreadCommentEntity> findLatestByThreadId(UUID threadId) {
        return find("threadId", Sort.by("createdAt").descending(), threadId)
                .firstResultOptional();
    }

    /**
     * 複数スレッドの最新コメントを一括取得する（N+1問題対策）
     *
     * <p>各スレッドIDに対して最新のコメント1件をまとめて取得し、
     * スレッドID→最新コメントのMapとして返却する。</p>
     *
     * @param threadIds スレッドIDのリスト
     * @return スレッドIDをキー、最新コメントを値とするMap
     */
    public Map<UUID, ThreadCommentEntity> findLatestByThreadIds(List<UUID> threadIds) {
        if (threadIds.isEmpty()) {
            return Map.of();
        }
        // 対象スレッドIDに紐づくコメントを作成日時降順で全件取得し、
        // スレッドIDごとに最新の1件だけを抽出する
        List<ThreadCommentEntity> allComments = list(
                "threadId in ?1 order by createdAt desc", threadIds);
        return allComments.stream()
                .collect(Collectors.toMap(
                        c -> c.threadId,
                        c -> c,
                        (existing, replacement) -> existing));
    }
}
