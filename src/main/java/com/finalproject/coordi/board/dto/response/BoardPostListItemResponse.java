package com.finalproject.coordi.board.dto.response;

import java.time.LocalDateTime;
import java.util.List;

// 게시판 목록 1건 응답 DTO
// 목록에서는 게시글 기본 정보 + 미리보기 아이템 목록을 포함한다.
public record BoardPostListItemResponse(
        Long postId,
        Long userId,
        String nickname,
        String title,
        String contentPreview,
        boolean isPublic,
        int viewCount,
        int commentCount,
        LocalDateTime createdAt,
        Long recId,
        String styleType,
        String tpoType,
        String weatherStatus,
        List<BoardPostPreviewItemResponse> previewItems,
        int extraItemCount,
        boolean mine,
        boolean edited
) {
}