package com.finalproject.coordi.board.vo;

import lombok.Getter;
import lombok.Setter;

// 게시글 상세에서 recommendation_item 조회 결과를 담는 VO
@Getter
@Setter
public class BoardRecommendationItemRow {
    private Long recItemId;
    private String slotKey;
    private String priority;
    private String productName;
    private String brand;
    private String imageUrl;
    private String link;
    private String color;
    private String material;
    private String fit;
    private String style;
}