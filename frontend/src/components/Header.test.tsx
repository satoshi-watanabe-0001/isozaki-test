/**
 * 共通ヘッダーコンポーネントの単体テスト
 *
 * 未ログイン時・ログイン済み時の表示切替、戻るボタンをテストする。
 */
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Header from "@/components/Header";
import { AuthProvider } from "@/contexts/AuthContext";

/** next/navigationのモック */
const mockBack = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), back: mockBack }),
  usePathname: () => "/",
}));

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
    expect(screen.getByText("Devin-Test")).toBeInTheDocument();
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
   * 【テストケース】セッション復元時（バックエンド検証成功）
   * 【期待結果】sessionStorageの情報からユーザーID・ユーザー名が表示される
   * 【ビジネス要件】ログインセッションのブラウザ保持
   */
  it("sessionStorageにセッションがある場合にログイン済み表示になること", async () => {
    const mockSession = {
      sessionId: "test-session-id",
      userId: "test-user-id",
      username: "テストユーザー",
    };
    sessionStorage.setItem("auth_session", JSON.stringify(mockSession));

    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      json: async () => ({ sessionId: "test-session-id", userId: "test-user-id" }),
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("user-id")).toHaveTextContent("ID: test-user-id");
    });
    expect(screen.getByTestId("user-name")).toHaveTextContent("テストユーザー");
    expect(screen.getByText("ログアウト")).toBeInTheDocument();
    expect(screen.queryByText("ログイン")).not.toBeInTheDocument();
  });

  /**
   * 【テスト対象】Header コンポーネント
   * 【テストケース】セッション復元時（バックエンド検証失敗）
   * 【期待結果】sessionStorageのセッションが無効化され、未ログイン表示になる
   * 【ビジネス要件】無効セッションの自動クリア
   */
  it("バックエンドでセッションが無効の場合に未ログイン表示になること", async () => {
    const mockSession = {
      sessionId: "expired-session-id",
      userId: "test-user-id",
      username: "テストユーザー",
    };
    sessionStorage.setItem("auth_session", JSON.stringify(mockSession));

    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: false,
      status: 404,
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByText("ログイン")).toBeInTheDocument();
    });
    expect(screen.queryByTestId("user-id")).not.toBeInTheDocument();
    expect(screen.queryByTestId("user-name")).not.toBeInTheDocument();
  });

  /**
   * 【テスト対象】Header コンポーネント
   * 【テストケース】ログアウトボタン押下時
   * 【期待結果】ユーザー情報が非表示になり、ログインボタンが再表示される
   * 【ビジネス要件】ログアウト機能（バックエンド連携）
   */
  it("ログアウトボタン押下時にログインボタンが再表示されること", async () => {
    const mockSession = {
      sessionId: "test-session-id",
      userId: "test-user-id",
      username: "テストユーザー",
    };
    sessionStorage.setItem("auth_session", JSON.stringify(mockSession));

    const fetchSpy = vi.spyOn(global, "fetch").mockResolvedValue({
      ok: true,
      json: async () => ({ sessionId: "test-session-id", userId: "test-user-id" }),
    } as Response);

    renderWithAuth();

    // セッション復元を待つ
    await waitFor(() => {
      expect(screen.getByText("ログアウト")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("ログアウト"));

    expect(screen.getByText("ログイン")).toBeInTheDocument();
    expect(screen.queryByTestId("user-id")).not.toBeInTheDocument();
    expect(screen.queryByTestId("user-name")).not.toBeInTheDocument();

    // バックエンドのセッション削除APIが呼ばれることを確認
    const deleteCalls = fetchSpy.mock.calls.filter(
      (call) => {
        const url = call[0] as string;
        const options = call[1] as RequestInit | undefined;
        return url.includes("/api/v1/session/") && options?.method === "DELETE";
      },
    );
    expect(deleteCalls.length).toBe(1);
  });

  /**
   * 【テスト対象】Header コンポーネント
   * 【テストケース】戻るボタンの表示
   * 【期待結果】戻るボタンが表示される
   * 【ビジネス要件】共通ヘッダの戻るアイコン
   */
  it("戻るボタンが表示されること", () => {
    renderWithAuth();

    expect(screen.getByTestId("back-button")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】Header コンポーネント
   * 【テストケース】戻るボタンクリック時
   * 【期待結果】router.backが呼ばれる
   * 【ビジネス要件】前ページへの遷移
   */
  it("戻るボタンクリックでrouter.backが呼ばれること", () => {
    // history.lengthが2以上の場合は活性化する
    Object.defineProperty(window.history, "length", { value: 3, writable: true });

    renderWithAuth();

    fireEvent.click(screen.getByTestId("back-button"));

    expect(mockBack).toHaveBeenCalled();
  });
});
