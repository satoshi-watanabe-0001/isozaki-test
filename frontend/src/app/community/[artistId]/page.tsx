/**
 * コミュニティTOPページコンポーネント（SSR）
 *
 * アーティストのコミュニティTOPページを表示する。
 * サーバサイドでバックエンドAPIからデータを取得し、
 * Client ComponentのCommunityTopContentにデータを渡して描画する。
 *
 * @since 1.2
 * @modified 1.5 CSRからSSR（Server Component + Client Component）に変更
 */

import type { ReactNode } from "react";
import { notFound } from "next/navigation";
import type { CommunityTop } from "@/types/community";
import CommunityTopContent from "@/components/CommunityTopContent";

/** バックエンドAPIのベースURL（サーバサイド用） */
const BACKEND_URL: string =
  process.env.BACKEND_URL ?? "http://localhost:8080";

/** ページコンポーネントのprops型（Next.js App Router） */
interface CommunityTopPageProps {
  /** ルートパラメータ */
  params: Promise<{ artistId: string }>;
}

/**
 * コミュニティTOPページコンポーネント（Server Component）
 *
 * サーバサイドでバックエンドAPIからコミュニティTOP情報を取得し、
 * CommunityTopContent（Client Component）にデータを渡して表示する。
 * アーティストが存在しない場合は404ページへ遷移する。
 *
 * @param props - ルートパラメータ（artistId）
 * @returns コミュニティTOPページのJSX要素
 */
export default async function CommunityTopPage({
  params,
}: CommunityTopPageProps): Promise<ReactNode> {
  const { artistId } = await params;

  const response: Response = await fetch(
    `${BACKEND_URL}/api/v1/community/${artistId}`,
    { cache: "no-store" },
  );

  if (response.status === 404) {
    notFound();
  }

  if (!response.ok) {
    throw new Error(
      `コミュニティ情報の取得に失敗しました（${response.status}）`,
    );
  }

  const communityData: CommunityTop = await response.json();

  return (
    <CommunityTopContent communityData={communityData} artistId={artistId} />
  );
}
