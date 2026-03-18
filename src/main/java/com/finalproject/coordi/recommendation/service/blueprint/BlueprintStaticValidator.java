package com.finalproject.coordi.recommendation.service.blueprint;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import org.springframework.stereotype.Component;

@Component
public class BlueprintStaticValidator {
    public RawBlueprintDto validateRawBlueprint(RawBlueprintDto rawBlueprint) {
        if (rawBlueprint == null) {
            throw RecommendationException.blueprintResponseEmpty();
        }

        RawBlueprintDto.AiBlueprint aiBlueprint = rawBlueprint.aiBlueprint();
        if (aiBlueprint == null) {
            throw RecommendationException.blueprintRootMissing();
        }

        RawBlueprintDto.Coordination coordination = aiBlueprint.coordination();
        if (coordination == null) {
            throw RecommendationException.blueprintTypeMismatch();
        }

        validateSlot(coordination.tops(), CategoryType.TOPS);
        validateSlot(coordination.bottoms(), CategoryType.BOTTOMS);
        validateSlot(coordination.outerwear(), CategoryType.OUTERWEAR);
        validateSlot(coordination.shoes(), CategoryType.SHOES);
        validateSlot(coordination.accessories(), CategoryType.ACCESSORIES);
        return rawBlueprint;
    }

    private void validateSlot(RawBlueprintDto.ItemInfo item, CategoryType expectedCategoryType) {
        if (item == null || item.slotKey() != expectedCategoryType) {
            throw RecommendationException.blueprintTypeMismatch();
        }
    }
}

