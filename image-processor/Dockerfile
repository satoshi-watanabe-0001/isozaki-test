# image-processor ローカル開発用Dockerfile
# MinIO Webhook通知を受信して画像リサイズ・WebP変換を実行する
FROM node:20-slim

WORKDIR /app

# 依存パッケージのインストール
COPY package.json package-lock.json* ./
RUN npm install --production

# ソースコードのコピー
COPY src/ ./src/

# ヘルスチェック用ポート
EXPOSE 3001

# ローカル用エントリーポイントで起動
CMD ["node", "src/localServer.js"]
