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
      artistId: "01908b7e-2001-7000-8000-000000000001",
      name: "あいみょん",
      nameKana: "あいみょん",
      iconUrl: "https://placehold.co/150x150?text=A",
    },
    {
      artistId: "01908b7e-2006-7000-8000-000000000006",
      name: "嵐",
      nameKana: "あらし",
      iconUrl: "https://placehold.co/150x150?text=AR",
    },
    {
      artistId: "01908b7e-2002-7000-8000-000000000002",
      name: "いきものがかり",
      nameKana: "いきものがかり",
      iconUrl: "https://placehold.co/150x150?text=I",
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
   * 【期待結果】エラーメッセージが表示される
   * 【ビジネス要件】エラー時のユーザーフィードバック
   */
  it("API取得エラー時にエラーメッセージが表示されること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 500,
    });

    render(<ArtistsPage />);

    await waitFor(() => {
      expect(screen.getByTestId("error-message")).toBeInTheDocument();
    });
  });

  /**
   * 【テスト対象】ArtistsPage コンポーネント
   * 【テストケース】ネットワークエラー時の表示
   * 【期待結果】エラーメッセージが表示される
   * 【ビジネス要件】ネットワーク障害時のユーザーフィードバック
   */
  it("ネットワークエラー時にエラーメッセージが表示されること", async () => {
    mockFetch.mockRejectedValueOnce(new Error("Network error"));

    render(<ArtistsPage />);

    await waitFor(() => {
      expect(screen.getByTestId("error-message")).toBeInTheDocument();
    });

    expect(screen.getByText("Network error")).toBeInTheDocument();
  });
});
