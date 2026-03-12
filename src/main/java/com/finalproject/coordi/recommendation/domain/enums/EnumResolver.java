package com.finalproject.coordi.recommendation.domain.enums;

/**
 * code 문자열을 열거형 상수로 해석하기 위한 공통 유틸.
 * JSON 역직렬화 시 외부에서 전달된 code 값을 내부 enum 상수로 변환할 때 사용한다.
 */
public final class EnumResolver {
    private EnumResolver() {
    }

    public static <E extends Enum<E> & CodedEnum> E fromCode(Class<E> enumType, String rawCode) {
        if (rawCode == null) {
            return null;
        }
        for (E value : enumType.getEnumConstants()) {
            if (value.code().equalsIgnoreCase(rawCode)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown " + enumType.getSimpleName() + " code: " + rawCode);
    }
}