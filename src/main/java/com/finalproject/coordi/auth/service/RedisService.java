package com.finalproject.coordi.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // RefreshToken을 Redis에 저장합니다.
    // 동일한 유저 ID로 다시 저장하면 기존 토큰은 자동으로 폐기 (중복 로그인 방지)
    public void saveRefreshToken(Long userId, String token) {
        redisTemplate.opsForValue().set(
                "RT:" + userId, 
                token, 
                7, // 유효 기간을 7일
                TimeUnit.DAYS
        );
    }

    // Redis에서 유저의 현재 활성화된 RefreshToken을 조회
    public String getRefreshToken(Long userId) {
        return (String) redisTemplate.opsForValue().get("RT:" + userId);
    }

    // 로그아웃 시 Redis에서 토큰 정보를 삭제합
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("RT:" + userId);
    }
}
