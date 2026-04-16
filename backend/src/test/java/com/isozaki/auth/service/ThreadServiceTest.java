/**
 * ThreadServiceの単体テスト
 *
 * <p>スレッド機能のビジネスロジックをテストする。
 * 各リポジトリとSessionServiceはモックを使用する。</p>
 *
 * @since 1.3
 */

package com.isozaki.auth.service;

import com.isozaki.auth.dto.CreateCommentRequest;
import com.isozaki.auth.dto.CreateThreadRequest;
import com.isozaki.auth.dto.ThreadCommentResponse;
import com.isozaki.auth.dto.ThreadDetailResponse;
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

    private ThreadService threadService;

    private static final UUID USER_UUID_1 =
            UUID.fromString("01908b7e-1234-7000-8000-000000000001");
    private static final UUID USER_UUID_2 =
            UUID.fromString("01908b7e-1234-7000-8000-000000000002");
    private static final String VALID_SESSION = "valid-session";
    private static final String INVALID_SESSION = "invalid-session";

    @BeforeEach
    void setUp() {
        threadService = new ThreadService(
                threadRepository,
                threadCommentRepository,
                artistRepository,
                sessionService,
                userRepository);
    }

    /**
     * テスト用のThreadEntityを生成するヘルパー
     */
    private ThreadEntity createThreadEntity(
            int id, String artistId, String title,
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
     * テスト用のThreadCommentEntityを生成するヘルパー
     */
    private ThreadCommentEntity createCommentEntity(
            int commentId, int threadId, String content,
            UUID createdBy, Instant createdAt) {
        ThreadCommentEntity entity = new ThreadCommentEntity();
        entity.commentId = commentId;
        entity.threadId = threadId;
        entity.content = content;
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
     * 【ビジネス要件】スレッド一覧取得 - 正常系
     */
    @Test
    @DisplayName("スレッド一覧: 正常系、一覧が返される")
    void shouldReturnThreadListWhenArtistExists() {
        Instant now = Instant.now();
        Instant earlier = now.minusSeconds(3600);

        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(threadRepository.countByArtistId("aimyon"))
                .thenReturn(2L);

        ThreadEntity thread1 = createThreadEntity(
                1, "aimyon", "スレッド1", USER_UUID_1, earlier);
        ThreadEntity thread2 = createThreadEntity(
                2, "aimyon", "スレッド2", USER_UUID_2, now);
        when(threadRepository.findByArtistId("aimyon", 0, 20))
                .thenReturn(List.of(thread1, thread2));

        // ユーザ名解決のモック
        when(userRepository.findByUserId(USER_UUID_1))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_1, "テストユーザ1")));
        when(userRepository.findByUserId(USER_UUID_2))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_2, "テストユーザ2")));

        // 最新コメントのモック
        ThreadCommentEntity latestComment1 = createCommentEntity(
                10, 1, "最新コメント1", USER_UUID_1, now);
        when(threadCommentRepository.findLatestByThreadId(1))
                .thenReturn(Optional.of(latestComment1));
        when(threadCommentRepository.findLatestByThreadId(2))
                .thenReturn(Optional.empty());

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 20);

        assertTrue(result.isPresent());
        ThreadListResponse response = result.get();
        assertEquals(2L, response.totalCount());
        assertEquals(1, response.page());
        assertEquals(20, response.size());
        assertEquals(2, response.threads().size());
        // 降順ソート確認: スレッド1（コメントあり）が先
        assertEquals("スレッド1", response.threads().get(0).title());
        assertEquals(
                "テストユーザ1",
                response.threads().get(0).createdByUsername());
        assertEquals(
                "最新コメント1",
                response.threads().get(0).latestComment());
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
        when(threadRepository.findByArtistId("aimyon", 0, 20))
                .thenReturn(List.of());

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 20);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().threads().size());
        assertEquals(0L, result.get().totalCount());
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
        when(threadRepository.findById(999)).thenReturn(null);

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetail("aimyon", 999, 1, 10);

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
        ThreadEntity thread = createThreadEntity(
                1, "aimyon", "テスト", USER_UUID_1, Instant.now());
        when(threadRepository.findById(1)).thenReturn(thread);

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetail(
                        "different-artist", 1, 1, 10);

        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#getThreadDetail
     * 【テストケース】正常系、スレッド詳細が返却される
     * 【期待結果】スレッド詳細とコメントが返却される
     * 【ビジネス要件】スレッド詳細取得 - 正常系
     */
    @Test
    @DisplayName("スレッド詳細: 正常系、詳細が返される")
    void shouldReturnThreadDetailWhenExists() {
        Instant now = Instant.now();
        ThreadEntity thread = createThreadEntity(
                1, "aimyon", "テストスレッド", USER_UUID_1, now);
        when(threadRepository.findById(1)).thenReturn(thread);

        ThreadCommentEntity comment1 = createCommentEntity(
                1, 1, "コメント1", USER_UUID_1, now);
        ThreadCommentEntity comment2 = createCommentEntity(
                2, 1, "コメント2\n改行あり", USER_UUID_2,
                now.minusSeconds(60));
        when(threadCommentRepository.countByThreadId(1))
                .thenReturn(2L);
        when(threadCommentRepository.findByThreadId(1, 0, 10))
                .thenReturn(List.of(comment1, comment2));

        when(userRepository.findByUserId(USER_UUID_1))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_1, "ユーザ1")));
        when(userRepository.findByUserId(USER_UUID_2))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_2, "ユーザ2")));

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetail("aimyon", 1, 1, 10);

        assertTrue(result.isPresent());
        ThreadDetailResponse detail = result.get();
        assertEquals(1, detail.threadId());
        assertEquals("テストスレッド", detail.title());
        assertEquals("ユーザ1", detail.createdByUsername());
        assertEquals(2L, detail.totalComments());
        assertEquals(2, detail.comments().size());
        assertEquals("コメント1", detail.comments().get(0).content());
        assertEquals("ユーザ1",
                detail.comments().get(0).createdByUsername());
        assertEquals("コメント2\n改行あり",
                detail.comments().get(1).content());
    }

    /**
     * 【テスト対象】ThreadService#getThreadDetail
     * 【テストケース】ユーザが見つからない場合
     * 【期待結果】「不明なユーザ」として返却される
     * 【ビジネス要件】スレッド詳細取得 - ユーザ不在
     */
    @Test
    @DisplayName("スレッド詳細: ユーザ不在時、不明なユーザと表示される")
    void shouldReturnUnknownUsernameWhenUserNotFound() {
        Instant now = Instant.now();
        ThreadEntity thread = createThreadEntity(
                1, "aimyon", "テスト", USER_UUID_1, now);
        when(threadRepository.findById(1)).thenReturn(thread);

        when(threadCommentRepository.countByThreadId(1))
                .thenReturn(0L);
        when(threadCommentRepository.findByThreadId(1, 0, 10))
                .thenReturn(List.of());

        // ユーザが見つからないケース
        when(userRepository.findByUserId(USER_UUID_1))
                .thenReturn(Optional.empty());

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetail("aimyon", 1, 1, 10);

        assertTrue(result.isPresent());
        assertEquals("不明なユーザ", result.get().createdByUsername());
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
     * 【テストケース】正常系、スレッドが作成される
     * 【期待結果】スレッドとコメントがpersistされる
     * 【ビジネス要件】スレッド作成 - 正常系
     */
    @Test
    @DisplayName("スレッド作成: 正常系、persistが呼ばれる")
    void shouldCreateThreadSuccessfully() {
        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_UUID_1.toString());

        // persist時にthreadIdを設定するモック
        doAnswer(invocation -> {
            ThreadEntity entity = invocation.getArgument(0);
            entity.threadId = 100;
            return null;
        }).when(threadRepository).persist(any(ThreadEntity.class));

        // 作成後のgetThreadDetail用モック
        ThreadEntity savedThread = createThreadEntity(
                100, "aimyon", "新規スレッド",
                USER_UUID_1, Instant.now());
        when(threadRepository.findById(100))
                .thenReturn(savedThread);
        when(threadCommentRepository.countByThreadId(100))
                .thenReturn(1L);

        ThreadCommentEntity savedComment = createCommentEntity(
                1, 100, "初回コメント",
                USER_UUID_1, Instant.now());
        when(threadCommentRepository.findByThreadId(100, 0, 10))
                .thenReturn(List.of(savedComment));

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
        assertEquals(100, result.get().threadId());
        assertEquals("新規スレッド", result.get().title());
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
        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_UUID_1.toString());

        // persistされるThreadEntityをキャプチャ
        doAnswer(invocation -> {
            ThreadEntity entity = invocation.getArgument(0);
            entity.threadId = 101;
            // タイトルから改行が除去されていることを確認
            assertFalse(entity.title.contains("\n"));
            assertFalse(entity.title.contains("\r"));
            assertEquals("テスト改行なし", entity.title);
            return null;
        }).when(threadRepository).persist(any(ThreadEntity.class));

        // getThreadDetail用モック（作成後の取得）
        ThreadEntity savedThread = createThreadEntity(
                101, "aimyon", "テスト改行なし",
                USER_UUID_1, Instant.now());
        when(threadRepository.findById(101))
                .thenReturn(savedThread);
        when(threadCommentRepository.countByThreadId(101))
                .thenReturn(1L);
        when(threadCommentRepository.findByThreadId(101, 0, 10))
                .thenReturn(List.of(createCommentEntity(
                        1, 101, "コメント",
                        USER_UUID_1, Instant.now())));
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
        when(threadRepository.findById(999)).thenReturn(null);

        CreateCommentRequest request =
                new CreateCommentRequest("コメント", VALID_SESSION);

        Optional<ThreadCommentResponse> result =
                threadService.addComment("aimyon", 999, request);

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
                1, "aimyon", "テスト", USER_UUID_1, Instant.now());
        when(threadRepository.findById(1)).thenReturn(thread);

        CreateCommentRequest request =
                new CreateCommentRequest("コメント", VALID_SESSION);

        Optional<ThreadCommentResponse> result =
                threadService.addComment(
                        "different-artist", 1, request);

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
                1, "aimyon", "テスト", USER_UUID_1, Instant.now());
        when(threadRepository.findById(1)).thenReturn(thread);
        when(sessionService.getUserIdBySession(INVALID_SESSION))
                .thenReturn(null);

        CreateCommentRequest request =
                new CreateCommentRequest(
                        "コメント", INVALID_SESSION);

        Optional<ThreadCommentResponse> result =
                threadService.addComment("aimyon", 1, request);

        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#addComment
     * 【テストケース】正常系、コメントが追加される
     * 【期待結果】コメントがpersistされレスポンスが返却される
     * 【ビジネス要件】コメント追加 - 正常系
     */
    @Test
    @DisplayName("コメント追加: 正常系、コメントが追加される")
    void shouldAddCommentSuccessfully() {
        ThreadEntity thread = createThreadEntity(
                1, "aimyon", "テスト", USER_UUID_1, Instant.now());
        when(threadRepository.findById(1)).thenReturn(thread);
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_UUID_2.toString());

        when(userRepository.findByUserId(USER_UUID_2))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_2, "コメントユーザ")));

        // persist時にcommentIdを設定するモック
        doAnswer(invocation -> {
            ThreadCommentEntity entity = invocation.getArgument(0);
            entity.commentId = 50;
            return null;
        }).when(threadCommentRepository)
                .persist(any(ThreadCommentEntity.class));

        CreateCommentRequest request =
                new CreateCommentRequest(
                        "新しいコメント", VALID_SESSION);

        Optional<ThreadCommentResponse> result =
                threadService.addComment("aimyon", 1, request);

        assertTrue(result.isPresent());
        assertEquals("新しいコメント", result.get().content());
        assertEquals("コメントユーザ",
                result.get().createdByUsername());
        assertNotNull(result.get().createdAt());
        verify(threadCommentRepository)
                .persist(any(ThreadCommentEntity.class));
    }

    /**
     * 【テスト対象】ThreadService#getThreadList
     * 【テストケース】コメントなしスレッドのソート順
     * 【期待結果】コメントなしスレッドは末尾にソートされる
     * 【ビジネス要件】スレッド一覧 - ソート順
     */
    @Test
    @DisplayName("スレッド一覧: コメントなしスレッドが末尾にソートされる")
    void shouldSortThreadsWithNoCommentsLast() {
        Instant now = Instant.now();
        Instant earlier = now.minusSeconds(7200);

        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(threadRepository.countByArtistId("aimyon"))
                .thenReturn(2L);

        // スレッド1: コメントなし、スレッド2: コメントあり
        ThreadEntity thread1 = createThreadEntity(
                1, "aimyon", "コメントなし",
                USER_UUID_1, earlier);
        ThreadEntity thread2 = createThreadEntity(
                2, "aimyon", "コメントあり",
                USER_UUID_1, now);
        when(threadRepository.findByArtistId("aimyon", 0, 20))
                .thenReturn(List.of(thread1, thread2));

        when(userRepository.findByUserId(USER_UUID_1))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_1, "ユーザ")));

        // thread1: コメントなし → latestCommentAtはcreatedAt
        when(threadCommentRepository.findLatestByThreadId(1))
                .thenReturn(Optional.empty());
        // thread2: コメントあり → latestCommentAtはコメント日時
        ThreadCommentEntity latestComment = createCommentEntity(
                10, 2, "最新", USER_UUID_1, now);
        when(threadCommentRepository.findLatestByThreadId(2))
                .thenReturn(Optional.of(latestComment));

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

        ThreadEntity thread = createThreadEntity(
                1, "aimyon", "テスト", USER_UUID_1, Instant.now());
        when(threadRepository.findByArtistId("aimyon", 0, 20))
                .thenReturn(List.of(thread));

        when(userRepository.findByUserId(USER_UUID_1))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_1, "ユーザ")));
        when(threadCommentRepository.findLatestByThreadId(1))
                .thenReturn(Optional.empty());

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 20);

        assertTrue(result.isPresent());
        assertNull(
                result.get().threads().get(0).latestComment());
    }
}
