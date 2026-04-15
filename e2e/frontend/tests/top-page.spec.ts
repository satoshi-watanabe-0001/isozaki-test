/**
 * 【テスト対象】トップページ
 * 【テストケース】トップページの基本表示確認
 * 【期待結果】ウェルカムメッセージ・アーティスト一覧リンク・ヘッダーが正しく表示される
 * 【ビジネス要件】フロントエンドトップページ表示機能
 */
import { test, expect } from "@playwright/test";
import { setupMockRoutes } from "../msw/setup";

test.describe("トップページ表示", () => {
  test.beforeEach(async ({ page }) => {
    // APIリクエストをモック化してバックエンドに依存しないテストを実現する
    await setupMockRoutes(page);
  });

  test("「ようこそEntm-Cloneへ」ウェルカムメッセージが表示されること", async ({
    page,
  }) => {
    // Given: トップページにアクセスする
    await page.goto("/");

    // When: ページが読み込まれる

    // Then: 「ようこそEntm-Cloneへ」のウェルカムメッセージがh1として表示される
    const heading = page.locator("h1", { hasText: "ようこそEntm-Cloneへ" });
    await expect(heading).toBeVisible();
  });

  test("「frontendテストページ」サブメッセージが表示されること", async ({
    page,
  }) => {
    // Given: トップページにアクセスする
    await page.goto("/");

    // When: ページが読み込まれる

    // Then: 「frontendテストページ」のサブメッセージが表示される
    const subMessage = page.locator("p", { hasText: "frontendテストページ" });
    await expect(subMessage).toBeVisible();
  });

  test("「アーティスト一覧」リンクが表示され遷移できること", async ({
    page,
  }) => {
    // Given: トップページにアクセスする
    await page.goto("/");

    // When: ページが読み込まれる

    // Then: 「アーティスト一覧」リンクが表示され、/artists へのhrefが設定されている
    const artistLink = page.locator("a", { hasText: "アーティスト一覧" });
    await expect(artistLink).toBeVisible();
    await expect(artistLink).toHaveAttribute("href", "/artists");
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
