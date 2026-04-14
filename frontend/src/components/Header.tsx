/**
 * 共通ヘッダーコンポーネント
 *
 * アプリケーション全ページで表示される共通ヘッダー。
 * 未ログイン時は「ログイン」ボタンを表示し、
 * ログイン済み時はユーザーIDとユーザー名を表示する。
 *
 * @since 1.0
 */
"use client";

import { useState, useCallback, type ReactNode } from "react";
import { useAuth } from "@/contexts/AuthContext";
import LoginModal from "@/components/LoginModal";

/**
 * 共通ヘッダーコンポーネント
 *
 * 認証状態に応じて表示内容を切り替える。
 * - 未ログイン時: 「ログイン」ボタンを表示
 * - ログイン済み時: ユーザーIDとユーザー名を表示
 *
 * @returns ヘッダーのJSX要素
 */
export default function Header(): ReactNode {
  const { user, isLoggedIn, logout } = useAuth();
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

  /**
   * ログインモーダルを開く
   */
  const handleOpenModal = useCallback((): void => {
    setIsModalOpen(true);
  }, []);

  /**
   * ログインモーダルを閉じる
   */
  const handleCloseModal = useCallback((): void => {
    setIsModalOpen(false);
  }, []);

  /**
   * ログアウト処理を実行する
   */
  const handleLogout = useCallback((): void => {
    logout();
  }, [logout]);

  return (
    <>
      <header className="w-full border-b border-gray-200 bg-white shadow-sm">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
          {/* サイトタイトル */}
          <h1 className="text-lg font-bold text-gray-900">
            Devin-Test
          </h1>

          {/* 認証エリア */}
          <div className="flex items-center gap-4">
            {isLoggedIn && user ? (
              <>
                {/* ログイン済み: ユーザーID・ユーザー名を表示 */}
                <div className="flex items-center gap-2 text-sm text-gray-700">
                  <span data-testid="user-id">ID: {user.userId}</span>
                  <span className="text-gray-300">|</span>
                  <span data-testid="user-name">{user.username}</span>
                </div>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
                >
                  ログアウト
                </button>
              </>
            ) : (
              /* 未ログイン: ログインボタンを表示 */
              <button
                type="button"
                onClick={handleOpenModal}
                className="rounded-md bg-blue-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              >
                ログイン
              </button>
            )}
          </div>
        </div>
      </header>

      {/* ログインモーダル */}
      <LoginModal isOpen={isModalOpen} onClose={handleCloseModal} />
    </>
  );
}
