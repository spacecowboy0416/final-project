package com.finalproject.coordi.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finalproject.coordi.recommendation.domain.DraftRecommendationItem;
import com.finalproject.coordi.recommendation.domain.RecommendationContext;
import com.finalproject.coordi.recommendation.domain.RecommendationResult;
import com.finalproject.coordi.recommendation.domain.ScoredRecommendationItem;
import com.finalproject.coordi.recommendation.domain.type.DraftSource;
import com.finalproject.coordi.recommendation.domain.type.RecommendationStatus;
import com.finalproject.coordi.recommendation.dto.RecommendationRequest;
import com.finalproject.coordi.recommendation.dto.RecommendationResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrchestratorService {
    private final ObjectMapper objectMapper;
    private final ContextResolver contextResolver;
    private final DraftService draftService;
    private final NormalizationService normalizationService;
    private final ScoringService scoringService;
    private final ProductService productService;
    private final PersistenceService persistenceService;
    private final ResponseMapper responseMapper;

    public OrchestratorService(
        ObjectMapper objectMapper,
        ContextResolver contextResolver,
        DraftService draftService,
        NormalizationService normalizationService,
        ScoringService scoringService,
        ProductService productService,
        PersistenceService persistenceService,
        ResponseMapper responseMapper
    ) {
        this.objectMapper = objectMapper;
        this.contextResolver = contextResolver;
        this.draftService = draftService;
        this.normalizationService = normalizationService;
        this.scoringService = scoringService;
        this.productService = productService;
        this.persistenceService = persistenceService;
        this.responseMapper = responseMapper;
    }

    // 실서비스 추천 실행: Gemini 실패 시 fallback 없이 에러를 반환한다.
    @Transactional
    public RecommendationResponse recommendReal(RecommendationRequest request) {
        return recommendInternal(request, false);
    }

    // 테스트 추천 실행: Gemini 실패 시 fallback DRAFT를 허용한다.
    @Transactional
    public RecommendationResponse recommendTest(RecommendationRequest request) {
        return recommendInternal(request, true);
    }

    // 추천 실행 전체 흐름을 오케스트레이션한다.
    private RecommendationResponse recommendInternal(RecommendationRequest request, boolean allowFallback) {
        RecommendationContext context = contextResolver.resolve(request);
        DraftService.DraftGenerationResult draftResult = draftService.generate(request, context, allowFallback);
        List<DraftRecommendationItem> drafts = draftResult.items();

        ObjectNode coordination = objectMapper.createObjectNode();
        List<ScoredRecommendationItem> scoredItems = new ArrayList<>();
        for (DraftRecommendationItem draft : drafts) {
            String style = String.valueOf(draft.attributes().getOrDefault("style", "comfortable"));
            DraftRecommendationItem normalized = normalizationService.normalize(
                draft,
                normalizationService.buildStyleVector(style, objectMapper.createObjectNode())
            );
            ScoredRecommendationItem scored = scoringService.score(normalized, context);
            coordination.set(normalized.slotKey().code(), scored.blueprintSlotNode());
            scoredItems.add(scored);
        }

        List<ScoredRecommendationItem> enrichedItems = productService.attachProducts(scoredItems);
        ObjectNode blueprint = buildBlueprint(context, coordination);
        RecommendationResult prePersist = new RecommendationResult(
            null,
            RecommendationStatus.SUCCESS,
            draftResult.source(),
            context.tpoType(),
            context.styleMode(),
            blueprint,
            enrichedItems
        );
        Long recId = persistenceService.persist(request, prePersist);

        RecommendationResult persisted = new RecommendationResult(
            recId,
            prePersist.status(),
            prePersist.draftSource(),
            prePersist.tpoType(),
            prePersist.styleMode(),
            prePersist.aiBlueprint(),
            prePersist.scoredItems()
        );
        return responseMapper.toResponse(persisted, request);
    }

    // 최종 ai_blueprint JSON을 구성한다.
    private ObjectNode buildBlueprint(RecommendationContext context, ObjectNode coordination) {
        ObjectNode aiBlueprintRoot = objectMapper.createObjectNode();
        ObjectNode blueprint = objectMapper.createObjectNode();
        blueprint.put("schemaVersion", "1.0.0");
        blueprint.set("main_item_analysis", buildMainItemAnalysis(context));
        blueprint.set("coordination", coordination);
        blueprint.put("styling_rule_applied", "톤온톤 배색 + 상하 실루엣 밸런스");
        blueprint.put("workflow", "AI-First, Rule-Second");
        blueprint.put("final_threshold", 70.0);
        aiBlueprintRoot.set("ai_blueprint", blueprint);
        return aiBlueprintRoot;
    }

    // main_item_analysis 블록을 구성한다.
    private ObjectNode buildMainItemAnalysis(RecommendationContext context) {
        ObjectNode node = objectMapper.createObjectNode();
        int low = (int) Math.floor(context.currentTemp() - 2);
        int high = (int) Math.ceil(context.currentTemp() + 2);
        node.put("temp", low + "-" + high + "C");
        node.put("season", context.season().code());
        node.put("color", "navy");
        node.put("type", "jacket");
        node.put("style", context.styleMode().code());
        return node;
    }
}
