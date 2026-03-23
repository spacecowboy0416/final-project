package com.finalproject.coordi.recommendation.service.blueprint;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.CategoryType;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto.Coordination;
import com.finalproject.coordi.recommendation.dto.api.RawBlueprintDto.ItemInfo;
import com.finalproject.coordi.exception.recommendation.RecommendationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlueprintStaticValidator {
    // 허용 문자 패턴 (한글, 숫자, 공백, 기본 문장부호)
    private static final Pattern AI_EXPLANATION_PATTERN = 
        Pattern.compile("^[\\p{IsHangul}\\d\\s.,!?~()/%+\\-:'\"\\[\\]]+$");

    private final Validator validator;

    public RawBlueprintDto validateRawBlueprint(RawBlueprintDto rawBlueprint) {
        // 1. 객체 자체의 존재 여부만 최소한으로 확인 (Fail-Fast)
        if (rawBlueprint == null) throw RecommendationException.blueprintResponseEmpty();

        // 2. Bean Validation 실행 (@NotNull, @NotBlank, @Size 등 일괄 처리)
        validateBeanConstraints(rawBlueprint);

        // 3. 비즈니스 정합성 검증 (필드 간 관계 및 도메인 특화 룰)
        validateBusinessRules(rawBlueprint.aiBlueprint());
        
        return rawBlueprint;
    }

    private void validateBusinessRules(RawBlueprintDto.AiBlueprint aiBlueprint) {
        // 슬롯 카테고리 일치 여부 검증
        validateSlotConsistency(aiBlueprint.coordination());
        
        // AI 설명문의 도메인 정책(한글 포함 등) 검증
        validateAiExplanation(aiBlueprint.aiExplanation());
    }

    private void validateSlotConsistency(Coordination co) {
        Stream.of(
            new SlotEntry(CategoryType.HEADWEAR, co.headwear()),
            new SlotEntry(CategoryType.TOPS, co.tops()),
            new SlotEntry(CategoryType.BOTTOMS, co.bottoms()),
            new SlotEntry(CategoryType.OUTERWEAR, co.outerwear()),
            new SlotEntry(CategoryType.SHOES, co.shoes()),
            new SlotEntry(CategoryType.ACCESSORIES, co.accessories())
        ).filter(entry -> entry.item() != null) // 선택적 슬롯(null)은 제외하고 검사
         .forEach(entry -> {
             if (entry.item().slotKey() != entry.type()) {
                 throw RecommendationException.blueprintTypeMismatch();
             }
         });
    }

    private void validateAiExplanation(String aiExplanation) {
        String trimmed = aiExplanation.trim();
        if (!containsHangul(trimmed) || !AI_EXPLANATION_PATTERN.matcher(trimmed).matches()) {
            throw RecommendationException.blueprintAiExplanationInvalid();
        }
    }

    private void validateBeanConstraints(RawBlueprintDto rawBlueprint) {
        Set<ConstraintViolation<RawBlueprintDto>> violations = validator.validate(rawBlueprint);
        if (!violations.isEmpty()) {
            // 상세 에러 로그가 필요하다면 여기서 violations를 순회하며 로깅 가능
            throw RecommendationException.blueprintTypeMismatch();
        }
    }

    private boolean containsHangul(String value) {
        return value.codePoints().anyMatch(this::isHangulCodePoint);
    }

    private boolean isHangulCodePoint(int cp) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(cp);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
            || block == Character.UnicodeBlock.HANGUL_JAMO
            || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
            || block == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_A
            || block == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B;
    }

    // 내부 검증용 임시 구조체
    private record SlotEntry(CategoryType type, ItemInfo item) {}
}