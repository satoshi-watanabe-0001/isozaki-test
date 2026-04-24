/**
 * コミュニティTOPページのローディングUI
 *
 * Next.js App Routerのストリーミング SSR時に、
 * データ取得完了までの間にフォールバックとして表示される。
 *
 * @since 1.5
 */

import type { ReactNode } from "react";
import LoadingSpinner from "@/components/LoadingSpinner";

/**
 * コミュニティTOPページのローディングコンポーネント
 *
 * @returns ローディングスピナーを中央配置したJSX要素
 */
export default function CommunityLoading(): ReactNode {
  return (
    <div className="flex flex-1 items-center justify-center bg-zinc-50 dark:bg-black">
      <LoadingSpinner size="lg" />
    </div>
  );
}
