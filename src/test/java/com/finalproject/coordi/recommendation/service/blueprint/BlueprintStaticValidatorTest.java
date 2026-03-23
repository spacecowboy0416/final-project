package com.finalproject.coordi.recommendation.service.blueprint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.BrandType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.SeasonType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class BlueprintStaticValidatorTest {
    private final BlueprintStaticValidator validator = new BlueprintStaticValidator();

    @Test
    void 정상Blueprint면_검증을통과한다() {
        RawBlueprintDto rawBlueprint = validRawBlueprint();

        RawBlueprintDto result = validator.validateRawBlueprint(rawBlueprint);

        assertSame(rawBlueprint, result);
    }

    @Test
    void rawBlueprint가Null이면_R121을던진다() {
        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> validator.validateRawBlueprint(null)
        );

        assertEquals(ErrorCode.RECOMMENDATION_BLUEPRINT_RESPONSE_EMPTY, exception.getErrorCode());
    }

    @Test
    void aiBlueprint가Null이면_R122를던진다() {
        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> new RawBlueprintDto(null)
        );

        assertEquals(ErrorCode.RECOMMENDATION_BLUEPRINT_ROOT_MISSING, exception.getErrorCode());
    }

    @Test
    void 필수슬롯이누락되면_R125를던진다() {
        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> new RawBlueprintDto.Coordination(
                validItem(CategoryType.HEADWEAR),
                null,
                validItem(CategoryType.BOTTOMS),
                validItem(CategoryType.OUTERWEAR),
                validItem(CategoryType.SHOES),
                validItem(CategoryType.ACCESSORIES)
            )
        );

        assertEquals(ErrorCode.RECOMMENDATION_BLUEPRINT_REQUIRED_SLOTS_MISSING, exception.getErrorCode());
    }

    @Test
    void 슬롯키가다르면_R123을던진다() {
        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> new RawBlueprintDto.Coordination(
                validItem(CategoryType.HEADWEAR),
                validItem(CategoryType.BOTTOMS),
                validItem(CategoryType.BOTTOMS),
                validItem(CategoryType.OUTERWEAR),
                validItem(CategoryType.SHOES),
                validItem(CategoryType.ACCESSORIES)
            )
        );

        assertEquals(ErrorCode.RECOMMENDATION_BLUEPRINT_TYPE_MISMATCH, exception.getErrorCode());
    }

    @Test
    void attributes가Null이면_R123을던진다() {
        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> new RawBlueprintDto.ItemInfo(
                CategoryType.TOPS,
                "아이템",
                ItemCategoryType.SHIRT,
                null,
                List.of(10, 20),
                "이유",
                PriorityType.ESSENTIAL
            )
        );

        assertEquals(ErrorCode.RECOMMENDATION_BLUEPRINT_TYPE_MISMATCH, exception.getErrorCode());
    }

    @Test
    void tempRange크기가2가아니면_R123을던진다() {
        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> new RawBlueprintDto.ItemInfo(
                CategoryType.TOPS,
                "아이템",
                ItemCategoryType.SHIRT,
                validAttributes(),
                List.of(10),
                "이유",
                PriorityType.ESSENTIAL
            )
        );

        assertEquals(ErrorCode.RECOMMENDATION_BLUEPRINT_TYPE_MISMATCH, exception.getErrorCode());
    }

    @Test
    void tempRange원소가Null이면_R123을던진다() {
        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> new RawBlueprintDto.ItemInfo(
                CategoryType.TOPS,
                "아이템",
                ItemCategoryType.SHIRT,
                validAttributes(),
                Arrays.asList(10, null),
                "이유",
                PriorityType.ESSENTIAL
            )
        );

        assertEquals(ErrorCode.RECOMMENDATION_BLUEPRINT_TYPE_MISMATCH, exception.getErrorCode());
    }

    @Test
    void aiExplanation이공백이면_R126을던진다() {
        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> validRawBlueprint(validCoordination(), " ")
        );

        assertEquals(ErrorCode.RECOMMENDATION_BLUEPRINT_AI_EXPLANATION_INVALID, exception.getErrorCode());
    }

    @Test
    void aiExplanation이한글이아니면_R126을던진다() {
        RecommendationException.ValidationException exception = assertThrows(
            RecommendationException.ValidationException.class,
            () -> validRawBlueprint(validCoordination(), "only english text")
        );

        assertEquals(ErrorCode.RECOMMENDATION_BLUEPRINT_AI_EXPLANATION_INVALID, exception.getErrorCode());
    }

    private RawBlueprintDto validRawBlueprint() {
        return validRawBlueprint(validCoordination(), "오늘 날씨와 분위기에 맞는 추천입니다.");
    }

    private RawBlueprintDto validRawBlueprint(RawBlueprintDto.Coordination coordination, String aiExplanation) {
        return new RawBlueprintDto(
            new RawBlueprintDto.AiBlueprint(
                GenderType.FEMALE,
                TpoType.DAILY,
                StyleType.CASUAL,
                CategoryType.TOPS,
                new RawBlueprintDto.MainItemAnalysis(
                    "mild",
                    SeasonType.SPRING,
                    ColorType.BLACK,
                    ItemCategoryType.SHIRT,
                    StyleType.CASUAL
                ),
                coordination,
                aiExplanation
            )
        );
    }

    private RawBlueprintDto.Coordination validCoordination() {
        return new RawBlueprintDto.Coordination(
            validItem(CategoryType.HEADWEAR),
            validItem(CategoryType.TOPS),
            validItem(CategoryType.BOTTOMS),
            validItem(CategoryType.OUTERWEAR),
            validItem(CategoryType.SHOES),
            validItem(CategoryType.ACCESSORIES)
        );
    }

    private RawBlueprintDto.ItemInfo validItem(CategoryType categoryType) {
        return new RawBlueprintDto.ItemInfo(
            categoryType,
            categoryType.getKeyword() + " 아이템",
            ItemCategoryType.SHIRT,
            validAttributes(),
            List.of(10, 20),
            "추천 이유",
            PriorityType.ESSENTIAL
        );
    }

    private RawBlueprintDto.Attributes validAttributes() {
        return new RawBlueprintDto.Attributes(
            GenderType.UNISEX,
            ColorType.BLACK,
            MaterialType.COTTON,
            FitType.STANDARD,
            BrandType.UNIQLO,
            StyleType.CASUAL
        );
    }
}
