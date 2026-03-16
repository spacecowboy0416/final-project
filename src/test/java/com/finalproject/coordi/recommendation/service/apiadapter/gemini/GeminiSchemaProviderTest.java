package com.finalproject.coordi.recommendation.service.apiadapter.gemini;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums;
import com.google.genai.types.Schema;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeminiSchemaProviderTest {

    @Test
    void outputSchema_enumUsesCodeValues() {
        GeminiSchemaProvider provider = new GeminiSchemaProvider();
        provider.initialize();

        Schema outputSchema = provider.getOutputSchema();
        assertNotNull(outputSchema);

        List<String> tpoExpected = List.of(
            CoordinationEnums.TpoType.DATE.code(),
            CoordinationEnums.TpoType.WORK.code(),
            CoordinationEnums.TpoType.CASUAL.code(),
            CoordinationEnums.TpoType.EXERCISE.code(),
            CoordinationEnums.TpoType.TRAVEL.code(),
            CoordinationEnums.TpoType.FORMAL.code(),
            CoordinationEnums.TpoType.FUNERAL.code(),
            CoordinationEnums.TpoType.WEDDING.code()
        );

        List<String> seasonExpected = List.of(
            WeatherEnums.SeasonType.SPRING.code(),
            WeatherEnums.SeasonType.SUMMER.code(),
            WeatherEnums.SeasonType.FALL.code(),
            WeatherEnums.SeasonType.WINTER.code()
        );

        List<String> tpoEnum = getEnum(outputSchema, "ai_blueprint", "tpoType");
        List<String> seasonEnum = getEnum(outputSchema, "ai_blueprint", "main_item_analysis", "season");

        assertEquals(tpoExpected, tpoEnum);
        assertEquals(seasonExpected, seasonEnum);
        assertTrue(tpoEnum.stream().noneMatch(value -> value.equals(value.toUpperCase())));
    }

    private List<String> getEnum(Schema root, String... path) {
        Schema current = root;
        for (String key : path) {
            Map<String, Schema> properties = current.properties().orElseThrow(
                () -> new IllegalStateException("Missing properties in schema path")
            );
            current = properties.get(key);
            if (current == null) {
                throw new IllegalStateException("Missing schema key: " + key);
            }
        }
        return current.enum_().orElseThrow(
            () -> new IllegalStateException("Missing enum values in schema path")
        );
    }
}
