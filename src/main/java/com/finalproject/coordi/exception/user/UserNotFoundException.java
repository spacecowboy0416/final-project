package com.finalproject.coordi.domain.exception.user;

import com.finalproject.coordi.domain.exception.BusinessException;
import com.finalproject.coordi.domain.exception.ErrorCode;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
