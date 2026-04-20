/**
 * スレッド詳細ページコンポーネントの単体テスト
 *
 * スレッド詳細の表示、コメント一覧、もっと見る、FAB表示をテストする。
 */
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import ThreadDetailPage from "@/app/community/[artistId]/threads/[threadId]/page";
import { AuthProvider } from "@/contexts/AuthContext";

/** react-hot-toastのモック */
vi.mock("react-hot-toast", () => ({
  default: {
    success: vi.fn(),
  },
  Toaster: () => null,
}));

/** next/navigationのモック */
vi.mock("next/navigation", () => ({
  useParams: () => ({ artistId: "aimyon", threadId: "01970000-1000-7000-8000-000000000001" }),
  useRouter: () => ({ push: vi.fn(), back: vi.fn() }),
  notFound: () => {
    throw new Error("NEXT_NOT_FOUND");
  },
}));

/** スレッド詳細テストデータ */
const mockThreadDetailResponse = {
  threadId: "01970000-1000-7000-8000-000000000001",
  title: "テストスレッド",
  createdByUsername: "テストユーザー1",
  createdAt: "2025-04-13T10:00:00Z",
  comments: [
    {
      commentId: "01970000-2000-7000-8000-000000000001",
      content: "テストコメント1",
      createdByUsername: "テストユーザー1",
      createdAt: "2025-04-13T10:05:00Z",
    },
    {
      commentId: "01970000-2000-7000-8000-000000000002",
      content: "テストコメント2\n改行テスト",
      createdByUsername: "テストユーザー2",
      createdAt: "2025-04-13T10:10:00Z",
    },
  ],
  totalComments: 2,
  page: 1,
  size: 10,
  totalPages: 1,
};

/**
 * AuthProviderでラップしてレンダリングするヘルパー関数
 */
function renderWithAuth(): ReturnType<typeof render> {
  return render(
    <AuthProvider>
      <ThreadDetailPage />
    </AuthProvider>,
  );
}

describe("ThreadDetailPage", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
  });

  /**
   * 【テスト対象】ThreadDetailPage コンポーネント
   * 【テストケース】初期ロード時
   * 【期待結果】スレッドタイトルとコメント一覧が表示される
   * 【ビジネス要件】スレッド詳細画面の表示
   */
  it("スレッドタイトルとコメント一覧が正しく表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockThreadDetailResponse,
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("thread-detail-title")).toHaveTextContent("テストスレッド");
    });

    expect(screen.getByTestId("comment-list")).toBeInTheDocument();
    expect(screen.getByTestId("comment-content-01970000-2000-7000-8000-000000000001")).toHaveTextContent("テストコメント1");
    expect(screen.getByTestId("comment-content-01970000-2000-7000-8000-000000000002")).toHaveTextContent("テストコメント2");
  });

  /**
   * 【テスト対象】ThreadDetailPage コンポーネント
   * 【テストケース】コメントの改行が保持される
   * 【期待結果】whitespace-pre-wrapで改行が表示される
   * 【ビジネス要件】コメント表示時の改行保持
   */
  it("コメントの改行が保持され長文が折り返されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockThreadDetailResponse,
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      const commentElement = screen.getByTestId("comment-content-01970000-2000-7000-8000-000000000002");
      expect(commentElement).toBeInTheDocument();
      expect(commentElement.className).toContain("whitespace-pre-wrap");
      expect(commentElement.className).toContain("break-words");
    });
  });

  /**
   * 【テスト対象】ThreadDetailPage コンポーネント
   * 【テストケース】10件以上のコメントがある場合
   * 【期待結果】「もっと見る」ボタンが表示される
   * 【ビジネス要件】コメントの追加読み込み
   */
  it("10件以上のコメントがある場合「もっと見る」ボタンが表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({
        ...mockThreadDetailResponse,
        totalComments: 15,
        totalPages: 2,
      }),
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("load-more-button")).toBeInTheDocument();
    });
    expect(screen.getByTestId("load-more-button")).toHaveTextContent("もっと見る");
  });

  /**
   * 【テスト対象】ThreadDetailPage コンポーネント
   * 【テストケース】コメントが全件表示された場合
   * 【期待結果】「もっと見る」ボタンが非表示になる
   * 【ビジネス要件】全コメント表示時のUI
   */
  it("全コメント表示時に「もっと見る」ボタンが非表示になること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockThreadDetailResponse,
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("thread-detail-title")).toBeInTheDocument();
    });
    expect(screen.queryByTestId("load-more-button")).not.toBeInTheDocument();
  });

  /**
   * 【テスト対象】ThreadDetailPage コンポーネント
   * 【テストケース】コメント追加FABが表示される
   * 【期待結果】FABが画面右下に表示される
   * 【ビジネス要件】コメント追加ボタンの表示
   */
  it("コメント追加FABが表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockThreadDetailResponse,
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("add-comment-fab")).toBeInTheDocument();
    });
  });

  /**
   * 【テスト対象】ThreadDetailPage コンポーネント
   * 【テストケース】未ログイン時にFABをクリック
   * 【期待結果】ログイン促進ダイアログが表示される
   * 【ビジネス要件】未ログインユーザのコメント追加制限
   */
  it("未ログイン時にFABクリックでログイン促進ダイアログが表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockThreadDetailResponse,
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("add-comment-fab")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId("add-comment-fab"));

    expect(screen.getByTestId("login-prompt-dialog")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】ThreadDetailPage コンポーネント
   * 【テストケース】コメントがない場合
   * 【期待結果】「コメントはまだありません」が表示される
   * 【ビジネス要件】空のコメント一覧表示
   */
  it("コメントがない場合にメッセージが表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({
        ...mockThreadDetailResponse,
        comments: [],
        totalComments: 0,
      }),
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("no-comments")).toHaveTextContent("コメントはまだありません");
    });
  });

  /**
   * 【テスト対象】ThreadDetailPage コンポーネント
   * 【テストケース】作成者情報の表示
   * 【期待結果】スレッド作成者名と作成日時が表示される
   * 【ビジネス要件】スレッド作成者の表示
   */
  it("スレッド作成者情報が表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockThreadDetailResponse,
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("thread-detail-creator")).toBeInTheDocument();
    });
    expect(screen.getByTestId("thread-detail-creator")).toHaveTextContent("テストユーザー1");
  });
});
