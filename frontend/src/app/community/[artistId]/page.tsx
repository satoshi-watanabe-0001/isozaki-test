/**
 * コミュニティTOPページコンポーネント
 *
 * アーティストのコミュニティTOPページを表示する。
 * アーティスト名、カルーセル画像、メニュー、キャンペーン、お知らせを縦に並べる。
 * カルーセルにはEmbla Carouselを使用し、スライドアニメーションで画像を切り替える。
 *
 * @since 1.2
 */
"use client";

import { useState, useEffect, useCallback, type ReactNode } from "react";
import { useParams, notFound } from "next/navigation";
import Image from "next/image";
import useEmblaCarousel from "embla-carousel-react";
import type { CommunityTop, MenuItem } from "@/types/community";

/** バックエンドAPIのベースURL */
const BACKEND_URL: string =
  process.env.NEXT_PUBLIC_BACKEND_URL ?? "http://localhost:8080";

/** メニュー項目の定義（4列×2行 = 6項目） */
const MENU_ITEMS: MenuItem[] = [
  { label: "プロフィール", icon: "👤" },
  { label: "イベント", icon: "🎵" },
  { label: "キャンペーン", icon: "🎁" },
  { label: "スレッド", icon: "💬" },
  { label: "お知らせ", icon: "📢" },
  { label: "公式ページ", icon: "🌐" },
];

/**
 * コミュニティTOPページコンポーネント
 *
 * マウント時にバックエンドAPIからコミュニティTOP情報を取得し、
 * アーティスト名、カルーセル画像、メニュー、キャンペーン、お知らせを表示する。
 *
 * @returns コミュニティTOPページのJSX要素
 */
