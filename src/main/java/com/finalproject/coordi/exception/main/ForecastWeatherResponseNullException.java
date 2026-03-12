package com.finalproject.coordi.domain.exception.main;

import com.finalproject.coordi.domain.exception.BusinessException;
import com.finalproject.coordi.domain.exception.ErrorCode;

public class ForecastWeatherResponseNullException extends BusinessException {

    public ForecastWeatherResponseNullException() {
        super(ErrorCode.FORECAST_WEATHER_RESPONSE_NULL);
    }
}
