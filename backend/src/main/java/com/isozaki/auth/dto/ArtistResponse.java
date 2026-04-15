/**
 * アーティストレスポンスDTOクラス
 *
 * <p>アーティスト一覧APIのレスポンスとして使用するDTO。
 * Entityを直接APIレスポンスとして返さないためのデータ転送オブジェクト。</p>
 *
 * @since 1.1
 */

package com.isozaki.auth.dto;

/**
 * アーティスト情報のレスポンスデータ転送オブジェクト
 *
 * <p>アーティストID（英名）、アーティスト名、アイコンURLを含む。</p>
 *
 * @param artistId アーティストID（英名文字列、URLパスとして利用可能）
 * @param name     アーティスト名
 * @param nameKana ソート用読み仮名（ひらがな）
 * @param iconUrl  アイコン画像のURL
 * @since 1.1
 */
public record ArtistResponse(
        String artistId,
        String name,
        String nameKana,
        String iconUrl
) {
}
