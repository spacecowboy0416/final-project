package com.finalproject.coordi.recommendation.config.annotation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class StagePerformanceAspect {
    private final StageExecutionTimes stageExecutionTimes;

    @Around("@annotation(logStage)")
    public Object measureTime(ProceedingJoinPoint joinPoint, LogStage logStage) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsed = (System.nanoTime() - startedAt) / 1_000_000;
            stageExecutionTimes.addTiming(logStage.value(), elapsed);
            log.info("recommendation stage={} elapsedMs={}", logStage.value(), elapsed);
        }
    }
}
