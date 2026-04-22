/**
 * 画像処理ユーティリティ
 *
 * Canvas APIを使用したEXIF情報削除・回転補正処理を提供する。
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
 * バイナリデータを直接パースし、SOI→APP1(EXIF)→IFD0のOrientation(0x0112)タグを検索する。
 * 外部ライブラリを使用せず、最小限のバイナリ解析で実現する。
 *
 * @param file - JPEG画像ファイル
 * @returns Orientation値（1-8、取得できない場合は1）
 */
export async function getExifOrientation(file: File): Promise<number> {
  // JPEG以外はデフォルト値を返却
  if (file.type !== "image/jpeg") {
    return 1;
  }

  const buffer: ArrayBuffer = await file.slice(0, 65536).arrayBuffer();
  const view: DataView = new DataView(buffer);

  // SOIマーカー（0xFFD8）の確認
  if (view.getUint16(0) !== 0xFFD8) {
    return 1;
  }

  let offset = 2;
  while (offset < view.byteLength - 2) {
    const marker: number = view.getUint16(offset);

    // APP1マーカー（0xFFE1）= EXIFデータ
    if (marker === 0xFFE1) {
      const exifOffset: number = offset + 4;

      // "Exif\0\0"シグネチャの確認
      if (
        view.getUint32(exifOffset) !== 0x45786966 ||
        view.getUint16(exifOffset + 4) !== 0x0000
      ) {
        return 1;
      }

      const tiffOffset: number = exifOffset + 6;

      // エンディアン判定（"II"=リトルエンディアン、"MM"=ビッグエンディアン）
      const littleEndian: boolean = view.getUint16(tiffOffset) === 0x4949;

      // IFD0エントリ数の取得
      const ifdOffset: number = tiffOffset + view.getUint32(tiffOffset + 4, littleEndian);
      const entryCount: number = view.getUint16(ifdOffset, littleEndian);

      // IFD0エントリからOrientation(0x0112)タグを検索
      for (let i = 0; i < entryCount; i++) {
        const entryOffset: number = ifdOffset + 2 + i * 12;
        if (entryOffset + 12 > view.byteLength) break;

        const tag: number = view.getUint16(entryOffset, littleEndian);
        if (tag === 0x0112) {
          const orientation: number = view.getUint16(entryOffset + 8, littleEndian);
          return orientation >= 1 && orientation <= 8 ? orientation : 1;
        }
      }

      return 1;
    }

    // 他のマーカーはスキップ
    if ((marker & 0xFF00) !== 0xFF00) break;
    const segmentLength: number = view.getUint16(offset + 2);
    offset += 2 + segmentLength;
  }

  return 1;
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
