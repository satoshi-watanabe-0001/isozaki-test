/**
 * 画像処理ユーティリティの単体テスト
 *
 * exifrライブラリによるEXIF Orientation読取、
 * ファイルバリデーション、定数値をテストする。
 */
import { describe, it, expect, vi } from "vitest";

// exifrモジュールのモック
vi.mock("exifr", () => ({
  default: {
    orientation: vi.fn(),
  },
}));

import exifr from "exifr";
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
   * 【テストケース】exifrがOrientation=6を返す場合
   * 【期待結果】6が返される
   * 【ビジネス要件】スマートフォン撮影画像の回転情報読み取り
   */
  it("Orientation=6のJPEGファイルで6が返されること", async () => {
    vi.mocked(exifr.orientation).mockResolvedValue(6);
    const file = new File(["test"], "rotated.jpg", { type: "image/jpeg" });
    const orientation = await getExifOrientation(file);
    expect(orientation).toBe(6);
  });

  /**
   * 【テスト対象】getExifOrientation
   * 【テストケース】exifrがOrientation=3を返す場合
   * 【期待結果】3が返される
   * 【ビジネス要件】上下反転画像の回転情報読み取り
   */
  it("Orientation=3のJPEGファイルで3が返されること", async () => {
    vi.mocked(exifr.orientation).mockResolvedValue(3);
    const file = new File(["test"], "flipped.jpg", { type: "image/jpeg" });
    const orientation = await getExifOrientation(file);
    expect(orientation).toBe(3);
  });

  /**
   * 【テスト対象】getExifOrientation
   * 【テストケース】exifrがnullを返す場合（EXIF情報なし）
   * 【期待結果】デフォルト値1が返される
   * 【ビジネス要件】EXIF情報のないJPEG画像の安全な処理
   */
  it("exifrがnullを返した場合にデフォルト値1が返されること", async () => {
    vi.mocked(exifr.orientation).mockResolvedValue(null as unknown as number);
    const file = new File(["test"], "no-exif.jpg", { type: "image/jpeg" });
    const orientation = await getExifOrientation(file);
    expect(orientation).toBe(1);
  });

  /**
   * 【テスト対象】getExifOrientation
   * 【テストケース】exifrがundefinedを返す場合
   * 【期待結果】デフォルト値1が返される
   * 【ビジネス要件】破損ファイルの安全な処理
   */
  it("exifrがundefinedを返した場合にデフォルト値1が返されること", async () => {
    vi.mocked(exifr.orientation).mockResolvedValue(undefined as unknown as number);
    const file = new File(["test"], "broken.jpg", { type: "image/jpeg" });
    const orientation = await getExifOrientation(file);
    expect(orientation).toBe(1);
  });

  /**
   * 【テスト対象】getExifOrientation
   * 【テストケース】exifrが例外をスローする場合
   * 【期待結果】デフォルト値1が返される
   * 【ビジネス要件】ライブラリエラー時の安全な処理
   */
  it("exifrが例外をスローした場合にデフォルト値1が返されること", async () => {
    vi.mocked(exifr.orientation).mockRejectedValue(new Error("パースエラー"));
    const file = new File(["test"], "invalid.jpg", { type: "image/jpeg" });
    const orientation = await getExifOrientation(file);
    expect(orientation).toBe(1);
  });

  /**
   * 【テスト対象】getExifOrientation
   * 【テストケース】exifrが範囲外の値を返す場合
   * 【期待結果】デフォルト値1が返される
   * 【ビジネス要件】不正なOrientation値の安全な処理
   */
  it("exifrが範囲外の値を返した場合にデフォルト値1が返されること", async () => {
    vi.mocked(exifr.orientation).mockResolvedValue(9);
    const file = new File(["test"], "invalid-orient.jpg", { type: "image/jpeg" });
    const orientation = await getExifOrientation(file);
    expect(orientation).toBe(1);
  });
});
