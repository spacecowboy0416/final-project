package com.finalproject.coordi.user.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Alias("udto")
public class UserDto {
    private Long userId;
    private String provider;
    private String providerUserId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private String status;
    private Timestamp createdAt;
    private Timestamp lastLoginAt;

    // DB에 저장되지 않는 필드: 신규 가입 여부 확인용
    private boolean isNewUser;
}
