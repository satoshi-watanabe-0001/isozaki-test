/**
 * コミュニティTOPページ関連の型定義
 *
 * バックエンドAPIから返却されるコミュニティTOPデータの型定義。
 * アーティスト情報、カルーセル画像、キャンペーン、お知らせを含む。
 *
 * @since 1.2
 */

/** カルーセル表示用アーティスト画像 */
export interface ArtistImage {
  /** 画像ID */
  imageId: number;
  /** 画像URL */
  imageUrl: string;
  /** 表示順 */
  displayOrder: number;
}

/** キャンペーン情報 */
export interface Campaign {
  /** キャンペーンID */
  campaignId: number;
  /** キャンペーンタイトル */
  title: string;
  /** キャンペーン画像URL */
  imageUrl: string;
}

/** お知らせ情報 */
export interface News {
  /** お知らせID */
  newsId: number;
  /** お知らせタイトル */
  title: string;
  /** 公開日時（ISO 8601文字列） */
  publishedAt: string;
}

/** コミュニティTOPページのレスポンスデータ */
export interface CommunityTop {
  /** アーティストID（英名文字列） */
  artistId: string;
  /** アーティスト名 */
  name: string;
  /** カルーセル表示用画像リスト（最大3件） */
  images: ArtistImage[];
  /** キャンペーンリスト（最大3件） */
  campaigns: Campaign[];
  /** お知らせリスト（最大5件、新着順） */
  news: News[];
}

/** メニュー項目の定義 */
export interface MenuItem {
  /** メニュー名 */
  label: string;
  /** メニューアイコン（絵文字またはアイコン名） */
  icon: string;
}
