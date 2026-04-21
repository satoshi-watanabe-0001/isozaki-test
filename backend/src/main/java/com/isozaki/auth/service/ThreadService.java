/**
 * スレッドサービスクラス
 *
 * <p>スレッド機能のビジネスロジックを担当するサービス。
 * スレッド一覧・詳細取得、スレッド作成、コメント追加を提供する。
 * スレッド一覧・コメント一覧取得時はDB JOINでユーザ名を取得し、
 * 最新コメント情報はthreadsテーブルの非正規化カラムから取得する。
 * コメント追加取得時はカーソルベースページング（commentId基準）で重複を回避する。</p>
 *
 * @since 1.3
 */

package com.isozaki.auth.service;

import com.isozaki.auth.dto.CommentProjection;
import com.isozaki.auth.dto.CreateCommentRequest;
import com.isozaki.auth.dto.CreateThreadRequest;
import com.isozaki.auth.dto.ThreadCommentResponse;
import com.isozaki.auth.dto.ThreadDetailProjection;
import com.isozaki.auth.dto.ThreadDetailResponse;
import com.isozaki.auth.dto.ThreadListItemResponse;
import com.isozaki.auth.dto.ThreadListProjection;
import com.isozaki.auth.dto.ThreadListResponse;
import com.isozaki.auth.entity.ThreadCommentEntity;
import com.isozaki.auth.entity.ThreadEntity;
import com.isozaki.auth.repository.ArtistRepository;
import com.isozaki.auth.repository.ThreadCommentRepository;
import com.isozaki.auth.repository.ThreadRepository;
import com.isozaki.auth.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * スレッド機能のビジネスロジックを提供するサービス
 *
 * <p>スレッドリポジトリ、コメントリポジトリ、セッションサービスと連携して、
 * スレッド一覧・詳細・作成・コメント追加の処理を行う。
 * 一覧・詳細取得時はJPQL JOINでユーザ名を取得し、N+1問題を回避する。
 * コメント追加取得時はカーソルベースページングで重複を回避する。</p>
 *
 * @since 1.3
 */
@ApplicationScoped
public class ThreadService {

    /** sizeパラメータの上限値 */
    private static final int MAX_PAGE_SIZE = 100;

    private final ThreadRepository threadRepository;
    private final ThreadCommentRepository threadCommentRepository;
    private final ArtistRepository artistRepository;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final UuidService uuidService;

    /**
     * 各リポジトリ・サービスを注入してスレッドサービスを初期化する
     *
     * @param threadRepository        スレッドリポジトリ
     * @param threadCommentRepository コメントリポジトリ
     * @param artistRepository        アーティストリポジトリ
     * @param sessionService          セッション管理サービス
     * @param userRepository          ユーザリポジトリ
     * @param uuidService             UUID生成サービス
     */
    @Inject
    public ThreadService(
            ThreadRepository threadRepository,
            ThreadCommentRepository threadCommentRepository,
            ArtistRepository artistRepository,
            SessionService sessionService,
            UserRepository userRepository,
            UuidService uuidService) {
        this.threadRepository = threadRepository;
        this.threadCommentRepository = threadCommentRepository;
        this.artistRepository = artistRepository;
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.uuidService = uuidService;
    }

    /**
     * 指定アーティストのスレッド一覧を取得する
     *
     * <p>JPQL JOINでusersテーブルからユーザ名を取得し、
     * 非正規化カラムから最新コメント情報を取得する。
     * 最新コメント日時の降順でソートされる（SQLで実施）。</p>
     *
     * @param artistId アーティストID
     * @param page     ページ番号（1始まり）
     * @param size     1ページあたりの件数
     * @return スレッド一覧レスポンス（アーティストが存在しない場合はOptional.empty）
     */
    public Optional<ThreadListResponse> getThreadList(String artistId, int page, int size) {
        int clampedSize = clampSize(size);

        // アーティスト存在チェック
        if (artistRepository.findById(artistId) == null) {
            return Optional.empty();
        }

        long totalCount = threadRepository.countByArtistId(artistId);
        int totalPages = (int) Math.ceil((double) totalCount / clampedSize);

        // JPQL JOINでスレッド一覧＋ユーザ名をProjection DTOとして取得
        List<ThreadListProjection> projections =
                threadRepository.findByArtistIdWithUsername(
                        artistId, page - 1, clampedSize);

        List<ThreadListItemResponse> items = projections.stream()
                .map(this::toThreadListItem)
                .toList();

        return Optional.of(new ThreadListResponse(
                items, totalCount, page, clampedSize, totalPages));
    }

