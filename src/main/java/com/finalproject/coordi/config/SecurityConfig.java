package com.finalproject.coordi.config;

import com.finalproject.coordi.auth.handler.OAuth2SuccessHandler;
import com.finalproject.coordi.auth.jwt.JwtAuthenticationFilter;
import com.finalproject.coordi.auth.jwt.JwtProvider;
import com.finalproject.coordi.auth.oauth.CustomOAuth2UserService;
import com.finalproject.coordi.auth.service.RedisService;
import com.finalproject.coordi.exception.ErrorCode;
import com.finalproject.coordi.exception.auth.OAuth2SuspendedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// 애플리케이션 전역 보안 및 인가 정책 설정
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

<<<<<<< HEAD
        // 정적 리소스 자원(CSS, JS, 이미지 등)에 대한 요청은 보안 검증 절차를 생략하도록 필터망 적용을 예외 처리(ignoring)합니다.
        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
                return (web) -> web.ignoring()
                                .requestMatchers("/css/**", "/js/**", "/images/**", "/admin/images/**", "/favicon.ico",
                                                "/error");
        }
=======
    // 보안 필터 체인 세부 구성 로직
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 및 기본 로그인 폼 비활성화 제어 (무상태 인증 방식)
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
>>>>>>> dbc4007ba0b81319092b9a595ada264e00c298ae

                // 세션 정책 JWT 사용 무상태(STATELESS) 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 세션리스 환경 SecurityContext 유지 설정
                .securityContext(context -> context
                        .requireExplicitSave(false))

                // URL 경로별 접근 권한 제어 로직
                .authorizeHttpRequests(auth -> auth
                        // 관리자 전용 경로 접근 권한 설정
                        .requestMatchers("/admin/super/**").hasRole("MASTER")
                        .requestMatchers("/admin/**", "/admin-management/**").hasAnyRole("ADMIN", "MASTER")
                        
                        // 로그인 필수 페이지 권한 설정
                        .requestMatchers("/board/write", "/board/*/edit").authenticated()
                        
                        // 인증 없이 접근 가능한 퍼블릭 경로 및 정적 리소스 접근 허용 로직
                        .requestMatchers(
                                "/", "/oauth2/**", "/logout", "/recommend/**",
                                "/common/**", "/login/**", "/main/**", "/recommendation/**", "/user/**",
                                "/board", "/board/*", "/board/css/**", "/board/js/**",
                                "/favicon.ico", "/css/**", "/js/**", "/images/**", "/image/**", "/admin/images/**", "/error",
                                "/api/main/**", "/api/recommendations/**"
                        ).permitAll()
                        
                        // 게시판 조회 API 공개 허용 로직
                        .requestMatchers(HttpMethod.GET,
                                "/api/board/posts",
                                "/api/board/posts/*",
                                "/api/board/posts/*/comments"
                        ).permitAll()

                        // 기타 모든 요청 퍼블릭 허용 (개발 완료 시 .authenticated() 전환 필요)
                        .anyRequest().permitAll())

                // OAuth2 소셜 로그인 파이프라인 구성 로직
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("소셜 로그인 인증 실패: {}", exception.getMessage());
                            
                            // 정지 유저 접속 차단 제어 로직
                            if (exception instanceof OAuth2SuspendedException) {
                                log.warn("정지 유저 접속 시도 차단: {}", ErrorCode.USER_SUSPENDED.getCode());
                                response.sendRedirect("/?error=" + ErrorCode.USER_SUSPENDED.getCode());
                                return;
                            }

                            // 일반 인증 실패 처리 및 Sentry 에러 기록 연동 로직
                            io.sentry.Sentry.captureException(exception);
                            String msg = (exception.getMessage() != null) ? exception.getMessage() : ErrorCode.AUTH_FAILED.getMessage();
                            String encodedMsg = URLEncoder.encode(msg, StandardCharsets.UTF_8);
                            response.sendRedirect("/?error=true&message=" + encodedMsg);
                        }))

                // 로그아웃 및 Redis 토큰 파기 제어 로직
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            if (authentication != null && authentication.getName() != null) {
                                try {
                                    Long userId = Long.parseLong(authentication.getName());
                                    redisService.deleteRefreshToken(userId);
                                } catch (Exception e) {
                                    log.debug("로그아웃 시 Redis 토큰 삭제 스킵: {}", e.getMessage());
                                }
                            }
                        })
                        .logoutSuccessUrl("/")
                        .deleteCookies("accessToken", "refreshToken")
                        .invalidateHttpSession(true))

                // 커스텀 JWT 인증 필터 주입 로직
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, redisService), 
                                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}