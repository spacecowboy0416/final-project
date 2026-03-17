package com.finalproject.coordi.recommendation.config.annotation;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * recommendation 요청 단위 stage 시간 측정값을 보관한다.
 */
@Component
@RequestScope
public class StageExecutionTimes {
    private final Map<String, Long> timings = new LinkedHashMap<>();

    public void addTiming(String stageName, long elapsedMillis) {
        timings.put(stageName, elapsedMillis);
    }

    public Map<String, Long> snapshot() {
        return new LinkedHashMap<>(timings);
    }
}