    /**
     * 指定スレッドの詳細情報を取得する（オフセットベース）
     *
     * <p>JPQL JOINでスレッド作成者・コメント作成者のユーザ名を取得する。
     * 初回コメント取得時に使用する。</p>
     *
     * @param artistId アーティストID
     * @param threadId スレッドID
     * @param page     コメントページ番号（1始まり）
     * @param size     1ページあたりのコメント件数
     * @return スレッド詳細レスポンス（存在しない場合はOptional.empty）
     */
    public Optional<ThreadDetailResponse> getThreadDetail(
            String artistId, UUID threadId, int page, int size) {
        int clampedSize = clampSize(size);

        // JPQL JOINでスレッド＋ユーザ名をProjection DTOとして取得
        ThreadDetailProjection threadProjection =
                threadRepository.findByIdAndArtistIdWithUsername(
                        threadId, artistId);
        if (threadProjection == null) {
            return Optional.empty();
        }

        long totalComments = threadCommentRepository.countByThreadId(threadId);
        int totalPages = (int) Math.ceil((double) totalComments / clampedSize);

        // JPQL JOINでコメント一覧＋ユーザ名をProjection DTOとして取得
        List<CommentProjection> commentProjections =
                threadCommentRepository.findByThreadIdWithUsername(
                        threadId, page - 1, clampedSize);

        List<ThreadCommentResponse> commentResponses = commentProjections.stream()
                .map(this::toCommentResponse)
                .toList();

        return Optional.of(new ThreadDetailResponse(
                threadProjection.threadId().toString(),
                threadProjection.title(),
                threadProjection.username(),
                threadProjection.createdAt(),
                commentResponses,
                totalComments,
                page,
                clampedSize,
                totalPages
        ));
    }

    /**
     * 指定スレッドの詳細情報を取得する（カーソルベースページング）
     *
     * <p>JPQL JOINでスレッド作成者・コメント作成者のユーザ名を取得する。
     * 指定されたcommentIdより前（古い）のコメントを取得する。
     * 別セッションでのコメント追加による重複取得を回避する。</p>
     *
     * @param artistId       アーティストID
     * @param threadId       スレッドID
     * @param beforeCommentId このコメントIDより前のコメントを取得
     * @param size           取得件数
     * @return スレッド詳細レスポンス（存在しない場合はOptional.empty）
     */
    public Optional<ThreadDetailResponse> getThreadDetailBefore(
            String artistId, UUID threadId, UUID beforeCommentId, int size) {
        int clampedSize = clampSize(size);

        // JPQL JOINでスレッド＋ユーザ名をProjection DTOとして取得
        ThreadDetailProjection threadProjection =
                threadRepository.findByIdAndArtistIdWithUsername(
                        threadId, artistId);
        if (threadProjection == null) {
            return Optional.empty();
        }

        long totalComments = threadCommentRepository.countByThreadId(threadId);

        // カーソルベースでコメントを取得
        List<CommentProjection> commentProjections =
                threadCommentRepository.findByThreadIdWithUsernameBefore(
                        threadId, beforeCommentId, clampedSize);

        List<ThreadCommentResponse> commentResponses = commentProjections.stream()
                .map(this::toCommentResponse)
                .toList();

        return Optional.of(new ThreadDetailResponse(
                threadProjection.threadId().toString(),
                threadProjection.title(),
                threadProjection.username(),
                threadProjection.createdAt(),
                commentResponses,
                totalComments,
                0,
                clampedSize,
                0
        ));
    }

    /**
     * 新しいスレッドを作成する
     *
     * <p>セッションIDからユーザを特定し、
     * スレッドと初回コメントを同時に作成する。
     * スレッドIDとコメントIDはUUIDv7で生成する。
     * 非正規化カラム（latestCommentContent, latestCommentAt）も同時に設定する。</p>
     *
     * @param artistId アーティストID
     * @param request  スレッド作成リクエスト
     * @return 作成されたスレッドの詳細レスポンス
     *         （認証失敗・アーティスト不在時はOptional.empty）
     */
    @Transactional
    public Optional<ThreadDetailResponse> createThread(
            String artistId, CreateThreadRequest request) {
        // アーティスト存在チェック
        if (artistRepository.findById(artistId) == null) {
            return Optional.empty();
        }

        // セッション認証
        String userId = sessionService.getUserIdBySession(request.sessionId());
        if (userId == null) {
            return Optional.empty();
        }

        UUID userUuid = UUID.fromString(userId);
        Instant now = Instant.now();
        String cleanTitle = request.title().replaceAll("[\\r\\n]", "");

        // スレッド作成（UUIDv7、非正規化カラム含む）
        ThreadEntity thread = new ThreadEntity();
        thread.threadId = UUID.fromString(uuidService.generateUuidV7());
        thread.artistId = artistId;
        thread.title = cleanTitle;
        thread.createdBy = userUuid;
        thread.createdAt = now;
        thread.latestCommentContent = request.comment();
        thread.latestCommentAt = now;
        threadRepository.persist(thread);

        // 初回コメント作成（UUIDv7）
        ThreadCommentEntity comment = new ThreadCommentEntity();
        comment.commentId = UUID.fromString(uuidService.generateUuidV7());
        comment.threadId = thread.threadId;
        comment.content = request.comment();
        comment.createdBy = userUuid;
        comment.createdAt = now;
        threadCommentRepository.persist(comment);

        // ユーザ名を取得してレスポンスを直接構築
        String username = resolveUsername(userUuid);

        ThreadCommentResponse commentResponse = new ThreadCommentResponse(
                comment.commentId.toString(),
                comment.content,
                username,
                now
        );

        return Optional.of(new ThreadDetailResponse(
                thread.threadId.toString(),
                cleanTitle,
                username,
                now,
                List.of(commentResponse),
                1L, 1, 10, 1
        ));
    }

