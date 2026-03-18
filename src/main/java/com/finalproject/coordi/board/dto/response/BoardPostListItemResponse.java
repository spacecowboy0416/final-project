package com.finalproject.coordi.board.dto.response;

import java.time.LocalDateTime;

// 게시판 목록 1건 응답 DTO
// 목록에서는 대표 코디 정보와 필터용 정보만 포함
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
        Long weatherId,
        String weatherStatus,
        Double temp,
        String placeName,
        String topItemName,
        String bottomItemName,
        String thumbnailImageUrl
) {
}