/**
 * Lambda用エントリーポイント
 *
 * S3 PUTイベントを受信し、共通画像処理ロジック（imageProcessor）を呼び出す。
 * AWS SAMテンプレートで Handler: src/lambdaHandler.handler として指定される。
 *
 * @since 1.4
 */

import { createS3Client, processImage } from "./imageProcessor.js";

/** S3クライアント（Lambda起動時に1回だけ初期化） */
const s3Client = createS3Client({
  region: process.env.AWS_REGION || "ap-northeast-1",
  accessKeyId: process.env.AWS_ACCESS_KEY_ID,
  secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY,
});

/**
 * Lambda関数ハンドラ
 *
 * S3イベント通知からバケット名とキーを抽出し、画像処理を実行する。
 *
 * @param {object} event - S3イベント
 * @returns {object} 処理結果
 */
export const handler = async (event) => {
  console.log("Lambda呼び出し:", JSON.stringify(event));

  const results = [];

  for (const record of event.Records) {
    const bucket = record.s3.bucket.name;
    const key = decodeURIComponent(record.s3.object.key.replace(/\+/g, " "));

    try {
      await processImage(s3Client, bucket, key);
      results.push({ bucket, key, status: "success" });
    } catch (error) {
      console.error(`画像処理エラー: bucket=${bucket}, key=${key}`, error);
      results.push({ bucket, key, status: "error", message: error.message });
    }
  }

  return { statusCode: 200, body: JSON.stringify(results) };
};
