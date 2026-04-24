/**
 * コメント追加モーダルコンポーネント
 *
 * Radix UI Dialogを使用したコメント追加用モーダル。
 * コメント（最大200文字）と画像（最大4枚、JPEG/PNG/GIF、5MB以下）を
 * 入力してスレッドにコメントを追加する。
 * 画像はPre-signed URL経由でS3/MinIOに直接アップロードされる。
 * ×ボタンおよびオーバーレイクリックで閉じることができる。
 *
 * @since 1.4
 */
"use client";

import { useState, useCallback, useRef, type ReactNode, type ChangeEvent } from "react";
import * as Dialog from "@radix-ui/react-dialog";
import {
  removeExif,
  validateImageFile,
  MAX_IMAGE_COUNT,
  ALLOWED_MIME_TYPES,
} from "@/utils/imageUtils";
import type { UploadUrlResponse } from "@/types/thread";

/** バックエンドAPIのベースURL */
const BACKEND_URL: string =
  process.env.NEXT_PUBLIC_BACKEND_URL ?? "http://localhost:8080";

/** コメントの最大文字数 */
const MAX_COMMENT_LENGTH = 200;

/**
 * コメント追加モーダルのProps型定義
 *
 * @property isOpen - モーダルの開閉状態
 * @property onClose - モーダルを閉じる関数
 * @property artistId - アーティストID
 * @property threadId - スレッドID
 * @property sessionId - セッションID
 * @property onCommentAdded - コメント追加成功時のコールバック
 */
interface AddCommentModalProps {
  isOpen: boolean;
  onClose: () => void;
  artistId: string;
  threadId: string;
  sessionId: string;
  onCommentAdded: () => void;
}

/** 選択された画像ファイルの情報 */
interface SelectedImage {
  /** 元ファイル */
  file: File;
  /** プレビュー用ObjectURL */
  previewUrl: string;
}

/**
 * コメント追加モーダルコンポーネント
 *
 * @param props - モーダルのProps
 * @returns モーダルのJSX要素
 */
