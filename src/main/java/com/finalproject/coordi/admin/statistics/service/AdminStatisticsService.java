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

    public AdminStatisticsDto getSystemStatistics() {
        AdminStatisticsDto stats = statisticsMapper.selectSystemSummary();
        if (stats != null) {
            stats.setDailyTrends(statisticsMapper.selectDailyTrends());
            stats.setPopularTags(statisticsMapper.selectPopularTags(10));
        }
        return stats;
    }
}
