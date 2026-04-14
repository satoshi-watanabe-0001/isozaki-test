/**
 * アーティスト一覧ページコンポーネント
 *
 * バックエンドAPIからアーティスト一覧を取得し、
 * 2列のグリッドレイアウトでアイコンと名前を表示する。
 * アーティストは50音順にソートされ、右下に「And more...」を表示する。
 *
 * @since 1.1
 */
"use client";

import { useState, useEffect, type ReactNode } from "react";
import type { Artist } from "@/types/artist";
import ArtistCard from "@/components/ArtistCard";

/** バックエンドAPIのベースURL */
const BACKEND_URL: string =
  process.env.NEXT_PUBLIC_BACKEND_URL ?? "http://localhost:8080";

/**
 * アーティスト一覧ページコンポーネント
 *
 * マウント時にバックエンドAPIからアーティスト一覧を取得し、
 * 50音順に2列で表示する。取得失敗時はエラーメッセージを表示する。
 *
 * @returns アーティスト一覧ページのJSX要素
 */
export default function ArtistsPage(): ReactNode {
  const [artists, setArtists] = useState<Artist[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    /**
     * バックエンドAPIからアーティスト一覧を取得する
     *
     * 50音順にソート済みのデータがAPIから返却されるため、
     * フロントエンド側での再ソートは不要。
     */
    const fetchArtists = async (): Promise<void> => {
      try {
        const response: Response = await fetch(`${BACKEND_URL}/api/v1/artists`);
        if (!response.ok) {
          throw new Error(`アーティスト一覧の取得に失敗しました（${response.status}）`);
        }
        const data: Artist[] = await response.json();
        setArtists(data);
      } catch (err) {
        const errorMessage: string =
          err instanceof Error ? err.message : "不明なエラーが発生しました";
        setError(errorMessage);
      } finally {
        setIsLoading(false);
      }
    };

    void fetchArtists();
  }, []);

  if (isLoading) {
    return (
      <div className="flex flex-1 items-center justify-center bg-zinc-50 dark:bg-black">
        <p className="text-gray-500" data-testid="loading-indicator">
          読み込み中...
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-1 items-center justify-center bg-zinc-50 dark:bg-black">
        <p className="text-red-500" data-testid="error-message">
          {error}
        </p>
      </div>
    );
  }

  return (
    <div className="flex flex-col flex-1 bg-zinc-50 dark:bg-black">
      <main className="mx-auto w-full max-w-3xl px-4 py-8 sm:px-6 lg:px-8">
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
