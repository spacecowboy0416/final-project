package com.finalproject.coordi.board.dto.response;

// 게시판 목록 카드에서 보여줄 미리보기 아이템 DTO
public record BoardPostPreviewItemResponse(
        String slotKey,
        String productName,
        String imageUrl
) {
}