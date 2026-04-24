/**
 * ThreadResourceの単体テスト
 *
 * <p>スレッド機能のエンドポイントのリクエスト処理とレスポンス生成をテストする。
 * ThreadServiceはモックを使用する。</p>
 *
 * @since 1.3
 */

package com.isozaki.auth.resource;

import com.isozaki.auth.dto.CreateCommentRequest;
import com.isozaki.auth.dto.CreateThreadRequest;
import com.isozaki.auth.dto.ThreadCommentResponse;
import com.isozaki.auth.dto.ThreadDetailResponse;
import com.isozaki.auth.dto.ThreadListItemResponse;
import com.isozaki.auth.dto.ThreadListResponse;
import com.isozaki.auth.service.ThreadService;
import jakarta.ws.rs.core.Response;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ThreadResourceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ThreadResource テスト")
class ThreadResourceTest {

    @Mock
    private ThreadService threadService;

    private ThreadResource threadResource;

    private static final UUID THREAD_UUID_1 =
            UUID.fromString("01970000-1000-7000-8000-000000000001");
    private static final UUID THREAD_UUID_2 =
            UUID.fromString("01970000-1000-7000-8000-000000000002");
    private static final UUID THREAD_UUID_NEW =
            UUID.fromString("01970000-1000-7000-8000-000000000100");

    @BeforeEach
    void setUp() {
        threadResource = new ThreadResource(threadService);
    }

    /**
     * 【テスト対象】ThreadResource#getThreadList
     * 【テストケース】アーティストが存在する場合のスレッド一覧取得
     * 【期待結果】HTTP 200 OKとスレッド一覧が返却される
     * 【ビジネス要件】スレッド一覧API - 正常系
     */
    @Test
    @DisplayName("スレッド一覧取得: アーティスト存在時、200 OKとスレッド一覧が返される")
    void shouldReturnOkWithThreadList() {
        // Given: スレッド一覧データが存在する
        ThreadListResponse expectedResponse = new ThreadListResponse(
                List.of(
                        new ThreadListItemResponse(
                                THREAD_UUID_1.toString(),
                                "テストスレッド", "テストユーザー",
                                "最新コメント",
                                Instant.parse("2025-04-13T10:00:00Z")),
                        new ThreadListItemResponse(
                                THREAD_UUID_2.toString(),
                                "テストスレッド2", "テストユーザー2",
                                null, null)
                ),
                2L, 1, 20, 1
        );
        when(threadService.getThreadList("aimyon", 1, 20))
                .thenReturn(Optional.of(expectedResponse));

        // When: スレッド一覧取得APIを実行
        Response response = threadResource.getThreadList("aimyon", 1, 20);

        // Then: HTTP 200 OKと正しいスレッド一覧が返却される
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadListResponse body = (ThreadListResponse) response.getEntity();
        assertNotNull(body);
        assertEquals(2, body.threads().size());
        assertEquals(2L, body.totalCount());
        verify(threadService).getThreadList("aimyon", 1, 20);
    }

