package com.finalproject.coordi.admin.user.service;

import com.finalproject.coordi.admin.user.domain.User;
import com.finalproject.coordi.admin.user.dto.UserResponse;
import com.finalproject.coordi.admin.user.mapper.UserMapperInter;
import com.finalproject.coordi.exception.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserMapperInter userMapperInter;

    // 동적인 조건(역할, 검색어, 정렬)에 맞춰 DB에서 사용자 목록을 조회하고, 프론트엔드에 전달할 DTO로 변환하기 위함.
    public List<UserResponse> getAllUsers(String role, String searchTerm, String sort) {
        List<User> users = userMapperInter.findAll(role, searchTerm, sort);
        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    // 사용자 역할을 안전하게 변경하기 위해, 대상 사용자가 존재하는지 먼저 확인한 후 업데이트를 수행함.
    @Transactional
    public void updateUserRole(Long userId, String role) {
        // findById로 사용자 존재 여부를 먼저 검증하고, 없으면 예외를 발생시켜 비정상적인 업데이트를 막음.
        userMapperInter.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int updatedRows = userMapperInter.updateUser(userId, role, null);

        if (updatedRows == 0) {
            throw new RuntimeException("User role update failed for userId: " + userId);
        }
    }

    // 사용자 상태를 안전하게 변경하기 위해, 대상 사용자가 존재하는지 먼저 확인한 후 업데이트를 수행함.
    @Transactional
    public void updateUserStatus(Long userId, String status) {
        // findById로 사용자 존재 여부를 먼저 검증하고, 없으면 예외를 발생시켜 비정상적인 업데이트를 막음.
        userMapperInter.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int updatedRows = userMapperInter.updateUserStatus(userId, status);

        if (updatedRows == 0) {
            throw new RuntimeException("User status update failed for userId: " + userId);
        }
    }
}
