package com.finalproject.coordi.recommendation.service.payload;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.UserRequest;
import org.springframework.stereotype.Component;

/**
 * 인바운드 API 요청 DTO를 recommendation 내부 요청 모델로 변환한다.
 */
@Component
public class UserRequestMapper {
    public UserRequest map(UserRequestDto source) {
        if (source == null) {
            return null;
        }
        return new UserRequest(
            source.naturalText(),
            toGenderType(source.gender()),
            source.scheduleTime(),
            toWeather(source.weather())
        );
    }

    private GenderType toGenderType(UserRequestDto.GenderType source) {
        return source == null ? null : GenderType.valueOf(source.name());
    }

    private com.finalproject.coordi.recommendation.dto.internal.Weather toWeather(UserRequestDto.WeatherInput source) {
        if (source == null) {
            return null;
        }
        return new com.finalproject.coordi.recommendation.dto.internal.Weather(
            source.temperature(),
            source.feelsLike(),
            source.status().name(), // or maybe we shouldn't pass name? wait, WeatherStatusType is enum.
            null, // rainProbability 
            "USER_PROVIDED" // source
        );
    }
}
