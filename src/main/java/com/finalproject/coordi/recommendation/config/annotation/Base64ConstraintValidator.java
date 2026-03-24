package com.finalproject.coordi.recommendation.config.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Base64;

/**
 * Base64 문자열 및 디코딩 결과 길이를 검증한다.
 */
public class Base64ConstraintValidator implements ConstraintValidator<ValidBase64, String> {
    private int minBytes;

    @Override
    public void initialize(ValidBase64 constraintAnnotation) {
        this.minBytes = Math.max(0, constraintAnnotation.minBytes());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            return decoded.length >= minBytes;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
