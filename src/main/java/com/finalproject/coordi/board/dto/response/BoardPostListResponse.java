package com.finalproject.coordi.board.dto.response;

import java.util.List;

// 게시판 목록 응답 DTO
// 페이징 정보와 게시글 목록을 같이 내려준다.
public record BoardPostListResponse(
        List<BoardPostListItemResponse> posts,
        int page,
        int size,
        boolean hasNext
) {
}