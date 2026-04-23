/**
 * 画像処理ユーティリティ
 *
 * exifrライブラリによるEXIF Orientation読取と、
 * Canvas APIによる回転補正・EXIF削除処理を提供する。
 *
 * @since 1.4
 */

import exifr from "exifr";

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
 * EXIF Orientation値の定義
 *
 * JPEG画像のEXIFデータに含まれるOrientation値（1-8）は
 * 画像の回転・反転状態を示す。
 */
interface OrientationTransform {
  /** Canvas描画前の回転角度（ラジアン） */
  rotation: number;
  /** 幅と高さを入れ替えるかどうか */
  swapDimensions: boolean;
  /** 水平方向に反転するかどうか */
  flipHorizontal: boolean;
  /** 垂直方向に反転するかどうか */
  flipVertical: boolean;
}

/** Orientation値に対応する変換パラメータ */
const ORIENTATION_TRANSFORMS: Record<number, OrientationTransform> = {
  1: { rotation: 0, swapDimensions: false, flipHorizontal: false, flipVertical: false },
  2: { rotation: 0, swapDimensions: false, flipHorizontal: true, flipVertical: false },
  3: { rotation: Math.PI, swapDimensions: false, flipHorizontal: false, flipVertical: false },
  4: { rotation: 0, swapDimensions: false, flipHorizontal: false, flipVertical: true },
  5: { rotation: Math.PI / 2, swapDimensions: true, flipHorizontal: true, flipVertical: false },
  6: { rotation: Math.PI / 2, swapDimensions: true, flipHorizontal: false, flipVertical: false },
  7: { rotation: -Math.PI / 2, swapDimensions: true, flipHorizontal: true, flipVertical: false },
  8: { rotation: -Math.PI / 2, swapDimensions: true, flipHorizontal: false, flipVertical: false },
};

/**
 * JPEGファイルからEXIF Orientation値を読み取る
 *
 * exifrライブラリを使用してEXIFメタデータからOrientation値を取得する。
 * JPEG以外のファイルや取得できない場合はデフォルト値1を返却する。
 *
 * @param file - 画像ファイル
 * @returns Orientation値（1-8、取得できない場合は1）
 */
export async function getExifOrientation(file: File): Promise<number> {
  // JPEG以外はデフォルト値を返却
  if (file.type !== "image/jpeg") {
    return 1;
  }

  try {
    const orientation = await exifr.orientation(file);
    return (orientation != null && orientation >= 1 && orientation <= 8)
      ? orientation
      : 1;
  } catch {
    return 1;
  }
}

/**
 * Canvas APIでEXIF情報を削除し、回転情報に従って画像を回転させたBlobを生成する
 *
 * 元画像をCanvas上に描画し、再エクスポートすることで
 * EXIF情報（GPS座標、カメラ情報等）を除去する。
 * EXIF Orientation値が含まれる場合は、その情報に従って
 * 画像を正しい向きに回転させてからエクスポートする。
 * GIF画像はEXIF削除不要のためそのまま返却する。
 *
 * @param file - 元画像ファイル
 * @returns EXIF削除・回転補正済みのBlob
 */
export async function removeExif(file: File): Promise<Blob> {
  // GIF画像はEXIF情報を持たないためそのまま返却
  if (file.type === "image/gif") {
    return file;
  }

  // EXIF Orientation値を取得
  const orientation: number = await getExifOrientation(file);
  const transform: OrientationTransform = ORIENTATION_TRANSFORMS[orientation] || ORIENTATION_TRANSFORMS[1];

  return new Promise<Blob>((resolve, reject) => {
    const img: HTMLImageElement = new Image();
    const url: string = URL.createObjectURL(file);

    img.onload = (): void => {
      URL.revokeObjectURL(url);

      const naturalWidth: number = img.naturalWidth;
      const naturalHeight: number = img.naturalHeight;

      // 回転により幅と高さが入れ替わる場合のCanvas サイズ設定
      const canvas: HTMLCanvasElement = document.createElement("canvas");
      if (transform.swapDimensions) {
        canvas.width = naturalHeight;
        canvas.height = naturalWidth;
      } else {
        canvas.width = naturalWidth;
        canvas.height = naturalHeight;
      }

      const ctx: CanvasRenderingContext2D | null = canvas.getContext("2d");
      if (!ctx) {
        reject(new Error("Canvas 2Dコンテキストの取得に失敗しました"));
        return;
      }

      // Canvas中心に移動して変換を適用
      ctx.translate(canvas.width / 2, canvas.height / 2);

      // 反転処理
      if (transform.flipHorizontal) {
        ctx.scale(-1, 1);
      }
      if (transform.flipVertical) {
        ctx.scale(1, -1);
      }

      // 回転処理
      if (transform.rotation !== 0) {
        ctx.rotate(transform.rotation);
      }

      // 画像を描画（中心基準）
      ctx.drawImage(img, -naturalWidth / 2, -naturalHeight / 2);

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
