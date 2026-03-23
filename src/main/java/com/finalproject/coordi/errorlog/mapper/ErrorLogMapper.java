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

    // 에러 재발생 시 횟수 및 시간 업데이트 제어
    @Update("UPDATE system_error_log SET occurrence_count = occurrence_count + 1, last_occurred_at = NOW() WHERE error_hash = #{errorHash}")
    void incrementOccurrence(String errorHash);

    // 신규 에러 발생 시 유저 ID를 포함하여 DB 저장 제어
    @Insert("INSERT INTO system_error_log (error_hash, error_type, message, stack_trace, ai_solution, user_id) " +
            "VALUES (#{errorHash}, #{errorType}, #{message}, #{stackTrace}, #{aiSolution}, #{userId})")
    void insertLog(SystemErrorLog log);

    // 오래된 로그 자동 삭제 제어
    @Delete("DELETE FROM system_error_log WHERE last_occurred_at < DATE_SUB(NOW(), INTERVAL 30 DAY)")
    void deleteOldLogs();
    
    // 관리자 화면용 전체 로그 조회 기능
    @Select("SELECT * FROM system_error_log ORDER BY last_occurred_at DESC")
    List<SystemErrorLog> findAllLogs();
}