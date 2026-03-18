package com.finalproject.coordi.recommendation.dto.api;

/**
 * AI 어댑터로 전달하는 payload 경계 DTO.
 */
public record PayloadDto(
    String systemPrompt,
    String userPrompt,
    PayloadImageData imageData
) {
    public record PayloadImageData(
        byte[] imageBytes,
        String mimeType
    ) {
    }
}
