/**
 * 共通エラーページの単体テスト
 *
 * 「エラーが発生しました」メッセージと「TOPページへ戻る」リンクが
 * 正しく表示されることをテストする。
 */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";

/** next/link のモック */
vi.mock("next/link", () => ({
  default: ({ children, href, ...props }: { children: React.ReactNode; href: string; [key: string]: unknown }) => (
    <a href={href} {...props}>{children}</a>
  ),
}));

import ErrorPage from "@/app/error";

/** テスト用エラーオブジェクト */
const mockError = new Error("テストエラー");

/** テスト用リセット関数 */
const mockReset = vi.fn();

describe("ErrorPage", () => {
  /**
   * 【テスト対象】ErrorPage コンポーネント
   * 【テストケース】エラーメッセージの表示
   * 【期待結果】「エラーが発生しました」が表示される
   * 【ビジネス要件】共通エラーページのメッセージ表示
   */
  it("「エラーが発生しました」が表示されること", () => {
    render(<ErrorPage error={mockError} reset={mockReset} />);
    expect(screen.getByTestId("error-page-title")).toHaveTextContent("エラーが発生しました");
  });

  /**
   * 【テスト対象】ErrorPage コンポーネント
   * 【テストケース】TOPページリンクの表示
   * 【期待結果】「TOPページへ戻る」リンクが"/"へのリンクとして表示される
   * 【ビジネス要件】エラーページからTOPページへの導線
   */
  it("「TOPページへ戻る」リンクが表示されること", () => {
    render(<ErrorPage error={mockError} reset={mockReset} />);
    const link = screen.getByTestId("error-page-home-link");
    expect(link).toHaveTextContent("TOPページへ戻る");
    expect(link).toHaveAttribute("href", "/");
  });
});
