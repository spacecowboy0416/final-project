package com.finalproject.coordi.recommendation.service.imagefilter;

import com.finalproject.coordi.recommendation.domain.enums.imagefilter.FilterEvaluation;
import com.finalproject.coordi.recommendation.domain.enums.imagefilter.FilterReason;
import com.finalproject.coordi.recommendation.infra.imageAnalysis.ImageAnalysisProperties;
import com.finalproject.coordi.recommendation.service.productSearch.ShoppingPort.SearchedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 단일 상품 이미지가 1차 필터를 통과할 수 있는지 판정한다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ImageFilterEvaluator {
    private final ImageAnalysisPort imageAnalysisPort;
    private final ImageAnalysisProperties imageFilterProperties;

    public FilterEvaluation evaluate(SearchedProduct searchedProduct) {
        if (searchedProduct == null || !StringUtils.hasText(searchedProduct.productImageUrl())) {
            return logResult(
                searchedProduct,
                new FilterEvaluation(false, FilterReason.REJECT_MISSING_IMAGE_URL, 0.0, 0.0)
            );
        }

        if (!imageFilterProperties.isEnabled()) {
            return logResult(
                searchedProduct,
                new FilterEvaluation(true, FilterReason.PASS_FILTER_DISABLED, 0.0, 0.0)
            );
        }

        ImageAnalysisPort.ImageAnalysisResult analysisResult = imageAnalysisPort.analyze(searchedProduct.productImageUrl());
        if (analysisResult == null || !analysisResult.analyzable()) {
            return logResult(
                searchedProduct,
                new FilterEvaluation(false, FilterReason.REJECT_ANALYSIS_UNAVAILABLE, 0.0, 0.0)
            );
        }

        if (imageFilterProperties.isPersonCheckEnabled()
            && analysisResult.personRatio() < imageFilterProperties.getMinPersonRatio()) {
            return logResult(
                searchedProduct,
                new FilterEvaluation(
                    false,
                    FilterReason.REJECT_LOW_PERSON_RATIO,
                    analysisResult.personRatio(),
                    analysisResult.garmentRatio()
                )
            );
        }

        if (analysisResult.garmentRatio() < imageFilterProperties.getMinGarmentRatio()) {
            return logResult(
                searchedProduct,
                new FilterEvaluation(
                    false,
                    FilterReason.REJECT_LOW_GARMENT_RATIO,
                    analysisResult.personRatio(),
                    analysisResult.garmentRatio()
                )
            );
        }

        return logResult(
            searchedProduct,
            new FilterEvaluation(
                true,
                FilterReason.PASS_THRESHOLD,
                analysisResult.personRatio(),
                analysisResult.garmentRatio()
            )
        );
    }

    private FilterEvaluation logResult(SearchedProduct searchedProduct, FilterEvaluation evaluation) {
        String productId = searchedProduct == null ? "unknown" : searchedProduct.marketplaceProductId();
        String productName = searchedProduct == null ? "unknown" : searchedProduct.productName();
        if (evaluation.passed()) {
            log.debug(
                "image-filter passed productId={} productName={} reason={} personRatio={} garmentRatio={}",
                productId,
                productName,
                evaluation.reason(),
                evaluation.personRatio(),
                evaluation.garmentRatio()
            );
        } else {
            log.info(
                "image-filter rejected productId={} productName={} reason={} personRatio={} garmentRatio={}",
                productId,
                productName,
                evaluation.reason(),
                evaluation.personRatio(),
                evaluation.garmentRatio()
            );
        }
        return evaluation;
    }
}
