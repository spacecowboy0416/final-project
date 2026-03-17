package com.finalproject.coordi.exception.auth;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

public class AuthFailedException extends BusinessException {
    public AuthFailedException() {
        super(ErrorCode.AUTH_FAILED);
    }
}
