package com.finalproject.coordi.admin.service;

import com.finalproject.coordi.admin.dto.TagDto;

import com.finalproject.coordi.admin.mapper.AdminMapper;
import com.finalproject.coordi.admin.domain.user.User;
import com.finalproject.coordi.admin.domain.user.UserMapperInter;
import com.finalproject.coordi.admin.domain.user.dto.UserResponse;
import com.finalproject.coordi.admin.domain.user.dto.UserUpdateRequest;
import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.exception.user.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserMapperInter userMapperInter;
    private final AdminMapper adminMapper;

    public List<UserResponse> getAllUsers(String role, String searchTerm, String sort) {
        List<User> users = userMapperInter.findAll(role, searchTerm, sort);
        return users.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUser(Long userId, UserUpdateRequest request) {
        userMapperInter.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int updatedRows = userMapperInter.updateUser(userId, request.getRole(), request.getStatus());

        if (updatedRows == 0) {
            // This might happen if the provided data is the same as the existing data,
            // or if the user ID does not exist. The findById check should handle the
            // latter.
            // Depending on requirements, this could be a no-op or an error.
            // For now, we'll treat it as a potential issue to log or handle.
            // Consider what should happen if no rows are updated.
        }
    }

    @Transactional
    public void updateUserStatus(Long userId, String status) {
        userMapperInter.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        int updatedRows = userMapperInter.updateUser(userId, null, status);

        if (updatedRows == 0) {
            throw new RuntimeException("User status update failed for userId: " + userId);
        }
    }

    // Tag management
    public List<String> getTagTypes() {
        return adminMapper.selectTagTypes();
    }

    public List<TagDto> getTagsByType(String type) {
        return adminMapper.selectTagsByType(type);
    }

    @Transactional
    public TagDto addTag(TagDto tag) {
        try {
            adminMapper.insertTag(tag);
            return tag;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Transactional
    public void updateTag(Long tagId, String name) {
        if (adminMapper.updateTag(tagId, name) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    @Transactional
    public void deleteTag(Long tagId) {
        if (adminMapper.deleteTag(tagId) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
