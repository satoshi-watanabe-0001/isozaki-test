/**
 * 【テスト対象】アーティスト一覧ページ
 * 【テストケース】アーティスト一覧の表示・グリッドレイアウト・コミュニティTOPへの遷移
 * 【期待結果】APIから取得したアーティストが2列グリッドで50音順に表示され、各カードからコミュニティTOPページへ遷移できる
 * 【ビジネス要件】アーティスト一覧表示・コミュニティTOPページ遷移機能
 */
import { test, expect } from "@playwright/test";
import { setupMockRoutes, TEST_ARTISTS } from "../msw/setup";

test.describe("アーティスト一覧ページ", () => {
  test.beforeEach(async ({ page }) => {
    // APIリクエストをモック化してバックエンドに依存しないテストを実現する
    await setupMockRoutes(page);
  });

  test("ページタイトル「アーティスト一覧」が表示されること", async ({
    page,
  }) => {
    // Given: アーティスト一覧ページにアクセスする
    await page.goto("/artists");

    // Then: ページタイトルがh1として表示される
    const heading = page.locator("h1", { hasText: "アーティスト一覧" });
    await expect(heading).toBeVisible();
  });

  test("アーティストが2列グリッドで表示されること", async ({ page }) => {
    // Given: アーティスト一覧ページにアクセスする
    await page.goto("/artists");

    // Then: 2列グリッドコンテナが表示される
    const grid = page.locator("[data-testid='artist-grid']");
    await expect(grid).toBeVisible();

    // Then: グリッドがgrid-cols-2クラスを持つ（2列レイアウト）
    await expect(grid).toHaveClass(/grid-cols-2/);
  });

  test("全アーティスト（10件）が表示されること", async ({ page }) => {
    // Given: アーティスト一覧ページにアクセスする
    await page.goto("/artists");

    // Then: テストデータの全アーティストカードが表示される
    for (const artist of TEST_ARTISTS) {
      const card = page.locator(
        `[data-testid='artist-card-${artist.artistId}']`,
      );
      await expect(card).toBeVisible();
    }
  });

  test("アーティストが50音順に表示されること", async ({ page }) => {
    // Given: アーティスト一覧ページにアクセスする
    await page.goto("/artists");

    // Then: 先頭のアーティストが「あいみょん」であること（50音順の最初）
    const firstCard = page.locator("[data-testid='artist-card-aimyon']");
    await expect(firstCard).toContainText("あいみょん");

    // Then: 末尾のアーティストが「DREAMS COME TRUE」であること（50音順の最後）
    const lastCard = page.locator(
      "[data-testid='artist-card-dreams-come-true']",
    );
    await expect(lastCard).toContainText("DREAMS COME TRUE");
  });

  test("「And more...」が右下に表示されること", async ({ page }) => {
    // Given: アーティスト一覧ページにアクセスする
    await page.goto("/artists");

    // Then: 「And more...」テキストが表示される
    const andMore = page.locator("[data-testid='and-more']");
    await expect(andMore).toBeVisible();
    await expect(andMore).toHaveText("And more...");
  });

  test("アーティストカードからコミュニティTOPページへ遷移できること", async ({
    page,
  }) => {
    // Given: アーティスト一覧ページにアクセスする
    await page.goto("/artists");

    // When: 「あいみょん」のカードをクリックする
    const aimyonCard = page.locator("[data-testid='artist-card-aimyon']");
    await expect(aimyonCard).toBeVisible();
    await aimyonCard.click();

    // Then: /community/aimyon に遷移する
    await page.waitForURL("**/community/aimyon");

    // Then: コミュニティTOPページのアーティスト名が表示される
    await expect(page.locator("[data-testid='artist-name']")).toHaveText(
      "あいみょん",
    );
  });

  test("アーティストカードにアイコン画像が表示されること", async ({
    page,
  }) => {
    // Given: アーティスト一覧ページにアクセスする
    await page.goto("/artists");

    // Then: 先頭のアーティストカード内に画像が表示される
    const card = page.locator("[data-testid='artist-card-aimyon']");
    await expect(card).toBeVisible();
    const img = card.locator("img");
    await expect(img).toBeVisible();
  });
});
