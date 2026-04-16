package com.isozaki.auth.dto;

import java.util.List;

/**
 * スレッド一覧ページングレスポンスDTO
 *
 * <p>スレッド一覧をページング情報とともに返却するDTO。</p>
 *
 * @param threads    スレッド一覧
 * @param totalCount 総スレッド数
 * @param page       現在のページ番号（1始まり）
 * @param size       1ページあたりの件数
 * @param totalPages 総ページ数
 * @since 1.3
 */
public record ThreadListResponse(
        List<ThreadListItemResponse> threads,
        long totalCount,
        int page,
        int size,
        int totalPages
) {
}
