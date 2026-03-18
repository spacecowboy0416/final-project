package com.finalproject.coordi.admin.statistics.controller;

import com.finalproject.coordi.admin.statistics.dto.AdminStatisticsDto;
import com.finalproject.coordi.admin.statistics.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    // 관리자가 서비스의 핵심 지표(사용자, 추천 수 등)를 한눈에 파악할 수 있도록 통계 데이터를 조회하여 제공함.
    @GetMapping("/summary")
    public ResponseEntity<AdminStatisticsDto> getStatisticsSummary() {
        AdminStatisticsDto stats = adminStatisticsService.getSystemStatistics();
        return ResponseEntity.ok(stats);
    }
}
