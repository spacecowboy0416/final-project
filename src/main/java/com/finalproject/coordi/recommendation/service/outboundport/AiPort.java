package com.finalproject.coordi.recommendation.service.outboundport;

import com.fasterxml.jackson.databind.JsonNode;
import com.finalproject.coordi.recommendation.dto.CoordinationRequest;

public interface AiPort{
    JsonNode generateCoordination(CoordinationRequest request);
}
