/**
 * スレッド一覧ページコンポーネント
 *
 * アーティストコミュニティのスレッド一覧を表示する。
 * スレッドタイトル、作成ユーザ名、最新コメント、書き込み日時を表示し、
 * 最新書き込み日時の降順でソートする。ページング対応（20件/ページ）。
 *
 * @since 1.3
 */
"use client";

import { useState, useEffect, useCallback, type ReactNode } from "react";
import { useParams, notFound } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/contexts/AuthContext";
import { formatRelativeDate } from "@/utils/dateFormat";
import CreateThreadModal from "@/components/CreateThreadModal";
import LoginPromptDialog from "@/components/LoginPromptDialog";
import toast, { Toaster } from "react-hot-toast";
import type { ThreadListResponse } from "@/types/thread";

/** バックエンドAPIのベースURL */
const BACKEND_URL: string =
  process.env.NEXT_PUBLIC_BACKEND_URL ?? "http://localhost:8080";

/** 1ページあたりの表示件数 */
const PAGE_SIZE = 20;

/**
 * スレッド一覧ページコンポーネント
 *
 * マウント時にバックエンドAPIからスレッド一覧を取得し、
 * ページング付きでスレッドを一覧表示する。
 * ログイン済みユーザはFABからスレッド作成モーダルを開くことができる。
 *
 * @returns スレッド一覧ページのJSX要素
 */
