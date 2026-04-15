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
 * @property artistId - アーティストID（英名文字列、URLパスとして利用可能）
 * @property name - アーティスト名
 * @property nameKana - ソート用読み仮名（ひらがな）
 * @property iconUrl - アイコン画像のURL（フロントエンド静的ファイルのパス）
 */
export interface Artist {
  artistId: string;
  name: string;
  nameKana: string;
  iconUrl: string | null;
}
