/**
 * スレッド詳細ページコンポーネント
 *
 * スレッドタイトルとコメント一覧を表示する。
 * コメントは書き込み日時の降順で10件ずつ表示し、
 * 「もっと見る」で追加読み込みが可能。
 *
 * @since 1.3
 */
"use client";

import { useState, useEffect, useCallback, type ReactNode } from "react";
import { useParams, notFound } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { formatRelativeDate } from "@/utils/dateFormat";
import AddCommentModal from "@/components/AddCommentModal";
import LoginPromptDialog from "@/components/LoginPromptDialog";
import toast, { Toaster } from "react-hot-toast";
import type { ThreadDetailResponse, ThreadComment } from "@/types/thread";

/** バックエンドAPIのベースURL */
const BACKEND_URL: string =
  process.env.NEXT_PUBLIC_BACKEND_URL ?? "http://localhost:8080";

/** 1ページあたりのコメント件数 */
const COMMENT_PAGE_SIZE = 10;

/**
 * スレッド詳細ページコンポーネント
 *
 * マウント時にバックエンドAPIからスレッド詳細とコメントを取得し、
 * スレッドタイトル・コメント一覧を表示する。
 * ログイン済みユーザはFABからコメントモーダルを開くことができる。
 *
 * @returns スレッド詳細ページのJSX要素
 */
