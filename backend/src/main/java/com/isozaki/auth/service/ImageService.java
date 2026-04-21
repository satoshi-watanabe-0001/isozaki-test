/**
 * 画像サービスクラス
 *
 * <p>画像アップロード機能のビジネスロジックを担当するサービス。
 * S3/MinIOへのPre-signed URL生成、PENDING/CONFIRMEDステータス管理、
 * 定期クリーンアップ処理を提供する。</p>
 *
 * @since 1.4
 */

package com.isozaki.auth.service;

import com.isozaki.auth.dto.CommentImageResponse;
import com.isozaki.auth.dto.UploadUrlItem;
import com.isozaki.auth.entity.CommentImageEntity;
import com.isozaki.auth.repository.CommentImageRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * 画像アップロード・管理サービス
 *
 * <p>S3クライアントを使用してPre-signed URL生成、
 * PENDING→CONFIRMED遷移、定期クリーンアップを行う。</p>
 *
 * @since 1.4
 */
@ApplicationScoped
public class ImageService {

    private static final Logger LOG = Logger.getLogger(ImageService.class);

    /** アップロード1回あたりの最大画像数 */
    private static final int MAX_UPLOAD_COUNT = 4;

    /** Pre-signed URLの有効期限（分） */
    private static final int PRESIGN_EXPIRATION_MINUTES = 15;

    /** PENDING画像のクリーンアップ閾値（時間） */
    private static final int CLEANUP_THRESHOLD_HOURS = 1;

    private final CommentImageRepository commentImageRepository;
    private final UuidService uuidService;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final String bucket;
    private final String s3Endpoint;

