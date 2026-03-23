package com.finalproject.coordi.admin.statistics.mapper;

import com.finalproject.coordi.admin.statistics.dto.AdminStatisticsDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    AdminStatisticsDto selectSystemSummary();

    List<Map<String, Object>> selectDailyTrends();

    // 실시간 인기 태그 10개 조회
    List<AdminStatisticsDto.PopularTagDto> selectPopularTags(@Param("limit") int limit);
}
