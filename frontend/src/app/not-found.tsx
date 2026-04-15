/**
 * 共通404エラーページ
 *
 * 存在しないページにアクセスした場合に表示する。
 * 「404 Not Found」と「ページが見つかりません」のメッセージを縦に並べて表示する。
 *
 * @since 1.3
 */
import type { ReactNode } from "react";
import Link from "next/link";

/**
 * 共通404エラーページコンポーネント
 *
 * Next.js App Routerの規約により、存在しないパスへのアクセス時に自動表示される。
 *
 * @returns 404エラーページのJSX要素
 */
export default function NotFound(): ReactNode {
  return (
    <div className="flex flex-1 flex-col items-center justify-center bg-zinc-50 dark:bg-black">
      <h1
        className="text-4xl font-bold text-gray-900 dark:text-zinc-50"
        data-testid="not-found-title"
      >
        404 Not Found
      </h1>
      <p
        className="mt-4 text-lg text-gray-600 dark:text-gray-400"
        data-testid="not-found-message"
      >
        ページが見つかりません
      </p>
      <Link
        href="/"
        className="mt-8 text-sm font-medium text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
        data-testid="not-found-home-link"
      >
        TOPページへ戻る
      </Link>
    </div>
  );
}
