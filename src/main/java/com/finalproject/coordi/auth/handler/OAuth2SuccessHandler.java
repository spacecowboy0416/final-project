package com.finalproject.coordi.auth.handler;

import com.finalproject.coordi.auth.jwt.JwtProvider;
import com.finalproject.coordi.user.dto.UserDto;
import com.finalproject.coordi.user.service.UserService;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // 1. 유저 정보 추출
        String email = (String) attributes.get("email");
        UserDto user = userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. JWT 토큰 생성 (Stateless)
        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken();

        // 3. 보안 쿠키 설정 (HttpOnly, Secure, SameSite=Strict)
        setTokenCookie(response, "accessToken", accessToken, 1800); // 30분
        setTokenCookie(response, "refreshToken", refreshToken, 604800); // 7일

        log.info("JWT 발급 및 쿠키 설정 완료 - User: {}", email);
        
        // 4. 메인 페이지로 리다이렉트
        response.sendRedirect("/");
    }

    private void setTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(true) // HTTPS가 아닐 경우 작동하지 않을 수 있으나 보안상 권장
                .sameSite("Strict") // CSRF 방어
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
