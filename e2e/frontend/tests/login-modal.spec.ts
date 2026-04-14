/**
 * 【テスト対象】ログインモーダル
 * 【テストケース】ログインモーダルの開閉・フォーム入力・認証処理
 * 【期待結果】モーダルが正しく開閉し、認証成功/失敗時に適切な画面遷移・エラー表示が行われる
 * 【ビジネス要件】ユーザー認証機能（ログインモーダル）
 */
import { test, expect } from "@playwright/test";
import { setupMockRoutes, TEST_USER } from "../msw/setup";

test.describe("ログインモーダル", () => {
  test.beforeEach(async ({ page }) => {
    // APIリクエストをモック化してバックエンドに依存しないテストを実現する
    await setupMockRoutes(page);
    await page.goto("/");
  });

  test("ログインボタン押下でモーダルが開くこと", async ({ page }) => {
    // Given: トップページが表示されている（未ログイン状態）
    const loginButton = page.locator("header button", {
      hasText: "ログイン",
    });
    await expect(loginButton).toBeVisible();

    // When: ログインボタンをクリックする
    await loginButton.click();

    // Then: ログインモーダルが表示される
    const modal = page.locator("[role='dialog']");
    await expect(modal).toBeVisible();

    // Then: モーダルタイトル「ログイン」が表示される
    const modalTitle = modal.locator("h2", { hasText: "ログイン" });
    await expect(modalTitle).toBeVisible();

    // Then: メールアドレスとパスワードの入力欄が表示される
    await expect(page.locator("#email")).toBeVisible();
    await expect(page.locator("#password")).toBeVisible();
  });

  test("クローズボタンでモーダルが閉じること", async ({ page }) => {
    // Given: ログインモーダルが開いている
    await page.locator("header button", { hasText: "ログイン" }).click();
    const modal = page.locator("[role='dialog']");
    await expect(modal).toBeVisible();

    // When: クローズボタン（×）をクリックする
    const closeButton = modal.locator("button[aria-label='閉じる']");
    await closeButton.click();

    // Then: モーダルが非表示になる
    await expect(modal).not.toBeVisible();
  });

  test("オーバーレイクリックでモーダルが閉じること", async ({ page }) => {
    // Given: ログインモーダルが開いている
    await page.locator("header button", { hasText: "ログイン" }).click();
    const modal = page.locator("[role='dialog']");
    await expect(modal).toBeVisible();

    // When: モーダル外のオーバーレイ領域をクリックする
    // モーダル本体を避けて左上隅をクリックすることで、オーバーレイのクリックイベントを発火させる
    await page.locator("[data-testid='modal-overlay']").click({
      position: { x: 10, y: 10 },
    });

    // Then: モーダルが非表示になる
    await expect(modal).not.toBeVisible();
  });

  test("正しい認証情報でログインが成功すること", async ({ page }) => {
    // Given: ログインモーダルが開いている
    await page.locator("header button", { hasText: "ログイン" }).click();
    const modal = page.locator("[role='dialog']");
    await expect(modal).toBeVisible();

    // When: 正しいメールアドレスとパスワードを入力してログインする
    await page.locator("#email").fill("test@example.com");
    await page.locator("#password").fill("password123");
    await modal.locator("button[type='submit']").click();

    // Then: モーダルが閉じる
    await expect(modal).not.toBeVisible();

    // Then: ヘッダーにユーザーIDとユーザー名が表示される
    await expect(page.locator("[data-testid='user-id']")).toContainText(
      TEST_USER.userId,
    );
    await expect(page.locator("[data-testid='user-name']")).toContainText(
      TEST_USER.username,
    );

    // Then: ログインボタンが非表示になり、ログアウトボタンが表示される
    await expect(
      page.locator("header button", { hasText: "ログイン" }),
    ).not.toBeVisible();
    await expect(
      page.locator("header button", { hasText: "ログアウト" }),
    ).toBeVisible();
  });

  test("不正な認証情報でログインが失敗しエラーメッセージが表示されること", async ({
    page,
  }) => {
    // Given: ログインモーダルが開いている
    await page.locator("header button", { hasText: "ログイン" }).click();
    const modal = page.locator("[role='dialog']");
    await expect(modal).toBeVisible();

    // When: 不正なパスワードを入力してログインする
    await page.locator("#email").fill("test@example.com");
    await page.locator("#password").fill("wrongpassword");
    await modal.locator("button[type='submit']").click();

    // Then: エラーメッセージが表示される
    const errorMessage = modal.locator("text=メールアドレスまたはパスワードが正しくありません");
    await expect(errorMessage).toBeVisible();

    // Then: モーダルは閉じない（エラー表示のまま）
    await expect(modal).toBeVisible();
  });
});
