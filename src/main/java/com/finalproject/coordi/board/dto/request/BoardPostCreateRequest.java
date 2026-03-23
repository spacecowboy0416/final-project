package com.finalproject.coordi.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 게시글 작성 요청 DTO
// 사용자가 저장한 recommendation의 recId를 전달해서 게시글 생성
public record BoardPostCreateRequest(
        @NotNull Long recId,                    // 공유할 저장 코디 ID
        @NotBlank @Size(max = 120) String title, // 게시글 제목
        @Size(max = 5000) String content,        // 게시글 본문
        @NotNull Boolean isPublic                // 공개 여부
) {
}