export default function AddCommentModal({
  isOpen,
  onClose,
  artistId,
  threadId,
  sessionId,
  onCommentAdded,
}: AddCommentModalProps): ReactNode {
  const [content, setContent] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [contentError, setContentError] = useState<string | null>(null);
  const [selectedImages, setSelectedImages] = useState<SelectedImage[]>([]);
  const [imageError, setImageError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  /** コメントのバリデーション */
  const validateContent = useCallback((value: string): boolean => {
    if (value.trim().length === 0) {
      setContentError("コメントを入力してください");
      return false;
    }
    if (value.length > MAX_COMMENT_LENGTH) {
      setContentError(
        `コメントは${MAX_COMMENT_LENGTH}文字以内で入力してください`,
      );
      return false;
    }
    setContentError(null);
    return true;
  }, []);

  /** 送信ボタンの無効化条件 */
  const isSubmitDisabled: boolean =
    isSubmitting ||
    content.trim().length === 0 ||
    content.length > MAX_COMMENT_LENGTH;

  /**
   * ファイル選択時の処理
   */
  const handleFileSelect = useCallback(
    (e: ChangeEvent<HTMLInputElement>): void => {
      setImageError(null);
      const files: FileList | null = e.target.files;
      if (!files) return;

      const newFiles: File[] = Array.from(files);
      const totalCount: number = selectedImages.length + newFiles.length;

      if (totalCount > MAX_IMAGE_COUNT) {
        setImageError(`画像は最大${MAX_IMAGE_COUNT}枚までです`);
        return;
      }

      // 各ファイルのバリデーション
      for (const file of newFiles) {
        const error: string | null = validateImageFile(file);
        if (error) {
          setImageError(error);
          return;
        }
      }

      // プレビューURLを生成して追加
      const newImages: SelectedImage[] = newFiles.map((file) => ({
        file,
        previewUrl: URL.createObjectURL(file),
      }));

      setSelectedImages((prev) => [...prev, ...newImages]);

      // input要素をリセット（同じファイルを再選択可能にする）
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    },
    [selectedImages],
  );

  /**
   * 選択済み画像を削除する
   */
  const handleRemoveImage = useCallback((index: number): void => {
    setSelectedImages((prev) => {
      const removed: SelectedImage | undefined = prev[index];
      if (removed) {
        URL.revokeObjectURL(removed.previewUrl);
      }
      return prev.filter((_, i) => i !== index);
    });
    setImageError(null);
  }, []);

  /**
   * 画像をPre-signed URL経由でS3にアップロードする
   *
   * @returns アップロードされた画像IDリスト
   */
  const uploadImages = useCallback(async (): Promise<string[]> => {
    if (selectedImages.length === 0) return [];

    // Pre-signed URL取得
    const fileNames: string[] = selectedImages.map((img) => img.file.name);
    const urlResponse: Response = await fetch(
      `${BACKEND_URL}/api/v1/community/${artistId}/threads/${threadId}/upload-urls`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ fileNames, sessionId }),
      },
    );

    if (!urlResponse.ok) {
      throw new Error(`Pre-signed URL取得に失敗しました（${urlResponse.status}）`);
    }

    const urlData: UploadUrlResponse = await urlResponse.json();

    // 各画像をEXIF削除してアップロード
    const imageIds: string[] = [];
    for (let i = 0; i < selectedImages.length; i++) {
      const image: SelectedImage = selectedImages[i];
      const uploadInfo = urlData.uploads[i];

      // EXIF情報を削除
      const cleanBlob: Blob = await removeExif(image.file);

      // Pre-signed URLにPUT
      const uploadResponse: Response = await fetch(uploadInfo.uploadUrl, {
        method: "PUT",
        body: cleanBlob,
        headers: { "Content-Type": image.file.type },
      });

      if (!uploadResponse.ok) {
        throw new Error(`画像アップロードに失敗しました（${uploadResponse.status}）`);
      }

      imageIds.push(uploadInfo.imageId);
    }

    return imageIds;
  }, [selectedImages, artistId, threadId, sessionId]);

  /**
   * フォーム送信処理
   */
  const handleSubmit = useCallback(async (): Promise<void> => {
    if (!validateContent(content)) {
      return;
    }

    setIsSubmitting(true);
    try {
      // 画像アップロード
      const imageIds: string[] = await uploadImages();

      // コメント投稿
      const response: Response = await fetch(
        `${BACKEND_URL}/api/v1/community/${artistId}/threads/${threadId}/comments`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ content, sessionId, imageIds }),
        },
      );
      if (!response.ok) {
        throw new Error(`コメントの投稿に失敗しました（${response.status}）`);
      }

      // 入力をリセット
      setContent("");
      setContentError(null);
      setImageError(null);
      // プレビューURLを解放
      for (const img of selectedImages) {
        URL.revokeObjectURL(img.previewUrl);
      }
      setSelectedImages([]);
      onCommentAdded();
    } catch {
      setContentError(
        "コメントの投稿に失敗しました。もう一度お試しください。",
      );
    } finally {
      setIsSubmitting(false);
    }
  }, [content, artistId, threadId, sessionId, validateContent, onCommentAdded, uploadImages, selectedImages]);

  /**
   * モーダルを閉じるときに入力をリセットする
   */
  const handleOpenChange = useCallback(
    (open: boolean): void => {
      if (!open) {
        setContent("");
        setContentError(null);
        setImageError(null);
        for (const img of selectedImages) {
          URL.revokeObjectURL(img.previewUrl);
        }
        setSelectedImages([]);
        onClose();
      }
    },
    [onClose, selectedImages],
  );

  return (
    <Dialog.Root open={isOpen} onOpenChange={handleOpenChange}>
      <Dialog.Portal>
        <Dialog.Overlay
          className="fixed inset-0 z-50 bg-black/50"
          data-testid="add-comment-overlay"
        />
        <Dialog.Content
          className="fixed left-1/2 top-1/2 z-50 w-full max-w-md -translate-x-1/2 -translate-y-1/2 rounded-lg bg-white p-6 shadow-xl dark:bg-zinc-900"
          data-testid="add-comment-modal"
        >
          {/* ヘッダー */}
          <div className="mb-4 flex items-center justify-between">
            <Dialog.Title className="text-lg font-bold text-gray-900 dark:text-zinc-50">
              コメントを追加
            </Dialog.Title>
            <Dialog.Close asChild>
              <button
                type="button"
                className="text-gray-400 hover:text-gray-600"
                aria-label="閉じる"
                data-testid="add-comment-close"
              >
                <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12" />
                </svg>
              </button>
            </Dialog.Close>
          </div>

          {/* コメント入力 */}
          <div className="mb-4">
            <label
              htmlFor="comment-content"
              className="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              コメント
            </label>
            <textarea
              id="comment-content"
              value={content}
              onChange={(e) => {
                setContent(e.target.value);
                validateContent(e.target.value);
              }}
              maxLength={MAX_COMMENT_LENGTH}
              rows={4}
              placeholder="コメントを入力"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-50"
              data-testid="comment-content-input"
            />
            <div className="mt-1 flex items-center justify-between">
              {contentError !== null ? (
                <p
                  className="text-xs text-red-500"
                  data-testid="comment-content-error"
                >
                  {contentError}
                </p>
              ) : (
                <span />
              )}
              <span
                className={`text-xs ${content.length > MAX_COMMENT_LENGTH ? "text-red-500" : "text-gray-400"}`}
                data-testid="comment-content-count"
              >
                {content.length}/{MAX_COMMENT_LENGTH}
              </span>
            </div>
          </div>

          {/* 画像選択 */}
          <div className="mb-4">
            <label className="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">
              画像（最大{MAX_IMAGE_COUNT}枚、JPEG/PNG/GIF、5MB以下）
            </label>
            <input
              ref={fileInputRef}
              type="file"
              accept={ALLOWED_MIME_TYPES.join(",")}
              multiple
              onChange={handleFileSelect}
              className="hidden"
              data-testid="image-file-input"
            />
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              disabled={selectedImages.length >= MAX_IMAGE_COUNT}
              className="rounded-md border border-dashed border-gray-300 px-4 py-2 text-sm text-gray-500 hover:border-blue-400 hover:text-blue-500 disabled:cursor-not-allowed disabled:opacity-50 dark:border-zinc-600 dark:text-gray-400"
              data-testid="image-select-button"
            >
              画像を選択
            </button>

            {/* 画像プレビュー */}
            {selectedImages.length > 0 && (
              <div
                className="mt-2 grid grid-cols-4 gap-2"
                data-testid="image-preview-grid"
              >
                {selectedImages.map((img, index) => (
                  <div key={img.previewUrl} className="relative">
                    <img
                      src={img.previewUrl}
                      alt={`選択画像${index + 1}`}
                      className="h-16 w-full rounded-md object-cover"
                      data-testid={`image-preview-${index}`}
                    />
                    <button
                      type="button"
                      onClick={() => handleRemoveImage(index)}
                      className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-xs text-white hover:bg-red-600"
                      aria-label={`画像${index + 1}を削除`}
                      data-testid={`image-remove-${index}`}
                    >
                      <svg className="h-3 w-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            )}

            {/* 画像エラー */}
            {imageError !== null && (
              <p
                className="mt-1 text-xs text-red-500"
                data-testid="image-error"
              >
                {imageError}
              </p>
            )}
          </div>

          {/* 送信ボタン */}
          <button
            type="button"
            onClick={handleSubmit}
            disabled={isSubmitDisabled}
            className="w-full rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
            data-testid="add-comment-submit"
          >
            {isSubmitting ? "投稿中..." : "投稿する"}
          </button>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
