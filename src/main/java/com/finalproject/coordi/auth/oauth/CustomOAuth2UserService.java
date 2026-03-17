package com.finalproject.coordi.auth.oauth;

import com.finalproject.coordi.exception.auth.AuthFailedException;
import com.finalproject.coordi.users.dto.UsersDto;
import com.finalproject.coordi.users.service.UsersService;
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
import java.util.HashMap;
import java.util.Map;

// OAuth2 로그인 시 사용자 정보 처리
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UsersService usersService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuth2 Provider로부터 사용자 정보(Entity) 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // 사용자 정보 내의 속성값(Attributes)을 Map 형태로 가져오기
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 소셜 서비스 구분(ex.google, kakao, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 해당 서비스가 고유 식별자로 사용 키값 (ex. google-sub or kakao/naver-id)
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                                           .getUserInfoEndpoint().getUserNameAttributeName();

        // 소셜 서비스별로 제공되는 유저 정보를 공통 UserDto로 변환
        UsersDto usersDto = extractUsersDto(registrationId, attributes);
        
        // UserDto를 DB에 저장하거나 업데이트하여 최종 UserDto 반환
        UsersDto savedUser = usersService.saveOrUpdate(usersDto);
        
        // attributes는 수정 불가일 수 있으므로 복사해서 필요한 정보(isNewUser) 추가
        Map<String, Object> customAttributes = new HashMap<>(attributes);
        customAttributes.put("isNewUser", savedUser.isNewUser());

        // 최종적으로 Spring Security가 사용할 OAuth2User 객체 생성하여 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + savedUser.getRole())),
                customAttributes,
                userNameAttributeName
        );
    }

    // 각 소셜 서비스별로 제공되는 유저 정보를 공통 UserDto로 변환
    @SuppressWarnings("unchecked")
    private UsersDto extractUsersDto(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return UsersDto.builder()
                    .provider("google")
                    .providerUserId((String) attributes.get("sub"))
                    .email((String) attributes.get("email"))
                    .nickname((String) attributes.get("name"))
                    .profileImageUrl((String) attributes.get("picture"))
                    .build();
        } else if ("kakao".equals(registrationId)) {
            // 카카오의 경우 'kakao_account'에 이메일과 프로필 정보가 들어있음
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (kakaoAccount != null) ? (Map<String, Object>) kakaoAccount.get("profile") : null;

            // 권한 없거나 동의 안했으면 null
            String email = (kakaoAccount != null) ? (String) kakaoAccount.get("email") : null;
            String nickname = (profile != null) ? (String) profile.get("nickname") : "KakaoUser";
            String profileImageUrl = (profile != null) ? (String) profile.get("profile_image_url") : null;
            String providerUserId = String.valueOf(attributes.get("id"));

            // 이메일 없을 경우 소셜 ID를 이메일로 대체
            if (email == null) {
                email = providerUserId + "@kakao";
            }

            return UsersDto.builder()
                    .provider("kakao")
                    .providerUserId(providerUserId)
                    .email(email)
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .build();
        } else if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return UsersDto.builder()
                    .provider("naver")
                    .providerUserId((String) response.get("id"))
                    .email((String) response.get("email"))
                    .nickname((String) response.get("name"))
                    .profileImageUrl((String) response.get("profile_image"))
                    .build();
        }
        // 지원하지 않는 소셜 서비스인 경우 예외 처리
        throw new AuthFailedException();
    }
}
