package com.finalproject.coordi.main.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.finalproject.coordi.main.dto.OpenWeatherForecastResponse;
import com.finalproject.coordi.main.dto.OpenWeatherResponse;
import com.finalproject.coordi.main.dto.WeatherContextDto;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.RainProbabilityType;
import com.finalproject.coordi.recommendation.domain.enums.WeatherEnums.WeatherStatusType;

/**
 * OpenWeather 현재 날씨/예보 응답을 내부 표준 날씨 DTO(WeatherContextDto)로 조립한다.
 * 날씨 상태 분류, 강수 확률, 오늘 강수 여부 등을 계산한다.
 */
@Component
public class WeatherContextAssembler {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    
    /**
     * 외부 API 응답을 내부 공통 날씨 컨텍스트로 변환한다.
     */

    public WeatherContextDto assemble(
            OpenWeatherResponse current,
            OpenWeatherForecastResponse forecast,
            String resolvedBy) {
        String rawMain = extractWeatherMain(current);
        String rawDescription = extractWeatherDescription(current);

        double temperature  = extractTemperature(current);
        double feelsLike = extractFeelsLike(current);
        int humidity = extractHumidity(current);
        double windMs = extractWindSpeed(current);
        double precipMm = extractCurrentPrecipMm(current);

        WeatherStatusType weatherStatus = mapWeatherStatus(rawMain, rawDescription, windMs);
        RainProbabilityType rainProbability = calculateRainProbability(forecast);
        boolean todayRain = calculateTodayRain(forecast);

        return WeatherContextDto.builder()
                .conditionText(rawDescription)
                .weatherStatus(weatherStatus)
                .rainProbability(rainProbability)
                .temperature(temperature)
                .feelsLike(feelsLike)
                .humidity(humidity)
                .windMs(windMs)
                .precipMm(precipMm)
                .todayRain(todayRain)
                .resolvedBy(resolvedBy)
                .recordedAt(LocalDateTime.now(KOREA_ZONE))
                .build();
    }

    private String extractWeatherMain(OpenWeatherResponse current) {
        if (current == null || current.getWeather() == null || current.getWeather().isEmpty()) {
            return "Clear";
        }
        String value = current.getWeather().get(0).getMain();
        return value != null ? value : "Clear";
    }

    private String extractWeatherDescription(OpenWeatherResponse current) {
        if (current == null || current.getWeather() == null || current.getWeather().isEmpty()) {
            return "";
        }
        String value = current.getWeather().get(0).getDescription();
        return value != null ? value : "";
    }

    private double extractTemperature(OpenWeatherResponse current) {
        if (current == null || current.getMain() == null) {
            return 0.0;
        }
        return current.getMain().getTemp();
    }

    private double extractFeelsLike(OpenWeatherResponse current) {
        if (current == null || current.getMain() == null) {
            return 0.0;
        }
        return current.getMain().getFeels_like();
    }

    private int extractHumidity(OpenWeatherResponse current) {
        if (current == null || current.getMain() == null) {
            return 0;
        }
        return current.getMain().getHumidity();
    }

    private double extractWindSpeed(OpenWeatherResponse current) {
        if (current == null || current.getWind() == null) {
            return 0.0;
        }
        return current.getWind().getSpeed();
    }

    private double extractCurrentPrecipMm(OpenWeatherResponse current) {
        if (current == null) {
            return 0.0;
        }
        double rainMm = extractPrecipValue(current.getRain());
        double snowMm = extractPrecipValue(current.getSnow());
        return rainMm + snowMm;
    }

    private double extractPrecipValue(Map<String, Double> source) {
        if (source == null) {
            return 0.0;
        }

        if (source.containsKey("1h") && source.get("1h") != null) {
            return source.get("1h");
        }

        if (source.containsKey("3h") && source.get("3h") != null) {
            return source.get("3h");
        }

        return 0.0;
    }

    private WeatherStatusType mapWeatherStatus(String rawMain, String rawDescription, double windMs) {
        String main = rawMain == null ? "" : rawMain.trim().toLowerCase();
        String desc = rawDescription == null ? "" : rawDescription.trim().toLowerCase();

        if (main.equals("thunderstorm")) {
            if (desc.contains("rain")) return WeatherStatusType.THUNDERSTORM_RAIN;
            return WeatherStatusType.THUNDERSTORM;
        }

        if (main.equals("drizzle") || main.equals("rain")) {
            if (desc.contains("cloud")) return WeatherStatusType.CLOUDY_RAIN;
            return WeatherStatusType.RAIN;
        }

        if (main.equals("snow")) {
            if (desc.contains("cloud")) return WeatherStatusType.CLOUDY_SNOW;
            if (desc.contains("sleet")) return WeatherStatusType.SLEET;
            if (desc.contains("hail")) return WeatherStatusType.HAIL;
            return WeatherStatusType.SNOW;
        }

        if (main.equals("clouds")) {
            if (desc.contains("few clouds") || desc.contains("scattered clouds")) {
                return WeatherStatusType.PARTLY_CLOUDY;
            }
            return WeatherStatusType.CLOUDY;
        }

        if (windMs >= 9.0) {
            return WeatherStatusType.WINDY;
        }

        return WeatherStatusType.CLEAR;
    }

    private RainProbabilityType calculateRainProbability(OpenWeatherForecastResponse forecast) {
        if (forecast == null || forecast.getList() == null || forecast.getList().isEmpty()) {
            return RainProbabilityType.VERY_LOW;
        }

        double maxPop = forecast.getList().stream()
                .mapToDouble(OpenWeatherForecastResponse.ForecastItem::getPop)
                .max()
                .orElse(0.0);

        double percent = maxPop * 100.0;

        if (percent >= 80) return RainProbabilityType.VERY_HIGH;
        if (percent >= 60) return RainProbabilityType.HIGH;
        if (percent >= 40) return RainProbabilityType.MEDIUM;
        if (percent >= 20) return RainProbabilityType.LOW;

        return RainProbabilityType.VERY_LOW;
    }

    private boolean calculateTodayRain(OpenWeatherForecastResponse forecast) {
        if (forecast == null || forecast.getList() == null || forecast.getList().isEmpty()) {
            return false;
        }

        LocalDate today = LocalDate.now(KOREA_ZONE);

        for (OpenWeatherForecastResponse.ForecastItem item : forecast.getList()) {
            LocalDate itemDate = Instant.ofEpochSecond(item.getDt())
                    .atZone(KOREA_ZONE)
                    .toLocalDate();

            if (!today.equals(itemDate)) continue;

            boolean rainVolume = extractPrecipValue(item.getRain()) > 0;
            boolean snowVolume = extractPrecipValue(item.getSnow()) > 0;
            boolean rainProb = item.getPop() >= 0.3;
            boolean weatherType = containsRainOrSnow(item.getWeather());

            if (rainVolume || snowVolume || rainProb || weatherType) {
                return true;
            }
        }

        return false;
    }

    private boolean containsRainOrSnow(List<OpenWeatherForecastResponse.Weather> list) {
        if (list == null || list.isEmpty()) return false;

        for (OpenWeatherForecastResponse.Weather w : list) {
            String main = w.getMain() == null ? "" : w.getMain().trim().toLowerCase();

            if (main.equals("rain")
                    || main.equals("drizzle")
                    || main.equals("snow")
                    || main.equals("thunderstorm")) {
                return true;
            }
        }

        return false;
    }
}