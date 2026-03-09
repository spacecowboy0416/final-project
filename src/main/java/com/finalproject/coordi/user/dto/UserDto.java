package com.finalproject.coordi.user.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Alias("udto")
public class UserDto {
    private Long userId;            // user_id
    private String provider;        // provider (google, kakao 등)
    private String providerUserId;  // provider_user_id (구글 고유 ID)
    private String email;           // email
    private String nickname;        // nickname
    private String profileImageUrl; // profile_image_url
    private String role;            // role (기본값 USER)
    private String status;          // status (기본값 ACTIVE)
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
