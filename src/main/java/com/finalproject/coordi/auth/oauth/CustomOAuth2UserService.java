package com.finalproject.coordi.auth.oauth;

import com.finalproject.coordi.domain.exception.auth.AuthFailedException;
import com.finalproject.coordi.user.dto.UserDto;
import com.finalproject.coordi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 소셜 로그인 시 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                                           .getUserInfoEndpoint().getUserNameAttributeName();

        log.info("OAuth2 로그인 처리 시작 - 서비스: {}", registrationId);
        
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // 2. 서비스별 유저 정보 통합 추출 (Google, Kakao, Naver 대응)
        UserDto userDto = extractUserDto(registrationId, attributes);
        
        // 3. 비즈니스 로직을 통한 DB 저장 또는 업데이트
        UserDto savedUser = userService.saveOrUpdate(userDto);
        
        // 4. 권한 정보를 담아 SecurityContext용 OAuth2User 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + savedUser.getRole())),
                attributes,
                userNameAttributeName
        );
    }

    /**
     * 각 소셜 서비스별로 제공되는 유저 정보를 공통 UserDto로 변환합니다.
     */
    @SuppressWarnings("unchecked")
    private UserDto extractUserDto(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return UserDto.builder()
                    .provider("google")
                    .providerUserId((String) attributes.get("sub"))
                    .email((String) attributes.get("email"))
                    .nickname((String) attributes.get("name"))
                    .profileImageUrl((String) attributes.get("picture"))
                    .build();
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            return UserDto.builder()
                    .provider("kakao")
                    .providerUserId(String.valueOf(attributes.get("id")))
                    .email((String) kakaoAccount.get("email"))
                    .nickname((String) profile.get("nickname"))
                    .profileImageUrl((String) profile.get("profile_image_url"))
                    .build();
        } else if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return UserDto.builder()
                    .provider("naver")
                    .providerUserId((String) response.get("id"))
                    .email((String) response.get("email"))
                    .nickname((String) response.get("name"))
                    .profileImageUrl((String) response.get("profile_image"))
                    .build();
        }
        throw new AuthFailedException();
    }
}
