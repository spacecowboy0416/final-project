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

    @GetMapping("/summary")
    public ResponseEntity<AdminStatisticsDto> getStatisticsSummary() {
        AdminStatisticsDto stats = adminStatisticsService.getSystemStatistics();
        return ResponseEntity.ok(stats);
    }
}
