/**
 * スレッド作成モーダルコンポーネント
 *
 * Radix UI Dialogを使用したスレッド作成用モーダル。
 * タイトル（最大50文字）と初回コメント（最大200文字）を入力して
 * スレッドを作成する。×ボタンおよびオーバーレイクリックで閉じることができる。
 *
 * @since 1.3
 */
"use client";

import { useState, useCallback, type ReactNode } from "react";
import * as Dialog from "@radix-ui/react-dialog";

/** バックエンドAPIのベースURL */
const BACKEND_URL: string =
  process.env.NEXT_PUBLIC_BACKEND_URL ?? "http://localhost:8080";

/** タイトルの最大文字数 */
const MAX_TITLE_LENGTH = 50;

/** コメントの最大文字数 */
const MAX_COMMENT_LENGTH = 200;

/**
 * スレッド作成モーダルのProps型定義
 *
 * @property isOpen - モーダルの開閉状態
 * @property onClose - モーダルを閉じる関数
 * @property artistId - アーティストID
 * @property sessionId - セッションID
 * @property onCreated - スレッド作成成功時のコールバック
 */
interface CreateThreadModalProps {
  isOpen: boolean;
  onClose: () => void;
  artistId: string;
  sessionId: string;
  onCreated: () => void;
}

/**
 * スレッド作成モーダルコンポーネント
 *
 * @param props - モーダルのProps
 * @returns モーダルのJSX要素
 */
export default function CreateThreadModal({
  isOpen,
  onClose,
  artistId,
  sessionId,
  onCreated,
}: CreateThreadModalProps): ReactNode {
  const [title, setTitle] = useState<string>("");
  const [comment, setComment] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [titleError, setTitleError] = useState<string | null>(null);
  const [commentError, setCommentError] = useState<string | null>(null);

  /** タイトルのバリデーション */
  const validateTitle = useCallback((value: string): boolean => {
    if (value.trim().length === 0) {
      setTitleError("タイトルを入力してください");
      return false;
    }
    if (value.length > MAX_TITLE_LENGTH) {
      setTitleError(`タイトルは${MAX_TITLE_LENGTH}文字以内で入力してください`);
      return false;
    }
    setTitleError(null);
    return true;
  }, []);

  /** コメントのバリデーション */
  const validateComment = useCallback((value: string): boolean => {
    if (value.trim().length === 0) {
      setCommentError("コメントを入力してください");
      return false;
    }
    if (value.length > MAX_COMMENT_LENGTH) {
      setCommentError(
        `コメントは${MAX_COMMENT_LENGTH}文字以内で入力してください`,
      );
      return false;
    }
    setCommentError(null);
    return true;
  }, []);

  /** 送信ボタンの無効化条件 */
  const isSubmitDisabled: boolean =
    isSubmitting ||
    title.trim().length === 0 ||
    comment.trim().length === 0 ||
    title.length > MAX_TITLE_LENGTH ||
    comment.length > MAX_COMMENT_LENGTH;

  /**
   * フォーム送信処理
   */
  const handleSubmit = useCallback(async (): Promise<void> => {
    const isTitleValid: boolean = validateTitle(title);
    const isCommentValid: boolean = validateComment(comment);
    if (!isTitleValid || !isCommentValid) {
      return;
    }

    setIsSubmitting(true);
    try {
      const response: Response = await fetch(
        `${BACKEND_URL}/api/v1/community/${artistId}/threads`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            title: title.replace(/[\r\n]/g, ""),
            comment,
            sessionId,
          }),
        },
      );
      if (!response.ok) {
        throw new Error(`スレッドの作成に失敗しました（${response.status}）`);
      }
      // 入力をリセット
      setTitle("");
      setComment("");
      setTitleError(null);
      setCommentError(null);
      onCreated();
    } catch {
      setCommentError("スレッドの作成に失敗しました。もう一度お試しください。");
    } finally {
      setIsSubmitting(false);
    }
  }, [title, comment, artistId, sessionId, validateTitle, validateComment, onCreated]);

  /**
   * モーダルを閉じるときに入力をリセットする
   */
  const handleOpenChange = useCallback(
    (open: boolean): void => {
      if (!open) {
        setTitle("");
        setComment("");
        setTitleError(null);
        setCommentError(null);
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
          data-testid="create-thread-overlay"
        />
        <Dialog.Content
          className="fixed left-1/2 top-1/2 z-50 w-full max-w-md -translate-x-1/2 -translate-y-1/2 rounded-lg bg-white p-6 shadow-xl dark:bg-zinc-900"
          data-testid="create-thread-modal"
        >
          {/* ヘッダー */}
          <div className="mb-4 flex items-center justify-between">
            <Dialog.Title className="text-lg font-bold text-gray-900 dark:text-zinc-50">
              スレッドを作成
            </Dialog.Title>
            <Dialog.Close asChild>
              <button
                type="button"
                className="text-gray-400 hover:text-gray-600"
                aria-label="閉じる"
                data-testid="create-thread-close"
              >
                <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12" />
                </svg>
              </button>
            </Dialog.Close>
          </div>

          {/* タイトル入力 */}
          <div className="mb-4">
            <label
              htmlFor="thread-title"
              className="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              タイトル
            </label>
            <input
              id="thread-title"
              type="text"
              value={title}
              onChange={(e) => {
                const newValue: string = e.target.value.replace(/[\r\n]/g, "");
                setTitle(newValue);
                validateTitle(newValue);
              }}
              maxLength={MAX_TITLE_LENGTH + 10}
              placeholder="スレッドタイトルを入力"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-50"
              data-testid="thread-title-input"
            />
            <div className="mt-1 flex items-center justify-between">
              {titleError !== null ? (
                <p
                  className="text-xs text-red-500"
                  data-testid="thread-title-error"
                >
                  {titleError}
                </p>
              ) : (
                <span />
              )}
              <span
                className={`text-xs ${title.length > MAX_TITLE_LENGTH ? "text-red-500" : "text-gray-400"}`}
                data-testid="thread-title-count"
              >
                {title.length}/{MAX_TITLE_LENGTH}
              </span>
            </div>
          </div>

          {/* コメント入力 */}
          <div className="mb-6">
            <label
              htmlFor="thread-comment"
              className="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              コメント
            </label>
            <textarea
              id="thread-comment"
              value={comment}
              onChange={(e) => {
                setComment(e.target.value);
                validateComment(e.target.value);
              }}
              maxLength={MAX_COMMENT_LENGTH + 10}
              rows={4}
              placeholder="コメントを入力"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-50"
              data-testid="thread-comment-input"
            />
            <div className="mt-1 flex items-center justify-between">
              {commentError !== null ? (
                <p
                  className="text-xs text-red-500"
                  data-testid="thread-comment-error"
                >
                  {commentError}
                </p>
              ) : (
                <span />
              )}
              <span
                className={`text-xs ${comment.length > MAX_COMMENT_LENGTH ? "text-red-500" : "text-gray-400"}`}
                data-testid="thread-comment-count"
              >
                {comment.length}/{MAX_COMMENT_LENGTH}
              </span>
            </div>
          </div>

          {/* 送信ボタン */}
          <button
            type="button"
            onClick={handleSubmit}
            disabled={isSubmitDisabled}
            className="w-full rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
            data-testid="create-thread-submit"
          >
            {isSubmitting ? "作成中..." : "作成する"}
          </button>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
