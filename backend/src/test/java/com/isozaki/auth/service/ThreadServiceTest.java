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
import com.isozaki.auth.entity.ThreadEntity;
import com.isozaki.auth.repository.ArtistRepository;
import com.isozaki.auth.repository.ThreadCommentRepository;
import com.isozaki.auth.repository.ThreadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

    private ThreadService threadService;

    private static final UUID USER_UUID_1 = UUID.fromString("01908b7e-1234-7000-8000-000000000001");

    @BeforeEach
    void setUp() {
        threadService = new ThreadService(threadRepository, threadCommentRepository, artistRepository, sessionService);
    }

    /**
     * テスト用のThreadEntityを生成するヘルパーメソッド
     */
    private ThreadEntity createThreadEntity(int id, String artistId, String title, UUID createdBy, Instant createdAt) {
        ThreadEntity entity = new ThreadEntity();
        entity.threadId = id;
        entity.artistId = artistId;
        entity.title = title;
        entity.createdBy = createdBy;
        entity.createdAt = createdAt;
        return entity;
    }

    /**
     * 【テスト対象】ThreadService#getThreadList
     * 【テストケース】アーティストが存在しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】スレッド一覧取得 - アーティスト不在
     */
    @Test
    @DisplayName("スレッド一覧: アーティストが存在しない場合、emptyが返されること")
    void shouldReturnEmptyWhenArtistNotFoundForList() {
        // Given: アーティストが存在しない
        when(artistRepository.findById("unknown")).thenReturn(null);

        // When: スレッド一覧取得を実行
        Optional<ThreadListResponse> result = threadService.getThreadList("unknown", 1, 20);

        // Then: Optional.emptyが返却される
        assertFalse(result.isPresent());
        verify(artistRepository).findById("unknown");
    }

    /**
     * 【テスト対象】ThreadService#getThreadDetail
     * 【テストケース】スレッドが存在しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】スレッド詳細取得 - スレッド不在
     */
    @Test
    @DisplayName("スレッド詳細: スレッドが存在しない場合、emptyが返されること")
    void shouldReturnEmptyWhenThreadNotFound() {
        // Given: スレッドが存在しない
        when(threadRepository.findById(999)).thenReturn(null);

        // When: スレッド詳細取得を実行
        Optional<ThreadDetailResponse> result = threadService.getThreadDetail("aimyon", 999, 1, 10);

        // Then: Optional.emptyが返却される
        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#getThreadDetail
     * 【テストケース】スレッドのアーティストIDが一致しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】スレッド詳細取得 - アーティスト不一致
     */
    @Test
    @DisplayName("スレッド詳細: アーティストIDが一致しない場合、emptyが返されること")
    void shouldReturnEmptyWhenArtistMismatch() {
        // Given: 別のアーティストのスレッドが存在する
        ThreadEntity thread = createThreadEntity(1, "aimyon", "テスト", USER_UUID_1, Instant.now());
        when(threadRepository.findById(1)).thenReturn(thread);

        // When: 異なるアーティストIDでスレッド詳細取得を実行
        Optional<ThreadDetailResponse> result = threadService.getThreadDetail("different-artist", 1, 1, 10);

        // Then: Optional.emptyが返却される
        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#createThread
     * 【テストケース】アーティストが存在しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】スレッド作成 - アーティスト不在
     */
    @Test
    @DisplayName("スレッド作成: アーティストが存在しない場合、emptyが返されること")
    void shouldReturnEmptyWhenCreateThreadArtistNotFound() {
        // Given: アーティストが存在しない
        when(artistRepository.findById("unknown")).thenReturn(null);

        CreateThreadRequest request = new CreateThreadRequest("テスト", "コメント", "session-id");

        // When: 存在しないアーティストIDでスレッド作成を実行
        Optional<ThreadDetailResponse> result = threadService.createThread("unknown", request);

        // Then: Optional.emptyが返却される
        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#createThread
     * 【テストケース】認証失敗時のスレッド作成
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】スレッド作成 - 認証失敗
     */
    @Test
    @DisplayName("スレッド作成: 認証失敗時、emptyが返されること")
    void shouldReturnEmptyWhenCreateThreadAuthFails() {
        // Given: アーティストは存在するが認証が失敗する
        when(artistRepository.findById("aimyon")).thenReturn(new com.isozaki.auth.entity.ArtistEntity());
        when(sessionService.getUserIdBySession("invalid-session")).thenReturn(null);

        CreateThreadRequest request = new CreateThreadRequest("テスト", "コメント", "invalid-session");

        // When: 未認証状態でスレッド作成を実行
        Optional<ThreadDetailResponse> result = threadService.createThread("aimyon", request);

        // Then: Optional.emptyが返却される
        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#addComment
     * 【テストケース】スレッドが存在しない場合
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】コメント追加 - スレッド不在
     */
    @Test
    @DisplayName("コメント追加: スレッドが存在しない場合、emptyが返されること")
    void shouldReturnEmptyWhenAddCommentThreadNotFound() {
        // Given: スレッドが存在しない
        when(threadRepository.findById(999)).thenReturn(null);

        CreateCommentRequest request = new CreateCommentRequest("コメント", "session-id");

        // When: 存在しないスレッドIDでコメント追加を実行
        Optional<ThreadCommentResponse> result = threadService.addComment("aimyon", 999, request);

        // Then: Optional.emptyが返却される
        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#addComment
     * 【テストケース】認証失敗時のコメント追加
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】コメント追加 - 認証失敗
     */
    @Test
    @DisplayName("コメント追加: 認証失敗時、emptyが返されること")
    void shouldReturnEmptyWhenAddCommentAuthFails() {
        // Given: スレッドは存在するが認証が失敗する
        ThreadEntity thread = createThreadEntity(1, "aimyon", "テスト", USER_UUID_1, Instant.now());
        when(threadRepository.findById(1)).thenReturn(thread);
        when(sessionService.getUserIdBySession("invalid-session")).thenReturn(null);

        CreateCommentRequest request = new CreateCommentRequest("コメント", "invalid-session");

        // When: 未認証状態でコメント追加を実行
        Optional<ThreadCommentResponse> result = threadService.addComment("aimyon", 1, request);

        // Then: Optional.emptyが返却される
        assertFalse(result.isPresent());
    }

    /**
     * 【テスト対象】ThreadService#addComment
     * 【テストケース】アーティストIDが一致しない場合のコメント追加
     * 【期待結果】Optional.emptyが返却される
     * 【ビジネス要件】コメント追加 - アーティスト不一致
     */
    @Test
    @DisplayName("コメント追加: アーティストIDが一致しない場合、emptyが返されること")
    void shouldReturnEmptyWhenAddCommentArtistMismatch() {
        // Given: 別のアーティストのスレッドが存在する
        ThreadEntity thread = createThreadEntity(1, "aimyon", "テスト", USER_UUID_1, Instant.now());
        when(threadRepository.findById(1)).thenReturn(thread);

        CreateCommentRequest request = new CreateCommentRequest("コメント", "session-id");

        // When: 異なるアーティストIDでコメント追加を実行
        Optional<ThreadCommentResponse> result = threadService.addComment("different-artist", 1, request);

        // Then: Optional.emptyが返却される
        assertFalse(result.isPresent());
    }
}
