package com.finalproject.coordi.auth.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 모든 요청이 서버에 들어올 때마다 가장 먼저 실행되는 검문소
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 쿠키에서 AccessToken과 RefreshToken 추출
        String accessToken = resolveToken(request, "accessToken");
        String refreshToken = resolveToken(request, "refreshToken");

        // 2. AccessToken 유효성 검사
        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
            setAuthentication(accessToken);
        } 
        // 3. AccessToken 만료 시 RefreshToken으로 자동 갱신 시도
        else if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
            log.info("AccessToken 만료 감지. RefreshToken으로 갱신을 시도합니다.");
            
            // RefreshToken에서 정보 추출
            Claims claims = jwtProvider.parseClaims(refreshToken);
            Long userId = Long.parseLong(claims.getSubject());
            String role = (String) claims.get("role");

            // 새 AccessToken 생성
            String newAccessToken = jwtProvider.createAccessToken(userId, role);
            
            // 새 쿠키를 브라우저에 다시 전달
            ResponseCookie newCookie = ResponseCookie.from("accessToken", newAccessToken)
                    .path("/")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .maxAge(1800) // 30분
                    .build();
            response.addHeader("Set-Cookie", newCookie.toString());

            // 인증 정보 설정
            setAuthentication(newAccessToken);
            log.info("AccessToken 자동 갱신 완료 - User ID: {}", userId);
        }

        filterChain.doFilter(request, response);
    }

    // SecurityContext에 유저 정보를 등록
    private void setAuthentication(String token) {
        Authentication authentication = jwtProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    //브라우저가 보낸 모든 쿠키 중에서 원하는 이름의 쿠키만 가져오기
    private String resolveToken(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
