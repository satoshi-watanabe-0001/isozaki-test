/**
 * 共通ヘッダーコンポーネントの単体テスト
 *
 * 未ログイン時・ログイン済み時の表示切替をテストする。
 */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Header from "@/components/Header";
import { AuthProvider } from "@/contexts/AuthContext";

/**
 * AuthProviderでラップしてレンダリングするヘルパー関数
 */
function renderWithAuth(): ReturnType<typeof render> {
  return render(
    <AuthProvider>
      <Header />
    </AuthProvider>,
  );
}

describe("Header", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
  });

  /**
   * 【テスト対象】Header コンポーネント
   * 【テストケース】未ログイン時の初期表示
   * 【期待結果】「ログイン」ボタンが表示される
   * 【ビジネス要件】未ログイン時のヘッダー表示
   */
  it("未ログイン時に「ログイン」ボタンが表示されること", () => {
    renderWithAuth();

    expect(screen.getByText("ログイン")).toBeInTheDocument();
    expect(screen.getByText("isozaki-test")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】Header コンポーネント
   * 【テストケース】未ログイン時にユーザー情報が非表示
   * 【期待結果】ユーザーID・ユーザー名・ログアウトボタンが表示されない
   * 【ビジネス要件】未ログイン時の情報非表示
   */
  it("未ログイン時にユーザー情報が表示されないこと", () => {
    renderWithAuth();

    expect(screen.queryByTestId("user-id")).not.toBeInTheDocument();
    expect(screen.queryByTestId("user-name")).not.toBeInTheDocument();
    expect(screen.queryByText("ログアウト")).not.toBeInTheDocument();
  });

  /**
   * 【テスト対象】Header コンポーネント
   * 【テストケース】ログインボタン押下時
   * 【期待結果】ログインモーダルが表示される
   * 【ビジネス要件】ログインモーダルの起動
   */
  it("ログインボタン押下時にモーダルが表示されること", () => {
    renderWithAuth();

    fireEvent.click(screen.getByText("ログイン"));

    expect(screen.getByRole("dialog")).toBeInTheDocument();
    expect(screen.getByLabelText("メールアドレス")).toBeInTheDocument();
    expect(screen.getByLabelText("パスワード")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】Header コンポーネント
   * 【テストケース】セッション復元時
   * 【期待結果】sessionStorageの情報からユーザーID・ユーザー名が表示される
   * 【ビジネス要件】ログインセッションのブラウザ保持
   */
  it("sessionStorageにセッションがある場合にログイン済み表示になること", () => {
    const mockSession = {
      sessionId: "test-session-id",
      userId: "test-user-id",
      username: "テストユーザー",
    };
    sessionStorage.setItem("auth_session", JSON.stringify(mockSession));

    renderWithAuth();

    expect(screen.getByTestId("user-id")).toHaveTextContent("ID: test-user-id");
    expect(screen.getByTestId("user-name")).toHaveTextContent("テストユーザー");
    expect(screen.getByText("ログアウト")).toBeInTheDocument();
    expect(screen.queryByText("ログイン")).not.toBeInTheDocument();
  });

  /**
   * 【テスト対象】Header コンポーネント
   * 【テストケース】ログアウトボタン押下時
   * 【期待結果】ユーザー情報が非表示になり、ログインボタンが再表示される
   * 【ビジネス要件】ログアウト機能
   */
  it("ログアウトボタン押下時にログインボタンが再表示されること", () => {
    const mockSession = {
      sessionId: "test-session-id",
      userId: "test-user-id",
      username: "テストユーザー",
    };
    sessionStorage.setItem("auth_session", JSON.stringify(mockSession));

    renderWithAuth();

    fireEvent.click(screen.getByText("ログアウト"));

    expect(screen.getByText("ログイン")).toBeInTheDocument();
    expect(screen.queryByTestId("user-id")).not.toBeInTheDocument();
    expect(screen.queryByTestId("user-name")).not.toBeInTheDocument();
  });
});
