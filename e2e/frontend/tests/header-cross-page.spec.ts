/**
 * 【テスト対象】共通ヘッダー（ページ横断）
 * 【テストケース】複数ページでの共通ヘッダー表示確認
 * 【期待結果】トップページ・テストページの両方で共通ヘッダーが正しく表示される
 * 【ビジネス要件】全ページ共通ヘッダー表示機能
 */
import { test, expect } from "@playwright/test";
import { setupMockRoutes, TEST_USER } from "../msw/setup";

test.describe("共通ヘッダー（ページ横断）", () => {
  test.beforeEach(async ({ page }) => {
    // APIリクエストをモック化してバックエンドに依存しないテストを実現する
    await setupMockRoutes(page);
  });

  test("トップページでヘッダーが表示されること", async ({ page }) => {
    // Given: トップページにアクセスする
    await page.goto("/");

    // Then: ヘッダーにサイトタイトルが表示される
    await expect(
      page.locator("header h1", { hasText: "Devin-Test" }),
    ).toBeVisible();
  });

  test("/test ページでも同じヘッダーが表示されること", async ({ page }) => {
    // Given: テストページにアクセスする
    await page.goto("/test");

    // Then: ヘッダーにサイトタイトルが表示される
    await expect(
      page.locator("header h1", { hasText: "Devin-Test" }),
    ).toBeVisible();

    // Then: テストページ固有のコンテンツも表示される
    await expect(
      page.locator("h1", { hasText: "テストページ" }),
    ).toBeVisible();
  });

  test("ログイン状態が別ページに遷移しても維持されること", async ({
    page,
  }) => {
    // Given: トップページでログインする
    await page.goto("/");
    await page.locator("header button", { hasText: "ログイン" }).click();
    await page.locator("#email").fill("test@example.com");
    await page.locator("#password").fill("password123");
    await page.locator("[role='dialog'] button[type='submit']").click();
    await expect(page.locator("[data-testid='user-id']")).toContainText(
      TEST_USER.userId,
    );

    // When: テストページに遷移する
    await page.goto("/test");

    // Then: テストページでもログイン状態が維持される
    // sessionStorageはタブ単位で維持されるため、ページ遷移後も有効
    await expect(page.locator("[data-testid='user-id']")).toContainText(
      TEST_USER.userId,
    );
    await expect(page.locator("[data-testid='user-name']")).toContainText(
      TEST_USER.username,
    );
    await expect(
      page.locator("header button", { hasText: "ログアウト" }),
    ).toBeVisible();
  });

  test("未ログイン状態でテストページにアクセスしてもログインボタンが表示されること", async ({
    page,
  }) => {
    // Given: テストページに直接アクセスする（未ログイン状態）
    await page.goto("/test");

    // Then: ヘッダーにログインボタンが表示される
    await expect(
      page.locator("header button", { hasText: "ログイン" }),
    ).toBeVisible();
  });
});
