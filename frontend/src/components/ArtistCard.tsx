/**
 * アーティストカードコンポーネント
 *
 * アーティストのアイコンと名前を表示するカードUI。
 * アイコン未設定時のフォールバックを備える。
 *
 * @since 1.1
 */

import type { ReactNode } from "react";
import type { Artist } from "@/types/artist";
import Image from "next/image";
import Link from "next/link";

/**
 * ArtistCardコンポーネントのProps定義
 */
interface ArtistCardProps {
  /** 表示するアーティスト情報 */
  artist: Artist;
}

/** デフォルトアイコン画像のパス（アイコンURL未設定時に使用） */
const DEFAULT_ICON_PATH: string = "/images/artists/default.svg";

/**
 * アーティストカードコンポーネント
 *
 * アーティストのアイコン画像とその下にアーティスト名を表示する。
 * アイコンURLが未設定の場合はデフォルト画像を使用する。
 *
 * @param props - アーティスト情報を含むProps
 * @returns アーティストカードのJSX要素
 */
export default function ArtistCard({ artist }: ArtistCardProps): ReactNode {
  /** アイコンURLが未設定の場合、デフォルト画像を使用する */
  const iconUrl: string = artist.iconUrl ?? DEFAULT_ICON_PATH;

  return (
    <Link href={`/community/${artist.artistId}`} className="flex flex-col items-center" data-testid={`artist-card-${artist.artistId}`}>
      <div className="relative h-36 w-36 overflow-hidden rounded-full bg-gray-200">
        <Image
          src={iconUrl}
          alt={`${artist.name}のアイコン`}
          fill
          className="object-cover"
          sizes="144px"
        />
      </div>
      <p className="mt-2 text-center text-sm font-medium text-gray-900 dark:text-zinc-50">
        {artist.name}
      </p>
    </Link>
  );
}
