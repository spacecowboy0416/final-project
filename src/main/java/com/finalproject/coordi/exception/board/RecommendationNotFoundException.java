package com.finalproject.coordi.exception.board;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

//게시글로 공유하려는 recommendation이 없을 때 사용하는 예외
public class RecommendationNotFoundException extends BusinessException {
    public RecommendationNotFoundException() {
        super(ErrorCode.RECOMMENDATION_NOT_FOUND);
    }
}