/**
 * 画像処理ユーティリティの単体テスト
 *
 * EXIF削除・回転補正、ファイルバリデーション、定数値をテストする。
 */
import { describe, it, expect } from "vitest";
import {
  validateImageFile,
  getExifOrientation,
  MAX_FILE_SIZE,
  MAX_IMAGE_COUNT,
  ALLOWED_MIME_TYPES,
} from "@/utils/imageUtils";

describe("imageUtils", () => {
  /**
   * 【テスト対象】定数値
   * 【テストケース】定数が正しい値を持つこと
   * 【期待結果】各定数が仕様通りの値
   * 【ビジネス要件】画像アップロード制限
   */
  it("定数値が正しいこと", () => {
    expect(MAX_FILE_SIZE).toBe(5 * 1024 * 1024);
    expect(MAX_IMAGE_COUNT).toBe(4);
    expect(ALLOWED_MIME_TYPES).toContain("image/jpeg");
    expect(ALLOWED_MIME_TYPES).toContain("image/png");
    expect(ALLOWED_MIME_TYPES).toContain("image/gif");
  });

  /**
   * 【テスト対象】validateImageFile
   * 【テストケース】有効なJPEGファイル
   * 【期待結果】nullが返される（エラーなし）
   * 【ビジネス要件】JPEG画像のバリデーション
   */
  it("有効なJPEGファイルでnullが返されること", () => {
    const file = new File(["test"], "photo.jpg", { type: "image/jpeg" });
    Object.defineProperty(file, "size", { value: 1024 * 1024 });
    expect(validateImageFile(file)).toBeNull();
  });

  /**
   * 【テスト対象】validateImageFile
   * 【テストケース】有効なPNGファイル
   * 【期待結果】nullが返される（エラーなし）
   * 【ビジネス要件】PNG画像のバリデーション
   */
  it("有効なPNGファイルでnullが返されること", () => {
    const file = new File(["test"], "image.png", { type: "image/png" });
    Object.defineProperty(file, "size", { value: 1024 * 1024 });
    expect(validateImageFile(file)).toBeNull();
  });

  /**
   * 【テスト対象】validateImageFile
   * 【テストケース】有効なGIFファイル
   * 【期待結果】nullが返される（エラーなし）
   * 【ビジネス要件】GIF画像のバリデーション
   */
  it("有効なGIFファイルでnullが返されること", () => {
    const file = new File(["test"], "anim.gif", { type: "image/gif" });
    Object.defineProperty(file, "size", { value: 1024 * 1024 });
    expect(validateImageFile(file)).toBeNull();
  });

  /**
   * 【テスト対象】validateImageFile
   * 【テストケース】許可されていないMIMEタイプ（WebP）
   * 【期待結果】エラーメッセージが返される
   * 【ビジネス要件】非対応形式の拒否
   */
  it("WebPファイルでエラーが返されること", () => {
    const file = new File(["test"], "image.webp", { type: "image/webp" });
    Object.defineProperty(file, "size", { value: 1024 });
    const result = validateImageFile(file);
    expect(result).not.toBeNull();
    expect(result).toContain("JPEG、PNG、GIF");
  });

  /**
   * 【テスト対象】validateImageFile
   * 【テストケース】5MBを超えるファイル
   * 【期待結果】エラーメッセージが返される
   * 【ビジネス要件】ファイルサイズ制限
   */
  it("5MB超のファイルでエラーが返されること", () => {
    const file = new File(["test"], "large.jpg", { type: "image/jpeg" });
    Object.defineProperty(file, "size", { value: 6 * 1024 * 1024 });
    const result = validateImageFile(file);
    expect(result).not.toBeNull();
    expect(result).toContain("5MB");
  });

  /**
   * 【テスト対象】validateImageFile
   * 【テストケース】テキストファイル
   * 【期待結果】エラーメッセージが返される
   * 【ビジネス要件】非画像ファイルの拒否
   */
  it("テキストファイルでエラーが返されること", () => {
    const file = new File(["test"], "doc.txt", { type: "text/plain" });
    Object.defineProperty(file, "size", { value: 100 });
    const result = validateImageFile(file);
    expect(result).not.toBeNull();
  });
});

