/**
 * 認証コンテキスト
 *
 * アプリケーション全体でログイン状態を管理するコンテキスト。
 * sessionStorageを使用してブラウザでのセッション保持・復元を行う。
 *
 * @since 1.0
 */
"use client";

import {
  createContext,
  useContext,
  useState,
  useCallback,
  useEffect,
  type ReactNode,
} from "react";

/** セッション情報の保存キー */
const SESSION_STORAGE_KEY = "auth_session";

/**
 * 認証済みユーザー情報の型定義
 *
 * @property sessionId - セッションID
 * @property userId - ユーザーID（UUIDv7）
 * @property username - ユーザー名
 */
interface AuthUser {
  sessionId: string;
  userId: string;
  username: string;
}

/**
 * 認証コンテキストの値型定義
 *
 * @property user - ログイン済みユーザー情報（未ログイン時はnull）
 * @property isLoggedIn - ログイン済みかどうか
 * @property login - ログイン処理を実行する関数
 * @property logout - ログアウト処理を実行する関数
 */
interface AuthContextValue {
  user: AuthUser | null;
  isLoggedIn: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const BACKEND_URL: string =
  process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

/**
 * sessionStorageからセッション情報を復元する
 *
 * @returns 保存済みのユーザー情報、存在しない場合はnull
 */
function loadSession(): AuthUser | null {
  if (typeof window === "undefined") {
    return null;
  }
  try {
    const stored: string | null = sessionStorage.getItem(SESSION_STORAGE_KEY);
    if (!stored) {
      return null;
    }
    const parsed: AuthUser = JSON.parse(stored);
    if (parsed.sessionId && parsed.userId && parsed.username) {
      return parsed;
    }
    return null;
  } catch {
    return null;
  }
}

/**
 * sessionStorageにセッション情報を保存する
 *
 * @param user - 保存するユーザー情報
 */
function saveSession(user: AuthUser): void {
  sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(user));
}

/**
 * sessionStorageからセッション情報を削除する
 */
function clearSession(): void {
  sessionStorage.removeItem(SESSION_STORAGE_KEY);
}

/**
 * 認証プロバイダーのProps型定義
 *
 * @property children - 子コンポーネント
 */
interface AuthProviderProps {
  children: ReactNode;
}

/**
 * 認証プロバイダーコンポーネント
 *
 * アプリケーション全体をラップし、認証状態を提供する。
 * 初期化時にsessionStorageからセッションを復元する。
 *
 * @param props - 子コンポーネントを含むProps
 * @returns 認証コンテキストを提供するプロバイダー
 */
export function AuthProvider({ children }: AuthProviderProps): ReactNode {
  const [user, setUser] = useState<AuthUser | null>(null);

  /**
   * クライアント側でsessionStorageからセッションを復元し、
   * バックエンド側でセッションの有効性を検証する
   *
   * SSR時はsessionStorageにアクセスできないため、
   * useEffectでクライアントマウント後にセッションを復元する。
   * これによりサーバー・クライアント間のhydration不整合を防止する。
   *
   * sessionStorageにセッション情報が存在する場合、
   * バックエンドAPIでセッションがRedisに存在するか確認する。
   * 無効な場合はローカルのセッション情報をクリアする。
   */
  useEffect(() => {
    const restoreAndValidateSession = async (): Promise<void> => {
      const storedUser: AuthUser | null = loadSession();
      if (!storedUser) {
        return;
      }
      try {
        const response: Response = await fetch(
          `${BACKEND_URL}/api/v1/session/${storedUser.sessionId}`,
        );
        if (response.ok) {
          setUser(storedUser);
        } else {
          setUser(null);
          clearSession();
        }
      } catch {
        // ネットワークエラー時はローカルのセッションを復元する
        setUser(storedUser);
      }
    };
    void restoreAndValidateSession();
  }, []);

  /**
   * ログイン処理
   *
   * バックエンドAPIにメールアドレスとパスワードを送信し、
   * 認証成功時にセッション情報をstateとsessionStorageに保存する。
   *
   * @param email - メールアドレス
   * @param password - パスワード
   * @throws Error 認証失敗時またはネットワークエラー時
   */
  const login = useCallback(async (email: string, password: string): Promise<void> => {
    const response: Response = await fetch(`${BACKEND_URL}/api/v1/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("メールアドレスまたはパスワードが正しくありません");
      }
      throw new Error(`ログインに失敗しました (HTTP ${response.status})`);
    }

    const data: AuthUser = await response.json();
    setUser(data);
    saveSession(data);
  }, []);

  /**
   * ログアウト処理
   *
   * バックエンドAPIでRedisのセッションを削除し、
   * ローカルのセッション情報もクリアする。
   */
  const logout = useCallback((): void => {
    const currentUser: AuthUser | null = loadSession();
    if (currentUser) {
      fetch(`${BACKEND_URL}/api/v1/session/${currentUser.sessionId}`, {
        method: "DELETE",
      }).catch(() => {
        // ネットワークエラー時もローカルのセッションはクリアする
      });
    }
    setUser(null);
    clearSession();
  }, []);

  const value: AuthContextValue = {
    user,
    isLoggedIn: user !== null,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/**
 * 認証コンテキストを使用するカスタムフック
 *
 * AuthProvider配下でのみ使用可能。
 *
 * @returns 認証コンテキストの値
 * @throws Error AuthProvider外で使用された場合
 */
export function useAuth(): AuthContextValue {
  const context: AuthContextValue | undefined = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuthはAuthProvider内で使用してください");
  }
  return context;
}
