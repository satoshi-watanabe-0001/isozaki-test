/**
 * 【テスト対象】コミュニティTOPページ
 * 【テストケース】アーティスト名・カルーセル・メニュー・キャンペーン・お知らせの各セクション表示
 * 【期待結果】コミュニティTOP APIからのデータが各セクションに正しく表示される
 * 【ビジネス要件】コミュニティTOPページ表示機能
 */
import { test, expect } from "@playwright/test";
import { setupMockRoutes, TEST_COMMUNITY_DATA } from "../msw/setup";

/** テスト用あいみょんデータの型アサーション */
const aimyonData = TEST_COMMUNITY_DATA["aimyon"] as {
  artistId: string;
  name: string;
  images: { imageId: number; imageUrl: string; displayOrder: number }[];
  campaigns: { campaignId: number; title: string; imageUrl: string }[];
  news: {
    newsId: number;
    title: string;
    publishedAt: string;
  }[];
};

test.describe("コミュニティTOPページ", () => {
  test.beforeEach(async ({ page }) => {
    // APIリクエストをモック化してバックエンドに依存しないテストを実現する
    await setupMockRoutes(page);
  });

  test("アーティスト名が先頭に表示されること", async ({ page }) => {
    // Given: あいみょんのコミュニティTOPページにアクセスする
    await page.goto("/community/aimyon");

    // Then: アーティスト名「あいみょん」がh1として表示される
    const artistName = page.locator("[data-testid='artist-name']");
    await expect(artistName).toBeVisible();
    await expect(artistName).toHaveText("あいみょん");
  });

  test("カルーセル画像が3件表示されインジケーターが機能すること", async ({
    page,
  }) => {
    // Given: あいみょんのコミュニティTOPページにアクセスする
    await page.goto("/community/aimyon");

    // Then: カルーセルセクションが表示される
    await expect(page.locator("[data-testid='carousel-section']")).toBeVisible();
    await expect(
      page.locator("[data-testid='carousel-container']"),
    ).toBeVisible();
    await expect(page.locator("[data-testid='carousel-image']")).toBeVisible();

    // Then: インジケーターが3つ表示される（画像3件に対応）
    const indicators = page.locator("[data-testid='carousel-indicators']");
    await expect(indicators).toBeVisible();
    for (let i = 0; i < aimyonData.images.length; i++) {
      await expect(
        page.locator(`[data-testid='carousel-indicator-${i}']`),
      ).toBeVisible();
    }

    // Then: 初期状態で1番目のインジケーターがアクティブ（bg-blue-600）
    await expect(
      page.locator("[data-testid='carousel-indicator-0']"),
    ).toHaveClass(/bg-blue-600/);

    // When: 2番目のインジケーターをクリックする
    await page.locator("[data-testid='carousel-indicator-1']").click();

    // Then: 2番目のインジケーターがアクティブになる
    await expect(
      page.locator("[data-testid='carousel-indicator-1']"),
    ).toHaveClass(/bg-blue-600/);
    await expect(
      page.locator("[data-testid='carousel-indicator-0']"),
    ).toHaveClass(/bg-gray-300/);
  });

  test("メニュー領域に6項目が表示されること", async ({ page }) => {
    // Given: あいみょんのコミュニティTOPページにアクセスする
    await page.goto("/community/aimyon");

    // Then: メニューセクションが表示される
    await expect(page.locator("[data-testid='menu-section']")).toBeVisible();

    // Then: 6つのメニュー項目が表示される
    const menuLabels = [
      "プロフィール",
      "イベント",
      "キャンペーン",
      "スレッド",
      "お知らせ",
      "公式ページ",
    ];
    for (const label of menuLabels) {
      await expect(
        page.locator(`[data-testid='menu-item-${label}']`),
      ).toBeVisible();
    }
  });

  test("キャンペーン領域に3件のキャンペーンが表示されること", async ({
    page,
  }) => {
    // Given: あいみょんのコミュニティTOPページにアクセスする
    await page.goto("/community/aimyon");

    // Then: キャンペーンセクションが表示される
    await expect(
      page.locator("[data-testid='campaign-section']"),
    ).toBeVisible();
    await expect(page.locator("[data-testid='campaign-list']")).toBeVisible();

    // Then: 3件のキャンペーンが表示される
    for (const campaign of aimyonData.campaigns) {
      await expect(
        page.locator(
          `[data-testid='campaign-item-${campaign.campaignId}']`,
        ),
      ).toBeVisible();
    }

    // Then: キャンペーンタイトルが表示される
    await expect(page.locator("text=ライブツアー2025")).toBeVisible();
    await expect(page.locator("text=ニューアルバム発売記念")).toBeVisible();
    await expect(page.locator("text=ファンクラブ限定イベント")).toBeVisible();
  });

  test("お知らせ領域に5件のニュースが新着順で表示されること", async ({
    page,
  }) => {
    // Given: あいみょんのコミュニティTOPページにアクセスする
    await page.goto("/community/aimyon");

    // Then: お知らせセクションが表示される
    await expect(page.locator("[data-testid='news-section']")).toBeVisible();
    await expect(page.locator("[data-testid='news-list']")).toBeVisible();

    // Then: 5件のお知らせが表示される
    for (const newsItem of aimyonData.news) {
      await expect(
        page.locator(`[data-testid='news-item-${newsItem.newsId}']`),
      ).toBeVisible();
    }

    // Then: お知らせタイトルが正しく表示される
    await expect(
      page.locator("text=ニューシングル「風になりたい」リリース決定"),
    ).toBeVisible();
    await expect(
      page.locator("text=ファンクラブ会員限定イベント開催"),
    ).toBeVisible();
  });

  test("存在しないアーティストIDの場合に共通404ページが表示されること", async ({
    page,
  }) => {
    // Given: 存在しないアーティストのコミュニティTOPページにアクセスする
    await page.goto("/community/unknown-artist");

    // Then: 共通404エラーページが表示される
    await expect(
      page.locator("[data-testid='not-found-title']"),
    ).toBeVisible();
    await expect(
      page.locator("[data-testid='not-found-title']"),
    ).toHaveText("404 Not Found");
    await expect(
      page.locator("[data-testid='not-found-message']"),
    ).toHaveText("ページが見つかりません");
  });

  test("関連データがないアーティストでもアーティスト名とメニューが表示されること", async ({
    page,
  }) => {
    // Given: 関連データが空のアーティスト（GLAY）のコミュニティTOPページにアクセスする
    await page.goto("/community/glay");

    // Then: アーティスト名が表示される
    await expect(page.locator("[data-testid='artist-name']")).toHaveText(
      "GLAY",
    );

    // Then: メニューセクションは表示される
    await expect(page.locator("[data-testid='menu-section']")).toBeVisible();

    // Then: カルーセル・キャンペーン・お知らせは非表示
    await expect(
      page.locator("[data-testid='carousel-section']"),
    ).not.toBeVisible();
    await expect(
      page.locator("[data-testid='campaign-section']"),
    ).not.toBeVisible();
    await expect(
      page.locator("[data-testid='news-section']"),
    ).not.toBeVisible();
  });

  test("共通ヘッダーがコミュニティTOPページにも表示されること", async ({
    page,
  }) => {
    // Given: コミュニティTOPページにアクセスする
    await page.goto("/community/aimyon");

    // Then: ヘッダーにサイトタイトル「Devin-Test」が表示される
    await expect(
      page.locator("header h1", { hasText: "Devin-Test" }),
    ).toBeVisible();

    // Then: 未ログイン状態でログインボタンが表示される
    await expect(
      page.locator("header button", { hasText: "ログイン" }),
    ).toBeVisible();
  });
});
