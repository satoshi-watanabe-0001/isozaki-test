/**
 * コメント画像グリッドコンポーネントの単体テスト
 *
 * 画像数に応じたグリッドレイアウト、クリック動作をテストする。
 */
import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import CommentImageGrid from "@/components/CommentImageGrid";
import type { CommentImage } from "@/types/thread";

describe("CommentImageGrid", () => {
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
    {
      imageId: "img-4",
      thumbnailUrl: "http://minio:9000/images/thumbnails/img-4.webp",
      displayUrl: "http://minio:9000/images/display/img-4.webp",
    },
  ];

  /**
   * 【テスト対象】CommentImageGrid
   * 【テストケース】画像が空の場合
   * 【期待結果】何も表示されない
   * 【ビジネス要件】画像なしコメントの表示
   */
  it("画像が空の場合、何も表示されないこと", () => {
    const { container } = render(
      <CommentImageGrid images={[]} onImageClick={vi.fn()} />,
    );
    expect(container.firstChild).toBeNull();
  });

  /**
   * 【テスト対象】CommentImageGrid
   * 【テストケース】1枚の画像
   * 【期待結果】1列グリッドで表示される
   * 【ビジネス要件】1枚画像=全幅表示
   */
  it("1枚の画像が正しく表示されること", () => {
    render(
      <CommentImageGrid images={[mockImages[0]]} onImageClick={vi.fn()} />,
    );

    expect(screen.getByTestId("comment-image-grid")).toBeInTheDocument();
    expect(screen.getByTestId("comment-image-0")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】CommentImageGrid
   * 【テストケース】2枚の画像
   * 【期待結果】2列グリッドで表示される
   * 【ビジネス要件】2枚画像=横並び表示
   */
  it("2枚の画像が正しく表示されること", () => {
    render(
      <CommentImageGrid
        images={mockImages.slice(0, 2)}
        onImageClick={vi.fn()}
      />,
    );

    expect(screen.getByTestId("comment-image-0")).toBeInTheDocument();
    expect(screen.getByTestId("comment-image-1")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】CommentImageGrid
   * 【テストケース】4枚の画像
   * 【期待結果】2×2グリッドで表示される
   * 【ビジネス要件】3-4枚画像=2×2グリッド表示
   */
  it("4枚の画像が正しく表示されること", () => {
    render(
      <CommentImageGrid images={mockImages} onImageClick={vi.fn()} />,
    );

    expect(screen.getByTestId("comment-image-0")).toBeInTheDocument();
    expect(screen.getByTestId("comment-image-1")).toBeInTheDocument();
    expect(screen.getByTestId("comment-image-2")).toBeInTheDocument();
    expect(screen.getByTestId("comment-image-3")).toBeInTheDocument();
  });

  /**
   * 【テスト対象】CommentImageGrid
   * 【テストケース】画像クリック時のコールバック
   * 【期待結果】onImageClickが正しいインデックスで呼ばれる
   * 【ビジネス要件】画像クリックでライトボックス表示
   */
  it("画像クリック時にonImageClickが呼ばれること", () => {
    const onImageClick = vi.fn();
    render(
      <CommentImageGrid
        images={mockImages.slice(0, 2)}
        onImageClick={onImageClick}
      />,
    );

    fireEvent.click(screen.getByTestId("comment-image-1"));
    expect(onImageClick).toHaveBeenCalledWith(1);
  });
});
