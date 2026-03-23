package com.finalproject.coordi.exception.auth;

import com.finalproject.coordi.exception.ErrorCode;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

// 정지된 유저가 로그인을 시도할 때 발생하는 예외
public class OAuth2SuspendedException extends OAuth2AuthenticationException {
    public OAuth2SuspendedException() {
        super(new OAuth2Error(ErrorCode.USER_SUSPENDED.getCode(), ErrorCode.USER_SUSPENDED.getMessage(), null));
    }
}
