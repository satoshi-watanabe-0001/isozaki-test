/**
 * コメント追加モーダルコンポーネント
 *
 * Radix UI Dialogを使用したコメント追加用モーダル。
 * コメント（最大200文字）を入力してスレッドにコメントを追加する。
 * ×ボタンおよびオーバーレイクリックで閉じることができる。
 *
 * @since 1.3
 */
"use client";

import { useState, useCallback, type ReactNode } from "react";
import * as Dialog from "@radix-ui/react-dialog";

/** バックエンドAPIのベースURL */
const BACKEND_URL: string =
  process.env.NEXT_PUBLIC_BACKEND_URL ?? "http://localhost:8080";

/** コメントの最大文字数 */
const MAX_COMMENT_LENGTH = 200;

/**
 * コメント追加モーダルのProps型定義
 *
 * @property isOpen - モーダルの開閉状態
 * @property onClose - モーダルを閉じる関数
 * @property artistId - アーティストID
 * @property threadId - スレッドID
 * @property sessionId - セッションID
 * @property onCommentAdded - コメント追加成功時のコールバック
 */
interface AddCommentModalProps {
  isOpen: boolean;
  onClose: () => void;
  artistId: string;
  threadId: string;
  sessionId: string;
  onCommentAdded: () => void;
}

/**
 * コメント追加モーダルコンポーネント
 *
 * @param props - モーダルのProps
 * @returns モーダルのJSX要素
 */
export default function AddCommentModal({
  isOpen,
  onClose,
  artistId,
  threadId,
  sessionId,
  onCommentAdded,
}: AddCommentModalProps): ReactNode {
  const [content, setContent] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [contentError, setContentError] = useState<string | null>(null);

  /** コメントのバリデーション */
  const validateContent = useCallback((value: string): boolean => {
    if (value.trim().length === 0) {
      setContentError("コメントを入力してください");
      return false;
    }
    if (value.length > MAX_COMMENT_LENGTH) {
      setContentError(
        `コメントは${MAX_COMMENT_LENGTH}文字以内で入力してください`,
      );
      return false;
    }
    setContentError(null);
    return true;
  }, []);

  /** 送信ボタンの無効化条件 */
  const isSubmitDisabled: boolean =
    isSubmitting ||
    content.trim().length === 0 ||
    content.length > MAX_COMMENT_LENGTH;

  /**
   * フォーム送信処理
   */
  const handleSubmit = useCallback(async (): Promise<void> => {
    if (!validateContent(content)) {
      return;
    }

    setIsSubmitting(true);
    try {
      const response: Response = await fetch(
        `${BACKEND_URL}/api/v1/community/${artistId}/threads/${threadId}/comments`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ content, sessionId }),
        },
      );
      if (!response.ok) {
        throw new Error(`コメントの投稿に失敗しました（${response.status}）`);
      }
      // 入力をリセット
      setContent("");
      setContentError(null);
      onCommentAdded();
    } catch {
      setContentError(
        "コメントの投稿に失敗しました。もう一度お試しください。",
      );
    } finally {
      setIsSubmitting(false);
    }
  }, [content, artistId, threadId, sessionId, validateContent, onCommentAdded]);

  /**
   * モーダルを閉じるときに入力をリセットする
   */
  const handleOpenChange = useCallback(
    (open: boolean): void => {
      if (!open) {
        setContent("");
        setContentError(null);
        onClose();
      }
    },
    [onClose],
  );

  return (
    <Dialog.Root open={isOpen} onOpenChange={handleOpenChange}>
      <Dialog.Portal>
        <Dialog.Overlay
          className="fixed inset-0 z-50 bg-black/50"
          data-testid="add-comment-overlay"
        />
        <Dialog.Content
          className="fixed left-1/2 top-1/2 z-50 w-full max-w-md -translate-x-1/2 -translate-y-1/2 rounded-lg bg-white p-6 shadow-xl dark:bg-zinc-900"
          data-testid="add-comment-modal"
        >
          {/* ヘッダー */}
          <div className="mb-4 flex items-center justify-between">
            <Dialog.Title className="text-lg font-bold text-gray-900 dark:text-zinc-50">
              コメントを追加
            </Dialog.Title>
            <Dialog.Close asChild>
              <button
                type="button"
                className="text-gray-400 hover:text-gray-600"
                aria-label="閉じる"
                data-testid="add-comment-close"
              >
                <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12" />
                </svg>
              </button>
            </Dialog.Close>
          </div>

          {/* コメント入力 */}
          <div className="mb-6">
            <label
              htmlFor="comment-content"
              className="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              コメント
            </label>
            <textarea
              id="comment-content"
              value={content}
              onChange={(e) => {
                setContent(e.target.value);
                validateContent(e.target.value);
              }}
              maxLength={MAX_COMMENT_LENGTH + 10}
              rows={4}
              placeholder="コメントを入力"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-50"
              data-testid="comment-content-input"
            />
            <div className="mt-1 flex items-center justify-between">
              {contentError !== null ? (
                <p
                  className="text-xs text-red-500"
                  data-testid="comment-content-error"
                >
                  {contentError}
                </p>
              ) : (
                <span />
              )}
              <span
                className={`text-xs ${content.length > MAX_COMMENT_LENGTH ? "text-red-500" : "text-gray-400"}`}
                data-testid="comment-content-count"
              >
                {content.length}/{MAX_COMMENT_LENGTH}
              </span>
            </div>
          </div>

          {/* 送信ボタン */}
          <button
            type="button"
            onClick={handleSubmit}
            disabled={isSubmitDisabled}
            className="w-full rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
            data-testid="add-comment-submit"
          >
            {isSubmitting ? "投稿中..." : "投稿する"}
          </button>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
