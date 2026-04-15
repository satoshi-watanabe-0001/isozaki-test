/**
 * トップページコンポーネントの単体テスト
 *
 * トップページに「ようこそEntm-Cloneへ」メッセージと
 * 「アーティスト一覧」リンクが表示されることをテストする。
 */
import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import Home from "@/app/page";

describe("Home", () => {
  /**
   * 【テスト対象】Home コンポーネント
   * 【テストケース】初期表示 - ウェルカムメッセージ
   * 【期待結果】「ようこそEntm-Cloneへ」のメッセージが表示される
   * 【ビジネス要件】トップページのウェルカムメッセージ表示
   */
  it("「ようこそEntm-Cloneへ」のメッセージが表示されること", () => {
    render(<Home />);

    expect(screen.getByText("ようこそEntm-Cloneへ")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】Home コンポーネント
   * 【テストケース】初期表示 - テストページメッセージ
   * 【期待結果】「frontendテストページ」のメッセージが表示される
   * 【ビジネス要件】トップページのサブメッセージ表示
   */
  it("「frontendテストページ」のメッセージが表示されること", () => {
    render(<Home />);

    expect(screen.getByText("frontendテストページ")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】Home コンポーネント
   * 【テストケース】初期表示 - アーティスト一覧リンク
   * 【期待結果】「アーティスト一覧」リンクが表示され、/artists への遷移が設定されている
   * 【ビジネス要件】アーティスト一覧ページへの導線
   */
  it("「アーティスト一覧」リンクが表示され /artists へのリンクであること", () => {
    render(<Home />);

    const link: HTMLElement = screen.getByRole("link", { name: "アーティスト一覧" });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute("href", "/artists");
  });
});
