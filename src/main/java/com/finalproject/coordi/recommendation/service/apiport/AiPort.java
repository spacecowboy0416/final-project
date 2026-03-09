package com.finalproject.coordi.recommendation.service.apiport;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.dto.api.CoordinationRequestDto;

public interface AiPort{
    JsonNode generateCoordination(CoordinationRequestDto request);
}