    /**
     * 【テスト対象】ThreadResource#getThreadList
     * 【テストケース】アーティストが存在しない場合
     * 【期待結果】HTTP 404 Not Foundが返却される
     * 【ビジネス要件】スレッド一覧API - アーティスト不在
     */
    @Test
    @DisplayName("スレッド一覧取得: アーティスト不在時、404 Not Foundが返される")
    void shouldReturnNotFoundForThreadList() {
        // Given: アーティストが存在しない
        when(threadService.getThreadList("unknown", 1, 20))
                .thenReturn(Optional.empty());

        // When: 存在しないアーティストIDでスレッド一覧取得を実行
        Response response = threadResource.getThreadList("unknown", 1, 20);

        // Then: HTTP 404 Not Foundが返却される
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus());
        verify(threadService).getThreadList("unknown", 1, 20);
    }

    /**
     * 【テスト対象】ThreadResource#getThreadDetail
     * 【テストケース】スレッドが存在する場合のスレッド詳細取得
     * 【期待結果】HTTP 200 OKとスレッド詳細が返却される
     * 【ビジネス要件】スレッド詳細API - 正常系（UUID文字列パラメータ）
     */
    @Test
    @DisplayName("スレッド詳細取得: スレッド存在時、200 OKとスレッド詳細が返される")
    void shouldReturnOkWithThreadDetail() {
        // Given: スレッド詳細データが存在する
        ThreadDetailResponse expectedResponse = new ThreadDetailResponse(
                THREAD_UUID_1.toString(),
                "テストスレッド", "テストユーザー",
                Instant.parse("2025-04-13T10:00:00Z"),
                List.of(new ThreadCommentResponse(
                        "01970000-2000-7000-8000-000000000001",
                        "テストコメント", "テストユーザー",
                        Instant.parse("2025-04-13T10:05:00Z"),
                        List.of())),
                1L, 1, 10, 1
        );
        when(threadService.getThreadDetail(
                "aimyon", THREAD_UUID_1, 1, 10))
                .thenReturn(Optional.of(expectedResponse));

        // When: スレッド詳細取得APIをUUID文字列で実行（beforeなし＝オフセットベース）
        Response response = threadResource.getThreadDetail(
                "aimyon", THREAD_UUID_1.toString(), 1, 10, null);

        // Then: HTTP 200 OKと正しいスレッド詳細が返却される
        assertEquals(Response.Status.OK.getStatusCode(),
                response.getStatus());
        ThreadDetailResponse body =
                (ThreadDetailResponse) response.getEntity();
        assertNotNull(body);
        assertEquals("テストスレッド", body.title());
        assertEquals(1, body.comments().size());
        verify(threadService).getThreadDetail(
                "aimyon", THREAD_UUID_1, 1, 10);
    }

    /**
     * 【テスト対象】ThreadResource#getThreadDetail
     * 【テストケース】スレッドが存在しない場合
     * 【期待結果】HTTP 404 Not Foundが返却される
     * 【ビジネス要件】スレッド詳細API - スレッド不在
     */
    @Test
    @DisplayName("スレッド詳細取得: スレッドが存在しない場合、404 Not Foundが返されること")
    void shouldReturnNotFoundForThreadDetail() {
        // Given: スレッドが存在しない
        UUID unknownThread = UUID.fromString(
                "01970000-1000-7000-8000-ffffffffffff");
        when(threadService.getThreadDetail(
                "aimyon", unknownThread, 1, 10))
                .thenReturn(Optional.empty());

        // When: 存在しないスレッドIDでスレッド詳細取得を実行
        Response response = threadResource.getThreadDetail(
                "aimyon", unknownThread.toString(), 1, 10, null);

        // Then: HTTP 404 Not Foundが返却される
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus());
        verify(threadService).getThreadDetail(
                "aimyon", unknownThread, 1, 10);
    }

    /**
     * 【テスト対象】ThreadResource#getThreadDetail
     * 【テストケース】不正なUUID形式のスレッドID
     * 【期待結果】HTTP 400 Bad Requestが返却される
     * 【ビジネス要件】スレッド詳細API - 不正なUUID形式
     */
    @Test
    @DisplayName("スレッド詳細取得: 不正なUUID形式の場合、400 Bad Requestが返される")
    void shouldReturnBadRequestForInvalidThreadIdFormat() {
        // When: 不正なUUID形式でスレッド詳細取得を実行
        Response response = threadResource.getThreadDetail(
                "aimyon", "invalid-uuid", 1, 10, null);

        // Then: HTTP 400 Bad Requestが返却される
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus());
    }

    /**
     * 【テスト対象】ThreadResource#getThreadDetail
     * 【テストケース】beforeパラメータ指定時のカーソルベースページング
     * 【期待結果】HTTP 200 OKとカーソルベースのコメントが返却される
     * 【ビジネス要件】スレッド詳細API - カーソルベースページング
     */
    @Test
    @DisplayName("スレッド詳細取得: beforeパラメータ指定時、カーソルベースで取得される")
    void shouldReturnOkWithCursorBasedPagination() {
        // Given: カーソルベースのスレッド詳細データが存在する
        UUID beforeCommentId = UUID.fromString(
                "01970000-2000-7000-8000-000000000010");
        ThreadDetailResponse expectedResponse = new ThreadDetailResponse(
                THREAD_UUID_1.toString(),
                "テストスレッド", "テストユーザー",
                Instant.parse("2025-04-13T10:00:00Z"),
                List.of(new ThreadCommentResponse(
                        "01970000-2000-7000-8000-000000000001",
                        "古いコメント", "テストユーザー",
                        Instant.parse("2025-04-13T09:00:00Z"),
                        List.of())),
                15L, 0, 10, 0
        );
        when(threadService.getThreadDetailBefore(
                "aimyon", THREAD_UUID_1, beforeCommentId, 10))
                .thenReturn(Optional.of(expectedResponse));

        // When: beforeパラメータ付きでスレッド詳細取得APIを実行
        Response response = threadResource.getThreadDetail(
                "aimyon", THREAD_UUID_1.toString(), 1, 10,
                beforeCommentId.toString());

        // Then: HTTP 200 OKとカーソルベースの結果が返却される
        assertEquals(Response.Status.OK.getStatusCode(),
                response.getStatus());
        ThreadDetailResponse body =
                (ThreadDetailResponse) response.getEntity();
        assertNotNull(body);
        assertEquals(1, body.comments().size());
        assertEquals("古いコメント", body.comments().get(0).content());
        verify(threadService).getThreadDetailBefore(
                "aimyon", THREAD_UUID_1, beforeCommentId, 10);
    }

    /**
     * 【テスト対象】ThreadResource#getThreadDetail
     * 【テストケース】不正なUUID形式のbeforeパラメータ
     * 【期待結果】HTTP 400 Bad Requestが返却される
     * 【ビジネス要件】スレッド詳細API - 不正なbeforeパラメータ
     */
    @Test
    @DisplayName("スレッド詳細取得: 不正なbeforeパラメータの場合、400 Bad Requestが返される")
    void shouldReturnBadRequestForInvalidBeforeParam() {
        // When: 不正なUUID形式のbeforeパラメータでスレッド詳細取得を実行
        Response response = threadResource.getThreadDetail(
                "aimyon", THREAD_UUID_1.toString(), 1, 10,
                "not-a-valid-uuid");

        // Then: HTTP 400 Bad Requestが返却される
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus());
    }

    /**
     * 【テスト対象】ThreadResource#createThread
     * 【テストケース】ログイン済みユーザによるスレッド作成
     * 【期待結果】HTTP 201 Createdとスレッド詳細が返却される
     * 【ビジネス要件】スレッド作成API - 正常系
     */
    @Test
    @DisplayName("スレッド作成: ログイン済みユーザ時、201 Createdとスレッド詳細が返される")
    void shouldReturnCreatedForNewThread() {
        // Given: ログイン済みユーザによるスレッド作成リクエスト
        CreateThreadRequest request = new CreateThreadRequest(
                "新規スレッド", "初回コメント", "valid-session-id");
        ThreadDetailResponse expectedResponse = new ThreadDetailResponse(
                THREAD_UUID_NEW.toString(),
                "新規スレッド", "テストユーザー", Instant.now(),
                List.of(new ThreadCommentResponse(
                        "01970000-2000-7000-8000-000000000100",
                        "初回コメント",
                        "テストユーザー", Instant.now(),
                        List.of())),
                1L, 1, 10, 1
        );
        when(threadService.createThread(
                eq("aimyon"), any(CreateThreadRequest.class)))
                .thenReturn(Optional.of(expectedResponse));

        // When: スレッド作成APIを実行
        Response response = threadResource.createThread("aimyon", request);

        // Then: HTTP 201 Createdと作成されたスレッド詳細が返却される
        assertEquals(Response.Status.CREATED.getStatusCode(),
                response.getStatus());
        ThreadDetailResponse body =
                (ThreadDetailResponse) response.getEntity();
        assertNotNull(body);
        assertEquals("新規スレッド", body.title());
        verify(threadService).createThread(
                eq("aimyon"), any(CreateThreadRequest.class));
    }

    /**
     * 【テスト対象】ThreadResource#createThread
     * 【テストケース】未ログインユーザによるスレッド作成
     * 【期待結果】HTTP 401 Unauthorizedが返却される
     * 【ビジネス要件】スレッド作成API - 認証失敗
     */
    @Test
    @DisplayName("スレッド作成: 未ログインユーザの場合、401 Unauthorizedが返されること")
    void shouldReturnUnauthorizedForCreateThread() {
        // Given: 無効なセッションによるスレッド作成リクエスト
        CreateThreadRequest request = new CreateThreadRequest(
                "新規スレッド", "初回コメント",
                "invalid-session-id");
        when(threadService.createThread(
                eq("aimyon"), any(CreateThreadRequest.class)))
                .thenReturn(Optional.empty());

        // When: 未認証状態でスレッド作成APIを実行
        Response response = threadResource.createThread("aimyon", request);

        // Then: HTTP 401 Unauthorizedが返却される
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
                response.getStatus());
    }

    /**
     * 【テスト対象】ThreadResource#addComment
     * 【テストケース】ログイン済みユーザによるコメント追加
     * 【期待結果】HTTP 201 Createdとコメント情報が返却される
     * 【ビジネス要件】コメント追加API - 正常系（UUID文字列パラメータ）
     */
    @Test
    @DisplayName("コメント追加: ログイン済みユーザ時、201 Createdとコメント情報が返される")
    void shouldReturnCreatedForNewComment() {
        // Given: ログイン済みユーザによるコメント追加リクエスト
        CreateCommentRequest request = new CreateCommentRequest(
                "新しいコメント", "valid-session-id", null);
        ThreadCommentResponse expectedResponse = new ThreadCommentResponse(
                "01970000-2000-7000-8000-000000000050",
                "新しいコメント", "テストユーザー", Instant.now(),
                List.of()
        );
        when(threadService.addComment(
                eq("aimyon"), eq(THREAD_UUID_1),
                any(CreateCommentRequest.class)))
                .thenReturn(Optional.of(expectedResponse));

        // When: コメント追加APIをUUID文字列で実行
        Response response = threadResource.addComment(
                "aimyon", THREAD_UUID_1.toString(), request);

        // Then: HTTP 201 Createdと作成されたコメント情報が返却される
        assertEquals(Response.Status.CREATED.getStatusCode(),
                response.getStatus());
        ThreadCommentResponse body =
                (ThreadCommentResponse) response.getEntity();
        assertNotNull(body);
        assertEquals("新しいコメント", body.content());
        verify(threadService).addComment(
                eq("aimyon"), eq(THREAD_UUID_1),
                any(CreateCommentRequest.class));
    }

    /**
     * 【テスト対象】ThreadResource#addComment
     * 【テストケース】未ログインユーザによるコメント追加
     * 【期待結果】HTTP 401 Unauthorizedが返却される
     * 【ビジネス要件】コメント追加API - 認証失敗
     */
    @Test
    @DisplayName("コメント追加: 未ログインユーザの場合、401 Unauthorizedが返されること")
    void shouldReturnUnauthorizedForAddComment() {
        // Given: 無効なセッションによるコメント追加リクエスト
        CreateCommentRequest request = new CreateCommentRequest(
                "新しいコメント", "invalid-session-id", null);
        when(threadService.addComment(
                eq("aimyon"), eq(THREAD_UUID_1),
                any(CreateCommentRequest.class)))
                .thenReturn(Optional.empty());

        // When: 未認証状態でコメント追加APIを実行
        Response response = threadResource.addComment(
                "aimyon", THREAD_UUID_1.toString(), request);

        // Then: HTTP 401 Unauthorizedが返却される
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
                response.getStatus());
    }

    /**
     * 【テスト対象】ThreadResource#addComment
     * 【テストケース】不正なUUID形式のスレッドID
     * 【期待結果】HTTP 400 Bad Requestが返却される
     * 【ビジネス要件】コメント追加API - 不正なUUID形式
     */
    @Test
    @DisplayName("コメント追加: 不正なUUID形式の場合、400 Bad Requestが返される")
    void shouldReturnBadRequestForInvalidThreadIdInComment() {
        // Given: コメント追加リクエスト
        CreateCommentRequest request = new CreateCommentRequest(
                "コメント", "valid-session-id", null);

        // When: 不正なUUID形式でコメント追加を実行
        Response response = threadResource.addComment(
                "aimyon", "not-a-uuid", request);

        // Then: HTTP 400 Bad Requestが返却される
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus());
    }
}
