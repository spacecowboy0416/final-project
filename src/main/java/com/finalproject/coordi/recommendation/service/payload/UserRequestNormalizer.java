package com.finalproject.coordi.recommendation.service.payload;

import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.PayloadDto;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * 사용자 요청 정규화를 담당하는 컴포넌트.
 * 정규화된 데이터 구조(Record)를 내부에 포함하여 응집도를 높였다.
 */
@Component
public class UserRequestNormalizer {

    /**
     * 외부 입력을 받아 정규화된 레코드로 변환한다.
     */
    public NormalizedUserRequest normalize(PayloadDto source) {
        return new NormalizedUserRequest(
            source.naturalText(),
            source.gender().name().toLowerCase(Locale.ROOT),
            String.valueOf(source.weather().feelsLike()),
            source.weather().weatherStatus(),
            source.weather().rainProbability(),
            new UserRequestDto.PayloadDto.PayloadImageData(
                source.image().imageBytes(),
                source.image().mimeType()
            )
        );
    }

    public record NormalizedUserRequest(
        String naturalText,
        String gender,
        String feelsLike,
        String weatherStatus,
        String rainProbability,
        UserRequestDto.PayloadDto.PayloadImageData imageData
    ) { }
}
