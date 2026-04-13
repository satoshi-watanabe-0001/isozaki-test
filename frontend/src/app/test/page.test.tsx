/**
 * テストページコンポーネントの単体テスト
 *
 * ヘルスチェック画面の表示・操作・エラーハンドリングをテストする。
 */
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import TestPage from "@/app/test/page";

describe("TestPage", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  /**
   * 【テスト対象】TestPage コンポーネント
   * 【テストケース】初期表示
   * 【期待結果】タイトルとボタンが表示される
   * 【ビジネス要件】ヘルスチェック画面の初期状態
   */
  it("初期表示でタイトルとボタンが表示されること", () => {
    render(<TestPage />);

    expect(screen.getByText("テストページ")).toBeInTheDocument();
    expect(screen.getByText("ヘルスチェック実行")).toBeInTheDocument();
    expect(
      screen.getByText("「ヘルスチェック実行」ボタンを押してください"),
    ).toBeInTheDocument();
  });

  /**
   * 【テスト対象】TestPage コンポーネント
   * 【テストケース】ヘルスチェック成功時
   * 【期待結果】ステータスUPとチェック結果が表示される
   * 【ビジネス要件】バックエンドの正常性確認表示
   */
  it("ヘルスチェック成功時にステータスが表示されること", async () => {
    const mockResponse = {
      status: "UP",
      checks: [
        { name: "アプリケーション稼働状態", status: "UP" },
        { name: "データベース接続", status: "UP" },
      ],
    };

    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      json: async () => mockResponse,
    } as Response);

    render(<TestPage />);
    fireEvent.click(screen.getByText("ヘルスチェック実行"));

    await waitFor(() => {
      expect(screen.getByText("全体ステータス: UP")).toBeInTheDocument();
    });

    expect(screen.getByText("アプリケーション稼働状態")).toBeInTheDocument();
    expect(screen.getByText("データベース接続")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】TestPage コンポーネント
   * 【テストケース】ヘルスチェック失敗時（ネットワークエラー）
   * 【期待結果】エラーメッセージが表示される
   * 【ビジネス要件】バックエンド障害時のエラー表示
   */
  it("ヘルスチェック失敗時にエラーが表示されること", async () => {
    vi.spyOn(global, "fetch").mockRejectedValueOnce(
      new Error("Network Error"),
    );

    render(<TestPage />);
    fireEvent.click(screen.getByText("ヘルスチェック実行"));

    await waitFor(() => {
      expect(screen.getByText("接続エラー")).toBeInTheDocument();
    });

    expect(screen.getByText("Network Error")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】TestPage コンポーネント
   * 【テストケース】ヘルスチェック中のローディング状態
   * 【期待結果】ボタンが「取得中...」に変わり非活性になる
   * 【ビジネス要件】ユーザーへのフィードバック表示
   */
  it("ヘルスチェック中にローディング状態が表示されること", async () => {
    let resolvePromise: (value: Response) => void;
    const fetchPromise = new Promise<Response>((resolve) => {
      resolvePromise = resolve;
    });

    vi.spyOn(global, "fetch").mockReturnValueOnce(fetchPromise);

    render(<TestPage />);
    fireEvent.click(screen.getByText("ヘルスチェック実行"));

    expect(screen.getByText("取得中...")).toBeInTheDocument();
    expect(screen.getByRole("button")).toBeDisabled();

    resolvePromise!({
      ok: true,
      json: async () => ({ status: "UP", checks: [] }),
    } as Response);

    await waitFor(() => {
      expect(screen.getByText("ヘルスチェック実行")).toBeInTheDocument();
    });
  });

  /**
   * 【テスト対象】TestPage コンポーネント
   * 【テストケース】HTTPエラーレスポンス受信時
   * 【期待結果】HTTPステータスを含むエラーメッセージが表示される
   * 【ビジネス要件】サーバーエラー時の適切なエラー表示
   */
  it("HTTPエラーレスポンス時にエラーが表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: false,
      status: 503,
      statusText: "Service Unavailable",
    } as Response);

    render(<TestPage />);
    fireEvent.click(screen.getByText("ヘルスチェック実行"));

    await waitFor(() => {
      expect(screen.getByText("接続エラー")).toBeInTheDocument();
    });

    expect(
      screen.getByText("HTTP 503: Service Unavailable"),
    ).toBeInTheDocument();
  });
});
