package com.finalproject.coordi.recommendation.service.blueprint;

import com.finalproject.coordi.recommendation.config.annotation.LogStage;
import com.finalproject.coordi.recommendation.dto.api.PayloadDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.internal.NormalizedBlueprintDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlueprintDebugStage {
    private final AiPort aiPort;
    private final BlueprintStaticValidator blueprintStaticValidator;
    private final BlueprintNormalizer blueprintNormalizer;

    @LogStage("blueprint.generateDebugBlueprint")
    public DebugBlueprintResult generateValidatedDebugBlueprint(PayloadDto payload) {
        RawBlueprintDto rawBlueprint = aiPort.generateBlueprint(payload);
        RawBlueprintDto validatedRawBlueprint = blueprintStaticValidator.validateRawBlueprint(rawBlueprint);
        NormalizedBlueprintDto validatedBlueprint = blueprintNormalizer.normalize(validatedRawBlueprint);
        return new DebugBlueprintResult(rawBlueprint, validatedBlueprint);
    }

    public record DebugBlueprintResult(
        RawBlueprintDto rawBlueprint,
        NormalizedBlueprintDto validatedBlueprint
    ) {
    }
}
