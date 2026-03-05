package com.finalproject.coordi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentryController {

    @GetMapping("/sentry-test")
    public String sentryTest() {
        throw new RuntimeException("This is a test exception for Sentry.");
    }
}
