/**
 * コミュニティTOPページコンポーネントの単体テスト
 *
 * SSR（Server Component + Client Component）に変更後のテスト。
 * Server Component（page.tsx）はasync関数を直接テストし、
 * Client Component（CommunityTopContent）はrenderでUIテストする。
 */
/* eslint-disable jsx-a11y/alt-text, @next/next/no-img-element */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, type Mock } from "vitest";
import type { CommunityTop } from "@/types/community";

/** next/navigation のモック（notFoundはNext.js同様にthrowして実行を中断する） */
const mockNotFound = vi.fn();
vi.mock("next/navigation", () => ({
  notFound: (): never => {
    mockNotFound();
    throw new Error("NEXT_NOT_FOUND");
  },
}));

/** embla-carousel-react のモック（テスト環境ではDOMレイアウトが存在しないため） */
vi.mock("embla-carousel-react", () => ({
  default: () => [vi.fn(), null],
}));

/** next/image のモック */
vi.mock("next/image", () => ({
  default: (props: Record<string, unknown>) => {
    const { fill, ...rest } = props;
    return <img {...rest} data-fill={fill ? "true" : undefined} />;
  },
}));

/** fetchをグローバルモックとして定義 */
const mockFetch: Mock = vi.fn();
global.fetch = mockFetch;

/** テスト用コミュニティTOPデータ */
const mockCommunityData: CommunityTop = {
  artistId: "aimyon",
  name: "あいみょん",
  images: [
    { imageId: 1, imageUrl: "/images/artists/aimyon.svg", displayOrder: 1 },
    { imageId: 2, imageUrl: "/images/artists/aimyon.svg", displayOrder: 2 },
    { imageId: 3, imageUrl: "/images/artists/aimyon.svg", displayOrder: 3 },
  ],
  campaigns: [
    { campaignId: 1, title: "ライブツアー2025", imageUrl: "/images/campaigns/default.svg" },
    { campaignId: 2, title: "ニューアルバム発売記念", imageUrl: "/images/campaigns/default.svg" },
    { campaignId: 3, title: "ファンクラブ限定イベント", imageUrl: "/images/campaigns/default.svg" },
  ],
  news: [
    { newsId: 1, title: "ニューシングル「風になりたい」リリース決定", publishedAt: "2025-04-10T10:00:00Z" },
    { newsId: 2, title: "全国ツアー2025 追加公演決定", publishedAt: "2025-04-08T12:00:00Z" },
    { newsId: 3, title: "テレビ出演情報（4月）", publishedAt: "2025-04-05T09:00:00Z" },
    { newsId: 4, title: "オフィシャルグッズ新商品のお知らせ", publishedAt: "2025-04-01T15:00:00Z" },
    { newsId: 5, title: "ファンクラブ会員限定イベント開催", publishedAt: "2025-03-28T11:00:00Z" },
  ],
};

describe("CommunityTopPage（Server Component）", () => {
  beforeEach(() => {
    mockFetch.mockReset();
    mockNotFound.mockClear();
  });

  /**
   * 【テスト対象】CommunityTopPage Server Component
   * 【テストケース】正常レスポンス時のSSR描画
   * 【期待結果】CommunityTopContentにデータが渡されアーティスト名が表示される
   * 【ビジネス要件】コミュニティTOPページのSSR表示
   */
  it("正常レスポンス時にアーティスト名が表示されること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockCommunityData,
    } as Response);

    const { default: CommunityTopPage } = await import("@/app/community/[artistId]/page");
    const jsx = await CommunityTopPage({ params: Promise.resolve({ artistId: "aimyon" }) });
    render(jsx);

    expect(screen.getByTestId("artist-name")).toHaveTextContent("あいみょん");
  });

  /**
   * 【テスト対象】CommunityTopPage Server Component
   * 【テストケース】API 404レスポンス時
   * 【期待結果】notFound()が呼び出される
   * 【ビジネス要件】存在しないアーティストの404ハンドリング
   */
  it("API 404レスポンス時にnotFound()が呼び出されること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 404,
    } as Response);

    const { default: CommunityTopPage } = await import("@/app/community/[artistId]/page");

    await expect(
      CommunityTopPage({ params: Promise.resolve({ artistId: "unknown" }) }),
    ).rejects.toThrow("NEXT_NOT_FOUND");

    expect(mockNotFound).toHaveBeenCalled();
  });

  /**
   * 【テスト対象】CommunityTopPage Server Component
   * 【テストケース】API 500エラー時
   * 【期待結果】Errorがthrowされる
   * 【ビジネス要件】サーバーエラー時のエラーバウンダリ遷移
   */
  it("API 500エラー時にErrorがthrowされること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 500,
    } as Response);

    const { default: CommunityTopPage } = await import("@/app/community/[artistId]/page");

    await expect(
      CommunityTopPage({ params: Promise.resolve({ artistId: "aimyon" }) }),
    ).rejects.toThrow("コミュニティ情報の取得に失敗しました（500）");
  });

  /**
   * 【テスト対象】CommunityTopPage Server Component
   * 【テストケース】fetchにcache: "no-store"が渡される
   * 【期待結果】SSRでキャッシュ無効化が設定されている
   * 【ビジネス要件】常に最新データを取得する
   */
  it("fetchにcache: \"no-store\"が渡されること", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockCommunityData,
    } as Response);

    const { default: CommunityTopPage } = await import("@/app/community/[artistId]/page");
    const jsx = await CommunityTopPage({ params: Promise.resolve({ artistId: "aimyon" }) });
    render(jsx);

    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining("/api/v1/community/aimyon"),
      { cache: "no-store" },
    );
  });
});