describe("getExifOrientation", () => {
  /**
   * 【テスト対象】getExifOrientation
   * 【テストケース】JPEG以外のファイル
   * 【期待結果】デフォルト値1が返される
   * 【ビジネス要件】PNG/GIF画像はEXIF回転情報なし
   */
  it("PNG画像でデフォルト値1が返されること", async () => {
    const file = new File(["test"], "image.png", { type: "image/png" });
    const orientation = await getExifOrientation(file);
    expect(orientation).toBe(1);
  });

  /**
   * 【テスト対象】getExifOrientation
   * 【テストケース】不正なJPEGファイル（SOIマーカーなし）
   * 【期待結果】デフォルト値1が返される
   * 【ビジネス要件】破損ファイルの安全な処理
   */
  it("不正なJPEGファイルでデフォルト値1が返されること", async () => {
    const data = new Uint8Array([0x00, 0x00, 0x00, 0x00]);
    const file = new File([data], "invalid.jpg", { type: "image/jpeg" });
    const orientation = await getExifOrientation(file);
    expect(orientation).toBe(1);
  });

  /**
   * 【テスト対象】getExifOrientation
   * 【テストケース】EXIFデータなしのJPEGファイル
   * 【期待結果】デフォルト値1が返される
   * 【ビジネス要件】EXIF情報のないJPEG画像の安全な処理
   */
  it("EXIFなしJPEGでデフォルト値1が返されること", async () => {
    // SOIマーカーのみの最小JPEGヘッダ + 非APP1マーカー
    const data = new Uint8Array([
      0xFF, 0xD8, // SOI
      0xFF, 0xE0, // APP0 (JFIF)
      0x00, 0x10, // APP0セグメント長
      0x4A, 0x46, 0x49, 0x46, 0x00, // "JFIF\0"
      0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
    ]);
    const file = new File([data], "no-exif.jpg", { type: "image/jpeg" });
    const orientation = await getExifOrientation(file);
    expect(orientation).toBe(1);
  });

  /**
   * 【テスト対象】getExifOrientation
   * 【テストケース】Orientation=6のJPEGファイル（90度右回転）
   * 【期待結果】6が返される
   * 【ビジネス要件】スマートフォン撮影画像の回転情報読み取り
   */
  it("Orientation=6のJPEGファイルで6が返されること", async () => {
    // EXIF Orientation=6を含む最小JPEGバイナリ（ビッグエンディアン）
    const data = new Uint8Array([
      0xFF, 0xD8, // SOI
      0xFF, 0xE1, // APP1
      0x00, 0x1E, // APP1セグメント長（30バイト）
      0x45, 0x78, 0x69, 0x66, 0x00, 0x00, // "Exif\0\0"
      0x4D, 0x4D, // ビッグエンディアン "MM"
      0x00, 0x2A, // TIFF マジックナンバー
      0x00, 0x00, 0x00, 0x08, // IFD0オフセット（8バイト目）
      0x00, 0x01, // IFD0エントリ数（1）
      0x01, 0x12, // Orientationタグ（0x0112）
      0x00, 0x03, // SHORT型
      0x00, 0x00, 0x00, 0x01, // カウント=1
      0x00, 0x06, 0x00, 0x00, // Orientation値=6（90度右回転）
    ]);
    const file = new File([data], "rotated.jpg", { type: "image/jpeg" });
    const orientation = await getExifOrientation(file);
    expect(orientation).toBe(6);
  });

  /**
   * 【テスト対象】getExifOrientation
   * 【テストケース】Orientation=3のJPEGファイル（180度回転）
   * 【期待結果】3が返される
   * 【ビジネス要件】上下反転画像の回転情報読み取り
   */
  it("Orientation=3のJPEGファイルで3が返されること", async () => {
    // EXIF Orientation=3を含む最小JPEGバイナリ（リトルエンディアン）
    const data = new Uint8Array([
      0xFF, 0xD8, // SOI
      0xFF, 0xE1, // APP1
      0x00, 0x1E, // APP1セグメント長
      0x45, 0x78, 0x69, 0x66, 0x00, 0x00, // "Exif\0\0"
      0x49, 0x49, // リトルエンディアン "II"
      0x2A, 0x00, // TIFF マジックナンバー
      0x08, 0x00, 0x00, 0x00, // IFD0オフセット
      0x01, 0x00, // IFD0エントリ数（1）
      0x12, 0x01, // Orientationタグ（0x0112、リトルエンディアン）
      0x03, 0x00, // SHORT型
      0x01, 0x00, 0x00, 0x00, // カウント=1
      0x03, 0x00, 0x00, 0x00, // Orientation値=3（180度回転）
    ]);
    const file = new File([data], "flipped.jpg", { type: "image/jpeg" });
    const orientation = await getExifOrientation(file);
    expect(orientation).toBe(3);
  });
});
