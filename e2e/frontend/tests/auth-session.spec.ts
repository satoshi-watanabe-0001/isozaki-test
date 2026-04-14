/**
 * 【テスト対象】認証状態管理（セッション保持・復元）
 * 【テストケース】ログイン後のセッション保持、ページリロード時の復元、ログアウト処理
 * 【期待結果】sessionStorageによるセッション保持とバックエンド検証が正しく動作する
 * 【ビジネス要件】ログインセッション保持機能
 */
import { test, expect } from "@playwright/test";
import { setupMockRoutes, TEST_USER } from "../msw/setup";

test.describe("認証状態管理", () => {
  test.beforeEach(async ({ page }) => {
    // APIリクエストをモック化してバックエンドに依存しないテストを実現する
    await setupMockRoutes(page);
  });

  test("ログイン後にページをリロードしてもログイン状態が維持されること", async ({
    page,
  }) => {
    // Given: ログインを実行する
    await page.goto("/");
    await page.locator("header button", { hasText: "ログイン" }).click();
    await page.locator("#email").fill("test@example.com");
    await page.locator("#password").fill("password123");
    await page.locator("[role='dialog'] button[type='submit']").click();

    // ログイン成功を確認する
    await expect(page.locator("[data-testid='user-id']")).toContainText(
      TEST_USER.userId,
    );

    // When: ページをリロードする
    // セッション検証APIのモックが設定済みのため、復元処理が正常動作する
    await page.reload();

    // Then: リロード後もログイン状態が維持される
    await expect(page.locator("[data-testid='user-id']")).toContainText(
      TEST_USER.userId,
    );
    await expect(page.locator("[data-testid='user-name']")).toContainText(
      TEST_USER.username,
    );
  });

  test("ログアウト後にログインボタンが再表示されること", async ({ page }) => {
    // Given: ログイン済みの状態にする
    await page.goto("/");
    await page.locator("header button", { hasText: "ログイン" }).click();
    await page.locator("#email").fill("test@example.com");
    await page.locator("#password").fill("password123");
    await page.locator("[role='dialog'] button[type='submit']").click();
    await expect(page.locator("[data-testid='user-id']")).toContainText(
      TEST_USER.userId,
    );

    // When: ログアウトボタンをクリックする
    await page.locator("header button", { hasText: "ログアウト" }).click();

    // Then: ログインボタンが再表示される
    await expect(
      page.locator("header button", { hasText: "ログイン" }),
    ).toBeVisible();

    // Then: ユーザー情報が非表示になる
    await expect(page.locator("[data-testid='user-id']")).not.toBeVisible();
    await expect(page.locator("[data-testid='user-name']")).not.toBeVisible();
  });

  test("ログアウト後にリロードしてもログアウト状態が維持されること", async ({
    page,
  }) => {
    // Given: ログイン後にログアウトする
    await page.goto("/");
    await page.locator("header button", { hasText: "ログイン" }).click();
    await page.locator("#email").fill("test@example.com");
    await page.locator("#password").fill("password123");
    await page.locator("[role='dialog'] button[type='submit']").click();
    await expect(page.locator("[data-testid='user-id']")).toContainText(
      TEST_USER.userId,
    );
    await page.locator("header button", { hasText: "ログアウト" }).click();

    // When: ページをリロードする
    await page.reload();

    // Then: ログアウト状態のまま（ログインボタンが表示される）
    await expect(
      page.locator("header button", { hasText: "ログイン" }),
    ).toBeVisible();
  });

  test("無効なセッションの場合にリロード時に自動ログアウトされること", async ({
    page,
  }) => {
    // Given: ログインを実行する
    await page.goto("/");
    await page.locator("header button", { hasText: "ログイン" }).click();
    await page.locator("#email").fill("test@example.com");
    await page.locator("#password").fill("password123");
    await page.locator("[role='dialog'] button[type='submit']").click();
    await expect(page.locator("[data-testid='user-id']")).toContainText(
      TEST_USER.userId,
    );

    // When: セッション検証APIが404を返すようにモックを変更してリロードする
    // セッション無効化をシミュレートするため、全セッション検証を404にする
    await page.route("**/api/v1/session/**", async (route) => {
      if (route.request().method() === "GET") {
        await route.fulfill({
          status: 404,
          contentType: "application/json",
          body: JSON.stringify({
            error: {
              code: "SESSION_NOT_FOUND",
              message: "セッションが見つかりません",
            },
          }),
        });
      } else {
        await route.continue();
      }
    });
    await page.reload();

    // Then: 自動ログアウトされ、ログインボタンが表示される
    await expect(
      page.locator("header button", { hasText: "ログイン" }),
    ).toBeVisible();
  });
});
