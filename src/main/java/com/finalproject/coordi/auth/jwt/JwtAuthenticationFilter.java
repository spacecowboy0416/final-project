package com.finalproject.coordi.auth.jwt;

import com.finalproject.coordi.auth.service.RedisService;
import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.exception.auth.InvalidTokenException;
import com.finalproject.coordi.exception.auth.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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
    private final RedisService redisService; // Redis 연동 추가

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 1. 쿠키에서 AccessToken과 RefreshToken 추출
        String accessToken = resolveToken(request, "accessToken");
        String refreshToken = resolveToken(request, "refreshToken");

        // 2. Token 유효성 검사
        try {
            // 2-1. AccessToken이 유효한 경우 -> 통과
            if (accessToken != null && jwtProvider.validateToken(accessToken)) {
                // 통과
                setAuthentication(accessToken);
            }
        } catch (ExpiredJwtException e) {
            // 2-2. AccessToken이 만료되었지만 RefreshToken이 유효한 경우 -> AccessToken 재발급
            if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
                // RefreshToken에서 정보 추출
                Claims claims = jwtProvider.parseClaims(refreshToken);
                Long userId = Long.parseLong(claims.getSubject());
                String role = (String) claims.get("role");

                // Redis에 저장된 최신 토큰과 현재 브라우저의 토큰을 대조 (중복 로그인 방지 핵심)
                String savedRefreshToken = redisService.getRefreshToken(userId);
                if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
                    log.warn(ErrorCode.DUPLICATE_LOGIN.getCode()); // 중복 로그인 로그
                    clearTokenCookies(response); // 잘못된 토큰 쿠키를 즉시 삭제
                    response.sendRedirect("/?error=" + ErrorCode.DUPLICATE_LOGIN.getCode()); // 메인 페이지로 리다이렉트
                    return;
                }
                // [정지 유저 체크] 실시간으로 정지된 계정인지 확인합니다.
                if (redisService.isSuspendedUser(userId)) {
                    log.warn(ErrorCode.USER_SUSPENDED.getCode()); // 정지 유저 로그
                    clearTokenCookies(response);
                    response.sendRedirect("/?error=" + ErrorCode.USER_SUSPENDED.getCode());
                    return;
                }

                // AccessToken 재발급
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

                // 통과
                setAuthentication(newAccessToken);
            } else {
                // 리프레시 토큰도 없거나 만료된 경우 쿠키 삭제 후 통과
                clearTokenCookies(response);
                throw new TokenExpiredException();
            }
        } catch (Exception e) {
            // 2-3. 토큰이 위조되었거나 잘못된 경우 (Sentry 수집을 위해 예외 던짐)
            throw new InvalidTokenException();
        }

        filterChain.doFilter(request, response);
    }

    // 브라우저의 인증 쿠키를 강제로 만료시켜 삭제
    private void clearTokenCookies(HttpServletResponse response) {
        deleteCookie(response, "accessToken");
        deleteCookie(response, "refreshToken");
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    // JWT 토큰에서 인증 정보를 추출하여 SecurityContext에 저장
    private void setAuthentication(String token) {
        Authentication authentication = jwtProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 브라우저가 보낸 모든 쿠키 중에서 원하는 이름의 쿠키만 가져오기
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
