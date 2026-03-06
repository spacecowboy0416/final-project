package com.finalproject.coordi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SentryController {

    @GetMapping("/sentry-test-page")
    public String sentryTestPage() {
        return "/sentry/sentry-test";
    }

    @GetMapping("/sentry-test-error")
    public String sentryTestError() {
        throw new RuntimeException("This is a test exception for Sentry.");
    }
}
