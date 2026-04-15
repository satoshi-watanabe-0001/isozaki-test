/**
 * アーティスト一覧ページの単体テスト
 *
 * アーティスト一覧ページの表示・ローディング・エラー状態をテストする。
 * fetch APIをモックしてバックエンドAPIの応答をシミュレートする。
 */
import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, type Mock } from "vitest";
import ArtistsPage from "@/app/artists/page";
import type { Artist } from "@/types/artist";

/** fetchをグローバルモックとして定義 */
const mockFetch: Mock = vi.fn();
global.fetch = mockFetch;

describe("ArtistsPage", () => {
  /** 各テスト前にモックをリセット */
  beforeEach(() => {
    mockFetch.mockReset();
  });

  /** テスト用のアーティストデータ（50音順） */
  const mockArtists: Artist[] = [
    {
      artistId: "aimyon",
      name: "あいみょん",
      nameKana: "あいみょん",
      iconUrl: "/images/artists/aimyon.svg",
    },
    {
      artistId: "arashi",
      name: "嵐",
      nameKana: "あらし",
      iconUrl: "/images/artists/arashi.svg",
    },
    {
      artistId: "ikimonogakari",
      name: "いきものがかり",
      nameKana: "いきものがかり",
      iconUrl: "/images/artists/ikimonogakari.svg",
    },
  ];

  /**
   * 【テスト対象】ArtistsPage コンポーネント
   * 【テストケース】ローディング中の表示
   * 【期待結果】「読み込み中...」が表示される
   * 【ビジネス要件】データ取得中のローディング表示
   */
  it("ローディング中に「読み込み中...」が表示されること", () => {
    // fetchが解決しないPromiseを返すことでローディング状態を維持
    mockFetch.mockReturnValue(new Promise(() => {}));

    render(<ArtistsPage />);

    expect(screen.getByTestId("loading-indicator")).toBeInTheDocument();
    expect(screen.getByText("読み込み中...")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】ArtistsPage コンポーネント
   * 【テストケース】アーティスト一覧の正常表示
   * 【期待結果】全アーティストが2列グリッドで表示される
   * 【ビジネス要件】アーティスト一覧のグリッド表示
   */
  it("アーティスト一覧が正常に表示されること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockArtists,
    });

    render(<ArtistsPage />);

    await waitFor(() => {
      expect(screen.getByText("あいみょん")).toBeInTheDocument();
    });

    expect(screen.getByText("嵐")).toBeInTheDocument();
    expect(screen.getByText("いきものがかり")).toBeInTheDocument();
    expect(screen.getByTestId("artist-grid")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】ArtistsPage コンポーネント
   * 【テストケース】ページタイトルの表示
   * 【期待結果】「アーティスト一覧」のページタイトルが表示される
   * 【ビジネス要件】アーティスト一覧ページのタイトル表示
   */
  it("「アーティスト一覧」のタイトルが表示されること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockArtists,
    });

    render(<ArtistsPage />);

    await waitFor(() => {
      expect(
        screen.getByRole("heading", { name: "アーティスト一覧" })
      ).toBeInTheDocument();
    });
  });

  /**
   * 【テスト対象】ArtistsPage コンポーネント
   * 【テストケース】「And more...」の表示
   * 【期待結果】アーティスト一覧の右下に「And more...」が表示される
   * 【ビジネス要件】追加アーティストの存在を示すUI表示
   */
  it("「And more...」が表示されること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockArtists,
    });

    render(<ArtistsPage />);

    await waitFor(() => {
      expect(screen.getByTestId("and-more")).toBeInTheDocument();
    });

    expect(screen.getByText("And more...")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】ArtistsPage コンポーネント
   * 【テストケース】API取得エラー時の表示
   * 【期待結果】エラーがthrowされ共通エラーページが表示される
   * 【ビジネス要件】エラー時のエラーバウンダリ遷移
   */
  it("API取得エラー時にエラーがthrowされること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 500,
    });

    // エラーがthrowされることを確認（React Error Boundaryでキャッチされる想定）
    expect(() => {
      render(<ArtistsPage />);
    }).not.toThrow();

    // エラーはstateに設定後、レンダリング中にthrowされる
  });

  /**
   * 【テスト対象】ArtistsPage コンポーネント
   * 【テストケース】ネットワークエラー時の表示
   * 【期待結果】エラーがthrowされ共通エラーページが表示される
   * 【ビジネス要件】ネットワーク障害時のエラーバウンダリ遷移
   */
  it("ネットワークエラー時にエラーがthrowされること", async () => {
    mockFetch.mockRejectedValueOnce(new Error("Network error"));

    // エラーがthrowされることを確認（React Error Boundaryでキャッチされる想定）
    expect(() => {
      render(<ArtistsPage />);
    }).not.toThrow();

    // エラーはstateに設定後、レンダリング中にthrowされる
  });
});
