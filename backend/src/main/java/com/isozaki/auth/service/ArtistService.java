/**
 * アーティストサービスクラス
 *
 * <p>アーティスト情報の取得に関するビジネスロジックを担当するサービス。
 * リポジトリからアーティストデータを取得し、DTOに変換して返却する。</p>
 *
 * @since 1.1
 */

package com.isozaki.auth.service;

import com.isozaki.auth.dto.ArtistResponse;
import com.isozaki.auth.entity.ArtistEntity;
import com.isozaki.auth.repository.ArtistRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/**
 * アーティスト情報の取得ロジックを提供するサービス
 *
 * <p>アーティストリポジトリからデータを取得し、
 * APIレスポンス用のDTOに変換する。</p>
 *
 * @since 1.1
 */
@ApplicationScoped
public class ArtistService {

    private final ArtistRepository artistRepository;

    /**
     * アーティストリポジトリを注入してサービスを初期化する
     *
     * @param artistRepository アーティストリポジトリ
     */
    @Inject
    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    /**
     * 全アーティストを50音順で取得する
     *
     * <p>読み仮名（nameKana）の昇順でソートされたアーティスト一覧を返却する。
     * EntityをDTOに変換してAPIレスポンスに適した形式で返す。</p>
     *
     * @return 50音順にソートされたアーティストレスポンスのリスト
     */
    public List<ArtistResponse> getAllArtists() {
        List<ArtistEntity> artists = artistRepository.findAllOrderByNameKana();
        return artists.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * アーティストエンティティをレスポンスDTOに変換する
     *
     * @param entity 変換対象のアーティストエンティティ
     * @return 変換後のアーティストレスポンスDTO
     */
    private ArtistResponse toResponse(ArtistEntity entity) {
        return new ArtistResponse(
                entity.artistId.toString(),
                entity.name,
                entity.nameKana,
                entity.iconUrl
        );
    }
}
