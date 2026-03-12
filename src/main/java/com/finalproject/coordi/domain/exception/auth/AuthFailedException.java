package com.finalproject.coordi.domain.exception.auth;

import com.finalproject.coordi.domain.exception.BusinessException;
import com.finalproject.coordi.domain.exception.ErrorCode;

public class AuthFailedException extends BusinessException {
    public AuthFailedException() {
        super(ErrorCode.AUTH_FAILED);
    }
}
