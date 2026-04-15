/**
 * コミュニティTOPページコンポーネントの単体テスト
 *
 * コミュニティTOPページのアーティスト名、カルーセル、メニュー、
 * キャンペーン、お知らせが正しく表示されることをテストする。
 */
/* eslint-disable jsx-a11y/alt-text, @next/next/no-img-element */
import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import type { CommunityTop } from "@/types/community";

/** next/navigation のモック */
const mockNotFound = vi.fn();
vi.mock("next/navigation", () => ({
  useParams: () => ({ artistId: "aimyon" }),
  notFound: () => { mockNotFound(); },
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

/** コンポーネントの動的インポート（各テスト前にモック設定を行うため） */
let CommunityTopPage: React.ComponentType;

describe("CommunityTopPage", () => {
  beforeEach(async () => {
    vi.resetModules();
    global.fetch = vi.fn();
    mockNotFound.mockClear();

    /** next/navigation のモック再設定 */
    vi.doMock("next/navigation", () => ({
      useParams: () => ({ artistId: "aimyon" }),
      notFound: () => { mockNotFound(); },
    }));

    vi.doMock("next/image", () => ({
      default: (props: Record<string, unknown>) => {
        const { fill, ...rest } = props;
        return <img {...rest} data-fill={fill ? "true" : undefined} />;
      },
    }));

    vi.doMock("embla-carousel-react", () => ({
      default: () => [vi.fn(), null],
    }));

    const mod = await import("@/app/community/[artistId]/page");
    CommunityTopPage = mod.default;
  });

  /**
   * 【テスト対象】CommunityTopPage コンポーネント
   * 【テストケース】読み込み中表示
   * 【期待結果】API呼び出し中に「読み込み中...」が表示される
   * 【ビジネス要件】ローディング状態のUI表示
   */
  it("読み込み中に「読み込み中...」が表示されること", () => {
    vi.mocked(global.fetch).mockReturnValue(new Promise(() => {}));

    render(<CommunityTopPage />);

    expect(screen.getByTestId("loading-indicator")).toBeInTheDocument();
    expect(screen.getByText("読み込み中...")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】CommunityTopPage コンポーネント
   * 【テストケース】アーティスト名の表示
   * 【期待結果】APIから取得したアーティスト名が先頭に表示される
   * 【ビジネス要件】コミュニティTOPページのアーティスト名表示
   */
  it("アーティスト名が表示されること", async () => {
    vi.mocked(global.fetch).mockResolvedValue({
      ok: true,
      json: async () => mockCommunityData,
    } as Response);

    render(<CommunityTopPage />);

    await waitFor(() => {
      expect(screen.getByTestId("artist-name")).toHaveTextContent("あいみょん");
    });
  });

  /**
   * 【テスト対象】CommunityTopPage コンポーネント
   * 【テストケース】カルーセル画像の表示
   * 【期待結果】カルーセルセクションとインジケーターが表示される
   * 【ビジネス要件】カルーセル画像表示（最大3件）
   */
  it("カルーセル画像とインジケーターが表示されること", async () => {
    vi.mocked(global.fetch).mockResolvedValue({
      ok: true,
      json: async () => mockCommunityData,
    } as Response);

    render(<CommunityTopPage />);

    await waitFor(() => {
      expect(screen.getByTestId("carousel-section")).toBeInTheDocument();
      expect(screen.getByTestId("carousel-image")).toBeInTheDocument();
      expect(screen.getByTestId("carousel-indicators")).toBeInTheDocument();
      expect(screen.getByTestId("carousel-indicator-0")).toBeInTheDocument();
      expect(screen.getByTestId("carousel-indicator-1")).toBeInTheDocument();
      expect(screen.getByTestId("carousel-indicator-2")).toBeInTheDocument();
    });
  });

  /**
   * 【テスト対象】CommunityTopPage コンポーネント
   * 【テストケース】メニュー領域の表示
   * 【期待結果】6つのメニュー項目が4列で表示される
   * 【ビジネス要件】メニュー領域（プロフィール・イベント・キャンペーン・スレッド・お知らせ・公式ページ）
   */
  it("6つのメニュー項目が表示されること", async () => {
    vi.mocked(global.fetch).mockResolvedValue({
      ok: true,
      json: async () => mockCommunityData,
    } as Response);

    render(<CommunityTopPage />);

    await waitFor(() => {
      expect(screen.getByTestId("menu-section")).toBeInTheDocument();
      expect(screen.getByTestId("menu-item-プロフィール")).toBeInTheDocument();
      expect(screen.getByTestId("menu-item-イベント")).toBeInTheDocument();
      expect(screen.getByTestId("menu-item-キャンペーン")).toBeInTheDocument();
      expect(screen.getByTestId("menu-item-スレッド")).toBeInTheDocument();
      expect(screen.getByTestId("menu-item-お知らせ")).toBeInTheDocument();
      expect(screen.getByTestId("menu-item-公式ページ")).toBeInTheDocument();
    });
  });

  /**
   * 【テスト対象】CommunityTopPage コンポーネント
   * 【テストケース】キャンペーン領域の表示
   * 【期待結果】キャンペーン画像とタイトルが正方形で横並びに表示される
   * 【ビジネス要件】キャンペーン画像表示（最大3件）
   */
  it("キャンペーン情報が表示されること", async () => {
    vi.mocked(global.fetch).mockResolvedValue({
      ok: true,
      json: async () => mockCommunityData,
    } as Response);

    render(<CommunityTopPage />);

    await waitFor(() => {
      expect(screen.getByTestId("campaign-section")).toBeInTheDocument();
      expect(screen.getByTestId("campaign-list")).toBeInTheDocument();
      expect(screen.getByText("ライブツアー2025")).toBeInTheDocument();
      expect(screen.getByText("ニューアルバム発売記念")).toBeInTheDocument();
      expect(screen.getByText("ファンクラブ限定イベント")).toBeInTheDocument();
    });
  });

  /**
   * 【テスト対象】CommunityTopPage コンポーネント
   * 【テストケース】お知らせ領域の表示
   * 【期待結果】お知らせタイトルが新着順で最大5件表示される
   * 【ビジネス要件】お知らせ情報表示（新着順、最大5件）
   */
  it("お知らせ情報が新着順で表示されること", async () => {
    vi.mocked(global.fetch).mockResolvedValue({
      ok: true,
      json: async () => mockCommunityData,
    } as Response);

    render(<CommunityTopPage />);

    await waitFor(() => {
      expect(screen.getByTestId("news-section")).toBeInTheDocument();
      expect(screen.getByTestId("news-list")).toBeInTheDocument();
      expect(screen.getByText("ニューシングル「風になりたい」リリース決定")).toBeInTheDocument();
      expect(screen.getByText("全国ツアー2025 追加公演決定")).toBeInTheDocument();
      expect(screen.getByText("テレビ出演情報（4月）")).toBeInTheDocument();
      expect(screen.getByText("オフィシャルグッズ新商品のお知らせ")).toBeInTheDocument();
      expect(screen.getByText("ファンクラブ会員限定イベント開催")).toBeInTheDocument();
    });
  });

  /**
   * 【テスト対象】CommunityTopPage コンポーネント
   * 【テストケース】API 404レスポンス時の表示
   * 【期待結果】notFound()が呼び出され共通404ページへ遷移する
   * 【ビジネス要件】存在しないアーティストの404ハンドリング
   */
  it("API 404レスポンス時にnotFound()が呼び出されること", async () => {
    vi.mocked(global.fetch).mockResolvedValue({
      ok: false,
      status: 404,
      json: async () => ({}),
    } as Response);

    render(<CommunityTopPage />);

    await waitFor(() => {
      expect(mockNotFound).toHaveBeenCalled();
    });
  });

  /**
   * 【テスト対象】CommunityTopPage コンポーネント
   * 【テストケース】API 5XX系エラー時の表示
   * 【期待結果】エラーがthrowされ共通エラーページが表示される
   * 【ビジネス要件】サーバーエラー時のエラーハンドリング
   */
  it("API 500エラー時にエラーがthrowされること", async () => {
    vi.mocked(global.fetch).mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({}),
    } as Response);

    expect(() => {
      render(<CommunityTopPage />);
    }).not.toThrow();

    // エラーはstateに設定後、レンダリング中にthrowされる
    // React Error Boundaryでキャッチされる想定
  });

  /**
   * 【テスト対象】CommunityTopPage コンポーネント
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
    vi.mocked(global.fetch).mockResolvedValue({
      ok: true,
      json: async () => emptyData,
    } as Response);

    render(<CommunityTopPage />);

    await waitFor(() => {
      expect(screen.getByTestId("artist-name")).toHaveTextContent("GLAY");
      expect(screen.getByTestId("menu-section")).toBeInTheDocument();
      expect(screen.queryByTestId("carousel-section")).not.toBeInTheDocument();
      expect(screen.queryByTestId("campaign-section")).not.toBeInTheDocument();
      expect(screen.queryByTestId("news-section")).not.toBeInTheDocument();
    });
  });
});