    /**
     * スレッドにコメントを追加する
     *
     * <p>セッションIDからユーザを特定し、指定スレッドにコメントを追加する。
     * コメントIDはUUIDv7で生成する。
     * 非正規化カラム（latestCommentContent, latestCommentAt）を同時に更新する。</p>
     *
     * @param artistId アーティストID
     * @param threadId スレッドID
     * @param request  コメント追加リクエスト
     * @return 追加されたコメントのレスポンス（認証失敗・スレッド不在時はOptional.empty）
     */
    @Transactional
    public Optional<ThreadCommentResponse> addComment(
            String artistId, UUID threadId, CreateCommentRequest request) {
        // スレッド存在・アーティスト整合チェック
        ThreadEntity thread = threadRepository.findById(threadId);
        if (thread == null || !thread.artistId.equals(artistId)) {
            return Optional.empty();
        }

        // セッション認証
        String userId = sessionService.getUserIdBySession(request.sessionId());
        if (userId == null) {
            return Optional.empty();
        }

        UUID userUuid = UUID.fromString(userId);
        Instant now = Instant.now();

        // コメント作成（UUIDv7）
        ThreadCommentEntity comment = new ThreadCommentEntity();
        comment.commentId = UUID.fromString(uuidService.generateUuidV7());
        comment.threadId = threadId;
        comment.content = request.content();
        comment.createdBy = userUuid;
        comment.createdAt = now;
        threadCommentRepository.persist(comment);

        // 非正規化: スレッドの最新コメント情報を更新
        thread.latestCommentContent = request.content();
        thread.latestCommentAt = now;
        threadRepository.persist(thread);

        // ユーザ名を取得してレスポンスを構築
        String username = resolveUsername(userUuid);

        return Optional.of(new ThreadCommentResponse(
                comment.commentId.toString(),
                comment.content,
                username,
                now
        ));
    }

    /**
     * ThreadListProjectionをスレッド一覧アイテムDTOに変換する
     *
     * @param projection JPQL JOIN結果のプロジェクション
     * @return スレッド一覧アイテムDTO
     */
    private ThreadListItemResponse toThreadListItem(ThreadListProjection projection) {
        return new ThreadListItemResponse(
                projection.threadId().toString(),
                projection.title(),
                projection.username(),
                projection.latestCommentContent(),
                projection.latestCommentAt() != null
                        ? projection.latestCommentAt()
                        : projection.createdAt()
        );
    }

    /**
     * CommentProjectionをコメントレスポンスDTOに変換する
     *
     * @param projection JPQL JOIN結果のプロジェクション
     * @return コメントレスポンスDTO
     */
    private ThreadCommentResponse toCommentResponse(CommentProjection projection) {
        return new ThreadCommentResponse(
                projection.commentId().toString(),
                projection.content(),
                projection.username(),
                projection.createdAt()
        );
    }

    /**
     * ユーザIDからユーザ名を解決する（単一ユーザ用）
     *
     * <p>書き込み操作（スレッド作成・コメント追加）後のレスポンス構築に使用する。
     * ユーザが見つからない場合は「不明なユーザ」を返却する。</p>
     *
     * @param userId ユーザID
     * @return ユーザ名
     */
    private String resolveUsername(UUID userId) {
        return userRepository.findByUserId(userId)
                .map(user -> user.username)
                .orElse("不明なユーザ");
    }

    /**
     * sizeパラメータを上限値で制限する
     *
     * <p>クライアントから巨大なsize値が指定された場合に
     * 大量のデータ返却を防ぐため、上限値を適用する。</p>
     *
     * @param size リクエストされたsize値
     * @return 上限値で制限されたsize値
     */
    private int clampSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
