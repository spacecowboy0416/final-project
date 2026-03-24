package com.finalproject.coordi.recommendation.dto.internal;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.PriorityType;
import java.util.List;

/**
 * recommendation 내부에서 최종 coordination 출력이 어떤 메타와 아이템 목록을 가져야 하는지 제약하는 DTO.
 *
 * @param blueprintId recommendation이 최종 코디를 식별할 때 사용하는 내부/외부 식별자
 * @param tpoType 코디의 TPO 분류
 * @param styleType 코디의 스타일 분류
 * @param aiExplanation 이번 recommendation 전체가 도출된 이유 요약
 * @param items 최종 recommendation에 포함되는 옷 목록
 */
public record CoordinationDto(
    String blueprintId,
    TpoType tpoType,
    StyleType styleType,
    String aiExplanation,
    List<CoordinationItem> items
) {
    /**
     * recommendation 내부에서 최종 coordination에 포함될 각 옷이 어떤 정보를 가져야 하는지 제약하는 DTO.
     *
     * @param slotKey coordination에서 이 옷이 배치되는 슬롯. 예: tops, shoes
     * @param itemName 사용자에게 노출할 옷 이름
     * @param imageUrl S3에 저장된 대표 이미지 URL
     * @param category 옷 자체의 카테고리 분류
     * @param matchScore 최종 종합 점수
     * @param tempMin 이 옷이 적합한 최소 온도
     * @param tempMax 이 옷이 적합한 최대 온도
     * @param priority 코디 안에서의 우선순위
     * @param reasoning 이 옷을 선택한 이유
     * @param color 옷 색상
     * @param material 옷 소재
     * @param fit 옷 핏
     * @param style 옷 스타일
     */
    public record CoordinationItem(
        CategoryType slotKey,
        String itemName,
        String imageUrl,
        ItemCategoryType category,
        double matchScore,
        Integer tempMin,
        Integer tempMax,
        PriorityType priority,
        String reasoning,
        ColorType color,
        MaterialType material,
        FitType fit,
        StyleType style
    ) {
    }
}
