package com.finalproject.coordi.domain.exception.main;

import com.finalproject.coordi.domain.exception.BusinessException;
import com.finalproject.coordi.domain.exception.ErrorCode;

public class CurrentWeatherResponseNullException extends BusinessException {

    public CurrentWeatherResponseNullException() {
        super(ErrorCode.CURRENT_WEATHER_RESPONSE_NULL);
    }
}