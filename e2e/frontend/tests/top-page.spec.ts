/**
 * 【テスト対象】トップページ
 * 【テストケース】トップページの基本表示確認
 * 【期待結果】「frontendテストページ」メッセージとヘッダーが正しく表示される
 * 【ビジネス要件】フロントエンドトップページ表示機能
 */
import { test, expect } from "@playwright/test";
import { setupMockRoutes } from "../msw/setup";

test.describe("トップページ表示", () => {
  test.beforeEach(async ({ page }) => {
    // APIリクエストをモック化してバックエンドに依存しないテストを実現する
    await setupMockRoutes(page);
  });

  test("「frontendテストページ」メッセージが表示されること", async ({
    page,
  }) => {
    // Given: トップページにアクセスする
    await page.goto("/");

    // When: ページが読み込まれる

    // Then: 「frontendテストページ」のテキストが表示される
    const heading = page.locator("h1", { hasText: "frontendテストページ" });
    await expect(heading).toBeVisible();
  });

  test("共通ヘッダーが表示されること", async ({ page }) => {
    // Given: トップページにアクセスする
    await page.goto("/");

    // When: ページが読み込まれる

    // Then: ヘッダーにサイトタイトル「Devin-Test」が表示される
    const headerTitle = page.locator("header h1", { hasText: "Devin-Test" });
    await expect(headerTitle).toBeVisible();
  });

  test("未ログイン状態でログインボタンが表示されること", async ({ page }) => {
    // Given: トップページにアクセスする（未ログイン状態）
    await page.goto("/");

    // When: ページが読み込まれる

    // Then: ヘッダーにログインボタンが表示される
    const loginButton = page.locator("header button", {
      hasText: "ログイン",
    });
    await expect(loginButton).toBeVisible();
  });
});
