/**
 * 日時の相対表示ユーティリティ
 *
 * 24時間以内の日時は「X秒前」「X分前」「X時間前」の相対表示を行い、
 * それ以降は「YYYY/MM/DD HH:mm」形式で表示する。
 *
 * @since 1.3
 */

/** 1分を秒で表した定数 */
const SECONDS_PER_MINUTE = 60;

/** 1時間を秒で表した定数 */
const SECONDS_PER_HOUR = 3600;

/** 24時間を秒で表した定数 */
const SECONDS_PER_DAY = 86400;

/**
 * 日時文字列を相対表示またはフォーマット済み文字列に変換する
 *
 * 24時間以内: 「X秒前」「X分前」「X時間前」
 * 24時間以降: 「YYYY/MM/DD HH:mm」
 *
 * @param dateString - ISO 8601形式の日時文字列
 * @returns フォーマット済みの日時文字列
 */
export function formatRelativeDate(dateString: string): string {
  const date: Date = new Date(dateString);
  const now: Date = new Date();
  const diffSeconds: number = Math.floor(
    (now.getTime() - date.getTime()) / 1000,
  );

  // 未来の日時の場合はフォーマット表示
  if (diffSeconds < 0) {
    return formatAbsoluteDate(date);
  }

  if (diffSeconds < SECONDS_PER_MINUTE) {
    return `${diffSeconds}秒前`;
  }

  if (diffSeconds < SECONDS_PER_HOUR) {
    const minutes: number = Math.floor(diffSeconds / SECONDS_PER_MINUTE);
    return `${minutes}分前`;
  }

  if (diffSeconds < SECONDS_PER_DAY) {
    const hours: number = Math.floor(diffSeconds / SECONDS_PER_HOUR);
    return `${hours}時間前`;
  }

  return formatAbsoluteDate(date);
}

/**
 * Dateオブジェクトを「YYYY/MM/DD HH:mm」形式の文字列に変換する
 *
 * @param date - 変換対象のDateオブジェクト
 * @returns フォーマット済みの日時文字列
 */
function formatAbsoluteDate(date: Date): string {
  const year: number = date.getFullYear();
  const month: string = String(date.getMonth() + 1).padStart(2, "0");
  const day: string = String(date.getDate()).padStart(2, "0");
  const hours: string = String(date.getHours()).padStart(2, "0");
  const minutes: string = String(date.getMinutes()).padStart(2, "0");
  return `${year}/${month}/${day} ${hours}:${minutes}`;
}
