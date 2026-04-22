/**
 * 画像ライトボックスコンポーネントの単体テスト
 *
 * react-image-galleryを使用したライトボックスの表示、閉じる操作をテストする。
 */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import ImageLightbox from "@/components/ImageLightbox";
import type { CommentImage } from "@/types/thread";

// react-image-galleryのモック
vi.mock("react-image-gallery", () => ({
  default: ({ items, startIndex, showNav, showThumbnails }: {
    items: { original: string; thumbnail: string; originalAlt: string; thumbnailAlt: string }[];
    startIndex: number;
    showNav: boolean;
    showThumbnails: boolean;
  }) => (
    <div data-testid="image-gallery-mock">
      <img
        src={items[startIndex]?.original}
        alt={items[startIndex]?.originalAlt}
        data-testid="lightbox-image"
      />
      {showNav && <span data-testid="gallery-nav">ナビゲーション</span>}
      {showThumbnails && <span data-testid="gallery-thumbnails">サムネイル</span>}
    </div>
  ),
}));

describe("ImageLightbox", () => {
  const mockImages: CommentImage[] = [
    {
      imageId: "img-1",
      thumbnailUrl: "http://minio:9000/images/thumbnails/img-1.webp",
      displayUrl: "http://minio:9000/images/display/img-1.webp",
    },
    {
      imageId: "img-2",
      thumbnailUrl: "http://minio:9000/images/thumbnails/img-2.webp",
      displayUrl: "http://minio:9000/images/display/img-2.webp",
    },
    {
      imageId: "img-3",
      thumbnailUrl: "http://minio:9000/images/thumbnails/img-3.webp",
      displayUrl: "http://minio:9000/images/display/img-3.webp",
    },
  ];

  /**
   * 【テスト対象】ImageLightbox
   * 【テストケース】ライトボックスが開いている場合
   * 【期待結果】ギャラリーと閉じるボタンが表示される
   * 【ビジネス要件】画像拡大表示
   */
  it("ライトボックスが正しく表示されること", () => {
    render(
      <ImageLightbox
        images={mockImages}
        initialIndex={0}
        isOpen={true}
        onClose={vi.fn()}
      />,
    );

    expect(screen.getByTestId("lightbox-modal")).toBeInTheDocument();
    expect(screen.getByTestId("lightbox-gallery")).toBeInTheDocument();
    expect(screen.getByTestId("lightbox-image")).toBeInTheDocument();
    expect(screen.getByTestId("lightbox-close")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】ImageLightbox
   * 【テストケース】複数画像時のナビゲーション表示
   * 【期待結果】ギャラリーナビゲーションとサムネイルが表示される
   * 【ビジネス要件】複数画像の切り替え
   */
  it("複数画像時にナビゲーションが表示されること", () => {
    render(
      <ImageLightbox
        images={mockImages}
        initialIndex={0}
        isOpen={true}
        onClose={vi.fn()}
      />,
    );

    expect(screen.getByTestId("gallery-nav")).toBeInTheDocument();
    expect(screen.getByTestId("gallery-thumbnails")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】ImageLightbox
   * 【テストケース】1枚画像時のナビゲーション非表示
   * 【期待結果】ギャラリーナビゲーションとサムネイルが非表示
   * 【ビジネス要件】1枚画像ではナビゲーション不要
   */
  it("1枚画像時にナビゲーションが非表示であること", () => {
    render(
      <ImageLightbox
        images={[mockImages[0]]}
        initialIndex={0}
        isOpen={true}
        onClose={vi.fn()}
      />,
    );

    expect(screen.queryByTestId("gallery-nav")).not.toBeInTheDocument();
    expect(screen.queryByTestId("gallery-thumbnails")).not.toBeInTheDocument();
  });

  /**
   * 【テスト対象】ImageLightbox
   * 【テストケース】閉じている場合
   * 【期待結果】何も表示されない
   * 【ビジネス要件】ライトボックスの非表示
   */
  it("isOpen=falseの場合、ライトボックスが非表示であること", () => {
    render(
      <ImageLightbox
        images={mockImages}
        initialIndex={0}
        isOpen={false}
        onClose={vi.fn()}
      />,
    );

    expect(screen.queryByTestId("lightbox-modal")).not.toBeInTheDocument();
  });

  /**
   * 【テスト対象】ImageLightbox
   * 【テストケース】×ボタンクリック
   * 【期待結果】onCloseが呼ばれる
   * 【ビジネス要件】ライトボックスの閉じ操作
   */
  it("×ボタンクリックでonCloseが呼ばれること", () => {
    const onClose = vi.fn();
    render(
      <ImageLightbox
        images={mockImages}
        initialIndex={0}
        isOpen={true}
        onClose={onClose}
      />,
    );

    fireEvent.click(screen.getByTestId("lightbox-close"));
    expect(onClose).toHaveBeenCalled();
  });

  /**
   * 【テスト対象】ImageLightbox
   * 【テストケース】空の画像リスト
   * 【期待結果】何も表示されない
   * 【ビジネス要件】画像なし時の安全な処理
   */
  it("空の画像リストで何も表示されないこと", () => {
    render(
      <ImageLightbox
        images={[]}
        initialIndex={0}
        isOpen={true}
        onClose={vi.fn()}
      />,
    );

    expect(screen.queryByTestId("lightbox-modal")).not.toBeInTheDocument();
  });

  /**
   * 【テスト対象】ImageLightbox
   * 【テストケース】initialIndexが正しく設定される
   * 【期待結果】指定インデックスの画像が表示される
   * 【ビジネス要件】クリックした画像からギャラリーを開始
   */
  it("initialIndexが正しく反映されること", () => {
    render(
      <ImageLightbox
        images={mockImages}
        initialIndex={1}
        isOpen={true}
        onClose={vi.fn()}
      />,
    );

    const image = screen.getByTestId("lightbox-image");
    expect(image).toHaveAttribute("src", mockImages[1].displayUrl);
  });
});
