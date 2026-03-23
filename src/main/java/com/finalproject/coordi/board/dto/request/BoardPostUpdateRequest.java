package com.finalproject.coordi.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 게시글 수정 요청 DTO
// recId는 수정 대상에서 제외하고 제목/내용/공개여부만 수정
public record BoardPostUpdateRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 5000) String content,
        @NotNull Boolean isPublic
) {
}