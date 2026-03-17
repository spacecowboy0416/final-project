package com.finalproject.coordi.main.service;

import org.springframework.stereotype.Service;

import com.finalproject.coordi.cache.WeatherCachePort;
import com.finalproject.coordi.main.dto.LocationResponse;
import com.finalproject.coordi.main.dto.OpenWeatherForecastResponse;
import com.finalproject.coordi.main.dto.OpenWeatherResponse;
import com.finalproject.coordi.main.dto.WeatherContextDto;
import com.finalproject.coordi.main.dto.WeatherResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final OpenWeatherClient openWeatherClient;
    private final LocationService locationService;
    private final WeatherContextAssembler assembler;
    private final WeatherCachePort weatherCachePort;

    public WeatherContextDto getWeatherContext(double lat, double lon) {
        LocationResponse region = locationService.getRegion(lat, lon);
        return getWeatherContext(lat, lon, region);
    }

    public WeatherResponse getToday(double lat, double lon) {
        LocationResponse region = locationService.getRegion(lat, lon);
        WeatherContextDto context = getWeatherContext(lat, lon, region);

        return WeatherResponse.builder()
                .locationText(buildLocationText(region))
                .weatherMainRaw(toRawMain(context))
                .weatherStatus(
                        context.getWeatherStatus() != null
                                ? context.getWeatherStatus().code()
                                : null
                )
                .conditionText(context.getConditionText())
                .weatherStateKo(
                        context.getWeatherStatus() != null
                                ? context.getWeatherStatus().displayNameKo()
                                : ""
                )
                .temperature(context.getTemperature())
                .feelsLike(context.getFeelsLike())
                .humidity(context.getHumidity())
                .windMs(context.getWindMs())
                .precipMm(context.getPrecipMm())
                .todayRain(context.isTodayRain())
                .build();
    }

    private WeatherContextDto getWeatherContext(double lat, double lon, LocationResponse region) {
        WeatherContextDto cached = weatherCachePort.getWeather(region.getCity(), region.getGu());
        if (cached != null) {
            return cached;
        }

        OpenWeatherResponse current = openWeatherClient.getCurrentWeather(lat, lon);
        OpenWeatherForecastResponse forecast = openWeatherClient.getForecast(lat, lon);

        WeatherContextDto context = assembler.assemble(
                current,
                forecast,
                "kakao-rest"
        );

        weatherCachePort.cacheWeather(region.getCity(), region.getGu(), context);

        return context;
    }

    private String buildLocationText(LocationResponse region) {
        if (region == null) {
            return "서울";
        }

        String city = region.getCity() != null ? region.getCity() : "서울";
        String gu = region.getGu() != null ? region.getGu() : "";

        if (!gu.isBlank()) {
            return city + " " + gu;
        }

        return city;
    }

    private String toRawMain(WeatherContextDto context) {
        if (context.getWeatherStatus() == null) {
            return "Clear";
        }

        return switch (context.getWeatherStatus()) {
            case PARTLY_CLOUDY, CLOUDY -> "Clouds";
            case RAIN, CLOUDY_RAIN -> "Rain";
            case THUNDERSTORM, THUNDERSTORM_RAIN -> "Thunderstorm";
            case SNOW, CLOUDY_SNOW, SLEET, HAIL -> "Snow";
            case WINDY -> "Clear";
            case CLEAR -> "Clear";
        };
    }
}