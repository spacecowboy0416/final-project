package com.finalproject.coordi.recommendation.service.component;

import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto.WeatherInfo;
import com.finalproject.coordi.recommendation.dto.api.BlueprintInputDto;
import com.finalproject.coordi.recommendation.service.apiport.WeatherPort;
import java.time.OffsetDateTime;
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
    public WeatherContext fetch(BlueprintInputDto request) {
        var location = request.location();
        var weatherData = weatherPort.fetchByDistrict(
            location.districtName(),
            request.scheduleTime()
        );

        return new WeatherContext(
            request.scheduleTime(),
            weatherData.temperature(),
            weatherData.feelsLike(),
            weatherData.weatherStatus() == null ? null : weatherData.weatherStatus().code(),
            weatherData.rainProbability() == null ? null : weatherData.rainProbability().code(),
            location.placeName(),
            location.districtName(),
            location.addressName(),
            location.latitude(),
            location.longitude(),
            weatherData.source() == null ? null : weatherData.source().code()
        );
    }

    /**
     * Redis 날씨 매핑 결과와 프론트에서 넘어온 위치 컨텍스트를 함께 담아 PromptBuilder로 넘기는 내부 스냅샷 모델.
     * 이 클래스에만 쓰여서 dto에 넣지 않고 컴포넌트 내부에 정의했다.
     */
    public record WeatherContext(
        OffsetDateTime scheduleTime,
        Double temperature,
        Double feelsLike,
        String weatherStatus,
        String rainProbability,
        String placeName,
        String districtName,
        String addressName,
        Double latitude,
        Double longitude,
        String weatherSource
    ) {
        public WeatherInfo toWeatherInfo() {
            return new WeatherInfo(
                temperature,
                feelsLike,
                weatherStatus,
                rainProbability,
                weatherSource
            );
        }
    }
}
