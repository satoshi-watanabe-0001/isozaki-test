/**
 * スレッド作成モーダルコンポーネントの単体テスト
 *
 * モーダルの開閉、入力バリデーション、文字数カウント表示をテストする。
 */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import CreateThreadModal from "@/components/CreateThreadModal";

describe("CreateThreadModal", () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
    artistId: "aimyon",
    sessionId: "test-session-id",
    onCreated: vi.fn(),
  };

  beforeEach(() => {
    vi.restoreAllMocks();
  });

  /**
   * 【テスト対象】CreateThreadModal コンポーネント
   * 【テストケース】モーダルが開いている場合
   * 【期待結果】タイトル入力欄、コメント入力欄、作成ボタンが表示される
   * 【ビジネス要件】スレッド作成モーダルの表示
   */
  it("モーダルが正しく表示されること", () => {
    render(<CreateThreadModal {...defaultProps} />);

    expect(screen.getByTestId("create-thread-modal")).toBeInTheDocument();
    expect(screen.getByTestId("thread-title-input")).toBeInTheDocument();
    expect(screen.getByTestId("thread-comment-input")).toBeInTheDocument();
    expect(screen.getByTestId("create-thread-submit")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】CreateThreadModal コンポーネント
   * 【テストケース】タイトル文字数カウントの表示
   * 【期待結果】「0/50」→入力後「5/50」に変化する
   * 【ビジネス要件】タイトル入力欄の文字数表示
   */
  it("タイトル文字数カウントが正しく表示されること", () => {
    render(<CreateThreadModal {...defaultProps} />);

    expect(screen.getByTestId("thread-title-count")).toHaveTextContent("0/50");

    fireEvent.change(screen.getByTestId("thread-title-input"), {
      target: { value: "テスト入力" },
    });

    expect(screen.getByTestId("thread-title-count")).toHaveTextContent("5/50");
  });

  /**
   * 【テスト対象】CreateThreadModal コンポーネント
   * 【テストケース】コメント文字数カウントの表示
   * 【期待結果】「0/200」→入力後「6/200」に変化する
   * 【ビジネス要件】コメント入力欄の文字数表示
   */
  it("コメント文字数カウントが正しく表示されること", () => {
    render(<CreateThreadModal {...defaultProps} />);

    expect(screen.getByTestId("thread-comment-count")).toHaveTextContent("0/200");

    fireEvent.change(screen.getByTestId("thread-comment-input"), {
      target: { value: "コメント入力" },
    });

    expect(screen.getByTestId("thread-comment-count")).toHaveTextContent("6/200");
  });

  /**
   * 【テスト対象】CreateThreadModal コンポーネント
   * 【テストケース】未入力時の送信ボタン
   * 【期待結果】送信ボタンが無効化される
   * 【ビジネス要件】必須入力のバリデーション
   */
  it("未入力時に送信ボタンが無効化されること", () => {
    render(<CreateThreadModal {...defaultProps} />);

    expect(screen.getByTestId("create-thread-submit")).toBeDisabled();
  });

  /**
   * 【テスト対象】CreateThreadModal コンポーネント
   * 【テストケース】有効な入力後の送信ボタン
   * 【期待結果】送信ボタンが有効になる
   * 【ビジネス要件】入力完了後の送信可能状態
   */
  it("有効な入力後に送信ボタンが有効になること", () => {
    render(<CreateThreadModal {...defaultProps} />);

    fireEvent.change(screen.getByTestId("thread-title-input"), {
      target: { value: "テストタイトル" },
    });
    fireEvent.change(screen.getByTestId("thread-comment-input"), {
      target: { value: "テストコメント" },
    });

    expect(screen.getByTestId("create-thread-submit")).not.toBeDisabled();
  });

  /**
   * 【テスト対象】CreateThreadModal コンポーネント
   * 【テストケース】×ボタンクリック
   * 【期待結果】onCloseが呼ばれる
   * 【ビジネス要件】モーダルの閉じ操作
   */
  it("×ボタンクリックでモーダルが閉じること", () => {
    render(<CreateThreadModal {...defaultProps} />);

    fireEvent.click(screen.getByTestId("create-thread-close"));

    expect(defaultProps.onClose).toHaveBeenCalled();
  });

  /**
   * 【テスト対象】CreateThreadModal コンポーネント
   * 【テストケース】モーダルが閉じている場合
   * 【期待結果】モーダルが非表示になる
   * 【ビジネス要件】モーダルの非表示状態
   */
  it("isOpen=falseの場合、モーダルが非表示になること", () => {
    render(<CreateThreadModal {...defaultProps} isOpen={false} />);

    expect(screen.queryByTestId("create-thread-modal")).not.toBeInTheDocument();
  });

  /**
   * 【テスト対象】CreateThreadModal コンポーネント
   * 【テストケース】タイトルに改行が入力された場合
   * 【期待結果】改行が除去される
   * 【ビジネス要件】スレッドタイトルの改行不可
   */
  it("タイトルの改行が除去されること", () => {
    render(<CreateThreadModal {...defaultProps} />);

    const input = screen.getByTestId("thread-title-input") as HTMLInputElement;
    fireEvent.change(input, { target: { value: "テスト\nタイトル" } });

    expect(input.value).toBe("テストタイトル");
  });
});
