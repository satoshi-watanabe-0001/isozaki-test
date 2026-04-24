/**
 * ローディングスピナーコンポーネント
 *
 * データ取得中などの読み込み状態を示すスピナーアニメーションを表示する。
 * Tailwind CSSのanimate-spinを使用したCSSアニメーション。
 *
 * @since 1.5
 */

import type { ReactNode } from "react";

/** スピナーサイズの定義 */
type SpinnerSize = "sm" | "md" | "lg";

/** サイズごとのCSSクラス定義 */
const SIZE_CLASSES: Record<SpinnerSize, string> = {
  sm: "h-5 w-5 border-2",
  md: "h-10 w-10 border-3",
  lg: "h-16 w-16 border-4",
};

/** LoadingSpinnerコンポーネントのprops */
interface LoadingSpinnerProps {
  /** スピナーのサイズ（デフォルト: md） */
  size?: SpinnerSize;
}

/**
 * ローディングスピナーコンポーネント
 *
 * 円形のボーダーアニメーションによるスピナーを表示する。
 * サイズはsm/md/lgの3段階から選択可能。
 *
 * @param props - スピナーサイズの指定
 * @returns スピナーのJSX要素
 */
export default function LoadingSpinner({
  size = "md",
}: LoadingSpinnerProps): ReactNode {
  return (
    <div
      className={`animate-spin rounded-full border-gray-300 border-t-blue-600 ${SIZE_CLASSES[size]}`}
      role="status"
      aria-label="読み込み中"
      data-testid="loading-spinner"
    />
  );
}
