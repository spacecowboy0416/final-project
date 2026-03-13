package com.finalproject.coordi.exception.main;

import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;

public class ForecastWeatherResponseNullException extends BusinessException {

    public ForecastWeatherResponseNullException() {
        super(ErrorCode.FORECAST_WEATHER_RESPONSE_NULL);
    }
}
