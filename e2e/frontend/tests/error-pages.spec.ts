/**
 * 【テスト対象】共通エラーページ
 * 【テストケース】404ページの表示・エラーページからの導線
 * 【期待結果】存在しないページでは404が表示され、TOPページへ戻れる
 * 【ビジネス要件】共通404エラーページ・共通エラーページ表示機能
 */
import { test, expect } from "@playwright/test";
import { setupMockRoutes } from "../msw/setup";

test.describe("共通エラーページ", () => {
  test.beforeEach(async ({ page }) => {
    await setupMockRoutes(page);
  });

  test("存在しないパスにアクセスすると404ページが表示されること", async ({
    page,
  }) => {
    // Given: 存在しないパスにアクセスする
    await page.goto("/nonexistent-page");

    // Then: 「404 Not Found」タイトルが表示される
    await expect(
      page.locator("[data-testid='not-found-title']"),
    ).toBeVisible();
    await expect(
      page.locator("[data-testid='not-found-title']"),
    ).toHaveText("404 Not Found");

    // Then: 「ページが見つかりません」メッセージが表示される
    await expect(
      page.locator("[data-testid='not-found-message']"),
    ).toBeVisible();
    await expect(
      page.locator("[data-testid='not-found-message']"),
    ).toHaveText("ページが見つかりません");
  });

  test("404ページに「TOPページへ戻る」リンクが表示されること", async ({
    page,
  }) => {
    // Given: 存在しないパスにアクセスする
    await page.goto("/nonexistent-page");

    // Then: 「TOPページへ戻る」リンクが表示される
    const homeLink = page.locator("[data-testid='not-found-home-link']");
    await expect(homeLink).toBeVisible();
    await expect(homeLink).toHaveText("TOPページへ戻る");
  });

  test("404ページから「TOPページへ戻る」でTOPページに遷移できること", async ({
    page,
  }) => {
    // Given: 存在しないパスにアクセスする
    await page.goto("/nonexistent-page");

    // When: 「TOPページへ戻る」リンクをクリックする
    await page.locator("[data-testid='not-found-home-link']").click();

    // Then: TOPページに遷移する
    await page.waitForURL("**/");
    await expect(
      page.locator("text=ようこそEntm-Cloneへ"),
    ).toBeVisible();
  });

  test("共通ヘッダーが404ページにも表示されること", async ({ page }) => {
    // Given: 存在しないパスにアクセスする
    await page.goto("/nonexistent-page");

    // Then: ヘッダーにサイトタイトル「Devin-Test」が表示される
    await expect(
      page.locator("header h1", { hasText: "Devin-Test" }),
    ).toBeVisible();
  });
});
