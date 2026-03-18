package com.finalproject.coordi.recommendation.service.blueprint;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.dto.api.PayloadDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * blueprint 생성 단계의 내부 세부 작업을 한곳에 묶는다.
 *
 */
@Component
@RequiredArgsConstructor
public class BlueprintStage {
    private final AiPort aiPort;
    private final BlueprintStaticValidator blueprintStaticValidator;
    private final BlueprintNormalizer blueprintNormalizer;

    @LogStage("blueprint.generateNormalizedBlueprint")
    public BlueprintStageResult generate(PayloadDto payload) {
        RawBlueprintDto rawBlueprint = aiPort.generateBlueprint(payload);
        var validatedBlueprint = blueprintStaticValidator.validateRawBlueprint(rawBlueprint);
        var normalizedBlueprint = blueprintNormalizer.normalize(validatedBlueprint);
        return new BlueprintStageResult(rawBlueprint, normalizedBlueprint);
    }

    public record BlueprintStageResult(
        RawBlueprintDto rawBlueprint,
        NormalizedBlueprintDto normalizedBlueprint
    ) {
    }
}