export default function ThreadListPage(): ReactNode {
  const params = useParams();
  const artistId: string = params.artistId as string;
  const { user, isLoggedIn } = useAuth();

  const [threadData, setThreadData] = useState<ThreadListResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [notFoundFlag, setNotFoundFlag] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState<number>(1);

  const [isCreateModalOpen, setIsCreateModalOpen] = useState<boolean>(false);
  const [isLoginDialogOpen, setIsLoginDialogOpen] = useState<boolean>(false);

  /**
   * スレッド一覧データを取得する
   */
  const fetchThreads = useCallback(
    async (page: number): Promise<void> => {
      setIsLoading(true);
      try {
        const response: Response = await fetch(
          `${BACKEND_URL}/api/v1/community/${artistId}/threads?page=${page}&size=${PAGE_SIZE}`,
        );
        if (response.status === 404) {
          setNotFoundFlag(true);
          return;
        }
        if (!response.ok) {
          throw new Error(
            `スレッド一覧の取得に失敗しました（${response.status}）`,
          );
        }
        const data: ThreadListResponse = await response.json();
        setThreadData(data);
      } catch (err) {
        const errorMessage: string =
          err instanceof Error ? err.message : "不明なエラーが発生しました";
        setError(errorMessage);
      } finally {
        setIsLoading(false);
      }
    },
    [artistId],
  );

  useEffect(() => {
    void fetchThreads(currentPage);
  }, [currentPage, fetchThreads]);

  /**
   * FABクリック時の処理（ログイン状態に応じてモーダルまたはダイアログを表示）
   */
  const handleFabClick = useCallback((): void => {
    if (isLoggedIn) {
      setIsCreateModalOpen(true);
    } else {
      setIsLoginDialogOpen(true);
    }
  }, [isLoggedIn]);

  /**
   * スレッド作成成功時のコールバック
   */
  const handleThreadCreated = useCallback((): void => {
    setIsCreateModalOpen(false);
    toast.success("スレッドを作成しました", { duration: 3000 });
    // 最新のスレッド一覧を再取得（1ページ目に戻る）
    setCurrentPage(1);
    void fetchThreads(1);
  }, [fetchThreads]);

  /**
   * ページ遷移処理
   */
  const handlePageChange = useCallback((page: number): void => {
    setCurrentPage(page);
  }, []);

  if (notFoundFlag) {
    notFound();
  }

  if (error) {
    throw new Error(error);
  }

  return (
    <div className="flex flex-1 flex-col bg-zinc-50 dark:bg-black">
      <Toaster position="top-center" />
      <main className="mx-auto w-full max-w-3xl px-4 py-8 sm:px-6 lg:px-8">
        <h1
          className="mb-6 text-xl font-bold text-gray-900 dark:text-zinc-50"
          data-testid="thread-list-title"
        >
          スレッド一覧
        </h1>

        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <p className="text-gray-500" data-testid="loading-indicator">
              読み込み中...
            </p>
          </div>
        ) : threadData && threadData.threads.length > 0 ? (
          <>
            <ul
              className="divide-y divide-gray-200 dark:divide-zinc-700"
              data-testid="thread-list"
            >
              {threadData.threads.map((thread) => (
                <li key={thread.threadId}>
                  <Link
                    href={`/community/${artistId}/threads/${thread.threadId}`}
                    className="block px-2 py-4 transition-colors hover:bg-gray-100 dark:hover:bg-zinc-800"
                    data-testid={`thread-item-${thread.threadId}`}
                  >
                    <div className="flex items-start justify-between">
                      <div className="min-w-0 flex-1">
                        <p
                          className="text-sm font-semibold text-gray-900 dark:text-zinc-50"
                          data-testid={`thread-title-${thread.threadId}`}
                        >
                          {thread.title}
                        </p>
                        <p className="mt-1 text-xs text-gray-500">
                          {thread.createdByUsername}
                        </p>
                        {thread.latestComment !== null && (
                          <p
                            className="mt-1 truncate text-xs text-gray-600 dark:text-gray-400"
                            data-testid={`thread-latest-comment-${thread.threadId}`}
                          >
                            {thread.latestComment}
                          </p>
                        )}
                      </div>
                      {thread.latestCommentAt !== null && (
                        <span
                          className="ml-2 shrink-0 text-xs text-gray-400"
                          data-testid={`thread-date-${thread.threadId}`}
                        >
                          {formatRelativeDate(thread.latestCommentAt)}
                        </span>
                      )}
                    </div>
                  </Link>
                </li>
              ))}
            </ul>

            {/* ページング */}
            {threadData.totalPages > 1 && (
              <div
                className="mt-6 flex items-center justify-center gap-2"
                data-testid="pagination"
              >
                <button
                  type="button"
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage <= 1}
                  className="rounded-md border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-zinc-600 dark:text-gray-300"
                  data-testid="pagination-prev"
                >
                  前へ
                </button>
                <span className="text-sm text-gray-600 dark:text-gray-400">
                  {currentPage} / {threadData.totalPages}
                </span>
                <button
                  type="button"
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage >= threadData.totalPages}
                  className="rounded-md border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-zinc-600 dark:text-gray-300"
                  data-testid="pagination-next"
                >
                  次へ
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="py-12 text-center">
            <p
              className="text-gray-500"
              data-testid="thread-list-empty"
            >
              スレッドはまだありません
            </p>
          </div>
        )}
      </main>

      {/* スレッド作成FAB */}
      <button
        type="button"
        onClick={handleFabClick}
        className="fixed bottom-6 right-6 z-40 flex h-14 w-14 items-center justify-center rounded-full bg-blue-600 text-white shadow-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        aria-label="スレッドを作成"
        data-testid="create-thread-fab"
      >
        <svg
          className="h-6 w-6"
          fill="none"
          viewBox="0 0 24 24"
          strokeWidth="2"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L6.832 19.82a4.5 4.5 0 0 1-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 0 1 1.13-1.897L16.863 4.487Zm0 0L19.5 7.125"
          />
        </svg>
      </button>

      {/* スレッド作成モーダル */}
      <CreateThreadModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        artistId={artistId}
        sessionId={user?.sessionId ?? ""}
        onCreated={handleThreadCreated}
      />

      {/* ログイン促進ダイアログ */}
      <LoginPromptDialog
        isOpen={isLoginDialogOpen}
        onClose={() => setIsLoginDialogOpen(false)}
      />
    </div>
  );
}
