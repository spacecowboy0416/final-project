package com.finalproject.coordi.recommendation.domain.enums;

public interface CodedKeywordedEnum extends CodedEnum {
    String getKeyword(); // 키워드가 필요한 곳에서만 사용
}