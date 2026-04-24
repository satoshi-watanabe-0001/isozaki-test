/**
 * スレッド機能関連の型定義
 *
 * バックエンドAPIから返却されるスレッド・コメントデータの型定義。
 *
 * @since 1.3
 */

/** コメント画像情報 */
export interface CommentImage {
  /** 画像ID（UUIDv7文字列） */
  imageId: string;
  /** サムネイル画像URL（400px幅WebP） */
  thumbnailUrl: string;
  /** 表示用画像URL（1200px幅WebP） */
  displayUrl: string;
}

/** スレッド一覧アイテム */
export interface ThreadListItem {
  /** スレッドID（UUIDv7文字列） */
  threadId: string;
  /** スレッドタイトル */
  title: string;
  /** スレッド作成ユーザ名 */
  createdByUsername: string;
  /** 最新コメント内容（存在しない場合はnull） */
  latestComment: string | null;
  /** 最新コメント日時（ISO 8601文字列、存在しない場合はnull） */
  latestCommentAt: string | null;
}

/** スレッド一覧レスポンス */
export interface ThreadListResponse {
  /** スレッド一覧 */
  threads: ThreadListItem[];
  /** 総スレッド数 */
  totalCount: number;
  /** 現在のページ番号（1始まり） */
  page: number;
  /** 1ページあたりの件数 */
  size: number;
  /** 総ページ数 */
  totalPages: number;
}

/** スレッドコメント */
export interface ThreadComment {
  /** コメントID（UUIDv7文字列） */
  commentId: string;
  /** コメント内容 */
  content: string;
  /** コメント作成ユーザ名 */
  createdByUsername: string;
  /** コメント作成日時（ISO 8601文字列） */
  createdAt: string;
  /** コメントに紐づく画像リスト */
  images: CommentImage[];
}

/** スレッド詳細レスポンス */
export interface ThreadDetailResponse {
  /** スレッドID（UUIDv7文字列） */
  threadId: string;
  /** スレッドタイトル */
  title: string;
  /** スレッド作成ユーザ名 */
  createdByUsername: string;
  /** スレッド作成日時（ISO 8601文字列） */
  createdAt: string;
  /** コメント一覧 */
  comments: ThreadComment[];
  /** 総コメント数 */
  totalComments: number;
  /** 現在のページ番号（1始まり） */
  page: number;
  /** 1ページあたりの件数 */
  size: number;
  /** 総ページ数 */
  totalPages: number;
}

/** Pre-signed URLレスポンスの個別画像情報 */
export interface UploadUrlItem {
  /** 画像ID（UUIDv7文字列） */
  imageId: string;
  /** Pre-signed URL（PUT用） */
  uploadUrl: string;
  /** S3オブジェクトキー */
  s3Key: string;
}

/** Pre-signed URLレスポンス */
export interface UploadUrlResponse {
  /** Pre-signed URL情報のリスト */
  uploads: UploadUrlItem[];
}
