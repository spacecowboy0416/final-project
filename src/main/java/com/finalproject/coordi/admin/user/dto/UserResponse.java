package com.finalproject.coordi.admin.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finalproject.coordi.admin.user.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponse {
    private Long userId;
    private String provider;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    // User 엔티티를 프론트엔드 전달용 DTO로 변환하며, 이 과정에서 사용자 경험을 위해 프로필 이미지 URL을 가공함.
    public UserResponse(User user) {
        this.userId = user.getUserId();
        this.provider = user.getProvider();
        this.email = user.getEmail();
        this.nickname = user.getNickname();

        String originalUrl = user.getProfileImageUrl();
        if (originalUrl == null || originalUrl.isBlank()) {
            // 프로필 이미지가 없는 경우, 기본 아바타 이미지를 제공함.
            this.profileImageUrl = "https://i.pravatar.cc/150?u=default";
        } else {
            // Google 프로필 이미지 URL에서 사이즈 파라미터(=s96-c)를 제거하여 고화질 원본 이미지를 요청함.
            this.profileImageUrl = originalUrl.replaceFirst("=s\\d+-c$", "");
        }

        this.role = user.getRole();
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
        this.lastLoginAt = user.getLastLoginAt();
    }
}
