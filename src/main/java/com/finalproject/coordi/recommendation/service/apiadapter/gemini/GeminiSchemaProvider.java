package com.finalproject.coordi.recommendation.service.apiadapter.gemini;

import com.finalproject.coordi.exception.recommendation.RecommendationException;
import com.finalproject.coordi.recommendation.domain.enums.CodedEnum;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ColorType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.FitType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.ItemCategoryType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.PriorityType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.StyleType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.TpoType;
import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.MaterialType;
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
 * Gemini structured output용 input/output schema를 수동으로 생성/보관한다.
 */
@Component
public class GeminiSchemaProvider {
    private Schema inputSchema;
    private Schema outputSchema;

    @PostConstruct
    public void initialize() {
        try {
            this.inputSchema = buildInputSchema();
            this.outputSchema = buildOutputSchema();
        } catch (Exception exception) {
            throw RecommendationException.outputSchemaBuildFailed(exception);
        }
    }

    public Schema getInputSchema() {
        return inputSchema;
    }

    public Schema getOutputSchema() {
        return outputSchema;
    }

    private Schema buildInputSchema() {
        return objectSchema(
            Map.of(
                "naturalText", stringSchema(1L, 1000L),
                "scheduleTime", stringSchema(1L, null),
                "weather", objectSchema(
                    Map.of(
                        "temperature", numberSchema(),
                        "feelsLike", numberSchema(),
                        "weatherStatus", stringSchema(1L, null),
                        "rainProbability", stringSchema(1L, null),
                        "weatherSource", stringSchema(1L, null)
                    ),
                    List.of("temperature", "feelsLike", "weatherStatus", "rainProbability", "weatherSource")
                ),
                "imageData", objectSchema(
                    Map.of(
                        "mimeType", stringSchema(1L, null)
                    ),
                    List.of("mimeType")
                )
            ),
            List.of("naturalText", "scheduleTime", "weather", "imageData")
        );
    }

    private Schema buildOutputSchema() {
        return objectSchema(
            Map.of(
                "ai_blueprint", objectSchema(
                    Map.of(
                        "tpoType", enumSchema(TpoType.class),
                        "styleType", enumSchema(StyleType.class),
                        "main_item_analysis", objectSchema(
                            Map.of(
                                "temp", stringSchema(1L, null),
                                "season", enumSchema(SeasonType.class),
                                "color", enumSchema(ColorType.class),
                                "type", enumSchema(ItemCategoryType.class),
                                "style", enumSchema(StyleType.class)
                            ),
                            List.of("temp", "season", "color", "type", "style")
                        ),
                        "coordination", objectSchema(
                            Map.of(
                                "tops", coordinationSlotSchema(),
                                "bottoms", coordinationSlotSchema(),
                                "outerwear", coordinationSlotSchema(),
                                "shoes", coordinationSlotSchema(),
                                "accessories", coordinationSlotSchema()
                            ),
                            List.of("tops", "bottoms", "outerwear", "shoes", "accessories")
                        ),
                        "styling_rule_applied", stringSchema(1L, null)
                    ),
                    List.of("tpoType", "styleType", "main_item_analysis", "coordination", "styling_rule_applied")
                )
            ),
            List.of("ai_blueprint")
        );
    }

    private Schema coordinationSlotSchema() {
        return objectSchema(
            Map.of(
                "item_name", stringSchema(1L, null),
                "search_query", stringSchema(1L, null),
                "category", enumSchema(ItemCategoryType.class),
                "attributes", objectSchema(
                    Map.of(
                        "color", enumSchema(ColorType.class),
                        "material", enumSchema(MaterialType.class),
                        "fit", enumSchema(FitType.class),
                        "style", enumSchema(StyleType.class)
                    ),
                    List.of("color", "material", "fit", "style")
                ),
                "temp_range", arraySchema(integerSchema(), 2L, 2L),
                "reasoning", stringSchema(1L, null),
                "priority", enumSchema(PriorityType.class)
            ),
            List.of("item_name", "search_query", "category", "attributes", "temp_range", "reasoning", "priority")
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
            .map(CodedEnum::code)
            .collect(Collectors.toList());
        return Schema.builder()
            .type(new Type(Type.Known.STRING))
            .enum_(codes)
            .build();
    }
}
