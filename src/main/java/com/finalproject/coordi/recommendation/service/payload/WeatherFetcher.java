package com.finalproject.coordi.recommendation.service.payload;

import com.finalproject.coordi.recommendation.dto.internal.UserRequest;
import com.finalproject.coordi.recommendation.dto.internal.Weather;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherFetcher {
    private final WeatherPort weatherPort;

    /**
     * 프론트 Kakao SDK가 추출해 전달한 districtName과 scheduleTime으로
     * Redis 캐시 서버의 날씨 정보를 조회하는 단계다.
     */
    public Weather fetch(UserRequest request) {
        var location = request.location();
        var weatherData = weatherPort.fetchByDistrict(
            location.districtName(),
            request.scheduleTime()
        );

        return new Weather(
            weatherData.temperature(),
            weatherData.feelsLike(),
            weatherData.weatherStatus() == null ? null : weatherData.weatherStatus().code(),
            weatherData.rainProbability() == null ? null : weatherData.rainProbability().code(),
            weatherData.source() == null ? null : weatherData.source().code()
        );
    }
}
