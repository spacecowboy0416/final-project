package com.finalproject.coordi.admin.statistics.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AdminStatisticsDto {
    private long totalUsers;
    private long todayNewUsers;
    private long totalRecommendations;
    private long todayRecommendations;
    private long totalClosetItems;
    private long activeErrors;

    private List<Map<String, Object>> dailyTrends;
}
