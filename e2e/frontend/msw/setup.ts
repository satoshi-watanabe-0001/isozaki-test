/**
 * MSWサーバーセットアップ
 *
 * Playwright統合テスト用のMSWサーバー設定。
 * テスト実行時にバックエンドAPIをモック化するための
 * ルート設定ヘルパーを提供する。
 *
 * @since 1.0
 */
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

/** テスト用アーティストデータ（50音順） */
const TEST_ARTISTS = [
  { artistId: "aimyon", name: "あいみょん", nameKana: "あいみょん", iconUrl: "/images/artists/aimyon.svg" },
  { artistId: "arashi", name: "嵐", nameKana: "あらし", iconUrl: "/images/artists/arashi.svg" },
  { artistId: "ikimonogakari", name: "いきものがかり", nameKana: "いきものがかり", iconUrl: "/images/artists/ikimonogakari.svg" },
  { artistId: "ulfuls", name: "ウルフルズ", nameKana: "うるふるず", iconUrl: "/images/artists/ulfuls.svg" },
  { artistId: "exile", name: "EXILE", nameKana: "えぐざいる", iconUrl: "/images/artists/exile.svg" },
  { artistId: "otsuka-ai", name: "大塚愛", nameKana: "おおつかあい", iconUrl: "/images/artists/otsuka-ai.svg" },
  { artistId: "glay", name: "GLAY", nameKana: "ぐれい", iconUrl: "/images/artists/glay.svg" },
  { artistId: "southern-all-stars", name: "サザンオールスターズ", nameKana: "さざんおーるすたーず", iconUrl: "/images/artists/southern-all-stars.svg" },
  { artistId: "spitz", name: "スピッツ", nameKana: "すぴっつ", iconUrl: "/images/artists/spitz.svg" },
  { artistId: "dreams-come-true", name: "DREAMS COME TRUE", nameKana: "どりーむずかむとぅるー", iconUrl: "/images/artists/dreams-come-true.svg" },
] as const;

/** テスト用コミュニティTOPデータ */
const TEST_COMMUNITY_DATA: Record<string, object> = {
  aimyon: {
    artistId: "aimyon",
    name: "あいみょん",
    images: [
      { imageId: 1, imageUrl: "/images/artists/aimyon.svg", displayOrder: 1 },
      { imageId: 2, imageUrl: "/images/artists/aimyon.svg", displayOrder: 2 },
      { imageId: 3, imageUrl: "/images/artists/aimyon.svg", displayOrder: 3 },
    ],
    campaigns: [
      { campaignId: 1, title: "ライブツアー2025", imageUrl: "/images/campaigns/default.svg" },
      { campaignId: 2, title: "ニューアルバム発売記念", imageUrl: "/images/campaigns/default.svg" },
      { campaignId: 3, title: "ファンクラブ限定イベント", imageUrl: "/images/campaigns/default.svg" },
    ],
    news: [
      { newsId: 1, title: "ニューシングル「風になりたい」リリース決定", publishedAt: "2025-04-10T10:00:00Z" },
      { newsId: 2, title: "全国ツアー2025 追加公演決定", publishedAt: "2025-04-08T12:00:00Z" },
      { newsId: 3, title: "テレビ出演情報（4月）", publishedAt: "2025-04-05T09:00:00Z" },
      { newsId: 4, title: "オフィシャルグッズ新商品のお知らせ", publishedAt: "2025-04-01T15:00:00Z" },
      { newsId: 5, title: "ファンクラブ会員限定イベント開催", publishedAt: "2025-03-28T11:00:00Z" },
    ],
  },
};

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

  // アーティスト一覧APIモック
  await page.route(`${BACKEND_URL}/api/v1/artists`, async (route) => {
    if (route.request().method() === "GET") {
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify(TEST_ARTISTS),
      });
    } else {
      await route.continue();
    }
  });

  // コミュニティTOP APIモック
  await page.route(
    `${BACKEND_URL}/api/v1/community/**`,
    async (route) => {
      if (route.request().method() !== "GET") {
        await route.continue();
        return;
      }
      const url: string = route.request().url();
      const artistIdMatch: RegExpMatchArray | null = url.match(
        /\/api\/v1\/community\/(.+)$/,
      );
      const artistId: string = artistIdMatch ? artistIdMatch[1] : "";

      const data = TEST_COMMUNITY_DATA[artistId];
      if (data) {
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify(data),
        });
        return;
      }

      /** テスト用アーティストデータからデフォルトレスポンスを生成 */
      const artist = TEST_ARTISTS.find((a) => a.artistId === artistId);
      if (artist) {
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify({
            artistId: artist.artistId,
            name: artist.name,
            images: [],
            campaigns: [],
            news: [],
          }),
        });
        return;
      }

      await route.fulfill({
        status: 404,
        contentType: "application/json",
        body: JSON.stringify({
          error: {
            code: "ARTIST_NOT_FOUND",
            message: "アーティストが見つかりません",
          },
        }),
      });
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

export { BACKEND_URL, TEST_USER, TEST_CREDENTIALS, TEST_ARTISTS, TEST_COMMUNITY_DATA };
