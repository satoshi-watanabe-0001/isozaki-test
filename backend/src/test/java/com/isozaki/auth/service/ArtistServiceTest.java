/**
 * ArtistServiceの単体テスト
 *
 * <p>アーティスト一覧取得のビジネスロジックをテストする。
 * ArtistRepositoryはモックを使用する。</p>
 *
 * @since 1.1
 */

package com.isozaki.auth.service;

import com.isozaki.auth.dto.ArtistResponse;
import com.isozaki.auth.entity.ArtistEntity;
import com.isozaki.auth.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ArtistServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistService テスト")
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    private ArtistService artistService;

    @BeforeEach
    void setUp() {
        artistService = new ArtistService(artistRepository);
    }

    /**
     * テスト用のArtistEntityを生成するヘルパーメソッド
     */
    private ArtistEntity createArtistEntity(String id, String name, String nameKana, String iconUrl) {
        ArtistEntity entity = new ArtistEntity();
        entity.artistId = UUID.fromString(id);
        entity.name = name;
        entity.nameKana = nameKana;
        entity.iconUrl = iconUrl;
        entity.createdAt = Instant.now();
        entity.updatedAt = Instant.now();
        return entity;
    }

    /**
     * 【テスト対象】ArtistService#getAllArtists
     * 【テストケース】アーティストが複数存在する場合
     * 【期待結果】50音順にソートされたアーティストレスポンスのリストが返却される
     * 【ビジネス要件】アーティスト一覧取得 - 正常系
     */
    @Test
    @DisplayName("アーティストが複数存在する場合、DTOに変換されたリストが返されること")
    void shouldReturnArtistResponseList() {
        // Given: 50音順にソートされたアーティストエンティティ
        List<ArtistEntity> entities = List.of(
                createArtistEntity("01908b7e-2001-7000-8000-000000000001", "あいみょん", "あいみょん", "https://placehold.co/150x150?text=A"),
                createArtistEntity("01908b7e-2006-7000-8000-000000000006", "嵐", "あらし", "https://placehold.co/150x150?text=AR")
        );
        when(artistRepository.findAllOrderByNameKana()).thenReturn(entities);

        // When: 全アーティスト取得を実行
        List<ArtistResponse> result = artistService.getAllArtists();

        // Then: EntityからDTOに正しく変換された結果が返却される
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("01908b7e-2001-7000-8000-000000000001", result.get(0).artistId());
        assertEquals("あいみょん", result.get(0).name());
        assertEquals("あいみょん", result.get(0).nameKana());
        assertEquals("https://placehold.co/150x150?text=A", result.get(0).iconUrl());
        assertEquals("嵐", result.get(1).name());
        verify(artistRepository).findAllOrderByNameKana();
    }

    /**
     * 【テスト対象】ArtistService#getAllArtists
     * 【テストケース】アーティストが存在しない場合
     * 【期待結果】空のリストが返却される
     * 【ビジネス要件】アーティスト一覧取得 - データなし
     */
    @Test
    @DisplayName("アーティストが存在しない場合、空のリストが返されること")
    void shouldReturnEmptyListWhenNoArtists() {
        // Given: アーティストが0件
        when(artistRepository.findAllOrderByNameKana()).thenReturn(Collections.emptyList());

        // When: 全アーティスト取得を実行
        List<ArtistResponse> result = artistService.getAllArtists();

        // Then: 空のリストが返却される
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(artistRepository).findAllOrderByNameKana();
    }

    /**
     * 【テスト対象】ArtistService#getAllArtists
     * 【テストケース】アイコンURLがnullのアーティストが存在する場合
     * 【期待結果】iconUrlがnullのままDTOに変換される
     * 【ビジネス要件】アーティスト一覧取得 - アイコンなし
     */
    @Test
    @DisplayName("アイコンURLがnullの場合、nullのままDTOに変換されること")
    void shouldHandleNullIconUrl() {
        // Given: アイコンURLがnullのアーティスト
        List<ArtistEntity> entities = List.of(
                createArtistEntity("01908b7e-2001-7000-8000-000000000001", "テストアーティスト", "てすとあーてぃすと", null)
        );
        when(artistRepository.findAllOrderByNameKana()).thenReturn(entities);

        // When: 全アーティスト取得を実行
        List<ArtistResponse> result = artistService.getAllArtists();

        // Then: iconUrlがnullのままDTOに含まれる
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).iconUrl());
        verify(artistRepository).findAllOrderByNameKana();
    }
}
