package com.finalproject.coordi.exception.main;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

public class CurrentWeatherResponseNullException extends BusinessException {

    public CurrentWeatherResponseNullException() {
        super(ErrorCode.CURRENT_WEATHER_RESPONSE_NULL);
    }
}