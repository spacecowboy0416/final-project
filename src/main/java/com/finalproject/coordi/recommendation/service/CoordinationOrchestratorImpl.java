package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.DraftSource;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.RecommendationStatus;
import com.finalproject.coordi.recommendation.domain.enums.RecommendationEnums.SelectionStage;
import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.StyleMode;
import com.finalproject.coordi.recommendation.domain.enums.ContextEnums.TpoType;
import com.finalproject.coordi.recommendation.dto.CoordinationOutput;
import com.finalproject.coordi.recommendation.dto.CoordinationRequest;
import com.finalproject.coordi.recommendation.dto.CoordinationResponse;
import com.finalproject.coordi.recommendation.service.component.BlueprintSlotExtractor;
import com.finalproject.coordi.recommendation.service.component.CoordinationValidator;
import com.finalproject.coordi.recommendation.service.outboundport.AiPort;
import com.finalproject.coordi.recommendation.service.outboundport.ShoppingPort;
import com.finalproject.coordi.recommendation.service.outboundport.ShoppingPort.ShoppingSearchQuery;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CoordinationRequest를 받아 AI 생성과 출력 제약 검증을 수행하는 오케스트레이터 구현체.
 */
@Service
@RequiredArgsConstructor
public class CoordinationOrchestratorImpl implements CoordinationOrchestrator {
    private static final int DEFAULT_SHOPPING_DISPLAY = 5;

    private final AiPort aiPort;
    private final ShoppingPort shoppingPort;
    private final CoordinationValidator coordinationValidator;
    private final BlueprintSlotExtractor blueprintSlotExtractor;

    @Override
    @Transactional
    public CoordinationResponse coordinate(CoordinationRequest request) {
        // 핵심 흐름: AI blueprint를 검증한 뒤 슬롯별 search_query를 그대로 쇼핑 검색에 전달하고, 상위 1개를 최종 아이템으로 매핑한다.
        var aiRaw = aiPort.generateCoordination(request);
        var constrained = coordinationValidator.validate(aiRaw);
        var slotDrafts = blueprintSlotExtractor.extract(constrained);

        TpoType tpo = TpoType.CASUAL;
        StyleMode mode = StyleMode.COMFORTABLE;

        List<CoordinationOutput> coordination = new ArrayList<>();
        List<CoordinationResponse.ItemResult> finalItems = new ArrayList<>();
        for (BlueprintSlotExtractor.SlotDraft slotDraft : slotDrafts) {
            coordination.add(
                new CoordinationOutput(
                    slotDraft.slotKey(),
                    slotDraft.itemName(),
                    slotDraft.searchQuery(),
                    SelectionStage.DRAFT,
                    0.0,
                    slotDraft.color(),
                    slotDraft.material(),
                    slotDraft.fit(),
                    slotDraft.style()
                )
            );

            var candidates = shoppingPort.search(
                new ShoppingSearchQuery(slotDraft.searchQuery(), DEFAULT_SHOPPING_DISPLAY)
            );
            var topCandidate = candidates.isEmpty() ? null : candidates.get(0);
            finalItems.add(
                new CoordinationResponse.ItemResult(
                    slotDraft.slotKey(),
                    slotDraft.itemName(),
                    slotDraft.searchQuery(),
                    slotDraft.reasoning(),
                    slotDraft.priority(),
                    topCandidate == null ? SelectionStage.REJECTED : SelectionStage.FINAL,
                    topCandidate == null ? 0.0 : 1.0,
                    topCandidate == null
                        ? null
                        : new CoordinationResponse.ProductPreview(
                            topCandidate.productName(),
                            topCandidate.salePrice(),
                            topCandidate.productImageUrl(),
                            topCandidate.productDetailUrl()
                        )
                )
            );
        }

        return new CoordinationResponse(
            null,
            RecommendationStatus.SUCCESS,
            DraftSource.GEMINI,
            tpo,
            mode,
            constrained,
            coordination,
            finalItems
        );
    }
}
