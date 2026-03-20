package com.finalproject.coordi.exception.user;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

public class UserSuspendedException extends BusinessException {
    public UserSuspendedException() {
        super(ErrorCode.USER_SUSPENDED);
    }
}

