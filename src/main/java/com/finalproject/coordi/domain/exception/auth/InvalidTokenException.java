package com.finalproject.coordi.domain.exception.auth;

import com.finalproject.coordi.domain.exception.BusinessException;
import com.finalproject.coordi.domain.exception.ErrorCode;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}
