/**
 * アーティスト型定義
 *
 * バックエンドAPIから取得するアーティスト情報の型定義。
 *
 * @since 1.1
 */

/**
 * アーティスト情報の型
 *
 * @property artistId - アーティストID（UUIDv7）
 * @property name - アーティスト名
 * @property nameKana - ソート用読み仮名（ひらがな）
 * @property iconUrl - アイコン画像のURL
 */
export interface Artist {
  artistId: string;
  name: string;
  nameKana: string;
  iconUrl: string | null;
}