describe("CommunityTopContent（Client Component）", () => {
  /**
   * 【テスト対象】CommunityTopContent コンポーネント
   * 【テストケース】カルーセル画像の表示
   * 【期待結果】カルーセルセクションとインジケーターが表示される
   * 【ビジネス要件】カルーセル画像表示（最大3件）
   */
  it("カルーセル画像とインジケーターが表示されること", async () => {
    const { default: CommunityTopContent } = await import("@/components/CommunityTopContent");
    render(<CommunityTopContent communityData={mockCommunityData} artistId="aimyon" />);

    expect(screen.getByTestId("carousel-section")).toBeInTheDocument();
    expect(screen.getByTestId("carousel-image")).toBeInTheDocument();
    expect(screen.getByTestId("carousel-indicators")).toBeInTheDocument();
    expect(screen.getByTestId("carousel-indicator-0")).toBeInTheDocument();
    expect(screen.getByTestId("carousel-indicator-1")).toBeInTheDocument();
    expect(screen.getByTestId("carousel-indicator-2")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】CommunityTopContent コンポーネント
   * 【テストケース】メニュー領域の表示
   * 【期待結果】6つのメニュー項目が4列で表示される
   * 【ビジネス要件】メニュー領域の表示
   */
  it("6つのメニュー項目が表示されること", async () => {
    const { default: CommunityTopContent } = await import("@/components/CommunityTopContent");
    render(<CommunityTopContent communityData={mockCommunityData} artistId="aimyon" />);

    expect(screen.getByTestId("menu-section")).toBeInTheDocument();
    expect(screen.getByTestId("menu-item-プロフィール")).toBeInTheDocument();
    expect(screen.getByTestId("menu-item-イベント")).toBeInTheDocument();
    expect(screen.getByTestId("menu-item-キャンペーン")).toBeInTheDocument();
    expect(screen.getByTestId("menu-item-スレッド")).toBeInTheDocument();
    expect(screen.getByTestId("menu-item-お知らせ")).toBeInTheDocument();
    expect(screen.getByTestId("menu-item-公式ページ")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】CommunityTopContent コンポーネント
   * 【テストケース】キャンペーン領域の表示
   * 【期待結果】キャンペーン画像とタイトルが表示される
   * 【ビジネス要件】キャンペーン画像表示（最大3件）
   */
  it("キャンペーン情報が表示されること", async () => {
    const { default: CommunityTopContent } = await import("@/components/CommunityTopContent");
    render(<CommunityTopContent communityData={mockCommunityData} artistId="aimyon" />);

    expect(screen.getByTestId("campaign-section")).toBeInTheDocument();
    expect(screen.getByTestId("campaign-list")).toBeInTheDocument();
    expect(screen.getByText("ライブツアー2025")).toBeInTheDocument();
    expect(screen.getByText("ニューアルバム発売記念")).toBeInTheDocument();
    expect(screen.getByText("ファンクラブ限定イベント")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】CommunityTopContent コンポーネント
   * 【テストケース】お知らせ領域の表示
   * 【期待結果】お知らせタイトルが新着順で最大5件表示される
   * 【ビジネス要件】お知らせ情報表示（新着順、最大5件）
   */
  it("お知らせ情報が新着順で表示されること", async () => {
    const { default: CommunityTopContent } = await import("@/components/CommunityTopContent");
    render(<CommunityTopContent communityData={mockCommunityData} artistId="aimyon" />);

    expect(screen.getByTestId("news-section")).toBeInTheDocument();
    expect(screen.getByTestId("news-list")).toBeInTheDocument();
    expect(screen.getByText("ニューシングル「風になりたい」リリース決定")).toBeInTheDocument();
    expect(screen.getByText("全国ツアー2025 追加公演決定")).toBeInTheDocument();
    expect(screen.getByText("テレビ出演情報（4月）")).toBeInTheDocument();
    expect(screen.getByText("オフィシャルグッズ新商品のお知らせ")).toBeInTheDocument();
    expect(screen.getByText("ファンクラブ会員限定イベント開催")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】CommunityTopContent コンポーネント
   * 【テストケース】関連データが空の場合
   * 【期待結果】アーティスト名とメニューは表示されるが、カルーセル・キャンペーン・お知らせは非表示
   * 【ビジネス要件】関連データ未登録時のUI
   */
  it("関連データが空の場合、セクションが非表示になること", async () => {
    const emptyData: CommunityTop = {
      artistId: "glay",
      name: "GLAY",
      images: [],
      campaigns: [],
      news: [],
    };
    const { default: CommunityTopContent } = await import("@/components/CommunityTopContent");
    render(<CommunityTopContent communityData={emptyData} artistId="glay" />);

    expect(screen.getByTestId("artist-name")).toHaveTextContent("GLAY");
    expect(screen.getByTestId("menu-section")).toBeInTheDocument();
    expect(screen.queryByTestId("carousel-section")).not.toBeInTheDocument();
    expect(screen.queryByTestId("campaign-section")).not.toBeInTheDocument();
    expect(screen.queryByTestId("news-section")).not.toBeInTheDocument();
  });
});
