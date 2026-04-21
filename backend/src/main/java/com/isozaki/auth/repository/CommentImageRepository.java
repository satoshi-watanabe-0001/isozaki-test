/**
 * コメント画像リポジトリクラス
 *
 * <p>comment_imagesテーブルへのデータアクセスを提供するリポジトリ。
 * 画像のPENDING/CONFIRMED管理、コメントID紐付け、クリーンアップ処理をサポートする。</p>
 *
 * @since 1.4
 */

package com.isozaki.auth.repository;

import com.isozaki.auth.entity.CommentImageEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * コメント画像データアクセスリポジトリ
 *
 * <p>画像のCRUD操作、ステータス更新、クリーンアップ対象の検索を提供する。</p>
 *
 * @since 1.4
 */
@ApplicationScoped
public class CommentImageRepository implements PanacheRepositoryBase<CommentImageEntity, UUID> {

    /**
     * 指定コメントIDに紐づく画像一覧を取得する（CONFIRMED画像のみ）
     *
     * @param commentId コメントID
     * @return 画像エンティティのリスト
     */
    public List<CommentImageEntity> findByCommentId(UUID commentId) {
        return list("commentId = ?1 AND status = 'CONFIRMED'", commentId);
    }

    /**
     * 複数コメントIDに紐づく画像を一括取得する（N+1回避用）
     *
     * @param commentIds コメントIDのリスト
     * @return 画像エンティティのリスト
     */
    public List<CommentImageEntity> findByCommentIds(List<UUID> commentIds) {
        if (commentIds.isEmpty()) {
            return List.of();
        }
        return list("commentId IN ?1 AND status = 'CONFIRMED'", commentIds);
    }

    /**
     * 指定画像IDリストのPENDING画像を取得する（ユーザ認証付き）
     *
     * @param imageIds   画像IDのリスト
     * @param uploadedBy アップロードユーザID
     * @return PENDING状態の画像エンティティのリスト
     */
    public List<CommentImageEntity> findPendingByIdsAndUser(
            List<UUID> imageIds, UUID uploadedBy) {
        if (imageIds.isEmpty()) {
            return List.of();
        }
        return list(
                "imageId IN ?1 AND uploadedBy = ?2 AND status = 'PENDING'",
                imageIds, uploadedBy);
    }

    /**
     * クリーンアップ対象のPENDING画像を取得する
     *
     * <p>指定日時より前に作成されたPENDING状態の画像を返却する。</p>
     *
     * @param before この日時より前の画像を対象とする
     * @return クリーンアップ対象の画像エンティティのリスト
     */
    public List<CommentImageEntity> findExpiredPending(Instant before) {
        return list("status = 'PENDING' AND createdAt < ?1", before);
    }
}
