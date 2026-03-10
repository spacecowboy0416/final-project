package com.finalproject.coordi.errorlog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.finalproject.coordi.errorlog.dto.SystemErrorLog;

@Mapper
public interface ErrorLogMapper {
    
    // 해시값으로 기존 에러가 있는지 조회
    @Select("SELECT * FROM system_error_log WHERE error_hash = #{errorHash}")
    SystemErrorLog findByHash(String errorHash);

    // 똑같은 에러가 또 터지면 횟수와 시간만 업데이트
    @Update("UPDATE system_error_log SET occurrence_count = occurrence_count + 1, last_occurred_at = NOW() WHERE error_hash = #{errorHash}")
    void incrementOccurrence(String errorHash);

    // 완전 처음 보는 에러면 DB에 새로 저장
    @Insert("INSERT INTO system_error_log (error_hash, error_type, message, stack_trace, ai_solution) " +
            "VALUES (#{errorHash}, #{errorType}, #{message}, #{stackTrace}, #{aiSolution})")
    void insertLog(SystemErrorLog log);

    // AWS RDS 용량 방어: 30일 지난 오래된 로그 지우기
    @Delete("DELETE FROM system_error_log WHERE last_occurred_at < DATE_SUB(NOW(), INTERVAL 30 DAY)")
    void deleteOldLogs();
    
    // 관리자 화면
    @Select("SELECT * FROM system_error_log ORDER BY last_occurred_at DESC")
    List<SystemErrorLog> findAllLogs();
}