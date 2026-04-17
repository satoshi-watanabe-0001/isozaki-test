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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
    private static final UUID COMMENT_UUID_10 =
            UUID.fromString("01970000-2000-7000-8000-00000000000a");
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
     * テスト用のThreadCommentEntityを生成するヘルパー
     */
    private ThreadCommentEntity createCommentEntity(
            UUID commentId, UUID threadId, String content,
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
     * 【ビジネス要件】スレッド一覧取得 - 正常系（N+1問題対策: バッチ取得）
     */
    @Test
    @DisplayName("スレッド一覧: 正常系、一覧が返される（バッチ取得）")
    void shouldReturnThreadListWhenArtistExists() {
        Instant now = Instant.now();
        Instant earlier = now.minusSeconds(3600);

        when(artistRepository.findById("aimyon"))
                .thenReturn(new ArtistEntity());
        when(threadRepository.countByArtistId("aimyon"))
                .thenReturn(2L);

        ThreadEntity thread1 = createThreadEntity(
                THREAD_UUID_1, "aimyon", "スレッド1", USER_UUID_1, earlier);
        ThreadEntity thread2 = createThreadEntity(
                THREAD_UUID_2, "aimyon", "スレッド2", USER_UUID_2, now);
        when(threadRepository.findByArtistId("aimyon", 0, 20))
                .thenReturn(List.of(thread1, thread2));

        // 最新コメントの一括取得モック（N+1問題対策）
        ThreadCommentEntity latestComment1 = createCommentEntity(
                COMMENT_UUID_10, THREAD_UUID_1, "最新コメント1", USER_UUID_1, now);
        when(threadCommentRepository.findLatestByThreadIds(anyList()))
                .thenReturn(Map.of(THREAD_UUID_1, latestComment1));

        // ユーザ名の一括取得モック（N+1問題対策）
        when(userRepository.findUsernamesByUserIds(anyList()))
                .thenReturn(Map.of(
                        USER_UUID_1, "テストユーザ1",
                        USER_UUID_2, "テストユーザ2"));

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
        when(threadRepository.findByArtistId("aimyon", 0, 20))
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
        when(threadRepository.findByArtistId("aimyon", 0, 100))
                .thenReturn(List.of());

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 9999);

        assertTrue(result.isPresent());
        assertEquals(100, result.get().size());
        verify(threadRepository).findByArtistId("aimyon", 0, 100);
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
        when(threadRepository.findByArtistId("aimyon", 0, 20))
                .thenReturn(List.of());

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 0);

        assertTrue(result.isPresent());
        assertEquals(20, result.get().size());
        verify(threadRepository).findByArtistId("aimyon", 0, 20);
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
        when(threadRepository.findById(unknownThread)).thenReturn(null);

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
        ThreadEntity thread = createThreadEntity(
                THREAD_UUID_1, "aimyon", "テスト",
                USER_UUID_1, Instant.now());
        when(threadRepository.findById(THREAD_UUID_1))
                .thenReturn(thread);

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetail(
                        "different-artist", THREAD_UUID_1, 1, 10);

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
                THREAD_UUID_1, "aimyon", "テストスレッド",
                USER_UUID_1, now);
        when(threadRepository.findById(THREAD_UUID_1))
                .thenReturn(thread);

        ThreadCommentEntity comment1 = createCommentEntity(
                COMMENT_UUID_1, THREAD_UUID_1, "コメント1",
                USER_UUID_1, now);
        ThreadCommentEntity comment2 = createCommentEntity(
                COMMENT_UUID_2, THREAD_UUID_1, "コメント2\n改行あり",
                USER_UUID_2, now.minusSeconds(60));
        when(threadCommentRepository.countByThreadId(THREAD_UUID_1))
                .thenReturn(2L);
        when(threadCommentRepository.findByThreadId(THREAD_UUID_1, 0, 10))
                .thenReturn(List.of(comment1, comment2));

        when(userRepository.findByUserId(USER_UUID_1))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_1, "ユーザ1")));
        when(userRepository.findByUserId(USER_UUID_2))
                .thenReturn(Optional.of(
                        createUserEntity(USER_UUID_2, "ユーザ2")));

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
     * 【テストケース】ユーザが見つからない場合
     * 【期待結果】「不明なユーザ」として返却される
     * 【ビジネス要件】スレッド詳細取得 - ユーザ不在
     */
    @Test
    @DisplayName("スレッド詳細: ユーザ不在時、不明なユーザと表示される")
    void shouldReturnUnknownUsernameWhenUserNotFound() {
        Instant now = Instant.now();
        ThreadEntity thread = createThreadEntity(
                THREAD_UUID_1, "aimyon", "テスト", USER_UUID_1, now);
        when(threadRepository.findById(THREAD_UUID_1))
                .thenReturn(thread);

        when(threadCommentRepository.countByThreadId(THREAD_UUID_1))
                .thenReturn(0L);
        when(threadCommentRepository.findByThreadId(THREAD_UUID_1, 0, 10))
                .thenReturn(List.of());

        // ユーザが見つからないケース
        when(userRepository.findByUserId(USER_UUID_1))
                .thenReturn(Optional.empty());

        Optional<ThreadDetailResponse> result =
                threadService.getThreadDetail(
                        "aimyon", THREAD_UUID_1, 1, 10);

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
     * 【テストケース】正常系、スレッドが作成される（UUIDv7生成）
     * 【期待結果】スレッドとコメントがpersistされる
     * 【ビジネス要件】スレッド作成 - 正常系
     */
    @Test
    @DisplayName("スレッド作成: 正常系、UUIDv7でpersistが呼ばれる")
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

        // 作成後のgetThreadDetail用モック
        ThreadEntity savedThread = createThreadEntity(
                newThreadUuid, "aimyon", "新規スレッド",
                USER_UUID_1, Instant.now());
        when(threadRepository.findById(newThreadUuid))
                .thenReturn(savedThread);
        when(threadCommentRepository.countByThreadId(newThreadUuid))
                .thenReturn(1L);

        ThreadCommentEntity savedComment = createCommentEntity(
                newCommentUuid, newThreadUuid, "初回コメント",
                USER_UUID_1, Instant.now());
        when(threadCommentRepository.findByThreadId(
                newThreadUuid, 0, 10))
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
        assertEquals(newThreadUuid.toString(),
                result.get().threadId());
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
            return null;
        }).when(threadRepository).persist(any(ThreadEntity.class));

        // getThreadDetail用モック（作成後の取得）
        ThreadEntity savedThread = createThreadEntity(
                newThreadUuid, "aimyon", "テスト改行なし",
                USER_UUID_1, Instant.now());
        when(threadRepository.findById(newThreadUuid))
                .thenReturn(savedThread);
        when(threadCommentRepository.countByThreadId(newThreadUuid))
                .thenReturn(1L);
        when(threadCommentRepository.findByThreadId(
                newThreadUuid, 0, 10))
                .thenReturn(List.of(createCommentEntity(
                        newCommentUuid, newThreadUuid, "コメント",
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
     * 【テストケース】正常系、コメントが追加される（UUIDv7生成）
     * 【期待結果】コメントがpersistされレスポンスが返却される
     * 【ビジネス要件】コメント追加 - 正常系
     */
    @Test
    @DisplayName("コメント追加: 正常系、UUIDv7でコメントが追加される")
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
                THREAD_UUID_1, "aimyon", "コメントなし",
                USER_UUID_1, earlier);
        ThreadEntity thread2 = createThreadEntity(
                THREAD_UUID_2, "aimyon", "コメントあり",
                USER_UUID_1, now);
        when(threadRepository.findByArtistId("aimyon", 0, 20))
                .thenReturn(List.of(thread1, thread2));

        // N+1対策: 一括取得モック
        ThreadCommentEntity latestComment = createCommentEntity(
                COMMENT_UUID_10, THREAD_UUID_2, "最新", USER_UUID_1, now);
        when(threadCommentRepository.findLatestByThreadIds(anyList()))
                .thenReturn(Map.of(THREAD_UUID_2, latestComment));

        when(userRepository.findUsernamesByUserIds(anyList()))
                .thenReturn(Map.of(USER_UUID_1, "ユーザ"));

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
                THREAD_UUID_1, "aimyon", "テスト",
                USER_UUID_1, Instant.now());
        when(threadRepository.findByArtistId("aimyon", 0, 20))
                .thenReturn(List.of(thread));

        // N+1対策: 空Map返却
        when(threadCommentRepository.findLatestByThreadIds(anyList()))
                .thenReturn(Map.of());

        when(userRepository.findUsernamesByUserIds(anyList()))
                .thenReturn(Map.of(USER_UUID_1, "ユーザ"));

        Optional<ThreadListResponse> result =
                threadService.getThreadList("aimyon", 1, 20);

        assertTrue(result.isPresent());
        assertNull(
                result.get().threads().get(0).latestComment());
    }
}
