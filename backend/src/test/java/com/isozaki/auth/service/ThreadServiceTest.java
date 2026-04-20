/**
 * ThreadServiceの単体テスト
 *
 * <p>スレッド機能のビジネスロジックをテストする。
 * 各リポジトリとSessionServiceはモックを使用する。
 * スレッド一覧・コメント一覧はProjection DTOをモックで再現する。</p>
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
import com.isozaki.auth.dto.ThreadListProjection;
import com.isozaki.auth.dto.ThreadListResponse;
import com.isozaki.auth.entity.ArtistEntity;
import com.isozaki.auth.entity.ThreadCommentEntity;
import com.isozaki.auth.entity.ThreadEntity;
import com.isozaki.auth.entity.UserEntity;
import com.isozaki.auth.repository.ArtistRepository;
import com.isozaki.auth.repository.ThreadCommentRepository;
import com.isozaki.auth.repository.ThreadRepository;
import com.isozaki.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ThreadServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ThreadService テスト")
class ThreadServiceTest {

    @Mock
    private ThreadRepository threadRepository;

    @Mock
    private ThreadCommentRepository threadCommentRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private SessionService sessionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UuidService uuidService;

    private ThreadService threadService;

    private static final UUID USER_UUID_1 =
            UUID.fromString("01908b7e-1234-7000-8000-000000000001");
    private static final UUID USER_UUID_2 =
            UUID.fromString("01908b7e-1234-7000-8000-000000000002");
    private static final UUID THREAD_UUID_1 =
            UUID.fromString("01970000-1000-7000-8000-000000000001");
    private static final UUID THREAD_UUID_2 =
            UUID.fromString("01970000-1000-7000-8000-000000000002");
    private static final UUID COMMENT_UUID_1 =
            UUID.fromString("01970000-2000-7000-8000-000000000001");
    private static final UUID COMMENT_UUID_2 =
            UUID.fromString("01970000-2000-7000-8000-000000000002");
    private static final UUID COMMENT_UUID_50 =
            UUID.fromString("01970000-2000-7000-8000-000000000050");
    private static final String VALID_SESSION = "valid-session";
    private static final String INVALID_SESSION = "invalid-session";

    @BeforeEach
    void setUp() {
        threadService = new ThreadService(
                threadRepository,
                threadCommentRepository,
                artistRepository,
                sessionService,
                userRepository,
                uuidService);
    }

    /**
     * テスト用のThreadEntityを生成するヘルパー
     */
    private ThreadEntity createThreadEntity(
            UUID id, String artistId, String title,
            UUID createdBy, Instant createdAt) {
        ThreadEntity entity = new ThreadEntity();
        entity.threadId = id;
        entity.artistId = artistId;
        entity.title = title;
        entity.createdBy = createdBy;
        entity.createdAt = createdAt;
        return entity;
    }

    /**
     * テスト用のUserEntityを生成するヘルパー
     */
    private UserEntity createUserEntity(UUID userId, String username) {
        UserEntity user = new UserEntity();
        user.userId = userId;
        user.username = username;
        user.email = username + "@example.com";
        user.passwordHash = "hash";
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }

    // ========== getThreadList テスト ==========

    /**
     * 【テスト対象】ThreadService#getThreadList
     * 【テストケース】アーティストが存在しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】スレッド一覧取得 - アーティスト不在
     */
    @Test
    @DisplayName("スレッド一覧: アーティスト不在時、emptyが返される")
    void shouldReturnEmptyWhenArtistNotFoundForList() {
        when(artistRepository.findById("unknown")).thenReturn(null);

        Optional<ThreadListResponse> result =
                threadService.getThreadList("unknown", 1, 20);

        assertFalse(result.isPresent());
        verify(artistRepository).findById("unknown");
    }

    /**
     * 【テスト対象】ThreadService#getThreadList
     * 【テストケース】アーティスト存在時、スレッド一覧が返却される
     * 【期待結果】スレッド一覧がページング情報と共に返却される
     * 【ビジネス要件】スレッド一覧取得 - 正常系（Projection DTO）
     */
    @Test
    @DisplayName("スレッド一覧: 正常系、Projection DTOで一覧が返される")
    void shouldReturnThreadListWhenArtistExists() {
        Instant now = Instant.now();
        Instant earlier = now.minusSeconds(3600);

        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(threadRepository.countByArtistId("aimyon"))
                .thenReturn(2L);

        // Projection DTOのモック（ソート済み: latestCommentAtの降順）
        ThreadListProjection proj1 = new ThreadListProjection(
                THREAD_UUID_1, "スレッド1", "テストユーザ1",
                "最新コメント1", now, earlier);
        ThreadListProjection proj2 = new ThreadListProjection(
                THREAD_UUID_2, "スレッド2", "テストユーザ2",
                null, null, now);
        when(threadRepository.findByArtistIdWithUsername("aimyon", 0, 20))
                .thenReturn(List.of(proj1, proj2));

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 20);

        assertTrue(result.isPresent());
        ThreadListResponse response = result.get();
        assertEquals(2L, response.totalCount());
        assertEquals(1, response.page());
        assertEquals(20, response.size());
        assertEquals(2, response.threads().size());
        // Projection DTOの結果順（latestCommentAt降順）で返却
        assertEquals("スレッド1", response.threads().get(0).title());
        assertEquals(
                "テストユーザ1",
                response.threads().get(0).createdByUsername());
        assertEquals(
                "最新コメント1",
                response.threads().get(0).latestComment());
        // スレッドIDがUUID文字列で返却される
        assertEquals(
                THREAD_UUID_1.toString(),
                response.threads().get(0).threadId());
    }

    /**
     * 【テスト対象】ThreadService#getThreadList
     * 【テストケース】スレッドが0件の場合
     * 【期待結果】空のスレッド一覧が返却される
     * 【ビジネス要件】スレッド一覧取得 - 0件
     */
    @Test
    @DisplayName("スレッド一覧: 0件時、空一覧が返される")
    void shouldReturnEmptyListWhenNoThreads() {
        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(threadRepository.countByArtistId("aimyon"))
                .thenReturn(0L);
        when(threadRepository.findByArtistIdWithUsername("aimyon", 0, 20))
                .thenReturn(List.of());

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 20);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().threads().size());
        assertEquals(0L, result.get().totalCount());
    }

    /**
     * 【テスト対象】ThreadService#getThreadList
     * 【テストケース】sizeパラメータが上限値を超える場合
     * 【期待結果】上限値100で制限される
     * 【ビジネス要件】スレッド一覧取得 - size上限制限
     */
    @Test
    @DisplayName("スレッド一覧: sizeが上限100で制限される")
    void shouldClampSizeToMaxWhenExceeded() {
        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(threadRepository.countByArtistId("aimyon"))
                .thenReturn(0L);
        when(threadRepository.findByArtistIdWithUsername("aimyon", 0, 100))
                .thenReturn(List.of());

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 9999);

        assertTrue(result.isPresent());
        assertEquals(100, result.get().size());
        verify(threadRepository).findByArtistIdWithUsername("aimyon", 0, 100);
    }

    /**
     * 【テスト対象】ThreadService#getThreadList
     * 【テストケース】sizeパラメータが0以下の場合
     * 【期待結果】デフォルト値20が使用される
     * 【ビジネス要件】スレッド一覧取得 - size不正値
     */
    @Test
    @DisplayName("スレッド一覧: sizeが0以下時、デフォルト20が使用される")
    void shouldUseDefaultSizeWhenZeroOrNegative() {
        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(threadRepository.countByArtistId("aimyon"))
                .thenReturn(0L);
        when(threadRepository.findByArtistIdWithUsername("aimyon", 0, 20))
                .thenReturn(List.of());

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 0);

        assertTrue(result.isPresent());
        assertEquals(20, result.get().size());
        verify(threadRepository).findByArtistIdWithUsername("aimyon", 0, 20);
    }

    // ========== getThreadDetail テスト ==========

    /**
     * 【テスト対象】ThreadService#getThreadDetail
     * 【テストケース】スレッドが存在しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】スレッド詳細取得 - スレッド不在
     */
    @Test
    @DisplayName("スレッド詳細: スレッド不在時、emptyが返される")
    void shouldReturnEmptyWhenThreadNotFound() {
        UUID unknownThread = UUID.fromString(
                "01970000-1000-7000-8000-ffffffffffff");
        when(threadRepository.findByIdAndArtistIdWithUsername(
                unknownThread, "aimyon")).thenReturn(null);

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetail(
                        "aimyon", unknownThread, 1, 10);

        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#getThreadDetail
     * 【テストケース】アーティストIDが一致しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】スレッド詳細取得 - アーティスト不一致
     */
    @Test
    @DisplayName("スレッド詳細: アーティストID不一致時、emptyが返される")
    void shouldReturnEmptyWhenArtistMismatch() {
        when(threadRepository.findByIdAndArtistIdWithUsername(
                THREAD_UUID_1, "different-artist")).thenReturn(null);

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetail(
                        "different-artist", THREAD_UUID_1, 1, 10);

        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#getThreadDetail
     * 【テストケース】正常系、スレッド詳細が返却される
     * 【期待結果】スレッド詳細とコメントが返却される
     * 【ビジネス要件】スレッド詳細取得 - 正常系（Projection DTO）
     */
    @Test
    @DisplayName("スレッド詳細: 正常系、Projection DTOで詳細が返される")
    void shouldReturnThreadDetailWhenExists() {
        Instant now = Instant.now();

        // Projection DTOでスレッド＋ユーザ名を取得
        ThreadDetailProjection threadProj = new ThreadDetailProjection(
                THREAD_UUID_1, "テストスレッド", "ユーザ1", now);
        when(threadRepository.findByIdAndArtistIdWithUsername(
                THREAD_UUID_1, "aimyon")).thenReturn(threadProj);

        when(threadCommentRepository.countByThreadId(THREAD_UUID_1))
                .thenReturn(2L);

        // Projection DTOでコメント＋ユーザ名を取得
        CommentProjection commentProj1 = new CommentProjection(
                COMMENT_UUID_1, "コメント1", "ユーザ1", now);
        CommentProjection commentProj2 = new CommentProjection(
                COMMENT_UUID_2, "コメント2\n改行あり", "ユーザ2",
                now.minusSeconds(60));
        when(threadCommentRepository.findByThreadIdWithUsername(
                THREAD_UUID_1, 0, 10))
                .thenReturn(List.of(commentProj1, commentProj2));

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetail(
                        "aimyon", THREAD_UUID_1, 1, 10);

        assertTrue(result.isPresent());
        ThreadDetailResponse detail = result.get();
        assertEquals(THREAD_UUID_1.toString(), detail.threadId());
        assertEquals("テストスレッド", detail.title());
        assertEquals("ユーザ1", detail.createdByUsername());
        assertEquals(2L, detail.totalComments());
        assertEquals(2, detail.comments().size());
        assertEquals("コメント1", detail.comments().get(0).content());
        assertEquals("ユーザ1",
                detail.comments().get(0).createdByUsername());
        assertEquals("コメント2\n改行あり",
                detail.comments().get(1).content());
        // コメントIDがUUID文字列で返却される
        assertEquals(COMMENT_UUID_1.toString(),
                detail.comments().get(0).commentId());
    }

    /**
     * 【テスト対象】ThreadService#getThreadDetail
     * 【テストケース】ユーザが見つからない場合（LEFT JOIN）
     * 【期待結果】「不明なユーザ」として返却される
     * 【ビジネス要件】スレッド詳細取得 - ユーザ不在（LEFT JOIN COALESCE）
     */
    @Test
    @DisplayName("スレッド詳細: ユーザ不在時、不明なユーザと表示される")
    void shouldReturnUnknownUsernameWhenUserNotFound() {
        Instant now = Instant.now();

        // LEFT JOIN + COALESCEによりユーザ不在時は「不明なユーザ」が返る
        ThreadDetailProjection threadProj = new ThreadDetailProjection(
                THREAD_UUID_1, "テスト", "不明なユーザ", now);
        when(threadRepository.findByIdAndArtistIdWithUsername(
                THREAD_UUID_1, "aimyon")).thenReturn(threadProj);

        when(threadCommentRepository.countByThreadId(THREAD_UUID_1))
                .thenReturn(0L);
        when(threadCommentRepository.findByThreadIdWithUsername(
                THREAD_UUID_1, 0, 10))
                .thenReturn(List.of());

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetail(
                        "aimyon", THREAD_UUID_1, 1, 10);

        assertTrue(result.isPresent());
        assertEquals("不明なユーザ", result.get().createdByUsername());
    }

    // ========== getThreadDetailBefore テスト（カーソルベースページング） ==========

    /**
     * 【テスト対象】ThreadService#getThreadDetailBefore
     * 【テストケース】スレッドが存在しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】カーソルベースページング - スレッド不在
     */
    @Test
    @DisplayName("カーソルページング: スレッド不在時、emptyが返される")
    void shouldReturnEmptyWhenThreadNotFoundForCursor() {
        UUID unknownThread = UUID.fromString(
                "01970000-1000-7000-8000-ffffffffffff");
        when(threadRepository.findByIdAndArtistIdWithUsername(
                unknownThread, "aimyon")).thenReturn(null);

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetailBefore(
                        "aimyon", unknownThread, COMMENT_UUID_1, 10);

        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#getThreadDetailBefore
     * 【テストケース】正常系、カーソルより前のコメントが返却される
     * 【期待結果】指定commentIdより前のコメントが返却される
     * 【ビジネス要件】カーソルベースページング - 正常系
     */
    @Test
    @DisplayName("カーソルページング: 正常系、カーソルより前のコメントが返される")
    void shouldReturnCommentsBeforeCursor() {
        Instant now = Instant.now();

        ThreadDetailProjection threadProj = new ThreadDetailProjection(
                THREAD_UUID_1, "テストスレッド", "ユーザ1", now);
        when(threadRepository.findByIdAndArtistIdWithUsername(
                THREAD_UUID_1, "aimyon")).thenReturn(threadProj);

        when(threadCommentRepository.countByThreadId(THREAD_UUID_1))
                .thenReturn(15L);

        // カーソル（COMMENT_UUID_2）より前のコメントを返す
        CommentProjection olderComment = new CommentProjection(
                COMMENT_UUID_1, "古いコメント", "ユーザ2",
                now.minusSeconds(120));
        when(threadCommentRepository.findByThreadIdWithUsernameBefore(
                THREAD_UUID_1, COMMENT_UUID_2, 10))
                .thenReturn(List.of(olderComment));

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetailBefore(
                        "aimyon", THREAD_UUID_1, COMMENT_UUID_2, 10);

        assertTrue(result.isPresent());
        ThreadDetailResponse detail = result.get();
        assertEquals(1, detail.comments().size());
        assertEquals("古いコメント", detail.comments().get(0).content());
        assertEquals(15L, detail.totalComments());
        // カーソルベースの場合、page/totalPagesは0
        assertEquals(0, detail.page());
        assertEquals(0, detail.totalPages());
    }

    /**
     * 【テスト対象】ThreadService#getThreadDetailBefore
     * 【テストケース】カーソルより前にコメントがない場合
     * 【期待結果】空のコメントリストが返却される
     * 【ビジネス要件】カーソルベースページング - 末端到達
     */
    @Test
    @DisplayName("カーソルページング: カーソル以前にコメントなし、空リストが返される")
    void shouldReturnEmptyCommentsWhenNoneBeforeCursor() {
        Instant now = Instant.now();

        ThreadDetailProjection threadProj = new ThreadDetailProjection(
                THREAD_UUID_1, "テスト", "ユーザ1", now);
        when(threadRepository.findByIdAndArtistIdWithUsername(
                THREAD_UUID_1, "aimyon")).thenReturn(threadProj);

        when(threadCommentRepository.countByThreadId(THREAD_UUID_1))
                .thenReturn(5L);

        when(threadCommentRepository.findByThreadIdWithUsernameBefore(
                THREAD_UUID_1, COMMENT_UUID_1, 10))
                .thenReturn(List.of());

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetailBefore(
                        "aimyon", THREAD_UUID_1, COMMENT_UUID_1, 10);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().comments().size());
    }

    // ========== createThread テスト ==========

    /**
     * 【テスト対象】ThreadService#createThread
     * 【テストケース】アーティストが存在しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】スレッド作成 - アーティスト不在
     */
    @Test
    @DisplayName("スレッド作成: アーティスト不在時、emptyが返される")
    void shouldReturnEmptyWhenCreateThreadArtistNotFound() {
        when(artistRepository.findById("unknown")).thenReturn(null);

        CreateThreadRequest request =
                new CreateThreadRequest(
                        "テスト", "コメント", VALID_SESSION);

        Optional<ThreadDetailResponse> result =
                threadService.createThread("unknown", request);

        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#createThread
     * 【テストケース】認証失敗時
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】スレッド作成 - 認証失敗
     */
    @Test
    @DisplayName("スレッド作成: 認証失敗時、emptyが返される")
    void shouldReturnEmptyWhenCreateThreadAuthFails() {
        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(sessionService.getUserIdBySession(INVALID_SESSION))
                .thenReturn(null);

        CreateThreadRequest request =
                new CreateThreadRequest(
                        "テスト", "コメント", INVALID_SESSION);

        Optional<ThreadDetailResponse> result =
                threadService.createThread("aimyon", request);

        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#createThread
     * 【テストケース】正常系、スレッドが作成される（UUIDv7生成、非正規化カラム設定）
     * 【期待結果】スレッドとコメントがpersistされ、非正規化カラムが設定される
     * 【ビジネス要件】スレッド作成 - 正常系
     */
    @Test
    @DisplayName("スレッド作成: 正常系、UUIDv7でpersistされ非正規化カラムが設定される")
    void shouldCreateThreadSuccessfully() {
        UUID newThreadUuid = UUID.fromString(
                "01970000-1000-7000-8000-000000000100");
        UUID newCommentUuid = UUID.fromString(
                "01970000-2000-7000-8000-000000000100");

        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_UUID_1.toString());
        when(uuidService.generateUuidV7())
                .thenReturn(newThreadUuid.toString())
                .thenReturn(newCommentUuid.toString());

        // ユーザ名解決用モック
        when(userRepository.findByUserId(USER_UUID_1))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_1, "テストユーザ")));

        CreateThreadRequest request =
                new CreateThreadRequest(
                        "新規スレッド", "初回コメント",
                        VALID_SESSION);

        Optional<ThreadDetailResponse> result =
                threadService.createThread("aimyon", request);

        assertTrue(result.isPresent());
        assertEquals(newThreadUuid.toString(),
                result.get().threadId());
        assertEquals("新規スレッド", result.get().title());
        assertEquals("テストユーザ", result.get().createdByUsername());
        assertEquals(1, result.get().comments().size());
        assertEquals("初回コメント",
                result.get().comments().get(0).content());
        verify(threadRepository).persist(any(ThreadEntity.class));
        verify(threadCommentRepository)
                .persist(any(ThreadCommentEntity.class));
    }

    /**
     * 【テスト対象】ThreadService#createThread
     * 【テストケース】タイトルから改行が除去される
     * 【期待結果】persistされるタイトルに改行が含まれない
     * 【ビジネス要件】スレッド作成 - 改行除去
     */
    @Test
    @DisplayName("スレッド作成: タイトルの改行が除去される")
    void shouldRemoveNewlinesFromTitle() {
        UUID newThreadUuid = UUID.fromString(
                "01970000-1000-7000-8000-000000000101");
        UUID newCommentUuid = UUID.fromString(
                "01970000-2000-7000-8000-000000000101");

        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_UUID_1.toString());
        when(uuidService.generateUuidV7())
                .thenReturn(newThreadUuid.toString())
                .thenReturn(newCommentUuid.toString());

        // persistされるThreadEntityをキャプチャ
        doAnswer(invocation -> {
            ThreadEntity entity = invocation.getArgument(0);
            // タイトルから改行が除去されていることを確認
            assertFalse(entity.title.contains("\n"));
            assertFalse(entity.title.contains("\r"));
            assertEquals("テスト改行なし", entity.title);
            // 非正規化カラムが設定されていることを確認
            assertNotNull(entity.latestCommentContent);
            assertNotNull(entity.latestCommentAt);
            return null;
        }).when(threadRepository).persist(any(ThreadEntity.class));

        // ユーザ名解決用モック
        when(userRepository.findByUserId(USER_UUID_1))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_1, "ユーザ")));

        CreateThreadRequest request =
                new CreateThreadRequest(
                        "テスト\n改行\rなし", "コメント",
                        VALID_SESSION);

        threadService.createThread("aimyon", request);

        verify(threadRepository).persist(any(ThreadEntity.class));
    }

    /**
     * 【テスト対象】ThreadService#createThread
     * 【テストケース】非正規化カラムが初回コメントで設定される
     * 【期待結果】latestCommentContent/latestCommentAtがpersistされる
     * 【ビジネス要件】スレッド作成 - 非正規化カラム設定
     */
    @Test
    @DisplayName("スレッド作成: 非正規化カラムが初回コメントで設定される")
    void shouldSetDenormalizedColumnsOnCreate() {
        UUID newThreadUuid = UUID.fromString(
                "01970000-1000-7000-8000-000000000102");
        UUID newCommentUuid = UUID.fromString(
                "01970000-2000-7000-8000-000000000102");

        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_UUID_1.toString());
        when(uuidService.generateUuidV7())
                .thenReturn(newThreadUuid.toString())
                .thenReturn(newCommentUuid.toString());

        doAnswer(invocation -> {
            ThreadEntity entity = invocation.getArgument(0);
            assertEquals("テストコメント", entity.latestCommentContent);
            assertNotNull(entity.latestCommentAt);
            return null;
        }).when(threadRepository).persist(any(ThreadEntity.class));

        when(userRepository.findByUserId(USER_UUID_1))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_1, "ユーザ")));

        CreateThreadRequest request =
                new CreateThreadRequest(
                        "テスト", "テストコメント", VALID_SESSION);

        Optional<ThreadDetailResponse> result =
                threadService.createThread("aimyon", request);

        assertTrue(result.isPresent());
        verify(threadRepository).persist(any(ThreadEntity.class));
    }

    // ========== addComment テスト ==========

    /**
     * 【テスト対象】ThreadService#addComment
     * 【テストケース】スレッドが存在しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】コメント追加 - スレッド不在
     */
    @Test
    @DisplayName("コメント追加: スレッド不在時、emptyが返される")
    void shouldReturnEmptyWhenAddCommentThreadNotFound() {
        UUID unknownThread = UUID.fromString(
                "01970000-1000-7000-8000-ffffffffffff");
        when(threadRepository.findById(unknownThread)).thenReturn(null);

        CreateCommentRequest request =
                new CreateCommentRequest("コメント", VALID_SESSION);

        Optional<ThreadCommentResponse> result =
                threadService.addComment(
                        "aimyon", unknownThread, request);

        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#addComment
     * 【テストケース】アーティストIDが一致しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】コメント追加 - アーティスト不一致
     */
    @Test
    @DisplayName("コメント追加: アーティストID不一致時、emptyが返される")
    void shouldReturnEmptyWhenAddCommentArtistMismatch() {
        ThreadEntity thread = createThreadEntity(
                THREAD_UUID_1, "aimyon", "テスト",
                USER_UUID_1, Instant.now());
        when(threadRepository.findById(THREAD_UUID_1))
                .thenReturn(thread);

        CreateCommentRequest request =
                new CreateCommentRequest("コメント", VALID_SESSION);

        Optional<ThreadCommentResponse> result =
                threadService.addComment(
                        "different-artist", THREAD_UUID_1, request);

        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#addComment
     * 【テストケース】認証失敗時
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】コメント追加 - 認証失敗
     */
    @Test
    @DisplayName("コメント追加: 認証失敗時、emptyが返される")
    void shouldReturnEmptyWhenAddCommentAuthFails() {
        ThreadEntity thread = createThreadEntity(
                THREAD_UUID_1, "aimyon", "テスト",
                USER_UUID_1, Instant.now());
        when(threadRepository.findById(THREAD_UUID_1))
                .thenReturn(thread);
        when(sessionService.getUserIdBySession(INVALID_SESSION))
                .thenReturn(null);

        CreateCommentRequest request =
                new CreateCommentRequest(
                        "コメント", INVALID_SESSION);

        Optional<ThreadCommentResponse> result =
                threadService.addComment(
                        "aimyon", THREAD_UUID_1, request);

        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#addComment
     * 【テストケース】正常系、コメントが追加され非正規化カラムが更新される
     * 【期待結果】コメントがpersistされ、スレッドの非正規化カラムが更新される
     * 【ビジネス要件】コメント追加 - 正常系（非正規化更新）
     */
    @Test
    @DisplayName("コメント追加: 正常系、非正規化カラムが更新される")
    void shouldAddCommentSuccessfully() {
        ThreadEntity thread = createThreadEntity(
                THREAD_UUID_1, "aimyon", "テスト",
                USER_UUID_1, Instant.now());
        when(threadRepository.findById(THREAD_UUID_1))
                .thenReturn(thread);
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_UUID_2.toString());
        when(uuidService.generateUuidV7())
                .thenReturn(COMMENT_UUID_50.toString());

        when(userRepository.findByUserId(USER_UUID_2))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_2, "コメントユーザ")));

        CreateCommentRequest request =
                new CreateCommentRequest(
                        "新しいコメント", VALID_SESSION);

        Optional<ThreadCommentResponse> result =
                threadService.addComment(
                        "aimyon", THREAD_UUID_1, request);

        assertTrue(result.isPresent());
        assertEquals("新しいコメント", result.get().content());
        assertEquals("コメントユーザ",
                result.get().createdByUsername());
        assertNotNull(result.get().createdAt());
        // コメントIDがUUID文字列で返却される
        assertEquals(COMMENT_UUID_50.toString(),
                result.get().commentId());
        // コメントがpersistされる
        verify(threadCommentRepository)
                .persist(any(ThreadCommentEntity.class));
        // 非正規化カラムが更新される（threadRepository.persistが呼ばれる）
        verify(threadRepository).persist(any(ThreadEntity.class));
        // スレッドの非正規化カラムが更新されていることを確認
        assertEquals("新しいコメント", thread.latestCommentContent);
        assertNotNull(thread.latestCommentAt);
    }

    /**
     * 【テスト対象】ThreadService#getThreadList
     * 【テストケース】コメントなしスレッドのソート順
     * 【期待結果】SQL側でCOALESCE(latestCommentAt, createdAt)降順ソート済み
     * 【ビジネス要件】スレッド一覧 - ソート順（SQL実施）
     */
    @Test
    @DisplayName("スレッド一覧: コメントなしスレッドのソートがSQL側で実施される")
    void shouldSortThreadsWithNoCommentsLast() {
        Instant now = Instant.now();
        Instant earlier = now.minusSeconds(7200);

        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(threadRepository.countByArtistId("aimyon"))
                .thenReturn(2L);

        // SQL側でソート済みの結果を返す
        // コメントあり（latestCommentAt=now）→ コメントなし（createdAt=earlier）
        ThreadListProjection proj1 = new ThreadListProjection(
                THREAD_UUID_2, "コメントあり", "ユーザ",
                "最新", now, now);
        ThreadListProjection proj2 = new ThreadListProjection(
                THREAD_UUID_1, "コメントなし", "ユーザ",
                null, null, earlier);
        when(threadRepository.findByArtistIdWithUsername("aimyon", 0, 20))
                .thenReturn(List.of(proj1, proj2));

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 20);

        assertTrue(result.isPresent());
        List<String> titles = result.get().threads().stream()
                .map(t -> t.title()).toList();
        // コメントありが先、コメントなしが後
        assertEquals("コメントあり", titles.get(0));
        assertEquals("コメントなし", titles.get(1));
    }

    /**
     * 【テスト対象】ThreadService#getThreadList
     * 【テストケース】最新コメントがnullのスレッド
     * 【期待結果】latestCommentがnullで返却される
     * 【ビジネス要件】スレッド一覧 - コメントなし表示
     */
    @Test
    @DisplayName("スレッド一覧: コメントなしスレッドの最新コメントがnull")
    void shouldReturnNullLatestCommentWhenNoComments() {
        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(threadRepository.countByArtistId("aimyon"))
                .thenReturn(1L);

        ThreadListProjection proj = new ThreadListProjection(
                THREAD_UUID_1, "テスト", "ユーザ",
                null, null, Instant.now());
        when(threadRepository.findByArtistIdWithUsername("aimyon", 0, 20))
                .thenReturn(List.of(proj));

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 20);

        assertTrue(result.isPresent());
        assertNull(
                result.get().threads().get(0).latestComment());
    }
}
