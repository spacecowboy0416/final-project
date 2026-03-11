package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.service.apiadapter.GeminiAiAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * recommendation 애플리케이션 기동 직후 Gemini 프롬프트 캐시를 미리 생성한다.
 * 존재 이유는 첫 사용자 요청에서 캐시 생성 RTT(추가 네트워크 왕복)를 제거해 초기 지연을 줄이기 위함이다.
 *
 * 사용 맥락:
 * - PromptBuilder의 정적 prefix를 가져와 GeminiAiAdapter의 cachedContents 생성 경로를 선실행한다.
 * - warm-up 실패는 서비스 시작 실패로 간주하지 않고, 실제 요청에서 uncached 폴백을 허용한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiPromptCacheWarmup {
    private final PromptBuilder promptBuilder;
    private final GeminiAiAdapter geminiAiAdapter;

    @Value("${external.api.gemini.cache.warmup.enabled:true}")
    private boolean warmupEnabled;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        if (!warmupEnabled) {
            log.info("gemini prompt cache warm-up skipped because warmup is disabled.");
            return;
        }

        try {
            String cacheablePrompt = promptBuilder.loadCacheablePrompt();
            geminiAiAdapter.warmUpPromptCache(cacheablePrompt);
        } catch (Exception exception) {
            log.warn("gemini prompt cache warm-up failed. requests will fallback to uncached path.", exception);
        }
    }
}
