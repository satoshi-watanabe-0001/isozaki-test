/**
 * ImageServiceの単体テスト
 *
 * <p>画像アップロード機能のビジネスロジックをテストする。
 * S3Presigner、S3Client、CommentImageRepositoryはモックを使用する。</p>
 *
 * @since 1.4
 */

package com.isozaki.auth.service;

import com.isozaki.auth.dto.CommentImageResponse;
import com.isozaki.auth.dto.UploadUrlItem;
import com.isozaki.auth.entity.CommentImageEntity;
import com.isozaki.auth.repository.CommentImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ImageServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService テスト")
class ImageServiceTest {

    @Mock
    private CommentImageRepository commentImageRepository;

    @Mock
    private UuidService uuidService;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private S3Client s3Client;

    private ImageService imageService;

    private static final UUID USER_UUID =
            UUID.fromString("01908b7e-1234-7000-8000-000000000001");
    private static final UUID COMMENT_UUID =
            UUID.fromString("01970000-2000-7000-8000-000000000001");
    private static final UUID IMAGE_UUID_1 =
            UUID.fromString("01970000-3000-7000-8000-000000000001");
    private static final UUID IMAGE_UUID_2 =
            UUID.fromString("01970000-3000-7000-8000-000000000002");
    private static final String BUCKET = "images";
    private static final String IMAGE_BASE_URL = "http://localhost:9000/images";

    @BeforeEach
    void setUp() {
        imageService = new ImageService(
                commentImageRepository,
                uuidService,
                s3Presigner,
                s3Client,
                BUCKET,
                IMAGE_BASE_URL);
    }

    // ========== generateUploadUrls テスト ==========

    /**
     * 【テスト対象】ImageService#generateUploadUrls
     * 【テストケース】正常系、1ファイルのPre-signed URL生成
     * 【期待結果】1件のUploadUrlItemが返却され、PENDINGレコードが作成される
     * 【ビジネス要件】Pre-signed URL生成 - 正常系
     */
    @Test
    @DisplayName("Pre-signed URL生成: 正常系、1ファイル")
    void shouldGenerateUploadUrlForSingleFile() throws Exception {
        when(uuidService.generateUuidV7())
                .thenReturn(IMAGE_UUID_1.toString());

        PresignedPutObjectRequest presignedRequest =
                mockPresignedRequest("http://minio:9000/images/originals/" + IMAGE_UUID_1 + ".jpg");
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        doNothing().when(commentImageRepository)
                .persist(any(CommentImageEntity.class));

        List<UploadUrlItem> result = imageService.generateUploadUrls(
                List.of("photo.jpg"), USER_UUID);

        assertEquals(1, result.size());
        assertEquals(IMAGE_UUID_1.toString(), result.get(0).imageId());
        assertTrue(result.get(0).s3Key().startsWith("originals/"));
        assertTrue(result.get(0).s3Key().endsWith(".jpg"));

        // PENDINGレコードが作成されることを確認
        ArgumentCaptor<CommentImageEntity> captor =
                ArgumentCaptor.forClass(CommentImageEntity.class);
        verify(commentImageRepository).persist(captor.capture());
        CommentImageEntity savedEntity = captor.getValue();
        assertEquals("PENDING", savedEntity.status);
        assertEquals(USER_UUID, savedEntity.uploadedBy);
    }

    /**
     * 【テスト対象】ImageService#generateUploadUrls
     * 【テストケース】正常系、複数ファイル（最大4件）
     * 【期待結果】4件のUploadUrlItemが返却される
     * 【ビジネス要件】Pre-signed URL生成 - 複数ファイル
     */
    @Test
    @DisplayName("Pre-signed URL生成: 正常系、4ファイル")
    void shouldGenerateUploadUrlsForMultipleFiles() throws Exception {
        when(uuidService.generateUuidV7())
                .thenReturn(IMAGE_UUID_1.toString())
                .thenReturn(IMAGE_UUID_2.toString())
                .thenReturn(UUID.randomUUID().toString())
                .thenReturn(UUID.randomUUID().toString());

        PresignedPutObjectRequest presignedRequest =
                mockPresignedRequest("http://minio:9000/images/originals/test.jpg");
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        doNothing().when(commentImageRepository)
                .persist(any(CommentImageEntity.class));

        List<String> fileNames = List.of("a.jpg", "b.png", "c.gif", "d.jpeg");
        List<UploadUrlItem> result = imageService.generateUploadUrls(
                fileNames, USER_UUID);

        assertEquals(4, result.size());
        verify(commentImageRepository, times(4))
                .persist(any(CommentImageEntity.class));
    }

