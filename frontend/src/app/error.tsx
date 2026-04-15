/**
 * 共通エラーページ
 *
 * APIで5XX系エラーが発生した場合など、予期しないエラー時に表示する。
 * 「エラーが発生しました」メッセージと「TOPページへ戻る」リンクを表示する。
 *
 * @since 1.3
 */
"use client";

import type { ReactNode } from "react";
import Link from "next/link";

/**
 * 共通エラーページのProps定義
 */
interface ErrorPageProps {
  /** エラーオブジェクト */
  error: Error & { digest?: string };
  /** リトライ関数 */
  reset: () => void;
}

/**
 * 共通エラーページコンポーネント
 *
 * Next.js App Routerの規約により、レンダリング中にエラーが発生した場合に自動表示される。
 *
 * @param props - エラー情報とリセット関数を含むProps
 * @returns エラーページのJSX要素
 */
export default function ErrorPage({ error: _error, reset: _reset }: ErrorPageProps): ReactNode {
  return (
    <div className="flex flex-1 flex-col items-center justify-center bg-zinc-50 dark:bg-black">
      <h1
        className="text-2xl font-bold text-gray-900 dark:text-zinc-50"
        data-testid="error-page-title"
      >
        エラーが発生しました
      </h1>
      <Link
        href="/"
        className="mt-8 text-sm font-medium text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
        data-testid="error-page-home-link"
      >
        TOPページへ戻る
      </Link>
    </div>
  );
}
