/**
 * ログイン促進ダイアログコンポーネント
 *
 * Radix UI Dialogを使用した未ログインユーザ向けのログイン促進ダイアログ。
 * スレッド作成・コメント追加時にログインしていない場合に表示される。
 *
 * @since 1.3
 */
"use client";

import { useCallback, type ReactNode } from "react";
import * as Dialog from "@radix-ui/react-dialog";

/**
 * ログイン促進ダイアログのProps型定義
 *
 * @property isOpen - ダイアログの開閉状態
 * @property onClose - ダイアログを閉じる関数
 */
interface LoginPromptDialogProps {
  isOpen: boolean;
  onClose: () => void;
}

/**
 * ログイン促進ダイアログコンポーネント
 *
 * @param props - ダイアログのProps
 * @returns ダイアログのJSX要素
 */
export default function LoginPromptDialog({
  isOpen,
  onClose,
}: LoginPromptDialogProps): ReactNode {
  /**
   * ダイアログの開閉状態変更ハンドラ
   */
  const handleOpenChange = useCallback(
    (open: boolean): void => {
      if (!open) {
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
          data-testid="login-prompt-overlay"
        />
        <Dialog.Content
          className="fixed left-1/2 top-1/2 z-50 w-full max-w-sm -translate-x-1/2 -translate-y-1/2 rounded-lg bg-white p-6 shadow-xl dark:bg-zinc-900"
          data-testid="login-prompt-dialog"
        >
          {/* ヘッダー */}
          <div className="mb-4 flex items-center justify-between">
            <Dialog.Title className="text-lg font-bold text-gray-900 dark:text-zinc-50">
              ログインが必要です
            </Dialog.Title>
            <Dialog.Close asChild>
              <button
                type="button"
                className="text-gray-400 hover:text-gray-600"
                aria-label="閉じる"
                data-testid="login-prompt-close"
              >
                <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12" />
                </svg>
              </button>
            </Dialog.Close>
          </div>

          <Dialog.Description className="mb-6 text-sm text-gray-600 dark:text-gray-400">
            この操作を行うにはログインが必要です。ヘッダーの「ログイン」ボタンからログインしてください。
          </Dialog.Description>

          {/* 閉じるボタン */}
          <Dialog.Close asChild>
            <button
              type="button"
              className="w-full rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-zinc-600 dark:text-gray-300"
              data-testid="login-prompt-ok"
            >
              閉じる
            </button>
          </Dialog.Close>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