    /**
     * 【テスト対象】ImageService#generateUploadUrls
     * 【テストケース】5ファイル以上の場合
     * 【期待結果】IllegalArgumentExceptionがスローされる
     * 【ビジネス要件】Pre-signed URL生成 - 上限超過
     */
    @Test
    @DisplayName("Pre-signed URL生成: 5ファイル以上で例外")
    void shouldThrowExceptionWhenTooManyFiles() {
        List<String> fileNames = List.of("1.jpg", "2.jpg", "3.jpg", "4.jpg", "5.jpg");

        assertThrows(IllegalArgumentException.class,
                () -> imageService.generateUploadUrls(fileNames, USER_UUID));
    }

    /**
     * 【テスト対象】ImageService#generateUploadUrls
     * 【テストケース】拡張子抽出の正確性
     * 【期待結果】ファイル名から拡張子が正しく抽出されS3キーに使用される
     * 【ビジネス要件】Pre-signed URL生成 - 拡張子処理
     */
    @Test
    @DisplayName("Pre-signed URL生成: 拡張子が正しく抽出される")
    void shouldExtractExtensionCorrectly() throws Exception {
        when(uuidService.generateUuidV7())
                .thenReturn(IMAGE_UUID_1.toString());

        PresignedPutObjectRequest presignedRequest =
                mockPresignedRequest("http://minio:9000/images/originals/test.png");
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        doNothing().when(commentImageRepository)
                .persist(any(CommentImageEntity.class));

        List<UploadUrlItem> result = imageService.generateUploadUrls(
                List.of("photo.PNG"), USER_UUID);

        assertTrue(result.get(0).s3Key().endsWith(".png"));
    }

    // ========== confirmImages テスト ==========

    /**
     * 【テスト対象】ImageService#confirmImages
     * 【テストケース】正常系、PENDING画像がCONFIRMEDに遷移する
     * 【期待結果】ステータスがCONFIRMEDに更新される
     * 【ビジネス要件】画像確定 - 正常系
     */
    @Test
    @DisplayName("画像確定: PENDING→CONFIRMEDに遷移する")
    void shouldConfirmPendingImages() {
        CommentImageEntity entity1 = createPendingEntity(IMAGE_UUID_1);
        CommentImageEntity entity2 = createPendingEntity(IMAGE_UUID_2);

        when(commentImageRepository.findPendingByIdsAndUser(
                List.of(IMAGE_UUID_1, IMAGE_UUID_2), USER_UUID))
                .thenReturn(List.of(entity1, entity2));

        int confirmed = imageService.confirmImages(
                List.of(IMAGE_UUID_1, IMAGE_UUID_2), COMMENT_UUID, USER_UUID);

        assertEquals(2, confirmed);
        assertEquals("CONFIRMED", entity1.status);
        assertEquals(COMMENT_UUID, entity1.commentId);
        assertEquals("CONFIRMED", entity2.status);
        assertEquals(COMMENT_UUID, entity2.commentId);
    }

    /**
     * 【テスト対象】ImageService#confirmImages
     * 【テストケース】空のimageIdsリスト
     * 【期待結果】0件が返却される
     * 【ビジネス要件】画像確定 - 空リスト
     */
    @Test
    @DisplayName("画像確定: 空リストで0件")
    void shouldReturnZeroWhenEmptyImageIds() {
        when(commentImageRepository.findPendingByIdsAndUser(
                anyList(), any(UUID.class)))
                .thenReturn(List.of());

        int confirmed = imageService.confirmImages(
                List.of(), COMMENT_UUID, USER_UUID);

        assertEquals(0, confirmed);
    }

    // ========== getImagesByCommentIds テスト ==========

