package com.finalproject.coordi.recommendation.service.outboundadapter.naver;

/**
 * 네이버 쇼핑 응답 파싱에서 반복되는 문자열 정리/가격 파싱 처리를 한곳에 모아, 어댑터와 DTO가 비즈니스 흐름에만 집중하도록 돕는 유틸리티다.
 */
public final class NaverShoppingUtils {
    private NaverShoppingUtils() {
    }

    public static String stripHtml(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }
        return rawText.replaceAll("<[^>]*>", "");
    }

    public static int parsePrice(String rawPrice) {
        try {
            return Integer.parseInt(rawPrice);
        } catch (Exception ignored) {
            return 0;
        }
    }
}
