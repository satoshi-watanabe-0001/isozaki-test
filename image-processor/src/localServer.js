/**
 * ローカル開発用エントリーポイント
 *
 * Expressサーバを起動し、MinIO Webhook通知を受信して
 * 共通画像処理ロジック（imageProcessor）を呼び出す。
 * docker-composeで image-processor サービスとして起動される。
 *
 * @since 1.4
 */

import express from "express";
import { createS3Client, processImage } from "./imageProcessor.js";

const PORT = process.env.PORT || 3001;

/** S3クライアント（MinIOに接続） */
const s3Client = createS3Client({
  endpoint: process.env.S3_ENDPOINT || "http://minio:9000",
  region: process.env.AWS_REGION || "ap-northeast-1",
  accessKeyId: process.env.AWS_ACCESS_KEY_ID || "minioadmin",
  secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY || "minioadmin",
});

const app = express();
app.use(express.json());

/**
 * ヘルスチェックエンドポイント
 */
app.get("/health", (_req, res) => {
  res.json({ status: "ok" });
});

/**
 * MinIO Webhook通知受信エンドポイント
 *
 * MinIOからのPUTイベント通知を受信し、画像処理を実行する。
 */
app.post("/webhook", async (req, res) => {
  try {
    const event = req.body;
    console.log("MinIO Webhook受信:", JSON.stringify(event));

    // MinIOイベント形式からバケット名とキーを抽出
    const records = event.Records || [];
    for (const record of records) {
      const bucket = record.s3?.bucket?.name;
      const key = record.s3?.object?.key;

      if (bucket && key) {
        const decodedKey = decodeURIComponent(key.replace(/\+/g, " "));
        await processImage(s3Client, bucket, decodedKey);
      }
    }

    res.json({ status: "ok" });
  } catch (error) {
    console.error("Webhook処理エラー:", error);
    res.status(500).json({ status: "error", message: error.message });
  }
});

app.listen(PORT, () => {
  console.log(`image-processor ローカルサーバ起動: ポート ${PORT}`);
});
