/**
 * 画像処理ユーティリティの単体テスト
 *
 * EXIF削除、ファイルバリデーション、定数値をテストする。
 */
import { describe, it, expect } from "vitest";
import {
  validateImageFile,
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
