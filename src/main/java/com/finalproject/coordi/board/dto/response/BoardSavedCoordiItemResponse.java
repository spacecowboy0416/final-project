package com.finalproject.coordi.board.dto.response;

// 글쓰기에서 저장 코디 미리보기용 아이템 DTO
public record BoardSavedCoordiItemResponse(
        String slotKey,
        String label,
        String itemName,
        String imageUrl
) {
}