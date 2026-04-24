/**
 * SSR用モックAPIサーバー
 *
 * Playwright統合テスト実行時にNext.jsサーバーのSSR fetchリクエストを
 * 処理するための軽量HTTPサーバー。
 * ブラウザ側のリクエストはPlaywrightのpage.route()でモック化されるが、
 * Server ComponentのサーバーサイドfetchはNode.jsプロセス内で実行されるため、
 * 実際のHTTPサーバーが必要。
 *
 * @since 1.5
 */
const http = require("http");

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
];

/** テスト用コミュニティTOPデータ */
const TEST_COMMUNITY_DATA = {
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

/** テスト用スレッド一覧データ */
const TEST_THREAD_LIST = {
  threads: [
    { threadId: 1, title: "テストスレッド1", createdByUsername: "テストユーザ", latestComment: "最新コメント1", latestCommentAt: new Date().toISOString() },
    { threadId: 2, title: "テストスレッド2", createdByUsername: "テストユーザ2", latestComment: "最新コメント2", latestCommentAt: new Date().toISOString() },
    { threadId: 3, title: "テストスレッド3", createdByUsername: "テストユーザ", latestComment: null, latestCommentAt: new Date().toISOString() },
  ],
  totalCount: 3,
  page: 1,
  size: 20,
  totalPages: 1,
};

/** テスト用スレッド詳細データ */
const TEST_THREAD_DETAIL = {
  threadId: 1,
  title: "テストスレッド1",
  createdByUsername: "テストユーザ",
  createdAt: new Date().toISOString(),
  comments: [
    { commentId: 1, content: "コメント1の内容\n改行あり", createdByUsername: "テストユーザ", createdAt: new Date().toISOString() },
    { commentId: 2, content: "コメント2の内容", createdByUsername: "テストユーザ2", createdAt: new Date().toISOString() },
  ],
  totalComments: 2,
  page: 1,
  size: 10,
  totalPages: 1,
};

/**
 * JSONレスポンスを送信するヘルパー関数
 *
 * @param res - HTTPレスポンスオブジェクト
 * @param statusCode - HTTPステータスコード
 * @param data - レスポンスボディ
 */
function sendJson(res, statusCode, data) {
  res.writeHead(statusCode, { "Content-Type": "application/json" });
  res.end(JSON.stringify(data));
}

/**
 * リクエストボディを読み取るヘルパー関数
 *
 * @param req - HTTPリクエストオブジェクト
 * @returns リクエストボディ文字列のPromise
 */
function readBody(req) {
  return new Promise((resolve) => {
    let body = "";
    req.on("data", (chunk) => { body += chunk; });
    req.on("end", () => resolve(body));
  });
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://${req.headers.host}`);
  const pathname = url.pathname;
  const method = req.method;

  // アーティスト一覧API
  if (pathname === "/api/v1/artists" && method === "GET") {
    return sendJson(res, 200, TEST_ARTISTS);
  }

  // ヘルスチェックAPI
  if (pathname === "/q/health" || pathname === "/q/health/ready") {
    return sendJson(res, 200, { status: "UP", checks: [] });
  }

  // ログインAPI
  if (pathname === "/api/v1/login" && method === "POST") {
    const body = await readBody(req);
    const parsed = JSON.parse(body);
    if (parsed.email === "test@example.com" && parsed.password === "password123") {
      return sendJson(res, 200, {
        sessionId: "test-session-id-12345",
        userId: "01908b7e-1234-7000-8000-000000000001",
        username: "テストユーザ",
      });
    }
    return sendJson(res, 401, { error: { code: "AUTHENTICATION_FAILED", message: "認証失敗" } });
  }

  // セッション検証API
  const sessionMatch = pathname.match(/^\/api\/v1\/session\/(.+)$/);
  if (sessionMatch) {
    if (method === "GET") {
      if (sessionMatch[1] === "test-session-id-12345") {
        return sendJson(res, 200, { sessionId: "test-session-id-12345", userId: "01908b7e-1234-7000-8000-000000000001", username: "テストユーザ" });
      }
      return sendJson(res, 404, { error: { code: "SESSION_NOT_FOUND", message: "セッションが見つかりません" } });
    }
    if (method === "DELETE") {
      res.writeHead(204);
      return res.end();
    }
  }

  // スレッド詳細・コメントAPI（スレッド一覧より先にマッチさせる）
  const threadDetailMatch = pathname.match(/^\/api\/v1\/community\/([^/]+)\/threads\/(\d+)(\/comments)?$/);
  if (threadDetailMatch) {
    if (method === "GET") {
      return sendJson(res, 200, TEST_THREAD_DETAIL);
    }
    if (method === "POST") {
      return sendJson(res, 201, { commentId: 100, content: "新規コメント", createdByUsername: "テストユーザ", createdAt: new Date().toISOString() });
    }
  }

  // スレッド一覧・作成API
  const threadListMatch = pathname.match(/^\/api\/v1\/community\/([^/]+)\/threads$/);
  if (threadListMatch) {
    if (method === "GET") {
      return sendJson(res, 200, TEST_THREAD_LIST);
    }
    if (method === "POST") {
      return sendJson(res, 201, { ...TEST_THREAD_DETAIL, threadId: 100, title: "新規スレッド" });
    }
  }

  // コミュニティTOP API
  const communityMatch = pathname.match(/^\/api\/v1\/community\/([^/]+)$/);
  if (communityMatch) {
    const artistId = communityMatch[1];
    const data = TEST_COMMUNITY_DATA[artistId];
    if (data) {
      return sendJson(res, 200, data);
    }
    const artist = TEST_ARTISTS.find((a) => a.artistId === artistId);
    if (artist) {
      return sendJson(res, 200, { artistId: artist.artistId, name: artist.name, images: [], campaigns: [], news: [] });
    }
    return sendJson(res, 404, { error: { code: "ARTIST_NOT_FOUND", message: "アーティストが見つかりません" } });
  }

  // 未定義のエンドポイント
  sendJson(res, 404, { error: "Not Found", path: pathname });
});

const PORT = process.env.MOCK_PORT || 8080;
server.listen(PORT, () => {
  console.log(`モックAPIサーバー起動: http://localhost:${PORT}`);
});
