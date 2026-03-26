package com.finalproject.coordi.board.dto.response;

import java.time.LocalDateTime;
import java.util.List;

// 게시글 상세 응답 DTO
// 게시글 + 날씨 + recommendation 메타 + 코디 아이템 + 댓글 목록 포함
public record BoardPostDetailResponse(
        Long postId,
        Long userId,
        String nickname,
        String title,
        String content,
        boolean isPublic,
        int viewCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long recId,
        String styleType,
        String tpoType,
        String aiExplanation,
        String weatherStatus,
        
        boolean edited, 
        boolean mine,
        List<BoardRecommendationItemResponse> items,
        List<BoardCommentResponse> comments
) {
}