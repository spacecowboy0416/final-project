package com.finalproject.coordi.recommendation.service.blueprint;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class BlueprintStaticValidator {
    private static final String AI_EXPLANATION_ALLOWED_PATTERN = "^[\\p{IsHangul}\\d\\s.,!?~()/%+\\-:'\"\\[\\]]+$";

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

        validateOptionalSlot(coordination.headwear(), CategoryType.HEADWEAR);
        validateSlot(coordination.tops(), CategoryType.TOPS);
        validateSlot(coordination.bottoms(), CategoryType.BOTTOMS);
        validateSlot(coordination.outerwear(), CategoryType.OUTERWEAR);
        validateSlot(coordination.shoes(), CategoryType.SHOES);
        validateOptionalSlot(coordination.accessories(), CategoryType.ACCESSORIES);
        validateAiExplanation(aiBlueprint.aiExplanation());
        return rawBlueprint;
    }

    private void validateOptionalSlot(RawBlueprintDto.ItemInfo item, CategoryType expectedCategoryType) {
        if (item == null) {
            return;
        }
        validateSlot(item, expectedCategoryType);
    }

    private void validateSlot(RawBlueprintDto.ItemInfo item, CategoryType expectedCategoryType) {
        if (item == null || item.slotKey() != expectedCategoryType) {
            throw RecommendationException.blueprintTypeMismatch();
        }
    }

    private void validateAiExplanation(String aiExplanation) {
        if (!StringUtils.hasText(aiExplanation)) {
            throw RecommendationException.blueprintAiExplanationInvalid();
        }

        String normalizedAiExplanation = aiExplanation.trim();
        if (!containsHangul(normalizedAiExplanation)
            || !normalizedAiExplanation.matches(AI_EXPLANATION_ALLOWED_PATTERN)) {
            throw RecommendationException.blueprintAiExplanationInvalid();
        }
    }

    private boolean containsHangul(String value) {
        return value != null && value.codePoints().anyMatch(this::isHangulCodePoint);
    }

    private boolean isHangulCodePoint(int codePoint) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(codePoint);
        return unicodeBlock == Character.UnicodeBlock.HANGUL_SYLLABLES
            || unicodeBlock == Character.UnicodeBlock.HANGUL_JAMO
            || unicodeBlock == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
            || unicodeBlock == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_A
            || unicodeBlock == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B;
    }
}
