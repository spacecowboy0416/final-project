package com.finalproject.coordi.user.service;

import com.finalproject.coordi.exception.user.EmailDuplicationException;
import com.finalproject.coordi.user.dto.UserDto;
import com.finalproject.coordi.user.mapper.UserMapperInter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserMapperInter userMapper;

    // 이메일로 사용자 정보 조회
    public UserDto findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    // userId로 사용자 정보 조회
    public UserDto findByUserId(Long userId) {
        return userMapper.findByUserId(userId);
    }

    // OAuth2 로그인 성공 후 사용자 정보를 DB에 저장하거나 업데이트
    @Transactional
    public UserDto saveOrUpdate(UserDto userDto) {

        // 소셜 로그인 정보로 기존 사용자 조회
        UserDto existingUser = userMapper.findByProviderAndProviderUserId(userDto.getProvider(), userDto.getProviderUserId());

        if (existingUser != null) {
            // 기존 사용자 정보 최신화
            existingUser.setNickname(userDto.getNickname());
            existingUser.setProfileImageUrl(userDto.getProfileImageUrl());
            existingUser.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
            userMapper.update(existingUser);
            existingUser.setNewUser(false); // 기존 가입자임을 표시
            return existingUser;

        } else {
            // 이메일 중복 체크 (다른 소셜 계정 가입 여부 확인)
            UserDto emailUser = userMapper.findByEmail(userDto.getEmail());

            // 이메일이 이미 존재하는 경우 예외 처리
            if (emailUser != null) {
                throw new EmailDuplicationException();
            }

            // 신규 사용자 정보 등록
            userDto.setRole("USER");
            userDto.setStatus("ACTIVE");
            userDto.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            userDto.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
            userMapper.save(userDto);
            userDto.setNewUser(true); // 신규 가입자임을 표시
            return userDto;
        }
    }
}
