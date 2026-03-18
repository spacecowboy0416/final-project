package com.finalproject.coordi.recommendation.dto.internal;

import com.finalproject.coordi.recommendation.domain.enums.CoordinationEnums.GenderType;
import java.time.OffsetDateTime;

public record UserRequest(
    String naturalText,
    GenderType gender,
    OffsetDateTime scheduleTime,
    Location location,
    ImageData imageData
) {
}