export default function CommunityTopPage(): ReactNode {
  const params = useParams();
  const artistId: string = params.artistId as string;

  const [communityData, setCommunityData] = useState<CommunityTop | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [notFoundFlag, setNotFoundFlag] = useState<boolean>(false);

  /** Embla Carouselの初期化 */
  const [emblaRef, emblaApi] = useEmblaCarousel({ loop: false });

  /** カルーセルの現在表示インデックス */
  const [currentImageIndex, setCurrentImageIndex] = useState<number>(0);

  /**
   * Embla Carouselのスライド変更時にインジケーターを更新するコールバック
   */
  const onSelect = useCallback((): void => {
    if (!emblaApi) return;
    setCurrentImageIndex(emblaApi.selectedScrollSnap());
  }, [emblaApi]);

  /** Embla Carouselのイベントリスナー登録 */
  useEffect(() => {
    if (!emblaApi) return;
    emblaApi.on("select", onSelect);
    onSelect();
    return () => {
      emblaApi.off("select", onSelect);
    };
  }, [emblaApi, onSelect]);

  useEffect(() => {
    /**
     * バックエンドAPIからコミュニティTOP情報を取得する
     */
    const fetchCommunityData = async (): Promise<void> => {
      try {
        const response: Response = await fetch(
          `${BACKEND_URL}/api/v1/community/${artistId}`,
        );
        if (response.status === 404) {
          setNotFoundFlag(true);
          return;
        }
        if (!response.ok) {
          throw new Error(
            `コミュニティ情報の取得に失敗しました（${response.status}）`,
          );
        }
        const data: CommunityTop = await response.json();
        setCommunityData(data);
      } catch (err) {
        const errorMessage: string =
          err instanceof Error ? err.message : "不明なエラーが発生しました";
        setError(errorMessage);
      } finally {
        setIsLoading(false);
      }
    };

    void fetchCommunityData();
  }, [artistId]);

  /**
   * インジケータークリック時にカルーセルを指定スライドへスクロールする
   */
  const scrollTo = useCallback(
    (index: number): void => {
      if (!emblaApi) return;
      emblaApi.scrollTo(index);
    },
    [emblaApi],
  );

  if (isLoading) {
    return (
      <div className="flex flex-1 items-center justify-center bg-zinc-50 dark:bg-black">
        <p className="text-gray-500" data-testid="loading-indicator">
          読み込み中...
        </p>
      </div>
    );
  }

  if (notFoundFlag) {
    notFound();
  }

  if (error) {
    throw new Error(error);
  }

  if (!communityData) {
    return null;
  }

  return (
    <div className="flex flex-col flex-1 bg-zinc-50 dark:bg-black">
      <main className="mx-auto w-full max-w-3xl px-4 py-8 sm:px-6 lg:px-8">
        {/* アーティスト名 */}
        <h1
          className="mb-6 text-2xl font-bold text-gray-900 dark:text-zinc-50"
          data-testid="artist-name"
        >
          {communityData.name}
        </h1>

        {/* カルーセル画像領域（Embla Carousel） */}
        {communityData.images.length > 0 && (
          <div className="mb-8" data-testid="carousel-section">
            <div
              className="overflow-hidden rounded-lg"
              ref={emblaRef}
              data-testid="carousel-container"
            >
              <div className="flex">
                {communityData.images.map((image, index) => (
                  <div
                    key={image.imageId}
                    className="relative aspect-square w-full flex-[0_0_100%] min-w-0 bg-gray-200"
                    data-testid={`carousel-slide-${index}`}
                  >
                    <Image
                      src={image.imageUrl}
                      alt={`${communityData.name}の画像 ${index + 1}`}
                      fill
                      className="object-cover"
                      sizes="(max-width: 768px) 100vw, 768px"
                      data-testid={index === 0 ? "carousel-image" : undefined}
                    />
                  </div>
                ))}
              </div>
            </div>
            {/* カルーセルインジケーター */}
            {communityData.images.length > 1 && (
              <div
                className="mt-3 flex justify-center gap-2"
                data-testid="carousel-indicators"
              >
                {communityData.images.map((_, index) => (
                  <button
                    key={communityData.images[index].imageId}
                    type="button"
                    className={`h-2 w-2 rounded-full ${
                      index === currentImageIndex
                        ? "bg-blue-600"
                        : "bg-gray-300"
                    }`}
                    onClick={() => scrollTo(index)}
                    aria-label={`画像${index + 1}に移動`}
                    data-testid={`carousel-indicator-${index}`}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {/* メニュー領域 */}
        <div className="mb-8" data-testid="menu-section">
          <div className="grid grid-cols-4 gap-4">
            {MENU_ITEMS.map((item: MenuItem) => (
              <button
                key={item.label}
                type="button"
                className="flex flex-col items-center gap-1 rounded-lg p-3 transition-colors hover:bg-gray-100 dark:hover:bg-zinc-800"
                data-testid={`menu-item-${item.label}`}
              >
                <span className="text-2xl">{item.icon}</span>
                <span className="text-xs font-medium text-gray-700 dark:text-gray-300">
                  {item.label}
                </span>
              </button>
            ))}
          </div>
        </div>

        {/* キャンペーン領域 */}
        {communityData.campaigns.length > 0 && (
          <div className="mb-8" data-testid="campaign-section">
            <h2 className="mb-4 text-lg font-bold text-gray-900 dark:text-zinc-50">
              キャンペーン
            </h2>
            <div
              className="flex gap-4 overflow-x-auto campaign-scrollbar"
              data-testid="campaign-list"
            >
              {communityData.campaigns.map((campaign) => (
                <div
                  key={campaign.campaignId}
                  className="w-[calc(100vw-4rem)] max-w-xs flex-shrink-0"
                  data-testid={`campaign-item-${campaign.campaignId}`}
                >
                  <div className="relative aspect-square w-full overflow-hidden rounded-lg bg-gray-200">
                    <Image
                      src={campaign.imageUrl}
                      alt={campaign.title}
                      fill
                      className="object-cover"
                      sizes="(max-width: 640px) calc(100vw - 4rem), 320px"
                    />
                  </div>
                  <p className="mt-2 w-full truncate text-sm font-medium text-gray-700 dark:text-gray-300">
                    {campaign.title}
                  </p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* お知らせ領域 */}
        {communityData.news.length > 0 && (
          <div className="mb-8" data-testid="news-section">
            <h2 className="mb-4 text-lg font-bold text-gray-900 dark:text-zinc-50">
              お知らせ
            </h2>
            <ul className="divide-y divide-gray-200 dark:divide-zinc-700" data-testid="news-list">
              {communityData.news.map((newsItem) => (
                <li
                  key={newsItem.newsId}
                  className="py-3"
                  data-testid={`news-item-${newsItem.newsId}`}
                >
                  <p className="text-sm font-medium text-gray-900 dark:text-zinc-50">
                    {newsItem.title}
                  </p>
                  <p className="mt-1 text-xs text-gray-500">
                    {new Date(newsItem.publishedAt).toLocaleDateString("ja-JP")}
                  </p>
                </li>
              ))}
            </ul>
          </div>
        )}
      </main>
    </div>
  );
}
