package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.BlueprintOutputDto;
import com.finalproject.coordi.recommendation.dto.internal.BlueprintValidationDto;
import com.finalproject.coordi.domain.exception.RecommendationException;
import java.util.Arrays;
import java.util.EnumMap;
import org.springframework.stereotype.Component;

@Component
public class BlueprintValidator {
    /**
     * Gemini가 반환한 blueprint 출력 DTO를 recommendation 파이프라인에서 계속 사용할 수 있게
     * 최소 구조를 내부 계약 모델로 변환해 다음 단계로 넘긴다.
     */
    public BlueprintValidationDto.ValidatedBlueprint validate(BlueprintOutputDto rawBlueprintDto) {
        if (rawBlueprintDto == null) {
            throw RecommendationException.blueprintResponseEmpty();
        }

        BlueprintOutputDto.AiBlueprint aiBlueprint = rawBlueprintDto.aiBlueprint();
        if (aiBlueprint == null) {
            throw RecommendationException.blueprintRootMissing();
        }

        BlueprintOutputDto.Coordination coordination = aiBlueprint.coordination();
        EnumMap<CategoryType, BlueprintValidationDto.Slot> slotsByCategory = new EnumMap<>(CategoryType.class);
        Arrays.stream(CategoryType.values())
            .forEach(categoryType -> slotsByCategory.put(
                categoryType,
                new BlueprintValidationDto.Slot(categoryType, resolveSlot(coordination, categoryType))
            ));

        return new BlueprintValidationDto.ValidatedBlueprint(rawBlueprintDto, aiBlueprint, slotsByCategory);
    }

    private BlueprintOutputDto.CoordinationSlot resolveSlot(
        BlueprintOutputDto.Coordination coordination,
        CategoryType categoryType
    ) {
        if (coordination == null) {
            return null;
        }
        return switch (categoryType) {
            case TOPS -> coordination.tops();
            case BOTTOMS -> coordination.bottoms();
            case OUTERWEAR -> coordination.outerwear();
            case SHOES -> coordination.shoes();
            case ACCESSORIES -> coordination.accessories();
        };
    }
}
