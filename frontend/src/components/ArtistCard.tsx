/**
 * アーティストカードコンポーネント
 *
 * アーティストのアイコンと名前を表示するカードUI。
 * ダミー画像のフォールバックを備える。
 *
 * @since 1.1
 */

import type { ReactNode } from "react";
import type { Artist } from "@/types/artist";
import Image from "next/image";

/**
 * ArtistCardコンポーネントのProps定義
 */
interface ArtistCardProps {
  /** 表示するアーティスト情報 */
  artist: Artist;
}

/** ダミー画像のベースURL（アイコンURL未設定時に使用） */
const DUMMY_ICON_BASE_URL: string = "https://placehold.co/150x150?text=";

/**
 * アーティストカードコンポーネント
 *
 * アーティストのアイコン画像とその下にアーティスト名を表示する。
 * アイコンURLが未設定の場合はダミー画像を使用する。
 *
 * @param props - アーティスト情報を含むProps
 * @returns アーティストカードのJSX要素
 */
export default function ArtistCard({ artist }: ArtistCardProps): ReactNode {
  /** アイコンURLが未設定の場合、アーティスト名の先頭文字でダミー画像を生成する */
  const iconUrl: string = artist.iconUrl ?? `${DUMMY_ICON_BASE_URL}${encodeURIComponent(artist.name.charAt(0))}`;

  return (
    <div className="flex flex-col items-center" data-testid={`artist-card-${artist.artistId}`}>
      <div className="relative h-36 w-36 overflow-hidden rounded-full bg-gray-200">
        <Image
          src={iconUrl}
          alt={`${artist.name}のアイコン`}
          fill
          className="object-cover"
          sizes="144px"
          unoptimized
        />
      </div>
      <p className="mt-2 text-center text-sm font-medium text-gray-900 dark:text-zinc-50">
        {artist.name}
      </p>
    </div>
  );
}
