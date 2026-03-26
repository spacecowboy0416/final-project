package com.finalproject.coordi.exception.board;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

public class BoardLoginRequiredException extends BusinessException {
    public BoardLoginRequiredException() {
        super(ErrorCode.LOGIN_REQUIRED);
    }
}