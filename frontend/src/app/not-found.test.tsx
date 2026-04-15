/**
 * 共通404エラーページの単体テスト
 *
 * 「404 Not Found」「ページが見つかりません」の表示と
 * TOPページへのリンクが正しく表示されることをテストする。
 */
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";

/** next/link のモック */
vi.mock("next/link", () => ({
  default: ({ children, href, ...props }: { children: React.ReactNode; href: string; [key: string]: unknown }) => (
    <a href={href} {...props}>{children}</a>
  ),
}));

import NotFound from "@/app/not-found";

describe("NotFound", () => {
  /**
   * 【テスト対象】NotFound コンポーネント
   * 【テストケース】404タイトルの表示
   * 【期待結果】「404 Not Found」が表示される
   * 【ビジネス要件】共通404エラーページのタイトル表示
   */
  it("「404 Not Found」が表示されること", () => {
    render(<NotFound />);
    expect(screen.getByTestId("not-found-title")).toHaveTextContent("404 Not Found");
  });

  /**
   * 【テスト対象】NotFound コンポーネント
   * 【テストケース】メッセージの表示
   * 【期待結果】「ページが見つかりません」が表示される
   * 【ビジネス要件】共通404エラーページのメッセージ表示
   */
  it("「ページが見つかりません」が表示されること", () => {
    render(<NotFound />);
    expect(screen.getByTestId("not-found-message")).toHaveTextContent("ページが見つかりません");
  });

  /**
   * 【テスト対象】NotFound コンポーネント
   * 【テストケース】TOPページリンクの表示
   * 【期待結果】「TOPページへ戻る」リンクが"/"へのリンクとして表示される
   * 【ビジネス要件】404ページからTOPページへの導線
   */
  it("「TOPページへ戻る」リンクが表示されること", () => {
    render(<NotFound />);
    const link = screen.getByTestId("not-found-home-link");
    expect(link).toHaveTextContent("TOPページへ戻る");
    expect(link).toHaveAttribute("href", "/");
  });
});