export default function ThreadDetailPage(): ReactNode {
  const params = useParams();
  const artistId: string = params.artistId as string;
  const threadId: string = params.threadId as string;
  const { user, isLoggedIn } = useAuth();

  const [threadData, setThreadData] = useState<ThreadDetailResponse | null>(
    null,
  );
  const [allComments, setAllComments] = useState<ThreadComment[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [notFoundFlag, setNotFoundFlag] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [isLoadingMore, setIsLoadingMore] = useState<boolean>(false);

  const [isCommentModalOpen, setIsCommentModalOpen] = useState<boolean>(false);
  const [isLoginDialogOpen, setIsLoginDialogOpen] = useState<boolean>(false);

  /**
   * スレッド詳細データを取得する（初回読み込み）
   */
  const fetchThreadDetail = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    try {
      const response: Response = await fetch(
        `${BACKEND_URL}/api/v1/community/${artistId}/threads/${threadId}?page=1&size=${COMMENT_PAGE_SIZE}`,
      );
      if (response.status === 404) {
        setNotFoundFlag(true);
        return;
      }
      if (!response.ok) {
        throw new Error(
          `スレッド詳細の取得に失敗しました（${response.status}）`,
        );
      }
      const data: ThreadDetailResponse = await response.json();
      setThreadData(data);
      setAllComments(data.comments);
    } catch (err) {
      const errorMessage: string =
        err instanceof Error ? err.message : "不明なエラーが発生しました";
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  }, [artistId, threadId]);

  useEffect(() => {
    void fetchThreadDetail();
  }, [fetchThreadDetail]);

  /**
   * 「もっと見る」クリック時に追加コメントを読み込む（カーソルベースページング）
   *
   * 現在取得済みの最後のコメントのcommentIdをカーソルとして使用し、
   * それより前（古い）のコメントを取得する。
   * 別セッションでのコメント追加による重複取得を回避する。
   */
  const handleLoadMore = useCallback(async (): Promise<void> => {
    // 最後のコメントのIDをカーソルとして使用
    const lastComment: ThreadComment | undefined =
      allComments[allComments.length - 1];
    if (!lastComment) return;

    setIsLoadingMore(true);
    try {
      const response: Response = await fetch(
        `${BACKEND_URL}/api/v1/community/${artistId}/threads/${threadId}?before=${lastComment.commentId}&size=${COMMENT_PAGE_SIZE}`,
      );
      if (!response.ok) {
        throw new Error(
          `コメントの取得に失敗しました（${response.status}）`,
        );
      }
      const data: ThreadDetailResponse = await response.json();
      setAllComments((prev) => [...prev, ...data.comments]);
      setThreadData(data);
    } catch (err) {
      const errorMessage: string =
        err instanceof Error ? err.message : "不明なエラーが発生しました";
      setError(errorMessage);
    } finally {
      setIsLoadingMore(false);
    }
  }, [artistId, threadId, allComments]);

  /**
   * FABクリック時の処理（ログイン状態に応じてモーダルまたはダイアログを表示）
   */
  const handleFabClick = useCallback((): void => {
    if (isLoggedIn) {
      setIsCommentModalOpen(true);
    } else {
      setIsLoginDialogOpen(true);
    }
  }, [isLoggedIn]);

  /**
   * コメント追加成功時のコールバック
   */
  const handleCommentAdded = useCallback((): void => {
    setIsCommentModalOpen(false);
    toast.success("コメントを投稿しました", { duration: 3000 });
    // 最新のコメントを再取得
    void fetchThreadDetail();
  }, [fetchThreadDetail]);

  /** コメントが全件表示されているかどうか */
  const hasMoreComments: boolean =
    threadData !== null && allComments.length < threadData.totalComments;

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
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <p className="text-gray-500" data-testid="loading-indicator">
              読み込み中...
            </p>
          </div>
        ) : threadData !== null ? (
          <>
            {/* スレッドタイトル */}
            <h1
              className="mb-6 text-xl font-bold text-gray-900 dark:text-zinc-50"
              data-testid="thread-detail-title"
            >
              {threadData.title}
            </h1>
            <p className="mb-6 text-xs text-gray-500" data-testid="thread-detail-creator">
              作成者: {threadData.createdByUsername} ・{" "}
              {formatRelativeDate(threadData.createdAt)}
            </p>

            {/* コメント一覧 */}
            {allComments.length > 0 ? (
              <ul
                className="divide-y divide-gray-200 dark:divide-zinc-700"
                data-testid="comment-list"
              >
                {allComments.map((comment) => (
                  <li
                    key={comment.commentId}
                    className="px-2 py-4"
                    data-testid={`comment-item-${comment.commentId}`}
                  >
                    <div className="flex items-start justify-between">
                      <p className="text-xs font-medium text-gray-700 dark:text-gray-300">
                        {comment.createdByUsername}
                      </p>
                      <span className="ml-2 shrink-0 text-xs text-gray-400">
                        {formatRelativeDate(comment.createdAt)}
                      </span>
                    </div>
                    {/* コメント内容（改行保持・長文折り返し） */}
                    <p
                      className="mt-2 whitespace-pre-wrap break-words text-sm text-gray-900 dark:text-zinc-50"
                      data-testid={`comment-content-${comment.commentId}`}
                    >
                      {comment.content}
                    </p>
                  </li>
                ))}
              </ul>
            ) : (
              <div className="py-12 text-center">
                <p className="text-gray-500" data-testid="no-comments">
                  コメントはまだありません
                </p>
              </div>
            )}

            {/* もっと見る */}
            {hasMoreComments && (
              <div className="mt-4 text-center">
                <button
                  type="button"
                  onClick={handleLoadMore}
                  disabled={isLoadingMore}
                  className="rounded-md border border-gray-300 px-6 py-2 text-sm text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-zinc-600 dark:text-gray-300"
                  data-testid="load-more-button"
                >
                  {isLoadingMore ? "読み込み中..." : "もっと見る"}
                </button>
              </div>
            )}
          </>
        ) : null}
      </main>

      {/* コメント追加FAB */}
      <button
        type="button"
        onClick={handleFabClick}
        className="fixed bottom-6 right-6 z-40 flex h-14 w-14 items-center justify-center rounded-full bg-blue-600 text-white shadow-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        aria-label="コメントを追加"
        data-testid="add-comment-fab"
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
            d="M7.5 8.25h9m-9 3H12m-9.75 1.51c0 1.6 1.123 2.994 2.707 3.227 1.129.166 2.27.293 3.423.379.35.026.67.21.865.501L12 21l2.755-4.133a1.14 1.14 0 0 1 .865-.501 48.172 48.172 0 0 0 3.423-.379c1.584-.233 2.707-1.626 2.707-3.228V6.741c0-1.602-1.123-2.995-2.707-3.228A48.394 48.394 0 0 0 12 3c-2.392 0-4.744.175-7.043.513C3.373 3.746 2.25 5.14 2.25 6.741v6.018Z"
          />
        </svg>
      </button>

      {/* コメント追加モーダル */}
      <AddCommentModal
        isOpen={isCommentModalOpen}
        onClose={() => setIsCommentModalOpen(false)}
        artistId={artistId}
        threadId={threadId}
        sessionId={user?.sessionId ?? ""}
        onCommentAdded={handleCommentAdded}
      />

      {/* ログイン促進ダイアログ */}
      <LoginPromptDialog
        isOpen={isLoginDialogOpen}
        onClose={() => setIsLoginDialogOpen(false)}
      />
    </div>
  );
}
