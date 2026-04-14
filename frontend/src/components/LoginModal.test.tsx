/**
 * ログインモーダルコンポーネントの単体テスト
 *
 * モーダルの表示・非表示、フォーム入力、ログイン処理、
 * エラーハンドリング、クローズ動作をテストする。
 */
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import LoginModal from "@/components/LoginModal";
import { AuthProvider } from "@/contexts/AuthContext";

/**
 * AuthProviderでラップしてレンダリングするヘルパー関数
 *
 * @param isOpen - モーダルの表示状態
 * @param onClose - モーダルクローズ時のコールバック
 */
function renderWithAuth(
  isOpen: boolean,
  onClose: () => void,
): ReturnType<typeof render> {
  return render(
    <AuthProvider>
      <LoginModal isOpen={isOpen} onClose={onClose} />
    </AuthProvider>,
  );
}

describe("LoginModal", () => {
  const mockOnClose = vi.fn();

  beforeEach(() => {
    vi.restoreAllMocks();
    mockOnClose.mockClear();
    sessionStorage.clear();
  });

  /**
   * 【テスト対象】LoginModal コンポーネント
   * 【テストケース】isOpen=falseの場合
   * 【期待結果】モーダルが表示されない
   * 【ビジネス要件】モーダルの非表示制御
   */
  it("isOpen=falseの場合にモーダルが表示されないこと", () => {
    renderWithAuth(false, mockOnClose);

    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  /**
   * 【テスト対象】LoginModal コンポーネント
   * 【テストケース】isOpen=trueの場合
   * 【期待結果】フォーム要素が全て表示される
   * 【ビジネス要件】ログインモーダルの表示
   */
  it("isOpen=trueの場合にフォームが表示されること", () => {
    renderWithAuth(true, mockOnClose);

    expect(screen.getByRole("dialog")).toBeInTheDocument();
    expect(screen.getByLabelText("メールアドレス")).toBeInTheDocument();
    expect(screen.getByLabelText("パスワード")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "ログイン" }),
    ).toBeInTheDocument();
    expect(screen.getByLabelText("閉じる")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】LoginModal コンポーネント
   * 【テストケース】クローズボタン押下時
   * 【期待結果】onCloseコールバックが呼ばれる
   * 【ビジネス要件】モーダルのクローズ機能
   */
  it("クローズボタン押下時にonCloseが呼ばれること", () => {
    renderWithAuth(true, mockOnClose);

    fireEvent.click(screen.getByLabelText("閉じる"));

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  /**
   * 【テスト対象】LoginModal コンポーネント
   * 【テストケース】オーバーレイクリック時
   * 【期待結果】onCloseコールバックが呼ばれる
   * 【ビジネス要件】モーダルのオーバーレイクローズ
   */
  it("オーバーレイクリック時にonCloseが呼ばれること", () => {
    renderWithAuth(true, mockOnClose);

    fireEvent.click(screen.getByTestId("modal-overlay"));

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  /**
   * 【テスト対象】LoginModal コンポーネント
   * 【テストケース】ログイン成功時
   * 【期待結果】モーダルが閉じられる
   * 【ビジネス要件】ログイン成功後に元のページへ戻る
   */
  it("ログイン成功時にモーダルが閉じられること", async () => {
    const mockResponse = {
      sessionId: "session-123",
      userId: "user-123",
      username: "テストユーザー",
    };

    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      json: async () => mockResponse,
    } as Response);

    renderWithAuth(true, mockOnClose);

    fireEvent.change(screen.getByLabelText("メールアドレス"), {
      target: { value: "test@example.com" },
    });
    fireEvent.change(screen.getByLabelText("パスワード"), {
      target: { value: "password123" },
    });
    fireEvent.click(screen.getByRole("button", { name: "ログイン" }));

    await waitFor(() => {
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });
  });

  /**
   * 【テスト対象】LoginModal コンポーネント
   * 【テストケース】ログイン失敗時（認証エラー）
   * 【期待結果】エラーメッセージが表示される
   * 【ビジネス要件】ログイン失敗時のエラー表示
   */
  it("ログイン失敗時にエラーメッセージが表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: false,
      status: 401,
    } as Response);

    renderWithAuth(true, mockOnClose);

    fireEvent.change(screen.getByLabelText("メールアドレス"), {
      target: { value: "test@example.com" },
    });
    fireEvent.change(screen.getByLabelText("パスワード"), {
      target: { value: "wrongpassword" },
    });
    fireEvent.click(screen.getByRole("button", { name: "ログイン" }));

    await waitFor(() => {
      expect(
        screen.getByText("メールアドレスまたはパスワードが正しくありません"),
      ).toBeInTheDocument();
    });

    expect(mockOnClose).not.toHaveBeenCalled();
  });

  /**
   * 【テスト対象】LoginModal コンポーネント
   * 【テストケース】ログイン中のローディング状態
   * 【期待結果】ボタンが「ログイン中...」に変わり非活性になる
   * 【ビジネス要件】ユーザーへのフィードバック表示
   */
  it("ログイン中にローディング状態が表示されること", async () => {
    let resolvePromise: (value: Response) => void;
    const fetchPromise = new Promise<Response>((resolve) => {
      resolvePromise = resolve;
    });

    vi.spyOn(global, "fetch").mockReturnValueOnce(fetchPromise);

    renderWithAuth(true, mockOnClose);

    fireEvent.change(screen.getByLabelText("メールアドレス"), {
      target: { value: "test@example.com" },
    });
    fireEvent.change(screen.getByLabelText("パスワード"), {
      target: { value: "password123" },
    });
    fireEvent.click(screen.getByRole("button", { name: "ログイン" }));

    expect(screen.getByText("ログイン中...")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "ログイン中..." })).toBeDisabled();

    resolvePromise!({
      ok: true,
      json: async () => ({
        sessionId: "session-123",
        userId: "user-123",
        username: "テストユーザー",
      }),
    } as Response);

    await waitFor(() => {
      expect(mockOnClose).toHaveBeenCalled();
    });
  });
});
