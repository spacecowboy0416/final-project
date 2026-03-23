package com.finalproject.coordi.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 댓글 작성 요청 DTO
public record BoardCommentCreateRequest(
        @NotBlank @Size(max = 500) String content
) {
}