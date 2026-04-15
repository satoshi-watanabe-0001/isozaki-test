/**
 * トップページコンポーネント
 *
 * frontendアプリケーションのトップページ。
 * 「ようこそEntm-Cloneへ」のウェルカムメッセージと
 * アーティスト一覧ページへのリンクを表示する。
 *
 * @since 1.1
 */
import Link from "next/link";

export default function Home() {
  return (
    <div className="flex flex-col flex-1 items-center justify-center bg-zinc-50 font-sans dark:bg-black">
      <main className="flex flex-1 w-full max-w-3xl flex-col items-center justify-center py-32 px-16 bg-white dark:bg-black">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-zinc-50">
          ようこそEntm-Cloneへ
        </h1>
        <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
          frontendテストページ
        </p>
        <nav className="mt-8">
          <Link
            href="/artists"
            className="inline-block rounded-md bg-blue-600 px-6 py-2.5 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            アーティスト一覧
          </Link>
        </nav>
      </main>
    </div>
  );
}
