/**
 * ログインモーダルコンポーネント
 *
 * メールアドレスとパスワードを入力してログインを実行するモーダルダイアログ。
 * クローズボタンまたはオーバーレイクリックで閉じることができる。
 *
 * @since 1.0
 */
"use client";

import { useState, useCallback, type FormEvent, type ReactNode } from "react";
import { useAuth } from "@/contexts/AuthContext";

/**
 * LoginModalのProps型定義
 *
 * @property isOpen - モーダルの表示状態
 * @property onClose - モーダルを閉じるコールバック
 */
interface LoginModalProps {
  isOpen: boolean;
  onClose: () => void;
}

/**
 * ログインモーダルコンポーネント
 *
 * メールアドレスとパスワードの入力フォームを表示し、
 * バックエンドAPIと連携してログイン処理を実行する。
 * ログイン成功時はモーダルを閉じ、元のページに戻る。
 *
 * @param props - モーダルの表示制御Props
 * @returns ログインモーダルのJSX（非表示時はnull）
 */
export default function LoginModal({
  isOpen,
  onClose,
}: LoginModalProps): ReactNode {
  const { login } = useAuth();
  const [email, setEmail] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  /**
   * フォーム送信時のログイン処理
   *
   * バリデーション後、AuthContextのlogin関数を呼び出す。
   * 成功時はフォームをリセットしてモーダルを閉じる。
   *
   * @param e - フォーム送信イベント
   */
  const handleSubmit = useCallback(
    async (e: FormEvent<HTMLFormElement>): Promise<void> => {
      e.preventDefault();
      setError(null);
      setLoading(true);

      try {
        await login(email, password);
        setEmail("");
        setPassword("");
        onClose();
      } catch (err) {
        const message: string =
          err instanceof Error ? err.message : "ログインに失敗しました";
        setError(message);
      } finally {
        setLoading(false);
      }
    },
    [email, password, login, onClose],
  );

  /**
   * オーバーレイクリック時のモーダルクローズ処理
   */
  const handleOverlayClick = useCallback((): void => {
    onClose();
  }, [onClose]);

  if (!isOpen) {
    return null;
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      role="dialog"
      aria-modal="true"
      aria-labelledby="login-modal-title"
    >
      {/* オーバーレイ */}
      <div
        className="fixed inset-0 bg-black/50"
        onClick={handleOverlayClick}
        data-testid="modal-overlay"
      />

      {/* モーダル本体 */}
      <div className="relative z-10 w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
        {/* ヘッダー部分 */}
        <div className="mb-6 flex items-center justify-between">
          <h2
            id="login-modal-title"
            className="text-xl font-semibold text-gray-900"
          >
            ログイン
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
            aria-label="閉じる"
          >
            <svg
              className="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth="1.5"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        {/* エラーメッセージ */}
        {error && (
          <div className="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {/* ログインフォーム */}
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label
              htmlFor="email"
              className="mb-1 block text-sm font-medium text-gray-700"
            >
              メールアドレス
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              placeholder="example@mail.com"
              disabled={loading}
            />
          </div>

          <div className="mb-6">
            <label
              htmlFor="password"
              className="mb-1 block text-sm font-medium text-gray-700"
            >
              パスワード
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              placeholder="パスワードを入力"
              disabled={loading}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {loading ? "ログイン中..." : "ログイン"}
          </button>
        </form>
      </div>
    </div>
  );
}
