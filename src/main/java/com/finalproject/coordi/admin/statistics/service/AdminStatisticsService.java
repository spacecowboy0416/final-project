package com.finalproject.coordi.admin.statistics.service;

import com.finalproject.coordi.admin.statistics.mapper.StatisticsMapper;
import com.finalproject.coordi.admin.statistics.dto.AdminStatisticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatisticsService {

    private final StatisticsMapper statisticsMapper;

    // 관리자 대시보드에 필요한 여러 통계(핵심 요약, 일일 트렌드)를 조합하여 하나의 DTO로 제공하기 위함.
    public AdminStatisticsDto getSystemStatistics() {
        AdminStatisticsDto stats = statisticsMapper.selectSystemSummary();
        if (stats != null) {
            // 기본 통계 조회 후, 추가적으로 일일 트렌드를 조회하여 DTO에 채워넣음.
            stats.setDailyTrends(statisticsMapper.selectDailyTrends());
        }
        return stats;
    }
}
