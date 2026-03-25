package com.finalproject.coordi.board.dto.response;

import java.time.LocalDateTime;

// 댓글 응답 DTO
public record BoardCommentResponse(
        Long commentId,
        Long postId,
        Long userId,
        String nickname,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean mine,
        boolean deleted
) {
}