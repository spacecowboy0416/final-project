package com.finalproject.coordi.admin.statistics.mapper;

import com.finalproject.coordi.admin.statistics.dto.AdminStatisticsDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    AdminStatisticsDto selectSystemSummary();

    List<Map<String, Object>> selectDailyTrends();

}
