/**
 * 画像処理ユーティリティ
 *
 * Canvas APIを使用したEXIF情報削除処理を提供する。
 * ブラウザネイティブAPIのみを使用し、外部ライブラリに依存しない。
 *
 * @since 1.4
 */

/** 画像ファイルの最大サイズ（5MB） */
export const MAX_FILE_SIZE: number = 5 * 1024 * 1024;

/** 1回のアップロードで許可する最大画像数 */
export const MAX_IMAGE_COUNT: number = 4;

/** 許可する画像MIMEタイプ */
export const ALLOWED_MIME_TYPES: string[] = [
  "image/jpeg",
  "image/png",
  "image/gif",
];

/**
 * Canvas APIでEXIF情報を削除した画像Blobを生成する
 *
 * 元画像をCanvas上に描画し、再エクスポートすることで
 * EXIF情報（GPS座標、カメラ情報等）を除去する。
 * GIF画像はEXIF削除不要のためそのまま返却する。
 *
 * @param file - 元画像ファイル
 * @returns EXIF削除済みのBlob
 */
export async function removeExif(file: File): Promise<Blob> {
  // GIF画像はEXIF情報を持たないためそのまま返却
  if (file.type === "image/gif") {
    return file;
  }

  return new Promise<Blob>((resolve, reject) => {
    const img: HTMLImageElement = new Image();
    const url: string = URL.createObjectURL(file);

    img.onload = (): void => {
      URL.revokeObjectURL(url);
      const canvas: HTMLCanvasElement = document.createElement("canvas");
      canvas.width = img.naturalWidth;
      canvas.height = img.naturalHeight;

      const ctx: CanvasRenderingContext2D | null = canvas.getContext("2d");
      if (!ctx) {
        reject(new Error("Canvas 2Dコンテキストの取得に失敗しました"));
        return;
      }

      ctx.drawImage(img, 0, 0);

      // 元のMIMEタイプで再エクスポート（EXIF情報は含まれない）
      canvas.toBlob(
        (blob: Blob | null): void => {
          if (blob) {
            resolve(blob);
          } else {
            reject(new Error("画像のエクスポートに失敗しました"));
          }
        },
        file.type,
        0.95,
      );
    };

    img.onerror = (): void => {
      URL.revokeObjectURL(url);
      reject(new Error("画像の読み込みに失敗しました"));
    };

    img.src = url;
  });
}

/**
 * ファイルのバリデーションを行う
 *
 * @param file - バリデーション対象のファイル
 * @returns エラーメッセージ（問題ない場合はnull）
 */
export function validateImageFile(file: File): string | null {
  if (!ALLOWED_MIME_TYPES.includes(file.type)) {
    return `${file.name}: JPEG、PNG、GIF形式のみアップロード可能です`;
  }
  if (file.size > MAX_FILE_SIZE) {
    return `${file.name}: ファイルサイズは5MB以下にしてください`;
  }
  return null;
}