    /**
     * 【テスト対象】ImageService#getImagesByCommentIds
     * 【テストケース】正常系、画像がマップで返却される
     * 【期待結果】コメントIDをキーとした画像リストのマップが返却される
     * 【ビジネス要件】画像取得 - 正常系
     */
    @Test
    @DisplayName("画像取得: コメントIDで画像が取得できる")
    void shouldReturnImagesByCommentIds() {
        CommentImageEntity entity = new CommentImageEntity();
        entity.imageId = IMAGE_UUID_1;
        entity.commentId = COMMENT_UUID;
        entity.s3Key = "originals/" + IMAGE_UUID_1 + ".jpg";
        entity.status = "CONFIRMED";

        when(commentImageRepository.findByCommentIds(List.of(COMMENT_UUID)))
                .thenReturn(List.of(entity));

        Map<UUID, List<CommentImageResponse>> result =
                imageService.getImagesByCommentIds(List.of(COMMENT_UUID));

        assertTrue(result.containsKey(COMMENT_UUID));
        assertEquals(1, result.get(COMMENT_UUID).size());
        CommentImageResponse imageResponse = result.get(COMMENT_UUID).get(0);
        assertEquals(IMAGE_UUID_1.toString(), imageResponse.imageId());
        assertTrue(imageResponse.thumbnailUrl().contains("thumbnails/"));
        assertTrue(imageResponse.displayUrl().contains("display/"));
    }

    /**
     * 【テスト対象】ImageService#getImagesByCommentIds
     * 【テストケース】空のコメントIDリスト
     * 【期待結果】空のマップが返却される
     * 【ビジネス要件】画像取得 - 空リスト
     */
    @Test
    @DisplayName("画像取得: 空リストで空マップ")
    void shouldReturnEmptyMapWhenNoCommentIds() {
        when(commentImageRepository.findByCommentIds(List.of()))
                .thenReturn(List.of());

        Map<UUID, List<CommentImageResponse>> result =
                imageService.getImagesByCommentIds(List.of());

        assertTrue(result.isEmpty());
    }

    // ========== cleanupExpiredPendingImages テスト ==========

    /**
     * 【テスト対象】ImageService#cleanupExpiredPendingImages
     * 【テストケース】期限切れPENDING画像が存在する場合
     * 【期待結果】S3オブジェクトとDBレコードが削除される
     * 【ビジネス要件】クリーンアップ - 正常系
     */
    @Test
    @DisplayName("クリーンアップ: 期限切れPENDING画像が削除される")
    void shouldCleanupExpiredPendingImages() {
        CommentImageEntity expired = new CommentImageEntity();
        expired.imageId = IMAGE_UUID_1;
        expired.s3Key = "originals/" + IMAGE_UUID_1 + ".jpg";
        expired.status = "PENDING";
        expired.createdAt = Instant.now().minusSeconds(7200);

        when(commentImageRepository.findExpiredPending(any(Instant.class)))
                .thenReturn(List.of(expired));
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        imageService.cleanupExpiredPendingImages();

        // S3オブジェクト削除（originals + thumbnails + display）
        verify(s3Client, times(3)).deleteObject(any(DeleteObjectRequest.class));
        // DBレコード削除
        verify(commentImageRepository).delete(expired);
    }

    /**
     * 【テスト対象】ImageService#cleanupExpiredPendingImages
     * 【テストケース】期限切れPENDING画像が存在しない場合
     * 【期待結果】何も削除されない
     * 【ビジネス要件】クリーンアップ - 対象なし
     */
    @Test
    @DisplayName("クリーンアップ: 対象なしで何も削除されない")
    void shouldNotCleanupWhenNoPendingImages() {
        when(commentImageRepository.findExpiredPending(any(Instant.class)))
                .thenReturn(List.of());

        imageService.cleanupExpiredPendingImages();

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
        verify(commentImageRepository, never()).delete(any());
    }

    // ========== ヘルパーメソッド ==========

    /**
     * テスト用のPENDINGエンティティを生成する
     */
    private CommentImageEntity createPendingEntity(UUID imageId) {
        CommentImageEntity entity = new CommentImageEntity();
        entity.imageId = imageId;
        entity.s3Key = "originals/" + imageId + ".jpg";
        entity.status = "PENDING";
        entity.uploadedBy = USER_UUID;
        entity.createdAt = Instant.now();
        return entity;
    }

    /**
     * テスト用のPresignedPutObjectRequestモックを生成する
     */
    private PresignedPutObjectRequest mockPresignedRequest(String urlString)
            throws Exception {
        PresignedPutObjectRequest mock =
                org.mockito.Mockito.mock(PresignedPutObjectRequest.class);
        when(mock.url()).thenReturn(new URL(urlString));
        return mock;
    }
}
