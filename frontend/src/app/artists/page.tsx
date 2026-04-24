/**
 * アーティスト一覧ページコンポーネント（SSR）
 *
 * バックエンドAPIからアーティスト一覧をサーバサイドで取得し、
 * 2列のグリッドレイアウトでアイコンと名前を表示する。
 * アーティストは50音順にソートされ、右下に「And more...」を表示する。
 *
 * @since 1.1
 * @modified 1.5 CSRからSSR（Server Component）に変更
 */

import type { ReactNode } from "react";
import type { Artist } from "@/types/artist";
import ArtistCard from "@/components/ArtistCard";

/** バックエンドAPIのベースURL（サーバサイド用） */
const BACKEND_URL: string =
  process.env.BACKEND_URL ?? "http://localhost:8080";

/**
 * アーティスト一覧ページコンポーネント（Server Component）
 *
 * サーバサイドでバックエンドAPIからアーティスト一覧を取得し、
 * 50音順に2列で表示する。取得失敗時はError Boundaryに委譲する。
 *
 * @returns アーティスト一覧ページのJSX要素
 */
export default async function ArtistsPage(): Promise<ReactNode> {
  const response: Response = await fetch(`${BACKEND_URL}/api/v1/artists`, {
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`アーティスト一覧の取得に失敗しました（${response.status}）`);
  }

  const artists: Artist[] = await response.json();

  return (
    <div className="flex-1 bg-zinc-50 dark:bg-black">
      <main className="mx-auto w-full max-w-3xl px-4 py-8 pb-16 sm:px-6 lg:px-8">
        <h1 className="mb-8 text-2xl font-bold text-gray-900 dark:text-zinc-50">
          アーティスト一覧
        </h1>

        {/* アーティストグリッド（2列表示） */}
        <div
          className="grid grid-cols-2 gap-6"
          data-testid="artist-grid"
        >
          {artists.map((artist: Artist) => (
            <ArtistCard key={artist.artistId} artist={artist} />
          ))}
        </div>

        {/* And more... 表示（右下に配置） */}
        <div className="mt-6 flex justify-end">
          <p
            className="text-sm font-medium text-gray-500 dark:text-gray-400"
            data-testid="and-more"
          >
            And more...
          </p>
        </div>
      </main>
    </div>
  );
}
