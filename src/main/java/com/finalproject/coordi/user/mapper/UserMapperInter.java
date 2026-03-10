package com.finalproject.coordi.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.finalproject.coordi.user.dto.UserDto;

import java.util.Optional;

@Mapper
public interface UserMapperInter {
    // 소셜 로그인 정보로 유저 존재 여부 확인
    Optional<UserDto> findByProviderAndProviderUserId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);
    
    // 이메일로 사용자 조회
    Optional<UserDto> findByEmail(String email);
    
    // 신규 유저 저장
    void save(UserDto user);
    
    // 유저 정보 업데이트 (로그인 시마다 프로필 정보 갱신)
    void update(UserDto user);
}
