/**
 * フロントエンド環境変数の型定義
 *
 * Next.jsで使用する環境変数の型安全性を保証する。
 */

declare namespace NodeJS {
  interface ProcessEnv {
    /** バックエンドAPIのベースURL（ビルド時に埋め込まれる） */
    readonly NEXT_PUBLIC_BACKEND_URL?: string;
    readonly NODE_ENV: "development" | "production" | "test";
  }
}
