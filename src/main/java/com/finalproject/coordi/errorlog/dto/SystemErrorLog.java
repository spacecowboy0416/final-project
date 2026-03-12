package com.finalproject.coordi.errorlog.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemErrorLog {
    private Long logId;
    private String errorHash;
    private String errorType;
    private String message;
    private String stackTrace;
    private String aiSolution;
    private int occurrenceCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastOccurredAt;
}