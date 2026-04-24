/**
 * コメント画像グリッドコンポーネント
 *
 * コメントに紐づく画像をグリッドレイアウトで表示する。
 * 1枚=全幅、2枚=横並び、3-4枚=2×2グリッド。
 * X(旧Twitter)の画像表示を参考に、一定の枠内にトリム表示する。
 * サムネイル画像（400px幅WebP）を使用し、クリックでライトボックスを開く。
 *
 * @since 1.4
 */
"use client";

import { type ReactNode } from "react";
import type { CommentImage } from "@/types/thread";

/**
 * コメント画像グリッドのProps型定義
 *
 * @property images - 画像情報リスト
 * @property onImageClick - 画像クリック時のコールバック（画像インデックスを渡す）
 */
interface CommentImageGridProps {
  images: CommentImage[];
  onImageClick: (index: number) => void;
}

/**
 * コメント画像グリッドコンポーネント
 *
 * @param props - グリッドのProps
 * @returns グリッドのJSX要素（画像がない場合はnull）
 */
export default function CommentImageGrid({
  images,
  onImageClick,
}: CommentImageGridProps): ReactNode {
  if (!images || images.length === 0) {
    return null;
  }

  /** 画像数に応じたグリッドクラスを決定する */
  const getGridClass = (): string => {
    switch (images.length) {
      case 1:
        return "grid-cols-1";
      case 2:
        return "grid-cols-2";
      default:
        return "grid-cols-2";
    }
  };

  /** 画像数に応じた固定高さを決定する（X風トリム表示） */
  const getImageHeight = (): string => {
    switch (images.length) {
      case 1:
        return "h-64";
      case 2:
        return "h-48";
      default:
        return "h-36";
    }
  };

  return (
    <div
      className={`mt-2 grid gap-1 overflow-hidden rounded-xl border border-gray-200 ${getGridClass()}`}
      data-testid="comment-image-grid"
    >
      {images.map((image, index) => (
        <button
          key={image.imageId}
          type="button"
          onClick={() => onImageClick(index)}
          className={`overflow-hidden ${getImageHeight()}`}
          data-testid={`comment-image-${index}`}
        >
          <img
            src={image.thumbnailUrl}
            alt={`画像${index + 1}`}
            className="h-full w-full object-cover transition-opacity hover:opacity-80"
            loading="lazy"
          />
        </button>
      ))}
    </div>
  );
}
