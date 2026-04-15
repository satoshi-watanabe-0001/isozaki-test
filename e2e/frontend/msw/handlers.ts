/**
 * MSWハンドラー定義
 *
 * Frontend統合テスト用のAPIモックハンドラー。
 * バックエンドAPIのレスポンスをモック化し、
 * フロントエンド単独でのテストを可能にする。
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

/** テスト用メールアドレス・パスワード */
const TEST_CREDENTIALS = {
  email: "test@example.com",
  password: "password123",
} as const;

/**
 * ログインAPIのリクエストボディ型
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
 * デフォルトのAPIモックハンドラー
 *
 * 正常系のレスポンスを返すハンドラー群。
 * テストケースごとにオーバーライド可能。
 */
export const handlers = [
  /**
   * ログインAPI（POST /api/v1/login）
   *
   * 正しい認証情報の場合はセッション情報を返却し、
   * 不正な場合は401エラーを返却する。
   */
  http.post(`${BACKEND_URL}/api/v1/login`, async ({ request }) => {
    const body = (await request.json()) as LoginRequestBody;

    if (
      body.email === TEST_CREDENTIALS.email &&
      body.password === TEST_CREDENTIALS.password
    ) {
      return HttpResponse.json(TEST_USER, { status: 200 });
    }

    return HttpResponse.json(
      {
        error: {
          code: "AUTHENTICATION_FAILED",
          message: "メールアドレスまたはパスワードが正しくありません",
        },
      },
      { status: 401 },
    );
  }),

  /**
   * セッション検証API（GET /api/v1/session/:sessionId）
   *
   * 有効なセッションIDの場合はセッション情報を返却し、
   * 無効な場合は404エラーを返却する。
   */
  http.get(`${BACKEND_URL}/api/v1/session/:sessionId`, ({ params }) => {
    const sessionId = params.sessionId as string;

    if (sessionId === TEST_USER.sessionId) {
      return HttpResponse.json({
        sessionId: TEST_USER.sessionId,
        userId: TEST_USER.userId,
        username: TEST_USER.username,
      });
    }

    return HttpResponse.json(
      { error: { code: "SESSION_NOT_FOUND", message: "セッションが見つかりません" } },
      { status: 404 },
    );
  }),

  /**
   * セッション削除API（DELETE /api/v1/session/:sessionId）
   *
   * セッションを削除し、204 No Contentを返却する。
   */
  http.delete(`${BACKEND_URL}/api/v1/session/:sessionId`, () => {
    return new HttpResponse(null, { status: 204 });
  }),

  /**
   * アーティスト一覧API（GET /api/v1/artists）
   *
   * 50音順にソートされたアーティスト一覧を返却する。
   */
  http.get(`${BACKEND_URL}/api/v1/artists`, () => {
    return HttpResponse.json(TEST_ARTISTS);
  }),

  /**
   * コミュニティTOP API（GET /api/v1/community/:artistId）
   *
   * 指定アーティストのコミュニティTOPページ情報を返却する。
   * アーティストが存在しない場合は404エラーを返却する。
   */
  http.get(`${BACKEND_URL}/api/v1/community/:artistId`, ({ params }) => {
    const artistId = params.artistId as string;
    const data = TEST_COMMUNITY_DATA[artistId];

    if (data) {
      return HttpResponse.json(data);
    }

    /** テスト用アーティストデータからデフォルトレスポンスを生成 */
    const artist = TEST_ARTISTS.find((a) => a.artistId === artistId);
    if (artist) {
      return HttpResponse.json({
        artistId: artist.artistId,
        name: artist.name,
        images: [],
        campaigns: [],
        news: [],
      });
    }

    return HttpResponse.json(
      { error: { code: "ARTIST_NOT_FOUND", message: "アーティストが見つかりません" } },
      { status: 404 },
    );
  }),

  /**
   * ヘルスチェックAPI（GET /q/health）
   *
   * バックエンドのヘルスチェック結果をモック。
   */
  http.get(`${BACKEND_URL}/q/health`, () => {
    return HttpResponse.json({
      status: "UP",
      checks: [
        { name: "Database", status: "UP" },
        { name: "Redis", status: "UP" },
      ],
    });
  }),
];
