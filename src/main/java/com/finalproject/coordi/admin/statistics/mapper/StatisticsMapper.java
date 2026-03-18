package com.finalproject.coordi.admin.statistics.mapper;

import com.finalproject.coordi.admin.statistics.dto.AdminStatisticsDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    // 시스템 통계
    AdminStatisticsDto selectSystemSummary();
    List<Map<String, Object>> selectDailyTrends();
    List<AdminStatisticsDto.PopularTagDto> selectPopularTags(@Param("limit") int limit);
}
