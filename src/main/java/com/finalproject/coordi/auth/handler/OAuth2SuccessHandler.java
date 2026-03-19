package com.finalproject.coordi.auth.handler;

import com.finalproject.coordi.auth.jwt.JwtProvider;
import com.finalproject.coordi.auth.service.RedisService;
import com.finalproject.coordi.exception.user.UserNotFoundException;
import com.finalproject.coordi.users.dto.UsersDto;
import com.finalproject.coordi.users.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

// OAuth2 로그인 성공 시 호출되어 JWT 토큰 발급받고 보안 쿠키로 설정
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UsersService usersService;
    private final RedisService redisService; // Redis 연동 추가

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 1. 유저 정보 추출 (이메일이 없어도 유저를 식별할 수 있도록 서비스명과 고유 ID를 사용)
        String provider = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        String providerUserId = oAuth2User.getName();

        UsersDto user = usersService.findByProviderAndProviderUserId(provider, providerUserId);

        // 유저 정보가 없는 경우 예외 처리
        if (user == null)
            throw new UserNotFoundException();

        // 가입 상태 확인
        boolean isNewUser = (boolean) attributes.getOrDefault("isNewUser", false);

        // 2. JWT 토큰 생성 (Stateless 방식 - userId와 role만 포함)
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getRole());

        // 2.5 Redis에 RefreshToken 저장 (중복 로그인 방지 핵심 로직)
        redisService.saveRefreshToken(user.getUserId(), refreshToken);

        // 3. 보안 쿠키 설정 (HttpOnly, Secure, SameSite=Lax)
        setTokenCookie(response, "accessToken", accessToken, 1800); // 30분
        setTokenCookie(response, "refreshToken", refreshToken, 604800); // 7일
        
        // 메인 페이지로 리다이렉트 (신규 가입 여부 파라미터 추가)
        String targetUrl = "/?login=success&isNew=" + isNewUser;
        response.sendRedirect(targetUrl);
    }

    // 보안 쿠키 설정 메서드
    private void setTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(true) // HTTPS 환경 권장 (로컬 localhost에선 브라우저가 허용)
                .sameSite("Lax") // CSRF 방어 강화 (외부 리다이렉트 대응을 위해 Lax 권장)
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
