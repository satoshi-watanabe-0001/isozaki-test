/**
 * スレッド一覧ページコンポーネントの単体テスト
 *
 * スレッド一覧の表示、ページング、FAB表示をテストする。
 */
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import ThreadListPage from "@/app/community/[artistId]/threads/page";
import { AuthProvider } from "@/contexts/AuthContext";

/** react-hot-toastのモック */
vi.mock("react-hot-toast", () => ({
  default: {
    success: vi.fn(),
  },
  Toaster: () => null,
}));

/** next/navigationのモック */
const mockPush = vi.fn();
vi.mock("next/navigation", () => ({
  useParams: () => ({ artistId: "aimyon" }),
  useRouter: () => ({ push: mockPush, back: vi.fn() }),
  notFound: () => {
    throw new Error("NEXT_NOT_FOUND");
  },
}));

/** スレッド一覧テストデータ */
const mockThreadListResponse = {
  threads: [
    {
      threadId: "01970000-1000-7000-8000-000000000001",
      title: "テストスレッド1",
      createdByUsername: "テストユーザー1",
      latestComment: "最新コメント1",
      latestCommentAt: "2025-04-13T10:00:00Z",
    },
    {
      threadId: "01970000-1000-7000-8000-000000000002",
      title: "テストスレッド2",
      createdByUsername: "テストユーザー2",
      latestComment: null,
      latestCommentAt: null,
    },
  ],
  totalCount: 2,
  page: 1,
  size: 20,
  totalPages: 1,
};

/**
 * AuthProviderでラップしてレンダリングするヘルパー関数
 */
function renderWithAuth(): ReturnType<typeof render> {
  return render(
    <AuthProvider>
      <ThreadListPage />
    </AuthProvider>,
  );
}

describe("ThreadListPage", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
  });

  /**
   * 【テスト対象】ThreadListPage コンポーネント
   * 【テストケース】初期ロード時
   * 【期待結果】読み込み中表示の後、スレッド一覧が表示される
   * 【ビジネス要件】スレッド一覧画面の表示
   */
  it("スレッド一覧が正しく表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockThreadListResponse,
    } as Response);

    renderWithAuth();

    expect(screen.getByTestId("loading-indicator")).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByTestId("thread-list")).toBeInTheDocument();
    });

    expect(screen.getByTestId("thread-title-01970000-1000-7000-8000-000000000001")).toHaveTextContent("テストスレッド1");
    expect(screen.getByTestId("thread-title-01970000-1000-7000-8000-000000000002")).toHaveTextContent("テストスレッド2");
    expect(screen.getByTestId("thread-latest-comment-01970000-1000-7000-8000-000000000001")).toHaveTextContent("最新コメント1");
  });

  /**
   * 【テスト対象】ThreadListPage コンポーネント
   * 【テストケース】スレッドが0件の場合
   * 【期待結果】「スレッドはまだありません」が表示される
   * 【ビジネス要件】空のスレッド一覧表示
   */
  it("スレッドが0件の場合にメッセージが表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({
        threads: [],
        totalCount: 0,
        page: 1,
        size: 20,
        totalPages: 0,
      }),
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("thread-list-empty")).toHaveTextContent("スレッドはまだありません");
    });
  });

  /**
   * 【テスト対象】ThreadListPage コンポーネント
   * 【テストケース】FABが表示される
   * 【期待結果】スレッド作成FABが画面右下に表示される
   * 【ビジネス要件】スレッド作成ボタンの表示
   */
  it("スレッド作成FABが表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockThreadListResponse,
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("create-thread-fab")).toBeInTheDocument();
    });
  });

  /**
   * 【テスト対象】ThreadListPage コンポーネント
   * 【テストケース】未ログイン時にFABをクリック
   * 【期待結果】ログイン促進ダイアログが表示される
   * 【ビジネス要件】未ログインユーザのスレッド作成制限
   */
  it("未ログイン時にFABクリックでログイン促進ダイアログが表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockThreadListResponse,
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("create-thread-fab")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByTestId("create-thread-fab"));

    expect(screen.getByTestId("login-prompt-dialog")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】ThreadListPage コンポーネント
   * 【テストケース】ページングが表示される場合
   * 【期待結果】ページングコントロールが表示される
   * 【ビジネス要件】20件以上のスレッドがある場合のページング
   */
  it("ページングが表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({
        ...mockThreadListResponse,
        totalCount: 40,
        totalPages: 2,
      }),
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("pagination")).toBeInTheDocument();
    });

    expect(screen.getByTestId("pagination-prev")).toBeDisabled();
    expect(screen.getByTestId("pagination-next")).not.toBeDisabled();
  });

  /**
   * 【テスト対象】ThreadListPage コンポーネント
   * 【テストケース】タイトルが表示される
   * 【期待結果】「スレッド一覧」のタイトルが表示される
   * 【ビジネス要件】スレッド一覧画面のタイトル表示
   */
  it("「スレッド一覧」のタイトルが表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockThreadListResponse,
    } as Response);

    renderWithAuth();

    expect(screen.getByTestId("thread-list-title")).toHaveTextContent("スレッド一覧");
  });

  /**
   * 【テスト対象】ThreadListPage コンポーネント
   * 【テストケース】各スレッドに日時が表示される
   * 【期待結果】最新コメント日時が相対表示で表示される
   * 【ビジネス要件】書き込み日時の表示
   */
  it("最新コメント日時が表示されること", async () => {
    vi.spyOn(global, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockThreadListResponse,
    } as Response);

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId("thread-date-01970000-1000-7000-8000-000000000001")).toBeInTheDocument();
    });
  });
});
