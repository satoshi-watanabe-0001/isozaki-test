/**
 * 共通画像処理ロジック
 *
 * S3/MinIOから元画像を取得し、サムネイル（400px）と表示用（1200px）の
 * WebP画像を生成してS3/MinIOに保存する。
 * Lambda用エントリーポイント（lambdaHandler.js）と
 * ローカル用エントリーポイント（localServer.js）の両方から呼び出される。
 *
 * @since 1.4
 */

import { S3Client, GetObjectCommand, PutObjectCommand } from "@aws-sdk/client-s3";
import sharp from "sharp";

/** サムネイル画像の横幅（px） */
const THUMBNAIL_WIDTH = 400;

/** 表示用画像の横幅（px） */
const DISPLAY_WIDTH = 1200;

/** サムネイル画像のWebP品質 */
const THUMBNAIL_QUALITY = 80;

/** 表示用画像のWebP品質 */
const DISPLAY_QUALITY = 85;

/**
 * S3クライアントを生成する
 *
 * @param {object} options - S3クライアント設定
 * @param {string} options.endpoint - S3/MinIOエンドポイント
 * @param {string} options.region - AWSリージョン
 * @param {string} options.accessKeyId - アクセスキーID
 * @param {string} options.secretAccessKey - シークレットアクセスキー
 * @returns {S3Client} S3クライアント
 */
export function createS3Client({ endpoint, region, accessKeyId, secretAccessKey }) {
  const config = {
    region: region || "ap-northeast-1",
    credentials: {
      accessKeyId: accessKeyId || "minioadmin",
      secretAccessKey: secretAccessKey || "minioadmin",
    },
  };

  if (endpoint) {
    config.endpoint = endpoint;
    config.forcePathStyle = true;
  }

  return new S3Client(config);
}

/**
 * 画像を処理する（リサイズ・WebP変換）
 *
 * originals/プレフィックスの元画像を取得し、
 * thumbnails/とdisplay/プレフィックスにWebP変換した画像を保存する。
 *
 * @param {S3Client} s3Client - S3クライアント
 * @param {string} bucket - バケット名
 * @param {string} key - 元画像のS3キー（例: originals/xxx.jpg）
 */
export async function processImage(s3Client, bucket, key) {
  // originals/プレフィックスの画像のみ処理対象
  if (!key.startsWith("originals/")) {
    console.log(`スキップ: originals/プレフィックス以外のキー: ${key}`);
    return;
  }

  // 画像IDを抽出（例: originals/abc-123.jpg → abc-123）
  const fileName = key.replace("originals/", "");
  const imageId = fileName.replace(/\.[^.]+$/, "");

  console.log(`画像処理開始: bucket=${bucket}, key=${key}, imageId=${imageId}`);

  // 元画像を取得
  const getCommand = new GetObjectCommand({ Bucket: bucket, Key: key });
  const response = await s3Client.send(getCommand);
  const imageBuffer = Buffer.from(await response.Body.transformToByteArray());

  // サムネイル画像の生成（400px幅、WebP、品質80）
  const thumbnailBuffer = await sharp(imageBuffer)
    .resize({ width: THUMBNAIL_WIDTH, withoutEnlargement: true })
    .webp({ quality: THUMBNAIL_QUALITY })
    .toBuffer();

  // 表示用画像の生成（1200px幅、WebP、品質85）
  const displayBuffer = await sharp(imageBuffer)
    .resize({ width: DISPLAY_WIDTH, withoutEnlargement: true })
    .webp({ quality: DISPLAY_QUALITY })
    .toBuffer();

  // サムネイル画像をS3に保存
  const thumbnailKey = `thumbnails/${imageId}.webp`;
  await s3Client.send(new PutObjectCommand({
    Bucket: bucket,
    Key: thumbnailKey,
    Body: thumbnailBuffer,
    ContentType: "image/webp",
  }));

  // 表示用画像をS3に保存
  const displayKey = `display/${imageId}.webp`;
  await s3Client.send(new PutObjectCommand({
    Bucket: bucket,
    Key: displayKey,
    Body: displayBuffer,
    ContentType: "image/webp",
  }));

  console.log(`画像処理完了: thumbnails/${imageId}.webp, display/${imageId}.webp`);
}
