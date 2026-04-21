/**
 * ImageResourceのテストクラス
 *
 * <p>画像アップロードAPI（Pre-signed URL生成）のテスト。
 * セッション認証、バリデーション、正常系を検証する。</p>
 *
 * @since 1.4
 */

package com.isozaki.auth.resource;

import com.isozaki.auth.dto.UploadUrlItem;
import com.isozaki.auth.dto.UploadUrlRequest;
import com.isozaki.auth.dto.UploadUrlResponse;
import com.isozaki.auth.service.ImageService;
import com.isozaki.auth.service.SessionService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ImageResourceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageResource テスト")
class ImageResourceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private SessionService sessionService;

    private ImageResource imageResource;

    private static final String ARTIST_ID = "aimyon";
    private static final String THREAD_ID = "01970000-1000-7000-8000-000000000001";
    private static final String VALID_SESSION = "valid-session-id";
    private static final String USER_ID =
            "01970000-0000-7000-8000-000000000001";

    @BeforeEach
    void setUp() {
        imageResource = new ImageResource(imageService, sessionService);
    }

    /**
     * 【テスト対象】ImageResource#generateUploadUrls
     * 【テストケース】正常系: 1ファイルのPre-signed URL生成
     * 【期待結果】HTTP 200 OKとUploadUrlResponseが返却される
     * 【ビジネス要件】Pre-signed URL生成API - 正常系
     */
    @Test
    @DisplayName("Pre-signed URL生成: 正常系、1ファイルで200 OKが返される")
    void shouldReturnOkWithUploadUrls() {
        // Given: 有効なセッションとファイル名
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_ID);

        UploadUrlItem item = new UploadUrlItem(
                "image-id-1",
                "http://minio:9000/images/originals/image-id-1.jpg?presigned",
                "originals/image-id-1.jpg");
        when(imageService.generateUploadUrls(
                eq(List.of("photo.jpg")), any(UUID.class)))
                .thenReturn(List.of(item));

        UploadUrlRequest request = new UploadUrlRequest(
                List.of("photo.jpg"), VALID_SESSION);

        // When: Pre-signed URL生成APIを実行
        Response response = imageResource.generateUploadUrls(
                ARTIST_ID, THREAD_ID, request);

        // Then: HTTP 200 OKとUploadUrlResponseが返却される
        assertEquals(Response.Status.OK.getStatusCode(),
                response.getStatus());
        UploadUrlResponse body = (UploadUrlResponse) response.getEntity();
        assertNotNull(body);
        assertEquals(1, body.uploads().size());
        assertEquals("image-id-1", body.uploads().get(0).imageId());
        verify(imageService).generateUploadUrls(
                eq(List.of("photo.jpg")), any(UUID.class));
    }

    /**
     * 【テスト対象】ImageResource#generateUploadUrls
     * 【テストケース】セッションIDがnullの場合
     * 【期待結果】HTTP 401 Unauthorizedが返却される
     * 【ビジネス要件】Pre-signed URL生成API - 認証なし
     */
    @Test
    @DisplayName("Pre-signed URL生成: セッションIDがnullの場合、401が返される")
    void shouldReturnUnauthorizedWhenSessionIdIsNull() {
        // Given: セッションIDがnull
        UploadUrlRequest request = new UploadUrlRequest(
                List.of("photo.jpg"), null);

        // When: Pre-signed URL生成APIを実行
        Response response = imageResource.generateUploadUrls(
                ARTIST_ID, THREAD_ID, request);

        // Then: HTTP 401 Unauthorizedが返却される
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
                response.getStatus());
    }

    /**
     * 【テスト対象】ImageResource#generateUploadUrls
     * 【テストケース】セッションIDが空文字の場合
     * 【期待結果】HTTP 401 Unauthorizedが返却される
     * 【ビジネス要件】Pre-signed URL生成API - 空セッション
     */
    @Test
    @DisplayName("Pre-signed URL生成: セッションIDが空文字の場合、401が返される")
    void shouldReturnUnauthorizedWhenSessionIdIsBlank() {
        // Given: セッションIDが空文字
        UploadUrlRequest request = new UploadUrlRequest(
                List.of("photo.jpg"), "  ");

        // When: Pre-signed URL生成APIを実行
        Response response = imageResource.generateUploadUrls(
                ARTIST_ID, THREAD_ID, request);

        // Then: HTTP 401 Unauthorizedが返却される
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
                response.getStatus());
    }

    /**
     * 【テスト対象】ImageResource#generateUploadUrls
     * 【テストケース】無効なセッションIDの場合
     * 【期待結果】HTTP 401 Unauthorizedが返却される
     * 【ビジネス要件】Pre-signed URL生成API - 無効セッション
     */
    @Test
    @DisplayName("Pre-signed URL生成: 無効なセッションIDの場合、401が返される")
    void shouldReturnUnauthorizedWhenSessionIsInvalid() {
        // Given: セッションが無効
        when(sessionService.getUserIdBySession("invalid-session"))
                .thenReturn(null);

        UploadUrlRequest request = new UploadUrlRequest(
                List.of("photo.jpg"), "invalid-session");

        // When: Pre-signed URL生成APIを実行
        Response response = imageResource.generateUploadUrls(
                ARTIST_ID, THREAD_ID, request);

        // Then: HTTP 401 Unauthorizedが返却される
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
                response.getStatus());
    }

    /**
     * 【テスト対象】ImageResource#generateUploadUrls
     * 【テストケース】ファイル名リストがnullの場合
     * 【期待結果】HTTP 400 Bad Requestが返却される
     * 【ビジネス要件】Pre-signed URL生成API - ファイル名なし
     */
    @Test
    @DisplayName("Pre-signed URL生成: ファイル名がnullの場合、400が返される")
    void shouldReturnBadRequestWhenFileNamesIsNull() {
        // Given: ファイル名がnull
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_ID);

        UploadUrlRequest request = new UploadUrlRequest(
                null, VALID_SESSION);

        // When: Pre-signed URL生成APIを実行
        Response response = imageResource.generateUploadUrls(
                ARTIST_ID, THREAD_ID, request);

        // Then: HTTP 400 Bad Requestが返却される
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus());
    }

    /**
     * 【テスト対象】ImageResource#generateUploadUrls
     * 【テストケース】ファイル名リストが空の場合
     * 【期待結果】HTTP 400 Bad Requestが返却される
     * 【ビジネス要件】Pre-signed URL生成API - 空リスト
     */
    @Test
    @DisplayName("Pre-signed URL生成: ファイル名が空リストの場合、400が返される")
    void shouldReturnBadRequestWhenFileNamesIsEmpty() {
        // Given: ファイル名が空リスト
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_ID);

        UploadUrlRequest request = new UploadUrlRequest(
                List.of(), VALID_SESSION);

        // When: Pre-signed URL生成APIを実行
        Response response = imageResource.generateUploadUrls(
                ARTIST_ID, THREAD_ID, request);

        // Then: HTTP 400 Bad Requestが返却される
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus());
    }

    /**
     * 【テスト対象】ImageResource#generateUploadUrls
     * 【テストケース】ファイル名が5件以上の場合
     * 【期待結果】HTTP 400 Bad Requestが返却される
     * 【ビジネス要件】Pre-signed URL生成API - 上限超過
     */
    @Test
    @DisplayName("Pre-signed URL生成: ファイル名が5件以上の場合、400が返される")
    void shouldReturnBadRequestWhenTooManyFiles() {
        // Given: ファイル名が5件
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_ID);

        UploadUrlRequest request = new UploadUrlRequest(
                List.of("a.jpg", "b.jpg", "c.jpg", "d.jpg", "e.jpg"),
                VALID_SESSION);

        // When: Pre-signed URL生成APIを実行
        Response response = imageResource.generateUploadUrls(
                ARTIST_ID, THREAD_ID, request);

        // Then: HTTP 400 Bad Requestが返却される
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus());
    }

    /**
     * 【テスト対象】ImageResource#generateUploadUrls
     * 【テストケース】ImageServiceが例外をスローした場合
     * 【期待結果】HTTP 400 Bad Requestが返却される
     * 【ビジネス要件】Pre-signed URL生成API - サービス例外
     */
    @Test
    @DisplayName("Pre-signed URL生成: ImageServiceが例外をスローした場合、400が返される")
    void shouldReturnBadRequestWhenServiceThrowsException() {
        // Given: ImageServiceが例外をスロー
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_ID);
        when(imageService.generateUploadUrls(
                any(), any(UUID.class)))
                .thenThrow(new IllegalArgumentException("テストエラー"));

        UploadUrlRequest request = new UploadUrlRequest(
                List.of("photo.jpg"), VALID_SESSION);

        // When: Pre-signed URL生成APIを実行
        Response response = imageResource.generateUploadUrls(
                ARTIST_ID, THREAD_ID, request);

        // Then: HTTP 400 Bad Requestが返却される
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus());
    }

    /**
     * 【テスト対象】ImageResource#generateUploadUrls
     * 【テストケース】正常系: 4ファイルのPre-signed URL生成
     * 【期待結果】HTTP 200 OKと4件のUploadUrlItemが返却される
     * 【ビジネス要件】Pre-signed URL生成API - 最大件数
     */
    @Test
    @DisplayName("Pre-signed URL生成: 4ファイルで200 OKと4件のアイテムが返される")
    void shouldReturnOkWithFourUploadUrls() {
        // Given: 有効なセッションと4ファイル
        when(sessionService.getUserIdBySession(VALID_SESSION))
                .thenReturn(USER_ID);

        List<UploadUrlItem> items = List.of(
                new UploadUrlItem("id1", "url1", "key1"),
                new UploadUrlItem("id2", "url2", "key2"),
                new UploadUrlItem("id3", "url3", "key3"),
                new UploadUrlItem("id4", "url4", "key4"));
        List<String> fileNames = List.of(
                "a.jpg", "b.png", "c.gif", "d.jpeg");
        when(imageService.generateUploadUrls(
                eq(fileNames), any(UUID.class)))
                .thenReturn(items);

        UploadUrlRequest request = new UploadUrlRequest(
                fileNames, VALID_SESSION);

        // When: Pre-signed URL生成APIを実行
        Response response = imageResource.generateUploadUrls(
                ARTIST_ID, THREAD_ID, request);

        // Then: HTTP 200 OKと4件のアイテムが返却される
        assertEquals(Response.Status.OK.getStatusCode(),
                response.getStatus());
        UploadUrlResponse body = (UploadUrlResponse) response.getEntity();
        assertEquals(4, body.uploads().size());
    }
}
