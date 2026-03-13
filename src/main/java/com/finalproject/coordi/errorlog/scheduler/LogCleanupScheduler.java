package com.finalproject.coordi.errorlog.scheduler;

import com.finalproject.coordi.errorlog.mapper.ErrorLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogCleanupScheduler {

    private final ErrorLogMapper errorLogMapper;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    public void cleanup() {
        errorLogMapper.deleteOldLogs();
        System.out.println("30일 지난 오래된 AI 에러 로그 삭제 완료");
    }
}