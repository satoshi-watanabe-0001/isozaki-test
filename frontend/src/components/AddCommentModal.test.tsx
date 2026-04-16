/**
 * コメント追加モーダルコンポーネントの単体テスト
 *
 * モーダルの開閉、入力バリデーション、文字数カウント表示をテストする。
 */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import AddCommentModal from "@/components/AddCommentModal";

describe("AddCommentModal", () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
    artistId: "aimyon",
    threadId: "1",
    sessionId: "test-session-id",
    onCommentAdded: vi.fn(),
  };

  beforeEach(() => {
    vi.restoreAllMocks();
  });

  /**
   * 【テスト対象】AddCommentModal コンポーネント
   * 【テストケース】モーダルが開いている場合
   * 【期待結果】コメント入力欄と投稿ボタンが表示される
   * 【ビジネス要件】コメントモーダルの表示
   */
  it("モーダルが正しく表示されること", () => {
    render(<AddCommentModal {...defaultProps} />);

    expect(screen.getByTestId("add-comment-modal")).toBeInTheDocument();
    expect(screen.getByTestId("comment-content-input")).toBeInTheDocument();
    expect(screen.getByTestId("add-comment-submit")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】AddCommentModal コンポーネント
   * 【テストケース】コメント文字数カウントの表示
   * 【期待結果】「0/200」→入力後「6/200」に変化する
   * 【ビジネス要件】コメント入力欄の文字数表示
   */
  it("コメント文字数カウントが正しく表示されること", () => {
    render(<AddCommentModal {...defaultProps} />);

    expect(screen.getByTestId("comment-content-count")).toHaveTextContent("0/200");

    fireEvent.change(screen.getByTestId("comment-content-input"), {
      target: { value: "コメント入力" },
    });

    expect(screen.getByTestId("comment-content-count")).toHaveTextContent("6/200");
  });

  /**
   * 【テスト対象】AddCommentModal コンポーネント
   * 【テストケース】未入力時の送信ボタン
   * 【期待結果】送信ボタンが無効化される
   * 【ビジネス要件】必須入力のバリデーション
   */
  it("未入力時に送信ボタンが無効化されること", () => {
    render(<AddCommentModal {...defaultProps} />);

    expect(screen.getByTestId("add-comment-submit")).toBeDisabled();
  });

  /**
   * 【テスト対象】AddCommentModal コンポーネント
   * 【テストケース】有効な入力後の送信ボタン
   * 【期待結果】送信ボタンが有効になる
   * 【ビジネス要件】入力完了後の送信可能状態
   */
  it("有効な入力後に送信ボタンが有効になること", () => {
    render(<AddCommentModal {...defaultProps} />);

    fireEvent.change(screen.getByTestId("comment-content-input"), {
      target: { value: "テストコメント" },
    });

    expect(screen.getByTestId("add-comment-submit")).not.toBeDisabled();
  });

  /**
   * 【テスト対象】AddCommentModal コンポーネント
   * 【テストケース】×ボタンクリック
   * 【期待結果】onCloseが呼ばれる
   * 【ビジネス要件】モーダルの閉じ操作
   */
  it("×ボタンクリックでモーダルが閉じること", () => {
    render(<AddCommentModal {...defaultProps} />);

    fireEvent.click(screen.getByTestId("add-comment-close"));

    expect(defaultProps.onClose).toHaveBeenCalled();
  });

  /**
   * 【テスト対象】AddCommentModal コンポーネント
   * 【テストケース】モーダルが閉じている場合
   * 【期待結果】モーダルが非表示になる
   * 【ビジネス要件】モーダルの非表示状態
   */
  it("isOpen=falseの場合、モーダルが非表示になること", () => {
    render(<AddCommentModal {...defaultProps} isOpen={false} />);

    expect(screen.queryByTestId("add-comment-modal")).not.toBeInTheDocument();
  });
});
