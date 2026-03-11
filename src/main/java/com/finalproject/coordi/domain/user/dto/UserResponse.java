package com.finalproject.coordi.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.finalproject.coordi.domain.user.User;
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

    public UserResponse(User user) {
        this.userId = user.getUserId();
        this.provider = user.getProvider();
        this.email = user.getEmail();
        this.nickname = user.getNickname();

        String originalUrl = user.getProfileImageUrl();
        if (originalUrl == null || originalUrl.isBlank()) {
            this.profileImageUrl = "https://i.pravatar.cc/150?u=default";
        } else {
            // Google 프로필 이미지 URL에서 사이즈 파라미터(=s96-c) 제거하여 원본 이미지를 요청
            this.profileImageUrl = originalUrl.replaceFirst("=s\\d+-c$", "");
        }

        this.role = user.getRole();
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
        this.lastLoginAt = user.getLastLoginAt();
    }
}
