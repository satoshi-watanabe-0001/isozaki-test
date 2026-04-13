/**
 * トップページコンポーネントの単体テスト
 *
 * トップページに「frontendテストページ」メッセージが表示されることをテストする。
 */
import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import Home from "@/app/page";

describe("Home", () => {
  /**
   * 【テスト対象】Home コンポーネント
   * 【テストケース】初期表示
   * 【期待結果】「frontendテストページ」のメッセージが表示される
   * 【ビジネス要件】トップページのメッセージ表示
   */
  it("「frontendテストページ」のメッセージが表示されること", () => {
    render(<Home />);

    expect(screen.getByText("frontendテストページ")).toBeInTheDocument();
  });
});
