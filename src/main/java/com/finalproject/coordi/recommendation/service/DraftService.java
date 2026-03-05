package com.finalproject.coordi.recommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.coordi.recommendation.domain.DraftRecommendationItem;
import com.finalproject.coordi.recommendation.domain.RecommendationContext;
import com.finalproject.coordi.recommendation.domain.type.DraftSource;
import com.finalproject.coordi.recommendation.domain.type.PriorityType;
import com.finalproject.coordi.recommendation.domain.type.SlotKey;
import com.finalproject.coordi.recommendation.dto.RecommendationRequest;
import com.finalproject.coordi.recommendation.exception.AppException;
import com.finalproject.coordi.recommendation.exception.ErrorCode;
import com.finalproject.coordi.recommendation.outbound.GeminiDraftContext;
import com.finalproject.coordi.recommendation.outbound.GeminiPort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DraftService {
    private static final List<SlotKey> SLOT_KEYS = List.of(
        SlotKey.TOPS, SlotKey.BOTTOMS, SlotKey.OUTERWEAR, SlotKey.SHOES, SlotKey.ACCESSORIES
    );
    private static final Map<SlotKey, String> SLOT_CATEGORY = Map.of(
        SlotKey.TOPS, "top",
        SlotKey.BOTTOMS, "pants",
        SlotKey.OUTERWEAR, "outerwear",
        SlotKey.SHOES, "shoes",
        SlotKey.ACCESSORIES, "accessory"
    );

    private final GeminiPort geminiPort;
    private final ObjectMapper objectMapper;

    public DraftService(GeminiPort geminiPort, ObjectMapper objectMapper) {
        this.geminiPort = geminiPort;
        this.objectMapper = objectMapper;
    }

    /**
     * DRAFT 생성 결과(아이템 목록 + 생성 소스)를 보관한다.
     */
    public record DraftGenerationResult(
        List<DraftRecommendationItem> items,
        DraftSource source
    ) {
    }

    // Gemini DRAFT를 생성하고 정책에 따라 fallback을 허용/차단한다.
    public DraftGenerationResult generate(
        RecommendationRequest request,
        RecommendationContext context,
        boolean allowFallback
    ) {
        try {
            GeminiDraftContext geminiContext = new GeminiDraftContext(
                context.currentTemp(),
                context.styleMode().code(),
                context.season().code()
            );
            JsonNode geminiResponse = geminiPort.generateDraftBlueprint(request, geminiContext);
            List<DraftRecommendationItem> parsed = parseGeminiDraftItems(geminiResponse);
            if (!parsed.isEmpty()) {
                return new DraftGenerationResult(parsed, DraftSource.GEMINI);
            }
            if (!allowFallback) {
                throw new AppException(ErrorCode.EXTERNAL_API_ERROR, "Gemini 응답에서 coordination 슬롯 파싱에 실패했습니다.");
            }
        } catch (Exception ignored) {
            // 실서비스 모드에서는 외부 API 실패를 즉시 반환한다.
            if (!allowFallback) {
                throw new AppException(ErrorCode.EXTERNAL_API_ERROR, "Gemini DRAFT 생성에 실패했습니다.", ignored);
            }
        }
        return new DraftGenerationResult(generateFallbackDraftItems(context), DraftSource.FALLBACK);
    }

    // Gemini 응답의 coordination 블록을 내부 DRAFT 모델로 파싱한다.
    private List<DraftRecommendationItem> parseGeminiDraftItems(JsonNode geminiResponse) {
        JsonNode coordination = geminiResponse.path("ai_blueprint").path("coordination");
        if (coordination.isMissingNode() || !coordination.isObject()) {
            return List.of();
        }

        List<DraftRecommendationItem> result = new ArrayList<>();
        for (SlotKey slotKey : SLOT_KEYS) {
            JsonNode slot = coordination.path(slotKey.code());
            if (slot.isMissingNode() || !slot.isObject()) {
                continue;
            }
            Map<String, Object> attributes = objectMapper.convertValue(slot.path("attributes"), Map.class);
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            result.add(new DraftRecommendationItem(
                slotKey,
                slot.path("item_name").asText(slotKey.code() + " item"),
                slot.path("search_query").asText("남성 " + slotKey.code()),
                slot.path("category").asText(SLOT_CATEGORY.get(slotKey)),
                attributes,
                slot.path("temp_range").path(0).asInt(0),
                slot.path("temp_range").path(1).asInt(30),
                slot.path("reasoning").asText("추천 근거"),
                "optional".equalsIgnoreCase(slot.path("priority").asText()) ? PriorityType.OPTIONAL : PriorityType.ESSENTIAL
            ));
        }
        return result;
    }

    // 테스트 가능한 기본 DRAFT를 구성한다.
    private List<DraftRecommendationItem> generateFallbackDraftItems(RecommendationContext context) {
        List<DraftRecommendationItem> drafts = new ArrayList<>();
        for (SlotKey slotKey : SLOT_KEYS) {
            String itemName = recommendItemName(slotKey, context.styleMode().code(), context.currentTemp());
            String searchQuery = "남성 " + context.styleMode().code() + " " + context.season().code() + " " + itemName;
            PriorityType priority = (SlotKey.ACCESSORIES.equals(slotKey)) ? PriorityType.OPTIONAL : PriorityType.ESSENTIAL;

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("color", SlotKey.OUTERWEAR.equals(slotKey) ? "진청색" : "회색");
            attributes.put("material", context.currentTemp() < 12 ? "wool" : "cotton");
            attributes.put("fit", "regular");
            attributes.put("style", context.styleMode().code());
            attributes.put("thickness_level", context.currentTemp() < 10 ? 4 : (context.currentTemp() < 20 ? 3 : 2));

            drafts.add(new DraftRecommendationItem(
                slotKey,
                itemName,
                searchQuery,
                SLOT_CATEGORY.get(slotKey),
                attributes,
                (int) Math.floor(context.currentTemp() - 5),
                (int) Math.ceil(context.currentTemp() + 5),
                slotReasoning(slotKey, context.currentTemp(), context.styleMode().code()),
                priority
            ));
        }
        return drafts;
    }

    // 슬롯별 기본 아이템명을 반환한다.
    private String recommendItemName(SlotKey slotKey, String styleMode, double temp) {
        return switch (slotKey) {
            case TOPS -> temp < 12 ? "니트 스웨터" : "코튼 긴팔 티셔츠";
            case BOTTOMS -> "테이퍼드 슬랙스";
            case OUTERWEAR -> temp < 16 ? "미니멀 울 자켓" : "라이트 블루종";
            case SHOES -> "화이트 스니커즈";
            case ACCESSORIES -> "실버 메탈 시계";
            default -> styleMode + " 아이템";
        };
    }

    // 슬롯별 추천 근거 문장을 생성한다.
    private String slotReasoning(SlotKey slotKey, double temp, String styleMode) {
        return switch (slotKey) {
            case TOPS -> "현재 기온(" + temp + "도)에 맞춰 체온 유지와 레이어드 확장성을 고려했습니다.";
            case BOTTOMS -> styleMode + " 무드와 균형 잡힌 실루엣을 위해 하체 라인을 정돈했습니다.";
            case OUTERWEAR -> "기온 변동 대응을 위해 탈착 가능한 외투를 배치했습니다.";
            case SHOES -> "활동성과 전체 톤 밸런스를 동시에 맞추기 위해 뉴트럴 슈즈를 선택했습니다.";
            case ACCESSORIES -> "과하지 않게 스타일 완성도를 높이는 포인트 아이템입니다.";
            default -> "추천 근거";
        };
    }
}