    /**
     * 画像サービスを初期化する
     *
     * @param commentImageRepository 画像リポジトリ
     * @param uuidService            UUID生成サービス
     * @param s3Endpoint             S3/MinIOエンドポイント
     * @param bucket                 S3バケット名
     * @param accessKey              アクセスキー
     * @param secretKey              シークレットキー
     * @param region                 リージョン
     */
    @Inject
    public ImageService(
            CommentImageRepository commentImageRepository,
            UuidService uuidService,
            @ConfigProperty(name = "s3.endpoint") String s3Endpoint,
            @ConfigProperty(name = "s3.bucket") String bucket,
            @ConfigProperty(name = "s3.access-key") String accessKey,
            @ConfigProperty(name = "s3.secret-key") String secretKey,
            @ConfigProperty(name = "s3.region") String region) {
        this.commentImageRepository = commentImageRepository;
        this.uuidService = uuidService;
        this.bucket = bucket;
        this.s3Endpoint = s3Endpoint;

        StaticCredentialsProvider credentialsProvider =
                StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey));

        this.s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(s3Endpoint))
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(
                        software.amazon.awssdk.services.s3.S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build())
                .build();

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(s3Endpoint))
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(
                        software.amazon.awssdk.services.s3.S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build())
                .build();
    }

    /**
     * テスト用コンストラクタ（S3クライアントを直接注入）
     *
     * @param commentImageRepository 画像リポジトリ
     * @param uuidService            UUID生成サービス
     * @param s3Presigner            S3 Presigner
     * @param s3Client               S3クライアント
     * @param bucket                 バケット名
     * @param s3Endpoint             エンドポイント
     */
    ImageService(
            CommentImageRepository commentImageRepository,
            UuidService uuidService,
            S3Presigner s3Presigner,
            S3Client s3Client,
            String bucket,
            String s3Endpoint) {
        this.commentImageRepository = commentImageRepository;
        this.uuidService = uuidService;
        this.s3Presigner = s3Presigner;
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.s3Endpoint = s3Endpoint;
    }

    /**
     * Pre-signed URLを生成し、PENDING画像レコードを作成する
     *
     * <p>ファイル名ごとにUUIDv7ベースのS3キーを生成し、
     * PUTメソッド用のPre-signed URLを返却する。
     * 同時にPENDINGステータスの画像レコードをDBに作成する。</p>
     *
     * @param fileNames  ファイル名リスト（最大4件）
     * @param uploadedBy アップロードユーザID
     * @return Pre-signed URL情報のリスト
     */
    @Transactional
    public List<UploadUrlItem> generateUploadUrls(
            List<String> fileNames, UUID uploadedBy) {
        if (fileNames.size() > MAX_UPLOAD_COUNT) {
            throw new IllegalArgumentException(
                    "アップロード画像数は最大" + MAX_UPLOAD_COUNT + "件です");
        }

        List<UploadUrlItem> items = new ArrayList<>();
        Instant now = Instant.now();

        for (String fileName : fileNames) {
            String imageId = uuidService.generateUuidV7();
            String extension = extractExtension(fileName);
            String s3Key = "originals/" + imageId + "." + extension;

            // Pre-signed URL生成
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(PRESIGN_EXPIRATION_MINUTES))
                    .putObjectRequest(putRequest)
                    .build();

            String uploadUrl = s3Presigner.presignPutObject(presignRequest)
                    .url().toString();

            // PENDING画像レコード作成
            CommentImageEntity entity = new CommentImageEntity();
            entity.imageId = UUID.fromString(imageId);
            entity.s3Key = s3Key;
            entity.status = "PENDING";
            entity.uploadedBy = uploadedBy;
            entity.createdAt = now;
            commentImageRepository.persist(entity);

            items.add(new UploadUrlItem(imageId, uploadUrl, s3Key));
        }

        return items;
    }

    /**
     * PENDING画像をCONFIRMEDに遷移させる（コメント紐付け）
     *
     * <p>指定された画像IDリストのうち、アップロードユーザが一致し
     * PENDING状態のものをCONFIRMEDに更新する。</p>
     *
     * @param imageIds   画像IDリスト
     * @param commentId  コメントID
     * @param uploadedBy アップロードユーザID
     * @return 紐付けに成功した画像数
     */
    @Transactional
    public int confirmImages(List<UUID> imageIds, UUID commentId, UUID uploadedBy) {
        List<CommentImageEntity> pendingImages =
                commentImageRepository.findPendingByIdsAndUser(imageIds, uploadedBy);

        for (CommentImageEntity image : pendingImages) {
            image.commentId = commentId;
            image.status = "CONFIRMED";
            commentImageRepository.persist(image);
        }

        return pendingImages.size();
    }

    /**
     * コメントIDリストに紐づく画像情報をまとめて取得する（N+1回避）
     *
     * <p>コメントIDをキーとした画像レスポンスのマップを返却する。</p>
     *
     * @param commentIds コメントIDリスト
     * @return コメントIDをキー、画像レスポンスリストを値とするマップ
     */
    public Map<UUID, List<CommentImageResponse>> getImagesByCommentIds(
            List<UUID> commentIds) {
        List<CommentImageEntity> images =
                commentImageRepository.findByCommentIds(commentIds);

        return images.stream()
                .collect(Collectors.groupingBy(
                        img -> img.commentId,
                        Collectors.mapping(this::toImageResponse, Collectors.toList())));
    }

    /**
     * PENDING画像の定期クリーンアップ
     *
     * <p>1時間以上PENDING状態のままの画像レコードとS3オブジェクトを削除する。
     * Quarkus @Scheduledアノテーションで1時間ごとに自動実行される。</p>
     */
    @Scheduled(every = "1h")
    @Transactional
    void cleanupExpiredPendingImages() {
        Instant threshold = Instant.now().minusSeconds(
                (long) CLEANUP_THRESHOLD_HOURS * 3600);
        List<CommentImageEntity> expiredImages =
                commentImageRepository.findExpiredPending(threshold);

        if (expiredImages.isEmpty()) {
            return;
        }

        LOG.infof("PENDING画像クリーンアップ開始: %d件", expiredImages.size());

        for (CommentImageEntity image : expiredImages) {
            // S3オブジェクト削除（originals/ + thumbnails/ + display/）
            deleteS3Object(image.s3Key);
            String imageId = image.imageId.toString();
            deleteS3Object("thumbnails/" + imageId + ".webp");
            deleteS3Object("display/" + imageId + ".webp");

            // DBレコード削除
            commentImageRepository.delete(image);
        }

        LOG.infof("PENDING画像クリーンアップ完了: %d件削除", expiredImages.size());
    }

    /**
     * S3オブジェクトを削除する（エラー時はログ出力のみ）
     *
     * @param key S3オブジェクトキー
     */
    private void deleteS3Object(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            LOG.warnf("S3オブジェクト削除失敗: key=%s, error=%s", key, e.getMessage());
        }
    }

    /**
     * 画像エンティティをレスポンスDTOに変換する
     *
     * @param entity 画像エンティティ
     * @return 画像レスポンスDTO
     */
    private CommentImageResponse toImageResponse(CommentImageEntity entity) {
        String imageId = entity.imageId.toString();
        String baseUrl = s3Endpoint + "/" + bucket;
        return new CommentImageResponse(
                imageId,
                baseUrl + "/thumbnails/" + imageId + ".webp",
                baseUrl + "/display/" + imageId + ".webp"
        );
    }

    /**
     * ファイル名から拡張子を抽出する
     *
     * @param fileName ファイル名
     * @return 拡張子（小文字、デフォルトはjpg）
     */
    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "jpg";
    }
}
