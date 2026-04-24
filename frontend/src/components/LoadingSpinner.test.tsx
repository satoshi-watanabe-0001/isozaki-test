/**
 * ローディングスピナーコンポーネントの単体テスト
 *
 * LoadingSpinnerのサイズバリエーション、アクセシビリティ属性をテストする。
 *
 * @since 1.5
 */
import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import LoadingSpinner from "@/components/LoadingSpinner";

describe("LoadingSpinner", () => {
  /**
   * 【テスト対象】LoadingSpinner コンポーネント
   * 【テストケース】デフォルトサイズ（md）で表示
   * 【期待結果】data-testid="loading-spinner"が表示される
   * 【ビジネス要件】ローディングスピナーの表示
   */
  it("デフォルトサイズで表示されること", () => {
    render(<LoadingSpinner />);

    const spinner = screen.getByTestId("loading-spinner");
    expect(spinner).toBeInTheDocument();
    expect(spinner.className).toContain("h-10");
    expect(spinner.className).toContain("w-10");
  });

  /**
   * 【テスト対象】LoadingSpinner コンポーネント
   * 【テストケース】smサイズで表示
   * 【期待結果】h-5 w-5のスタイルが適用される
   * 【ビジネス要件】インラインスピナー（もっと見るボタン内等）
   */
  it("smサイズで表示されること", () => {
    render(<LoadingSpinner size="sm" />);

    const spinner = screen.getByTestId("loading-spinner");
    expect(spinner.className).toContain("h-5");
    expect(spinner.className).toContain("w-5");
  });

  /**
   * 【テスト対象】LoadingSpinner コンポーネント
   * 【テストケース】lgサイズで表示
   * 【期待結果】h-16 w-16のスタイルが適用される
   * 【ビジネス要件】ページ全体のローディング表示
   */
  it("lgサイズで表示されること", () => {
    render(<LoadingSpinner size="lg" />);

    const spinner = screen.getByTestId("loading-spinner");
    expect(spinner.className).toContain("h-16");
    expect(spinner.className).toContain("w-16");
  });

  /**
   * 【テスト対象】LoadingSpinner コンポーネント
   * 【テストケース】アクセシビリティ属性
   * 【期待結果】role="status"とaria-label="読み込み中"が設定される
   * 【ビジネス要件】スクリーンリーダー対応
   */
  it("role=\"status\"とaria-labelが設定されること", () => {
    render(<LoadingSpinner />);

    const spinner = screen.getByTestId("loading-spinner");
    expect(spinner).toHaveAttribute("role", "status");
    expect(spinner).toHaveAttribute("aria-label", "読み込み中");
  });

  /**
   * 【テスト対象】LoadingSpinner コンポーネント
   * 【テストケース】アニメーション用クラス
   * 【期待結果】animate-spinとrounded-fullが適用される
   * 【ビジネス要件】回転アニメーション表示
   */
  it("animate-spinクラスが適用されること", () => {
    render(<LoadingSpinner />);

    const spinner = screen.getByTestId("loading-spinner");
    expect(spinner.className).toContain("animate-spin");
    expect(spinner.className).toContain("rounded-full");
  });
});
