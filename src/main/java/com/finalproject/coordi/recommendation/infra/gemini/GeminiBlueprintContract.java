package com.finalproject.coordi.recommendation.infra.gemini;

import com.finalproject.coordi.exception.recommendation.RecommendationException;
import com.finalproject.coordi.recommendation.domain.enums.CodedEnum;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.*;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.SeasonType;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Gemini Blueprint 출력 계약을 계층형 Fragment 구조로 조립한다.
 */
@Component
public class GeminiBlueprintContract {
    public static final String RESPONSE_MIME_TYPE = "application/json";
    private Schema outputSchema;

    @PostConstruct
    public void initialize() {
        try {
            this.outputSchema = buildOutputSchema();
        } catch (Exception exception) {
            throw RecommendationException.outputSchemaBuildFailed(exception);
        }
    }

    public Schema getOutputSchema() {
        return outputSchema;
    }

    public String responseMimeType() {
        return RESPONSE_MIME_TYPE;
    }

    // ==========================================
    // 1. Root Schema (최상위 조립)
    // ==========================================

    private Schema buildOutputSchema() {
        return objectSchema(
            Map.of(SchemaKeys.AI_BLUEPRINT, aiBlueprintSchema()),
            List.of(SchemaKeys.AI_BLUEPRINT)
        );
    }

    private Schema aiBlueprintSchema() {
        return objectSchema(
            Map.of(
                SchemaKeys.AI_EXPLANATION, stringSchema(1L, null),
                SchemaKeys.GENDER, enumSchema(GenderType.class),
                SchemaKeys.TPO_TYPE, enumSchema(TpoType.class),
                SchemaKeys.STYLE_TYPE, enumSchema(StyleType.class),
                SchemaKeys.ANCHOR_SLOT, enumSchema(CategoryType.class),
                SchemaKeys.MAIN_ITEM_ANALYSIS, mainItemAnalysisSchema(),
                SchemaKeys.COORDINATION, coordinationSchema()
            ),
            List.of(
                SchemaKeys.GENDER, SchemaKeys.TPO_TYPE, SchemaKeys.STYLE_TYPE,
                SchemaKeys.ANCHOR_SLOT, SchemaKeys.MAIN_ITEM_ANALYSIS, SchemaKeys.COORDINATION,
                SchemaKeys.AI_EXPLANATION
            )
        );
    }

    // ==========================================
    // 2. Intermediate Fragments (중간 부품들)
    // ==========================================

    /**
     * 사용자가 업로드한 메인 아이템에 대한 분석 결과 스키마
     */
    private Schema mainItemAnalysisSchema() {
        return objectSchema(
            Map.of(
                SchemaKeys.TEMP, stringSchema(1L, null),
                SchemaKeys.SEASON, enumSchema(SeasonType.class),
                SchemaKeys.COLOR, enumSchema(ColorType.class),
                SchemaKeys.TYPE, enumSchema(ItemCategoryType.class),
                SchemaKeys.STYLE, enumSchema(StyleType.class)
            ),
            List.of(
                SchemaKeys.TEMP, SchemaKeys.SEASON, SchemaKeys.COLOR,
                SchemaKeys.TYPE, SchemaKeys.STYLE
            )
        );
    }

    /**
     * 전체 코디네이션 슬롯 스키마 (CategoryType의 모든 값을 동적으로 키로 생성)
     */
    private Schema coordinationSchema() {
        Map<String, Schema> properties = new LinkedHashMap<>();
        for (CategoryType categoryType : CategoryType.values()) {
            properties.put(categoryType.getCode(), coordinationSlotSchema());
        }

        List<String> required = Arrays.stream(CategoryType.values())
            .map(CategoryType::getCode)
            .toList();

        return objectSchema(properties, required);
    }

