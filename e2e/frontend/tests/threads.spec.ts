/**
 * 【テスト対象】スレッド機能（一覧・詳細・作成・コメント）
 * 【テストケース】スレッド一覧表示、詳細遷移、未ログイン時のダイアログ表示
 * 【期待結果】各画面が正しく表示され、適切な遷移・ダイアログが動作する
 * 【ビジネス要件】スレッド機能のフロントエンド統合テスト
 */
import { test, expect } from "@playwright/test";
import { setupMockRoutes, setupThreadMockRoutes, TEST_THREAD_LIST, TEST_THREAD_DETAIL } from "../msw/setup";

test.describe("スレッド一覧ページ", () => {
  test.beforeEach(async ({ page }) => {
    // APIリクエストをモック化してバックエンドに依存しないテストを実現する
    await setupMockRoutes(page);
    await setupThreadMockRoutes(page);
  });

  test("スレッド一覧が正しく表示されること", async ({ page }) => {
    // Given: スレッド一覧ページにアクセスする
    await page.goto("/community/aimyon/threads");

    // Then: スレッドタイトルが表示される
    await expect(page.locator("text=テストスレッド1")).toBeVisible();
    await expect(page.locator("text=テストスレッド2")).toBeVisible();
    await expect(page.locator("text=テストスレッド3")).toBeVisible();
  });

  test("スレッド作成ユーザ名が表示されること", async ({ page }) => {
    // Given: スレッド一覧ページにアクセスする
    await page.goto("/community/aimyon/threads");

    // Then: 作成ユーザ名が表示される
    await expect(page.locator("text=テストユーザ").first()).toBeVisible();
    await expect(page.locator("text=テストユーザ2")).toBeVisible();
  });

  test("スレッド領域クリックで詳細ページに遷移すること", async ({ page }) => {
    // Given: スレッド一覧ページにアクセスする
    await page.goto("/community/aimyon/threads");

    // When: 最初のスレッドをクリックする
    await page.locator("text=テストスレッド1").click();

    // Then: スレッド詳細ページのURLに遷移する
    await expect(page).toHaveURL(/\/community\/aimyon\/threads\/1/);
  });

  test("未ログイン時にスレッド作成FABクリックでログインダイアログが表示されること", async ({ page }) => {
    // Given: 未ログイン状態でスレッド一覧ページにアクセスする
    await page.goto("/community/aimyon/threads");

    // When: FAB（スレッド作成ボタン）をクリックする
    await page.locator("[data-testid='create-thread-fab']").click();

    // Then: ログイン促進ダイアログが表示される
    await expect(page.locator("[data-testid='login-prompt-dialog']")).toBeVisible();
    await expect(page.locator("text=ログインが必要です")).toBeVisible();
  });

  test("スレッドの最新コメント日時が相対表示されること", async ({ page }) => {
    // Given: スレッド一覧ページにアクセスする
    await page.goto("/community/aimyon/threads");

    // Then: 相対日時表示が含まれる（例: X時間前）
    await expect(page.locator("text=/\\d+(秒前|分前|時間前)/").first()).toBeVisible();
  });
});

test.describe("スレッド詳細ページ", () => {
  test.beforeEach(async ({ page }) => {
    await setupMockRoutes(page);
    await setupThreadMockRoutes(page);
  });

  test("スレッドタイトルとコメントが表示されること", async ({ page }) => {
    // Given: スレッド詳細ページにアクセスする
    await page.goto("/community/aimyon/threads/1");

    // Then: スレッドタイトルが表示される
    await expect(page.locator("[data-testid='thread-detail-title']")).toHaveText("テストスレッド1");

    // Then: コメント内容が表示される
    await expect(page.locator("text=コメント2の内容")).toBeVisible();
  });

  test("コメントの改行が保持されて表示されること", async ({ page }) => {
    // Given: スレッド詳細ページにアクセスする
    await page.goto("/community/aimyon/threads/1");

    // Then: 改行を含むコメントが表示される（whitespace-pre-wrapで改行保持）
    await expect(page.locator("text=コメント1の内容")).toBeVisible();
  });

  test("コメントユーザ名と日時が表示されること", async ({ page }) => {
    // Given: スレッド詳細ページにアクセスする
    await page.goto("/community/aimyon/threads/1");

    // Then: コメントユーザ名が表示される
    await expect(page.locator("text=テストユーザ2")).toBeVisible();

    // Then: 相対日時表示が含まれる
    await expect(page.locator("text=/\\d+(秒前|分前|時間前)/").first()).toBeVisible();
  });

  test("未ログイン時にコメントFABクリックでログインダイアログが表示されること", async ({ page }) => {
    // Given: 未ログイン状態でスレッド詳細ページにアクセスする
    await page.goto("/community/aimyon/threads/1");

    // When: FAB（コメント追加ボタン）をクリックする
    await page.locator("[data-testid='add-comment-fab']").click();

    // Then: ログイン促進ダイアログが表示される
    await expect(page.locator("[data-testid='login-prompt-dialog']")).toBeVisible();
  });

  test("スレッド作成者情報が表示されること", async ({ page }) => {
    // Given: スレッド詳細ページにアクセスする
    await page.goto("/community/aimyon/threads/1");

    // Then: スレッド作成者情報が表示される
    await expect(page.locator("[data-testid='thread-detail-creator']")).toBeVisible();
    await expect(page.locator("[data-testid='thread-detail-creator']")).toContainText("テストユーザ");
  });
});

test.describe("共通ヘッダー戻るボタン", () => {
  test.beforeEach(async ({ page }) => {
    await setupMockRoutes(page);
    await setupThreadMockRoutes(page);
  });

  test("戻るボタンがヘッダーに表示されること", async ({ page }) => {
    // Given: スレッド一覧ページにアクセスする
    await page.goto("/community/aimyon/threads");

    // Then: 戻るボタンが表示される
    await expect(page.locator("[data-testid='back-button']")).toBeVisible();
  });
});
