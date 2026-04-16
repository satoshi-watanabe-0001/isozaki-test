/**
 * 日時の相対表示ユーティリティの単体テスト
 *
 * formatRelativeDate関数の各パターン（秒前、分前、時間前、絶対日時）をテストする。
 */
import { describe, it, expect, vi, afterEach } from "vitest";
import { formatRelativeDate } from "@/utils/dateFormat";

describe("formatRelativeDate", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  /**
   * 【テスト対象】formatRelativeDate
   * 【テストケース】数秒前の日時
   * 【期待結果】「X秒前」形式で表示される
   */
  it("数秒前の日時は「X秒前」形式で表示されること", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2025-04-13T12:00:30Z"));

    const result = formatRelativeDate("2025-04-13T12:00:00Z");
    expect(result).toBe("30秒前");
  });

  /**
   * 【テスト対象】formatRelativeDate
   * 【テストケース】数分前の日時
   * 【期待結果】「X分前」形式で表示される
   */
  it("数分前の日時は「X分前」形式で表示されること", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2025-04-13T12:05:00Z"));

    const result = formatRelativeDate("2025-04-13T12:00:00Z");
    expect(result).toBe("5分前");
  });

  /**
   * 【テスト対象】formatRelativeDate
   * 【テストケース】数時間前の日時
   * 【期待結果】「X時間前」形式で表示される
   */
  it("数時間前の日時は「X時間前」形式で表示されること", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2025-04-13T15:00:00Z"));

    const result = formatRelativeDate("2025-04-13T12:00:00Z");
    expect(result).toBe("3時間前");
  });

  /**
   * 【テスト対象】formatRelativeDate
   * 【テストケース】24時間以上前の日時
   * 【期待結果】「YYYY/MM/DD HH:mm」形式で表示される
   */
  it("24時間以上前の日時は「YYYY/MM/DD HH:mm」形式で表示されること", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2025-04-14T12:00:00Z"));

    const result = formatRelativeDate("2025-04-13T10:00:00Z");
    // UTCのためローカルタイムゾーンに依存する
    expect(result).toMatch(/^\d{4}\/\d{2}\/\d{2} \d{2}:\d{2}$/);
  });

  /**
   * 【テスト対象】formatRelativeDate
   * 【テストケース】0秒前の日時
   * 【期待結果】「0秒前」と表示される
   */
  it("0秒前の日時は「0秒前」と表示されること", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2025-04-13T12:00:00Z"));

    const result = formatRelativeDate("2025-04-13T12:00:00Z");
    expect(result).toBe("0秒前");
  });

  /**
   * 【テスト対象】formatRelativeDate
   * 【テストケース】未来の日時
   * 【期待結果】「YYYY/MM/DD HH:mm」形式で表示される
   */
  it("未来の日時は「YYYY/MM/DD HH:mm」形式で表示されること", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2025-04-13T12:00:00Z"));

    const result = formatRelativeDate("2025-04-14T12:00:00Z");
    expect(result).toMatch(/^\d{4}\/\d{2}\/\d{2} \d{2}:\d{2}$/);
  });

  /**
   * 【テスト対象】formatRelativeDate
   * 【テストケース】59分59秒前
   * 【期待結果】「59分前」形式で表示される
   */
  it("59分59秒前の日時は「59分前」と表示されること", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2025-04-13T12:59:59Z"));

    const result = formatRelativeDate("2025-04-13T12:00:00Z");
    expect(result).toBe("59分前");
  });

  /**
   * 【テスト対象】formatRelativeDate
   * 【テストケース】23時間59分前
   * 【期待結果】「23時間前」形式で表示される
   */
  it("23時間59分前の日時は「23時間前」と表示されること", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2025-04-14T11:59:00Z"));

    const result = formatRelativeDate("2025-04-13T12:00:00Z");
    expect(result).toBe("23時間前");
  });
});
