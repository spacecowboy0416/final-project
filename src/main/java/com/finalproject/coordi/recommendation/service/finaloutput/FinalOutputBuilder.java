package com.finalproject.coordi.recommendation.service.finaloutput;

import com.finalproject.coordi.recommendation.dto.api.CoordinationOutputDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import com.finalproject.coordi.recommendation.service.coordination.CoordinationStage.CoordinationStageResult;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * 최종 API 응답 DTO를 조립한다.
 * 현재는 Product Search 리팩토링 단계이므로 코디 아이템은 빈 목록으로 반환한다.
 */
@Component
public class FinalOutputBuilder {
    private static final String EMPTY_STYLING_RULE = "";

    public CoordinationOutputDto build(
        NormalizedBlueprintDto normalizedBlueprint,
        CoordinationStageResult coordinationResult
    ) {
        RawBlueprintDto.AiBlueprint aiBlueprint = normalizedBlueprint == null ? null : normalizedBlueprint.aiBlueprint();
        String blueprintId = UUID.randomUUID().toString();
        return new CoordinationOutputDto(
            blueprintId,
            aiBlueprint == null ? null : aiBlueprint.tpoType(),
            aiBlueprint == null ? null : aiBlueprint.styleType(),
            aiBlueprint == null ? EMPTY_STYLING_RULE : aiBlueprint.stylingRuleApplied(),
            List.of()
        );
    }
}
