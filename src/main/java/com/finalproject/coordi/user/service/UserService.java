package com.finalproject.coordi.user.service;

import com.finalproject.coordi.user.dto.UserDto;
import com.finalproject.coordi.user.mapper.UserMapperInter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserMapperInter userMapper;

    //이메일로 사용자 정보를 조회
    public UserDto findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    //PK(userId)로 사용자 정보를 조회
    public UserDto findById(Long userId) {
        return userMapper.findById(userId);
    }

    // OAuth2 로그인 성공 후 사용자 정보를 DB에 저장하거나 업데이트
    @Transactional
    public UserDto saveOrUpdate(UserDto userDto) {
        log.info("사용자 확인 중 - Email: {}, Provider: {}", userDto.getEmail(), userDto.getProvider());

        // 1. 해당 소셜 계정으로 이미 가입된 이력이 있는지 확인
        UserDto existingUser = userMapper.findByProviderAndProviderUserId(userDto.getProvider(), userDto.getProviderUserId());

        if (existingUser != null) {
            // 기존 사용자 정보 최신화
            log.info("기존 소셜 계정 사용자 발견 (ID: {}). 정보를 최신화합니다.", existingUser.getUserId());
            existingUser.setNickname(userDto.getNickname());
            existingUser.setProfileImageUrl(userDto.getProfileImageUrl());
            existingUser.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
            userMapper.update(existingUser);
            return existingUser;
        } else {
            // 2. 이메일 중복 체크 (다른 소셜 계정 가입 여부 확인)
            UserDto emailUser = userMapper.findByEmail(userDto.getEmail());
            if (emailUser != null) {
                log.warn("중복 이메일 가입 시도 거절 - Email: {}, 기존 가입경로: {}", 
                         emailUser.getEmail(), emailUser.getProvider());
                throw new OAuth2AuthenticationException("이미 " + emailUser.getProvider() + " 계정으로 가입된 이메일입니다.");
            }

            // 3. 신규 사용자 등록
            log.info("신규 사용자 등록을 시작합니다: {}", userDto.getEmail());
            userDto.setRole("USER");
            userDto.setStatus("ACTIVE");
            userDto.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            userDto.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
            userMapper.save(userDto);
            return userDto;
        }
    }
}
