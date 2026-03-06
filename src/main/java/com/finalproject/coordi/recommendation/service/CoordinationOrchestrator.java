package com.finalproject.coordi.recommendation.service;

import com.finalproject.coordi.recommendation.dto.CoordinationRequest;
import com.finalproject.coordi.recommendation.dto.CoordinationResponse;

/**
 * 코디네이션 생성의 전체 프로세스를 관리하는 지휘자 인터페이스
 */
public interface CoordinationOrchestrator {
    CoordinationResponse coordinate(CoordinationRequest request);
}
