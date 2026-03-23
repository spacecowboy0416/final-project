package com.finalproject.coordi.board.dto.response;

import java.util.List;

// 게시글 작성 화면에서 "불러올 저장 코디 목록" 응답 DTO
public record BoardSavedCoordiResponse(
        Long recId,
        String weather,
        String style,
        String tpo,
        String aiExplanation,
        List<BoardSavedCoordiItemResponse> items
) {
}