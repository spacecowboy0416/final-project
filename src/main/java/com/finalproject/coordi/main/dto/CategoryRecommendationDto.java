package com.finalproject.coordi.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRecommendationDto {

    private String title;      // 상의, 하의, 아우터, 악세서리
    private String hint;       // 반팔티, 긴팔티/셔츠 ...
    private String icon;       // 아이콘 경로
    private boolean visible;   // 카드 노출 여부
}