/**
 * 画像ライトボックスコンポーネント
 *
 * react-image-galleryを使用した画像拡大表示モーダル。
 * 表示用画像（1200px幅WebP）をギャラリー形式で表示する。
 * 複数画像がある場合はスワイプ・矢印キーで切り替え可能。
 * オーバーレイクリックまたは×ボタンで閉じることができる。
 *
 * @since 1.4
 */
"use client";

import { useCallback, type ReactNode } from "react";
import * as Dialog from "@radix-ui/react-dialog";
import ImageGallery from "react-image-gallery";
import "react-image-gallery/styles/image-gallery.css";
import type { CommentImage } from "@/types/thread";

/**
 * ライトボックスのProps型定義
 *
 * @property images - 画像情報リスト
 * @property initialIndex - 初期表示する画像のインデックス
 * @property isOpen - ライトボックスの開閉状態
 * @property onClose - ライトボックスを閉じる関数
 */
interface ImageLightboxProps {
  images: CommentImage[];
  initialIndex: number;
  isOpen: boolean;
  onClose: () => void;
}

/**
 * 画像ライトボックスコンポーネント
 *
 * @param props - ライトボックスのProps
 * @returns ライトボックスのJSX要素
 */
export default function ImageLightbox({
  images,
  initialIndex,
  isOpen,
  onClose,
}: ImageLightboxProps): ReactNode {
  /** Dialogの開閉状態変更ハンドラ */
  const handleOpenChange = useCallback(
    (open: boolean): void => {
      if (!open) onClose();
    },
    [onClose],
  );

  if (!images || images.length === 0) {
    return null;
  }

  /** react-image-gallery用のアイテムリスト */
  const galleryItems = images.map((image, index) => ({
    original: image.displayUrl,
    thumbnail: image.thumbnailUrl,
    originalAlt: `画像${index + 1}/${images.length}`,
    thumbnailAlt: `サムネイル${index + 1}`,
  }));

  return (
    <Dialog.Root open={isOpen} onOpenChange={handleOpenChange}>
      <Dialog.Portal>
        <Dialog.Overlay
          className="fixed inset-0 z-[60] bg-black/80"
          data-testid="lightbox-overlay"
        />
        <Dialog.Content
          className="fixed inset-0 z-[60] flex items-center justify-center p-4"
          data-testid="lightbox-modal"
        >
          <Dialog.Title className="sr-only">画像拡大表示</Dialog.Title>

          {/* 閉じるボタン */}
          <Dialog.Close asChild>
            <button
              type="button"
              className="absolute right-4 top-4 z-[70] rounded-full bg-black/50 p-2 text-white hover:bg-black/70"
              aria-label="閉じる"
              data-testid="lightbox-close"
            >
              <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12" />
              </svg>
            </button>
          </Dialog.Close>

          {/* react-image-gallery */}
          <div className="w-full max-w-[90vw]" data-testid="lightbox-gallery">
            <ImageGallery
              items={galleryItems}
              startIndex={initialIndex}
              showPlayButton={false}
              showFullscreenButton={false}
              showThumbnails={images.length > 1}
              showBullets={false}
              showNav={images.length > 1}
              slideDuration={300}
              lazyLoad={true}
            />
          </div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
