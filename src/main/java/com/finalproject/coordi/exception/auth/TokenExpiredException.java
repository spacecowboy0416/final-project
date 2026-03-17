package com.finalproject.coordi.exception.auth;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

public class TokenExpiredException extends BusinessException {
    public TokenExpiredException() {
        super(ErrorCode.TOKEN_EXPIRED);
    }
}
