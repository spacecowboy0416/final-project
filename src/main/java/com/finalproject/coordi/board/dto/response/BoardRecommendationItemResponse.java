package com.finalproject.coordi.board.dto.response;

// 게시글 상세에서 보여줄 코디 아이템 응답 DTO
// recommendation_item + product + product_image_metadata 조인 결과
public record BoardRecommendationItemResponse(
        Long recItemId,
        String slotKey,      // tops / bottoms / outerwear / shoes / accessories
        String priority,     // essential / optional
        String productName,
        String brand,
        String imageUrl,
        String link,
        String color,
        String material,
        String fit,
        String style
) {
}