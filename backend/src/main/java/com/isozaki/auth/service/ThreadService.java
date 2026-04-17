/**
 * スレッドサービスクラス
 *
 * <p>スレッド機能のビジネスロジックを担当するサービス。
 * スレッド一覧・詳細取得、スレッド作成、コメント追加を提供する。</p>
 *
 * @since 1.3
 */

package com.isozaki.auth.service;

import com.isozaki.auth.dto.CreateCommentRequest;
import com.isozaki.auth.dto.CreateThreadRequest;
import com.isozaki.auth.dto.ThreadCommentResponse;
import com.isozaki.auth.dto.ThreadDetailResponse;
import com.isozaki.auth.dto.ThreadListItemResponse;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * スレッド機能のビジネスロジックを提供するサービス
 *
 * <p>スレッドリポジトリ、コメントリポジトリ、セッションサービスと連携して、
 * スレッド一覧・詳細・作成・コメント追加の処理を行う。</p>
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
     * <p>スレッドの最新コメント情報を含めて返却する。
     * 最新コメント日時の降順でソートされる。
     * N+1問題を回避するため、最新コメントとユーザ名を一括取得する。</p>
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

        // ページ番号は内部的に0始まりに変換
        List<ThreadEntity> threads = threadRepository.findByArtistId(
                artistId, page - 1, clampedSize);

        if (threads.isEmpty()) {
            return Optional.of(new ThreadListResponse(
                    List.of(), totalCount, page, clampedSize, totalPages));
        }

        // N+1問題対策: スレッドIDリストから最新コメントを一括取得
        List<UUID> threadIds = threads.stream()
                .map(t -> t.threadId)
                .toList();
        Map<UUID, ThreadCommentEntity> latestComments =
                threadCommentRepository.findLatestByThreadIds(threadIds);

        // N+1問題対策: 全関連ユーザIDからユーザ名を一括取得
        List<UUID> allUserIds = Stream.concat(
                threads.stream().map(t -> t.createdBy),
                latestComments.values().stream().map(c -> c.createdBy)
        ).distinct().collect(Collectors.toList());
        Map<UUID, String> usernameMap = userRepository.findUsernamesByUserIds(allUserIds);

        List<ThreadListItemResponse> items = threads.stream()
                .map(entity -> toThreadListItem(entity, latestComments, usernameMap))
                .sorted((a, b) -> {
                    // 最新コメント日時の降順ソート
                    Instant aTime = a.latestCommentAt();
                    Instant bTime = b.latestCommentAt();
                    if (aTime == null && bTime == null) {
                        return 0;
                    }
                    if (aTime == null) {
                        return 1;
                    }
                    if (bTime == null) {
                        return -1;
                    }
                    return bTime.compareTo(aTime);
                })
                .toList();

        return Optional.of(new ThreadListResponse(
                items, totalCount, page, clampedSize, totalPages));
    }

    /**
     * 指定スレッドの詳細情報を取得する
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

        ThreadEntity thread = threadRepository.findById(threadId);
        if (thread == null || !thread.artistId.equals(artistId)) {
            return Optional.empty();
        }

        long totalComments = threadCommentRepository.countByThreadId(threadId);
        int totalPages = (int) Math.ceil((double) totalComments / clampedSize);

        List<ThreadCommentEntity> comments =
                threadCommentRepository.findByThreadId(threadId, page - 1, clampedSize);

        List<ThreadCommentResponse> commentResponses = comments.stream()
                .map(this::toCommentResponse)
                .toList();

        String creatorUsername = resolveUsername(thread.createdBy);

        return Optional.of(new ThreadDetailResponse(
                thread.threadId.toString(),
                thread.title,
                creatorUsername,
                thread.createdAt,
                commentResponses,
                totalComments,
                page,
                clampedSize,
                totalPages
        ));
    }

    /**
     * 新しいスレッドを作成する
     *
     * <p>セッションIDからユーザを特定し、
     * スレッドと初回コメントを同時に作成する。
     * スレッドIDとコメントIDはUUIDv7で生成する。</p>
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

        // スレッド作成（UUIDv7、タイトルから改行を除去）
        ThreadEntity thread = new ThreadEntity();
        thread.threadId = UUID.fromString(uuidService.generateUuidV7());
        thread.artistId = artistId;
        thread.title = request.title().replaceAll("[\\r\\n]", "");
        thread.createdBy = userUuid;
        thread.createdAt = now;
        threadRepository.persist(thread);

        // 初回コメント作成（UUIDv7）
        ThreadCommentEntity comment = new ThreadCommentEntity();
        comment.commentId = UUID.fromString(uuidService.generateUuidV7());
        comment.threadId = thread.threadId;
        comment.content = request.comment();
        comment.createdBy = userUuid;
        comment.createdAt = now;
        threadCommentRepository.persist(comment);

        // 作成後の詳細を返却
        return getThreadDetail(artistId, thread.threadId, 1, 10);
    }

    /**
     * スレッドにコメントを追加する
     *
     * <p>セッションIDからユーザを特定し、指定スレッドにコメントを追加する。
     * コメントIDはUUIDv7で生成する。</p>
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

        ThreadCommentEntity comment = new ThreadCommentEntity();
        comment.commentId = UUID.fromString(uuidService.generateUuidV7());
        comment.threadId = threadId;
        comment.content = request.content();
        comment.createdBy = userUuid;
        comment.createdAt = Instant.now();
        threadCommentRepository.persist(comment);

        return Optional.of(toCommentResponse(comment));
    }

    /**
     * スレッドエンティティをスレッド一覧アイテムDTOに変換する（バッチ版）
     *
     * <p>事前に取得済みの最新コメントMap・ユーザ名Mapを参照して変換する。</p>
     *
     * @param entity          スレッドエンティティ
     * @param latestComments  スレッドID→最新コメントのMap
     * @param usernameMap     ユーザID→ユーザ名のMap
     * @return スレッド一覧アイテムDTO
     */
    private ThreadListItemResponse toThreadListItem(
            ThreadEntity entity,
            Map<UUID, ThreadCommentEntity> latestComments,
            Map<UUID, String> usernameMap) {
        String creatorUsername = usernameMap.getOrDefault(
                entity.createdBy, "不明なユーザ");

        ThreadCommentEntity latestComment = latestComments.get(entity.threadId);

        String latestCommentContent = latestComment != null
                ? latestComment.content : null;
        Instant latestCommentAt = latestComment != null
                ? latestComment.createdAt : entity.createdAt;

        return new ThreadListItemResponse(
                entity.threadId.toString(),
                entity.title,
                creatorUsername,
                latestCommentContent,
                latestCommentAt
        );
    }

    /**
     * コメントエンティティをコメントレスポンスDTOに変換する
     *
     * @param entity コメントエンティティ
     * @return コメントレスポンスDTO
     */
    private ThreadCommentResponse toCommentResponse(ThreadCommentEntity entity) {
        String username = resolveUsername(entity.createdBy);
        return new ThreadCommentResponse(
                entity.commentId.toString(),
                entity.content,
                username,
                entity.createdAt
        );
    }

    /**
     * ユーザIDからユーザ名を解決する
     *
     * <p>ユーザが見つからない場合は「不明なユーザ」を返却する。</p>
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
