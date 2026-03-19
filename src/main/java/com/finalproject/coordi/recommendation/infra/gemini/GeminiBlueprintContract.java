package com.finalproject.coordi.recommendation.infra.gemini;

import com.finalproject.coordi.exception.recommendation.RecommendationException;
import com.finalproject.coordi.recommendation.domain.enums.CodedEnum;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
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
 * Gemini blueprint 출력 계약을 enum 기반 schema로 조립한다.
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

    private Schema buildOutputSchema() {
        return objectSchema(
            Map.of(
                SchemaKeys.AI_BLUEPRINT, aiBlueprintSchema()
            ),
            List.of(SchemaKeys.AI_BLUEPRINT)
        );
    }

    private Schema aiBlueprintSchema() {
        return objectSchema(
            Map.of(
                SchemaKeys.GENDER, enumSchema(GenderType.class),
                SchemaKeys.TPO_TYPE, enumSchema(TpoType.class),
                SchemaKeys.STYLE_TYPE, enumSchema(StyleType.class),
                SchemaKeys.MAIN_ITEM_ANALYSIS, mainItemAnalysisSchema(),
                SchemaKeys.COORDINATION, coordinationSchema(),
                SchemaKeys.STYLING_RULE_APPLIED, stringSchema(1L, null)
            ),
            List.of(
                SchemaKeys.GENDER,
                SchemaKeys.TPO_TYPE,
                SchemaKeys.STYLE_TYPE,
                SchemaKeys.MAIN_ITEM_ANALYSIS,
                SchemaKeys.COORDINATION,
                SchemaKeys.STYLING_RULE_APPLIED
            )
        );
    }

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
                SchemaKeys.TEMP,
                SchemaKeys.SEASON,
                SchemaKeys.COLOR,
                SchemaKeys.TYPE,
                SchemaKeys.STYLE
            )
        );
    }

    private Schema coordinationSchema() {
        return objectSchema(coordinationSlotProperties(), coordinationSlotRequired());
    }

    private Map<String, Schema> coordinationSlotProperties() {
        Map<String, Schema> properties = new LinkedHashMap<>();
        for (CategoryType categoryType : CategoryType.values()) {
            properties.put(categoryType.getCode(), coordinationSlotSchema());
        }
        return properties;
    }

    private List<String> coordinationSlotRequired() {
        return Arrays.stream(CategoryType.values())
            .map(CategoryType::getCode)
            .toList();
    }

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
                SchemaKeys.SLOT_KEY,
                SchemaKeys.ITEM_NAME,
                SchemaKeys.CATEGORY,
                SchemaKeys.ATTRIBUTES,
                SchemaKeys.TEMP_RANGE,
                SchemaKeys.REASONING,
                SchemaKeys.PRIORITY
            )
        );
    }

    private Schema attributesSchema() {
        return objectSchema(
            Map.of(
                SchemaKeys.COLOR, enumSchema(ColorType.class),
                SchemaKeys.MATERIAL, enumSchema(MaterialType.class),
                SchemaKeys.FIT, enumSchema(FitType.class),
                SchemaKeys.STYLE, enumSchema(StyleType.class)
            ),
            List.of(
                SchemaKeys.COLOR,
                SchemaKeys.MATERIAL,
                SchemaKeys.FIT,
                SchemaKeys.STYLE
            )
        );
    }

    private Schema objectSchema(Map<String, Schema> properties, List<String> required) {
        return Schema.builder()
            .type(new Type(Type.Known.OBJECT))
            .properties(new LinkedHashMap<>(properties))
            .required(required)
            .build();
    }

    private Schema stringSchema(Long minLength, Long maxLength) {
        Schema.Builder builder = Schema.builder().type(new Type(Type.Known.STRING));
        if (minLength != null) {
            builder.minLength(minLength);
        }
        if (maxLength != null) {
            builder.maxLength(maxLength);
        }
        return builder.build();
    }

    private Schema integerSchema() {
        return Schema.builder()
            .type(new Type(Type.Known.INTEGER))
            .build();
    }

    private Schema numberSchema() {
        return Schema.builder()
            .type(new Type(Type.Known.NUMBER))
            .build();
    }

    private Schema arraySchema(Schema items, Long minItems, Long maxItems) {
        Schema.Builder builder = Schema.builder()
            .type(new Type(Type.Known.ARRAY))
            .items(items);
        if (minItems != null) {
            builder.minItems(minItems);
        }
        if (maxItems != null) {
            builder.maxItems(maxItems);
        }
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

    private static final class SchemaKeys {
        private static final String AI_BLUEPRINT = "ai_blueprint";
        private static final String GENDER = "gender";
        private static final String TPO_TYPE = "tpoType";
        private static final String STYLE_TYPE = "styleType";
        private static final String MAIN_ITEM_ANALYSIS = "main_item_analysis";
        private static final String COORDINATION = "coordination";
        private static final String STYLING_RULE_APPLIED = "styling_rule_applied";
        private static final String TEMP = "temp";
        private static final String SEASON = "season";
        private static final String COLOR = "color";
        private static final String TYPE = "type";
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

        private SchemaKeys() {
        }
    }
}
