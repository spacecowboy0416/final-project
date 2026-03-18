package com.finalproject.coordi.recommendation.service.payload;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import com.finalproject.coordi.recommendation.dto.api.UserRequestDto;
import com.finalproject.coordi.recommendation.dto.internal.ImageData;
import com.finalproject.coordi.recommendation.dto.internal.Location;
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
            toLocation(source.location()),
            toImageData(source.imageData())
        );
    }

    private GenderType toGenderType(UserRequestDto.GenderType source) {
        return source == null ? null : GenderType.valueOf(source.name());
    }

    private Location toLocation(UserRequestDto.LocationInfo source) {
        if (source == null) {
            return null;
        }
        return new Location(
            source.districtName(),
            source.placeName(),
            source.addressName(),
            source.latitude(),
            source.longitude()
        );
    }

    private ImageData toImageData(UserRequestDto.ImageData source) {
        if (source == null) {
            return null;
        }
        return new ImageData(source.imageBytes(), source.mimeType());
    }
}
