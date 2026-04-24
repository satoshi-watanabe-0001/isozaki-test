/**
 * アーティスト一覧ページの単体テスト
 *
 * SSR（Server Component）に変更後のアーティスト一覧ページをテストする。
 * async Server Componentのため、コンポーネント関数を直接awaitしてJSXを取得し描画する。
 */
import { render, screen } from "@testing-library/react";
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
   * 【テスト対象】ArtistsPage コンポーネント（SSR）
   * 【テストケース】アーティスト一覧の正常表示
   * 【期待結果】全アーティストが2列グリッドで表示される
   * 【ビジネス要件】アーティスト一覧のグリッド表示
   */
  it("アーティスト一覧が正常に表示されること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockArtists,
    });

    const jsx = await ArtistsPage();
    render(jsx);

    expect(screen.getByText("あいみょん")).toBeInTheDocument();
    expect(screen.getByText("嵐")).toBeInTheDocument();
    expect(screen.getByText("いきものがかり")).toBeInTheDocument();
    expect(screen.getByTestId("artist-grid")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】ArtistsPage コンポーネント（SSR）
   * 【テストケース】ページタイトルの表示
   * 【期待結果】「アーティスト一覧」のページタイトルが表示される
   * 【ビジネス要件】アーティスト一覧ページのタイトル表示
   */
  it("「アーティスト一覧」のタイトルが表示されること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockArtists,
    });

    const jsx = await ArtistsPage();
    render(jsx);

    expect(
      screen.getByRole("heading", { name: "アーティスト一覧" })
    ).toBeInTheDocument();
  });

  /**
   * 【テスト対象】ArtistsPage コンポーネント（SSR）
   * 【テストケース】「And more...」の表示
   * 【期待結果】アーティスト一覧の右下に「And more...」が表示される
   * 【ビジネス要件】追加アーティストの存在を示すUI表示
   */
  it("「And more...」が表示されること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockArtists,
    });

    const jsx = await ArtistsPage();
    render(jsx);

    expect(screen.getByTestId("and-more")).toBeInTheDocument();
    expect(screen.getByText("And more...")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】ArtistsPage コンポーネント（SSR）
   * 【テストケース】API取得エラー時
   * 【期待結果】Errorがthrowされる（Next.js Error Boundaryでキャッチ）
   * 【ビジネス要件】エラー時のエラーバウンダリ遷移
   */
  it("API取得エラー時にErrorがthrowされること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 500,
    });

    await expect(ArtistsPage()).rejects.toThrow(
      "アーティスト一覧の取得に失敗しました（500）"
    );
  });

  /**
   * 【テスト対象】ArtistsPage コンポーネント（SSR）
   * 【テストケース】ネットワークエラー時
   * 【期待結果】fetch自体がrejectしてErrorがthrowされる
   * 【ビジネス要件】ネットワーク障害時のエラーバウンダリ遷移
   */
  it("ネットワークエラー時にErrorがthrowされること", async () => {
    mockFetch.mockRejectedValueOnce(new Error("Network error"));

    await expect(ArtistsPage()).rejects.toThrow("Network error");
  });

  /**
   * 【テスト対象】ArtistsPage コンポーネント（SSR）
   * 【テストケース】fetchにcache: "no-store"が渡される
   * 【期待結果】SSRでキャッシュ無効化が設定されている
   * 【ビジネス要件】常に最新データを取得する
   */
  it("fetchにcache: \"no-store\"が渡されること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockArtists,
    });

    const jsx = await ArtistsPage();
    render(jsx);

    expect(mockFetch).toHaveBeenCalledWith(
      expect.any(String),
      { cache: "no-store" },
    );
  });
});
