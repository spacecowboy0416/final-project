package com.finalproject.coordi.recommendation.dto.internal;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import java.util.List;

/**
 * recommendation 내부에서 최종 coordination 출력이 어떤 메타와 아이템 목록을 가져야 하는지 제약하는 DTO.
 *
 * @param blueprintId recommendation이 최종 코디를 식별할 때 사용하는 내부/외부 식별자
 * @param tpoType 코디의 TPO 분류
 * @param styleType 코디의 스타일 분류
 * @param stylingRuleApplied 이번 recommendation 전체에 적용한 스타일링 규칙 요약
 * @param items 최종 recommendation에 포함되는 옷 목록
 */
public record CoordinationDto(
    String blueprintId,
    TpoType tpoType,
    StyleType styleType,
    String stylingRuleApplied,
    List<CoordinationItemDto> items
) {
}
