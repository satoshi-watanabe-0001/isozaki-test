/**
 * スレッドコメントリポジトリクラス
 *
 * <p>thread_commentsテーブルへのデータアクセスを提供するリポジトリ。
 * スレッドIDによるコメント検索をサポートする。
 * ユーザ名はLEFT JOINで取得し、N+1問題を回避する。
 * クエリ結果はProjection DTOにマッピングする。
 * カーソルベースページングをサポートし、コメント追加時の重複取得を回避する。</p>
 *
 * @since 1.3
 */

package com.isozaki.auth.repository;

import com.isozaki.auth.dto.CommentProjection;
import com.isozaki.auth.entity.ThreadCommentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/**
 * スレッドコメントデータアクセスリポジトリ
 *
 * <p>スレッドに紐づくコメントの検索・件数取得を行う。
 * コメント一覧取得時はusersテーブルとLEFT JOINし、ユーザ名を同時に取得する。
 * カーソルベースページング（commentId基準）で重複取得を回避する。</p>
 *
 * @since 1.3
 */
@ApplicationScoped
public class ThreadCommentRepository implements PanacheRepositoryBase<ThreadCommentEntity, UUID> {

    /**
     * 指定スレッドのコメント一覧をユーザ名付きで取得する（オフセットベース）
     *
     * <p>usersテーブルとLEFT JOINし、コメント作成者のユーザ名を同時に取得する。
     * 作成日時の降順でソートし、ページングを適用する。
     * 初回取得時に使用する。</p>
     *
     * @param threadId スレッドID
     * @param page     ページ番号（0始まり）
     * @param size     1ページあたりの件数
     * @return CommentProjectionのリスト
     */
    public List<CommentProjection> findByThreadIdWithUsername(
            UUID threadId, int page, int size) {
        return getEntityManager().createQuery(
                "SELECT NEW com.isozaki.auth.dto.CommentProjection("
                + "c.commentId, c.content, COALESCE(u.username, '不明なユーザ'), c.createdAt) "
                + "FROM ThreadCommentEntity c "
                + "LEFT JOIN UserEntity u ON c.createdBy = u.userId "
                + "WHERE c.threadId = :threadId "
                + "ORDER BY c.createdAt DESC",
                CommentProjection.class)
                .setParameter("threadId", threadId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * 指定スレッドのコメント一覧をカーソルベースで取得する（commentId基準）
     *
     * <p>usersテーブルとLEFT JOINし、コメント作成者のユーザ名を同時に取得する。
     * 指定されたcommentId（UUIDv7、時間順序保証）より前のコメントを取得する。
     * 別セッションでのコメント追加による重複取得を回避する。</p>
     *
     * @param threadId       スレッドID
     * @param beforeCommentId このコメントIDより前（古い）のコメントを取得
     * @param size           取得件数
     * @return CommentProjectionのリスト
     */
    public List<CommentProjection> findByThreadIdWithUsernameBefore(
            UUID threadId, UUID beforeCommentId, int size) {
        return getEntityManager().createQuery(
                "SELECT NEW com.isozaki.auth.dto.CommentProjection("
                + "c.commentId, c.content, COALESCE(u.username, '不明なユーザ'), c.createdAt) "
                + "FROM ThreadCommentEntity c "
                + "LEFT JOIN UserEntity u ON c.createdBy = u.userId "
                + "WHERE c.threadId = :threadId AND c.commentId < :beforeCommentId "
                + "ORDER BY c.createdAt DESC",
                CommentProjection.class)
                .setParameter("threadId", threadId)
                .setParameter("beforeCommentId", beforeCommentId)
                .setMaxResults(size)
                .getResultList();
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
}
