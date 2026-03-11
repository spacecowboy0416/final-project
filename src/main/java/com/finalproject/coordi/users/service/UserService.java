package com.finalproject.coordi.users.service;

import com.finalproject.coordi.users.dto.UserDto;
import com.finalproject.coordi.users.mapper.UserMapperInter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserMapperInter userMapper;

    /**
     * 이메일로 사용자 정보를 조회합니다. (로그인 후 정보 획득용)
     */
    public Optional<UserDto> findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    /**
     * OAuth2 로그인 성공 후 사용자 정보를 DB에 저장하거나 업데이트합니다.
     * 이메일 중복 체크를 통해 1인 1계정 원칙을 고수합니다.
     */
    @Transactional
    public UserDto saveOrUpdate(UserDto userDto) {
        log.info("사용자 확인 중 - Email: {}, Provider: {}", userDto.getEmail(), userDto.getProvider());

        // 1. 해당 소셜 계정(Provider + ProviderUserId)으로 이미 가입된 이력이 있는지 확인
        return userMapper.findByProviderAndProviderUserId(userDto.getProvider(), userDto.getProviderUserId())
                .map(existingUser -> {
                    log.info("기존 소셜 계정 사용자 발견 (ID: {}). 정보를 최신화합니다.", existingUser.getUserId());
                    existingUser.setNickname(userDto.getNickname());
                    existingUser.setProfileImageUrl(userDto.getProfileImageUrl());
                    existingUser.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
                    userMapper.update(existingUser);
                    return existingUser;
                })
                .orElseGet(() -> {
                    // 2. 소셜 계정은 처음이지만, 같은 이메일로 이미 다른 소셜 가입이 되어있는지 확인 (중복 가입 방지)
                    userMapper.findByEmail(userDto.getEmail()).ifPresent(existingEmailUser -> {
                        log.warn("중복 이메일 가입 시도 거절 - Email: {}, 기존 가입경로: {}", 
                                 existingEmailUser.getEmail(), existingEmailUser.getProvider());
                        // OAuth2AuthenticationException을 던지면 SecurityConfig의 failureHandler에서 처리됩니다.
                        throw new OAuth2AuthenticationException("이미 " + existingEmailUser.getProvider() + " 계정으로 가입된 이메일입니다.");
                    });

                    // 3. 완전히 새로운 사용자라면 신규 저장
                    log.info("신규 사용자 등록을 시작합니다: {}", userDto.getEmail());
                    userDto.setRole("USER");
                    userDto.setStatus("ACTIVE");
                    userDto.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    userDto.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
                    userMapper.save(userDto);
                    return userDto;
                });
    }
}
