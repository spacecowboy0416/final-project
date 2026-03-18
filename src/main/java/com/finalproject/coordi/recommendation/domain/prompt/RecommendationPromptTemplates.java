package com.finalproject.coordi.recommendation.domain.prompt;

public final class RecommendationPromptTemplates {
    private RecommendationPromptTemplates() {
    }

    public static final String SYSTEM_PROMPT_EN = """
        You are an AI personal shopper.
        Build a full-body outfit around the attached main fashion item using the user request and weather context.
        Return JSON only and follow the provided response schema exactly.

        Semantic rules:
        - Complete all five coordination slots.
        - `tpoType` is a situation code, not a style code.
        - `styleType` and `attributes.style` are style codes, not tpo codes.
        - `main_item_analysis.type` is an item category code, not a free-text garment name.
        - `main_item_analysis.style` is a style code, not a sentence.
        - Every coordination slot must include its own `slot_key`, and it must match the parent slot name.
        - `item_name` is a short user-facing display name.
        - `reasoning` is a brief explanation for why the slot item fits the outfit.
        """;

    public static final String USER_PROMPT_EN = """
        Inputs:
        - image: attached main clothing item
        - naturalText: %s
        - targetGender: %s
        - scheduleTime: %s
        - weather.temperature: %s
        - weather.feelsLike: %s
        - weather.status: %s
        - weather.rainProbability: %s
        - weather.source: %s
        """;

    public static final String SYSTEM_PROMPT_KR = """
        당신은 AI 퍼스널 쇼퍼입니다.
        사용자 요청, 날씨 맥락, 첨부된 메인 패션 아이템을 바탕으로 전신 코디를 완성하세요.
        출력은 반드시 JSON만 반환하고, 제공된 response schema를 정확히 따르세요.

        의미 규칙:
        - coordination의 다섯 슬롯을 모두 채우세요.
        - `tpoType`은 상황 코드이고, 스타일 코드가 아닙니다.
        - `styleType`과 `attributes.style`은 스타일 코드이고, tpo 코드가 아닙니다.
        - `main_item_analysis.type`은 자유 텍스트 옷 이름이 아니라 아이템 카테고리 코드입니다.
        - `main_item_analysis.style`은 문장이 아니라 스타일 코드입니다.
        - 각 coordination 슬롯에는 반드시 자신의 `slot_key`가 있어야 하고, 부모 슬롯 이름과 일치해야 합니다.
        - `item_name`은 사용자에게 보여줄 짧은 이름입니다.
        - `reasoning`은 해당 슬롯 아이템이 코디에 왜 맞는지에 대한 짧은 설명입니다.
        """;

    public static final String USER_PROMPT_KR = """
        입력값:
        - image: 첨부된 메인 의류 아이템
        - naturalText: %s
        - targetGender: %s
        - scheduleTime: %s
        - weather.temperature: %s
        - weather.feelsLike: %s
        - weather.status: %s
        - weather.rainProbability: %s
        - weather.source: %s
        """;
}
