/**
 * ログイン促進ダイアログコンポーネントの単体テスト
 *
 * ダイアログの開閉、表示内容をテストする。
 */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import LoginPromptDialog from "@/components/LoginPromptDialog";

describe("LoginPromptDialog", () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
  };

  beforeEach(() => {
    vi.restoreAllMocks();
  });

  /**
   * 【テスト対象】LoginPromptDialog コンポーネント
   * 【テストケース】ダイアログが開いている場合
   * 【期待結果】ログイン促進メッセージと閉じるボタンが表示される
   * 【ビジネス要件】未ログインユーザへのログイン誘導
   */
  it("ダイアログが正しく表示されること", () => {
    render(<LoginPromptDialog {...defaultProps} />);

    expect(screen.getByTestId("login-prompt-dialog")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "ログインが必要です" })).toBeInTheDocument();
  });

  /**
   * 【テスト対象】LoginPromptDialog コンポーネント
   * 【テストケース】閉じるボタンクリック
   * 【期待結果】onCloseが呼ばれる
   * 【ビジネス要件】ダイアログの閉じ操作
   */
  it("閉じるボタンクリックでonCloseが呼ばれること", () => {
    render(<LoginPromptDialog {...defaultProps} />);

    fireEvent.click(screen.getByTestId("login-prompt-ok"));

    expect(defaultProps.onClose).toHaveBeenCalled();
  });

  /**
   * 【テスト対象】LoginPromptDialog コンポーネント
   * 【テストケース】×ボタンクリック
   * 【期待結果】onCloseが呼ばれる
   * 【ビジネス要件】ダイアログの閉じ操作
   */
  it("×ボタンクリックでonCloseが呼ばれること", () => {
    render(<LoginPromptDialog {...defaultProps} />);

    fireEvent.click(screen.getByTestId("login-prompt-close"));

    expect(defaultProps.onClose).toHaveBeenCalled();
  });

  /**
   * 【テスト対象】LoginPromptDialog コンポーネント
   * 【テストケース】ダイアログが閉じている場合
   * 【期待結果】ダイアログが非表示になる
   * 【ビジネス要件】ダイアログの非表示状態
   */
  it("isOpen=falseの場合、ダイアログが非表示になること", () => {
    render(<LoginPromptDialog {...defaultProps} isOpen={false} />);

    expect(screen.queryByTestId("login-prompt-dialog")).not.toBeInTheDocument();
  });
});
