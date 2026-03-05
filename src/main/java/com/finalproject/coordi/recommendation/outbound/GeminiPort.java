package com.finalproject.coordi.recommendation.outbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.dto.RecommendationRequest;

public interface GeminiPort {
    JsonNode generateDraftBlueprint(RecommendationRequest request, GeminiDraftContext context);
}
