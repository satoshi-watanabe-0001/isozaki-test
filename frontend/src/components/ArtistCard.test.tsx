/**
 * ArtistCardコンポーネントの単体テスト
 *
 * アーティストカードにアイコンとアーティスト名が正しく表示されることをテストする。
 */
import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import ArtistCard from "@/components/ArtistCard";
import type { Artist } from "@/types/artist";

describe("ArtistCard", () => {
  const mockArtist: Artist = {
    artistId: "01908b7e-2001-7000-8000-000000000001",
    name: "あいみょん",
    nameKana: "あいみょん",
    iconUrl: "https://placehold.co/150x150?text=A",
  };

  /**
   * 【テスト対象】ArtistCard コンポーネント
   * 【テストケース】アーティスト名の表示
   * 【期待結果】アーティスト名が正しく表示される
   * 【ビジネス要件】アーティストカードの名前表示
   */
  it("アーティスト名が表示されること", () => {
    render(<ArtistCard artist={mockArtist} />);

    expect(screen.getByText("あいみょん")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】ArtistCard コンポーネント
   * 【テストケース】アイコン画像の表示
   * 【期待結果】alt属性にアーティスト名が設定されたアイコン画像が表示される
   * 【ビジネス要件】アーティストカードのアイコン表示
   */
  it("アイコン画像が正しいalt属性で表示されること", () => {
    render(<ArtistCard artist={mockArtist} />);

    const image: HTMLElement = screen.getByAltText("あいみょんのアイコン");
    expect(image).toBeInTheDocument();
    expect(image).toHaveAttribute("src", "https://placehold.co/150x150?text=A");
  });

  /**
   * 【テスト対象】ArtistCard コンポーネント
   * 【テストケース】アイコンURLがnullの場合のフォールバック
   * 【期待結果】アーティスト名の先頭文字を使ったダミー画像が表示される
   * 【ビジネス要件】アイコン未設定時のダミー画像表示
   */
  it("アイコンURLがnullの場合、ダミー画像が使用されること", () => {
    const artistWithoutIcon: Artist = {
      ...mockArtist,
      iconUrl: null,
    };
    render(<ArtistCard artist={artistWithoutIcon} />);

    const image: HTMLElement = screen.getByAltText("あいみょんのアイコン");
    expect(image).toBeInTheDocument();
    expect(image.getAttribute("src")).toContain("placehold.co");
  });

  /**
   * 【テスト対象】ArtistCard コンポーネント
   * 【テストケース】data-testid属性の設定
   * 【期待結果】アーティストIDを含むdata-testidが設定される
   * 【ビジネス要件】テスト容易性のためのdata-testid付与
   */
  it("data-testidにアーティストIDが含まれること", () => {
    render(<ArtistCard artist={mockArtist} />);

    const card: HTMLElement = screen.getByTestId(
      `artist-card-${mockArtist.artistId}`
    );
    expect(card).toBeInTheDocument();
  });
});
