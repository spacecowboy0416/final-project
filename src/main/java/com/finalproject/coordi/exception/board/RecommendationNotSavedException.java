package com.finalproject.coordi.exception.board;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

//recommendation은 존재하지만 저장된 코디가 아닐 때 사용하는 예외
public class RecommendationNotSavedException extends BusinessException {
    public RecommendationNotSavedException() {
        super(ErrorCode.RECOMMENDATION_NOT_SAVED);
    }
}