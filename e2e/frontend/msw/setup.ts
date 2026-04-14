/**
 * MSWサーバーセットアップ
 *
 * Playwright統合テスト用のMSWサーバー設定。
 * テスト実行時にバックエンドAPIをモック化するための
 * ルート設定ヘルパーを提供する。
 *
 * @since 1.0
 */
import { http, HttpResponse } from "msw";

/** バックエンドAPIのベースURL */
const BACKEND_URL: string =
  process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

/** テスト用ユーザー情報 */
const TEST_USER = {
  sessionId: "test-session-id-12345",
  userId: "01908b7e-1234-7000-8000-000000000001",
  username: "テストユーザ",
} as const;

/** テスト用認証情報 */
const TEST_CREDENTIALS = {
  email: "test@example.com",
  password: "password123",
} as const;

/**
 * ログインAPIリクエストボディ型
 */
interface LoginRequestBody {
  email: string;
  password: string;
}

/**
 * PlaywrightのRouteを利用してAPIリクエストをモック化する
 *
 * MSWのハンドラーロジックをPlaywrightのpage.routeで再利用し、
 * ブラウザからのAPIリクエストをインターセプトする。
 *
 * @param page - Playwrightのページオブジェクト
 */
export async function setupMockRoutes(
  page: import("@playwright/test").Page,
): Promise<void> {
  // ログインAPIモック
  await page.route(`${BACKEND_URL}/api/v1/login`, async (route) => {
    const request = route.request();
    if (request.method() === "POST") {
      const body = request.postDataJSON() as LoginRequestBody;

      if (
        body.email === TEST_CREDENTIALS.email &&
        body.password === TEST_CREDENTIALS.password
      ) {
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify(TEST_USER),
        });
      } else {
        await route.fulfill({
          status: 401,
          contentType: "application/json",
          body: JSON.stringify({
            error: {
              code: "AUTHENTICATION_FAILED",
              message: "メールアドレスまたはパスワードが正しくありません",
            },
          }),
        });
      }
    } else {
      await route.continue();
    }
  });

  // セッション検証APIモック
  await page.route(
    `${BACKEND_URL}/api/v1/session/**`,
    async (route) => {
      const request = route.request();
      const url: string = request.url();
      const sessionIdMatch: RegExpMatchArray | null = url.match(
        /\/api\/v1\/session\/(.+)$/,
      );
      const sessionId: string = sessionIdMatch ? sessionIdMatch[1] : "";

      if (request.method() === "GET") {
        if (sessionId === TEST_USER.sessionId) {
          await route.fulfill({
            status: 200,
            contentType: "application/json",
            body: JSON.stringify({
              sessionId: TEST_USER.sessionId,
              userId: TEST_USER.userId,
              username: TEST_USER.username,
            }),
          });
        } else {
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
        }
      } else if (request.method() === "DELETE") {
        await route.fulfill({
          status: 204,
          body: "",
        });
      } else {
        await route.continue();
      }
    },
  );

  // ヘルスチェックAPIモック
  await page.route(`${BACKEND_URL}/q/health`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: "UP",
        checks: [
          { name: "Database", status: "UP" },
          { name: "Redis", status: "UP" },
        ],
      }),
    });
  });

  // ヘルスチェック（ready）APIモック
  await page.route(`${BACKEND_URL}/q/health/ready`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({ status: "UP", checks: [] }),
    });
  });
}

export { BACKEND_URL, TEST_USER, TEST_CREDENTIALS };