    /**
     * 개별 코디 슬롯(상의, 하의 등)의 상세 스키마
     */
    private Schema coordinationSlotSchema() {
        return objectSchema(
            Map.of(
                SchemaKeys.SLOT_KEY, enumSchema(CategoryType.class),
                SchemaKeys.ITEM_NAME, stringSchema(1L, null),
                SchemaKeys.CATEGORY, enumSchema(ItemCategoryType.class),
                SchemaKeys.ATTRIBUTES, attributesSchema(),
                SchemaKeys.TEMP_RANGE, arraySchema(integerSchema(), 2L, 2L),
                SchemaKeys.REASONING, stringSchema(1L, null),
                SchemaKeys.PRIORITY, enumSchema(PriorityType.class)
            ),
            List.of(
                SchemaKeys.SLOT_KEY, SchemaKeys.ITEM_NAME, SchemaKeys.CATEGORY,
                SchemaKeys.ATTRIBUTES, SchemaKeys.TEMP_RANGE, SchemaKeys.REASONING, SchemaKeys.PRIORITY
            )
        );
    }

    /**
     * 아이템의 세부 속성(색상, 소재, 핏 등) 스키마
     */
    private Schema attributesSchema() {
        Map<String, Schema> properties = new LinkedHashMap<>();
        properties.put(SchemaKeys.GENDER, enumSchema(GenderType.class));
        properties.put(SchemaKeys.COLOR, enumSchema(ColorType.class));
        properties.put(SchemaKeys.MATERIAL, enumSchema(MaterialType.class));
        properties.put(SchemaKeys.FIT, enumSchema(FitType.class));
        properties.put(SchemaKeys.BRAND, enumSchema(BrandType.class));
        properties.put(SchemaKeys.PATTERN, enumSchema(PatternType.class));
        properties.put(SchemaKeys.STYLE, enumSchema(StyleType.class));

        return objectSchema(
            properties,
            List.of(
                SchemaKeys.COLOR, SchemaKeys.MATERIAL, SchemaKeys.FIT, SchemaKeys.STYLE
            )
        );
    }

    // ==========================================
    // 3. Schema Helpers (스키마 생성 도구)
    // ==========================================

    private Schema objectSchema(Map<String, Schema> properties, List<String> required) {
        return Schema.builder()
            .type(new Type(Type.Known.OBJECT))
            .properties(new LinkedHashMap<>(properties))
            .required(required)
            .build();
    }

    private Schema stringSchema(Long minLength, Long maxLength) {
        Schema.Builder builder = Schema.builder().type(new Type(Type.Known.STRING));
        if (minLength != null) builder.minLength(minLength);
        if (maxLength != null) builder.maxLength(maxLength);
        return builder.build();
    }

    private Schema integerSchema() {
        return Schema.builder().type(new Type(Type.Known.INTEGER)).build();
    }

    private Schema arraySchema(Schema items, Long minItems, Long maxItems) {
        Schema.Builder builder = Schema.builder().type(new Type(Type.Known.ARRAY)).items(items);
        if (minItems != null) builder.minItems(minItems);
        if (maxItems != null) builder.maxItems(maxItems);
        return builder.build();
    }

    private Schema enumSchema(Class<? extends CodedEnum> enumType) {
        List<String> codes = Arrays.stream(enumType.getEnumConstants())
            .map(CodedEnum::getCode)
            .collect(Collectors.toList());
        return Schema.builder()
            .type(new Type(Type.Known.STRING))
            .enum_(codes)
            .build();
    }

    // ==========================================
    // 4. Schema Keys (상수 관리)
    // ==========================================

    private static final class SchemaKeys {
        private static final String AI_BLUEPRINT = "ai_blueprint";
        private static final String GENDER = "gender";
        private static final String TPO_TYPE = "tpoType";
        private static final String STYLE_TYPE = "styleType";
        private static final String MAIN_ITEM_ANALYSIS = "main_item_analysis";
        private static final String COORDINATION = "coordination";
        private static final String AI_EXPLANATION = "ai_explanation";
        private static final String ANCHOR_SLOT = "anchor_slot";
        private static final String TEMP = "temp";
        private static final String SEASON = "season";
        private static final String COLOR = "color";
        private static final String TYPE = "type";
        private static final String BRAND = "brand";
        private static final String PATTERN = "pattern";
        private static final String STYLE = "style";
        private static final String SLOT_KEY = "slot_key";
        private static final String ITEM_NAME = "item_name";
        private static final String CATEGORY = "category";
        private static final String ATTRIBUTES = "attributes";
        private static final String MATERIAL = "material";
        private static final String FIT = "fit";
        private static final String TEMP_RANGE = "temp_range";
        private static final String REASONING = "reasoning";
        private static final String PRIORITY = "priority";

        private SchemaKeys() {}
    }
}
