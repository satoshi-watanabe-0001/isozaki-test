/**
 * 画像ライトボックスコンポーネント
 *
 * Radix UI Dialogを使用した画像拡大表示モーダル。
 * 表示用画像（1200px幅WebP）を表示する。
 * 複数画像がある場合は左右ナビゲーションで切り替え可能。
 * ×ボタンおよびオーバーレイクリックで閉じることができる。
 *
 * @since 1.4
 */
"use client";

import { useState, useCallback, useEffect, type ReactNode } from "react";
import * as Dialog from "@radix-ui/react-dialog";
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
  const [currentIndex, setCurrentIndex] = useState<number>(initialIndex);

  /** initialIndexが変わったら同期する */
  useEffect(() => {
    setCurrentIndex(initialIndex);
  }, [initialIndex]);

  /** 前の画像に移動 */
  const handlePrev = useCallback((): void => {
    setCurrentIndex((prev) =>
      prev > 0 ? prev - 1 : images.length - 1,
    );
  }, [images.length]);

  /** 次の画像に移動 */
  const handleNext = useCallback((): void => {
    setCurrentIndex((prev) =>
      prev < images.length - 1 ? prev + 1 : 0,
    );
  }, [images.length]);

  /** キーボードナビゲーション */
  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e: KeyboardEvent): void => {
      if (e.key === "ArrowLeft") {
        handlePrev();
      } else if (e.key === "ArrowRight") {
        handleNext();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, handlePrev, handleNext]);

  if (!images || images.length === 0) {
    return null;
  }

  const currentImage: CommentImage = images[currentIndex];

  return (
    <Dialog.Root open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <Dialog.Portal>
        <Dialog.Overlay
          className="fixed inset-0 z-[60] bg-black/80"
          data-testid="lightbox-overlay"
        />
        <Dialog.Content
          className="fixed inset-0 z-[60] flex items-center justify-center p-4"
          data-testid="lightbox-modal"
        >
          {/* 閉じるボタン */}
          <Dialog.Close asChild>
            <button
              type="button"
              className="absolute right-4 top-4 z-10 rounded-full bg-black/50 p-2 text-white hover:bg-black/70"
              aria-label="閉じる"
              data-testid="lightbox-close"
            >
              <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12" />
              </svg>
            </button>
          </Dialog.Close>

          {/* 前へボタン */}
          {images.length > 1 && (
            <button
              type="button"
              onClick={handlePrev}
              className="absolute left-4 z-10 rounded-full bg-black/50 p-2 text-white hover:bg-black/70"
              aria-label="前の画像"
              data-testid="lightbox-prev"
            >
              <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 18l-6-6 6-6" />
              </svg>
            </button>
          )}

          {/* 画像表示 */}
          <Dialog.Title className="sr-only">画像拡大表示</Dialog.Title>
          <img
            src={currentImage.displayUrl}
            alt={`画像${currentIndex + 1}/${images.length}`}
            className="max-h-[90vh] max-w-[90vw] rounded-lg object-contain"
            data-testid="lightbox-image"
          />

          {/* 次へボタン */}
          {images.length > 1 && (
            <button
              type="button"
              onClick={handleNext}
              className="absolute right-4 z-10 rounded-full bg-black/50 p-2 text-white hover:bg-black/70"
              aria-label="次の画像"
              data-testid="lightbox-next"
            >
              <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 18l6-6-6-6" />
              </svg>
            </button>
          )}

          {/* インジケーター */}
          {images.length > 1 && (
            <div
              className="absolute bottom-4 flex gap-1"
              data-testid="lightbox-indicator"
            >
              {images.map((img, index) => (
                <button
                  key={img.imageId}
                  type="button"
                  onClick={() => setCurrentIndex(index)}
                  className={`h-2 w-2 rounded-full ${
                    index === currentIndex
                      ? "bg-white"
                      : "bg-white/50"
                  }`}
                  aria-label={`画像${index + 1}を表示`}
                />
              ))}
            </div>
          )}
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
