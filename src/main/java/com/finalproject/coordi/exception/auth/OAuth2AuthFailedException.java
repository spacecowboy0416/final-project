package com.finalproject.coordi.exception.auth;

import com.finalproject.coordi.exception.ErrorCode;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

// 일반적인 인증 실패 (지원하지 않는 소셜 등)
public class OAuth2AuthFailedException extends OAuth2AuthenticationException {
    public OAuth2AuthFailedException() {
        super(new OAuth2Error(ErrorCode.AUTH_FAILED.getCode(), ErrorCode.AUTH_FAILED.getMessage(), null));
    }
}
