package com.finalproject.coordi.sentry.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice // 모든 컨트롤러에 걸쳐 전역적으로 전역되는 설정
public class GlobalControllerAdvice {

    @Value("${sentry.dsn-frontend}") // yml 파일에서 sentry.dsn-frontend 프로퍼티 값을 읽어와 아래 변수에 주입
    private String sentryDSNFrontend;

    @ModelAttribute("SENTRY_DSN_FRONTEND") // 메소드의 반환 값을 "SENTRY_DSN_FRONTEND" 라는 이름으로 모든 뷰 (Thymeleaf 템플릿)의 모델에 추가
    public String sentryDSNFrontend() {
        return sentryDSNFrontend;
    }
}
