package com.finalproject.coordi.config;

import com.finalproject.coordi.auth.handler.OAuth2SuccessHandler;
import com.finalproject.coordi.auth.jwt.JwtAuthenticationFilter;
import com.finalproject.coordi.auth.jwt.JwtProvider;
import com.finalproject.coordi.auth.oauth.CustomOAuth2UserService;
import com.finalproject.coordi.auth.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
// 애플리케이션 전역의 인가(Authorization) 및 인증(Authentication) 규칙을 관장하는 보안 설정 클래스입니다.
public class SecurityConfig {

        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final JwtProvider jwtProvider;
        private final RedisService redisService;

        // 애플리케이션으로 유입되는 HTTP 요청에 대한 세부 보안 필터 체인 규칙을 정의합니다.
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                
                                // RESTful API와 무상태(Stateless) 기반의 통신을 수행하므로, CSRF 방어 및 기본 로그인 폼 기능을 비활성화합니다.
                                .csrf(csrf -> csrf.disable()) 
                                .formLogin(form -> form.disable()) 
                                .httpBasic(basic -> basic.disable()) 

                                // JWT 토큰을 인증 수단으로 사용하기 위해 서버 측 세션을 생성하지 않도록 세션 관리 정책을 STATELESS로 설정합니다.
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // 세션리스 환경에서도 SecurityContext가 일관되게 유지될 수 있도록 명시적 저장 요구 조건을 해제합니다.
                                .securityContext(context -> context
                                                .requireExplicitSave(false))

                                // URI 경로별로 요구되는 접근 권한(인가) 수준을 지정합니다.
                                .authorizeHttpRequests(auth -> auth
                                                // 일반 관리자 및 최고 관리자 접근 허용 경로
                                                .requestMatchers("/admin/**", "/admin-management/**")
                                                .hasAnyRole("ADMIN", "MASTER")
                                                // 인증 절차 없이 누구나 자유롭게 접근할 수 있는 퍼블릭 엔드포인트를 지정합니다.
                                                .requestMatchers(
                                                        // 기본 페이지 및 인증
                                                        "/", "/oauth2/**", "/logout", "/recommend/**",
                                                        // 도메인별 리소스(js,css,image 등)
                                                        "/common/**", "/login/**", "/main/**", "/recommendation/**", "/user/**",
                                                        // 보완
                                                        "/favicon.ico", "/css/**", "/js/**", "/images/**", "/image/**", "/admin/images/**", "/error",
                                                        // 공개 API
                                                        "/api/main/**", "/api/recommendations/**")
                                                .permitAll()
                                                // 상기 명시되지 않은 모든 나머지 요청은 로그인된(인증된) 사용자만 허용합니다.
                                                .anyRequest().permitAll()  // [[[[[[개발완료되면 .authenticated()로 바꿔야 함]]]]]]
                                                )

                                // 외부 플랫폼(Google, Kakao 등)을 활용한 OAuth2 로그인 프로세스의 세부 흐름을 설정합니다.
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login") 
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService)) 
                                                .successHandler(oAuth2SuccessHandler) 
                                                .failureHandler((request, response, exception) -> {
                                                        log.error("소셜 로그인 실패: {}", exception.getMessage());

                                                        // 정지된 유저인 경우 (OAuth2AuthenticationException 에러코드 기반)
                                                        if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
                                                                String errorCode = oauth2Ex.getError().getErrorCode();
                                                                if ("suspended_user".equals(errorCode)) {
                                                                        response.sendRedirect("/?error=suspended_user");
                                                                        return;
                                                                }
                                                        }

                                                        // 그 외 소셜 인증 실패 처리
                                                        String msg = exception.getMessage();
                                                        String encodedMsg = URLEncoder.encode(msg != null ? msg : "인증 실패", StandardCharsets.UTF_8);
                                                        response.sendRedirect("/?error=true&message=" + encodedMsg);
                                                }))

                                // 로그아웃을 수행할 시점에 기존에 발급된 JWT 인증 쿠키를 브라우저에서 파기하는 처리 로직을 연결합니다.
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .addLogoutHandler((request, response, authentication) -> {
                                                        //로그아웃 시 Redis에서도 토큰 삭제
                                                        if (authentication != null && authentication.getName() != null) {
                                                                try {
                                                                        Long userId = Long.parseLong(authentication.getName());
                                                                        redisService.deleteRefreshToken(userId);
                                                                } catch (Exception e) {
                                                                        // 에러 무시 (이미 로그아웃된 경우 등)
                                                                }
                                                        }
                                                })
                                                .logoutSuccessUrl("/")
                                                .deleteCookies("accessToken", "refreshToken")
                                                .invalidateHttpSession(true))

                                // Spring Security의 기본 인증 필터가 동작하기 이전에 커스텀 JWT 필터(JwtAuthenticationFilter)를 먼저 수행하여 토큰의 유효성을 우선 검증합니다.
                                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, redisService),
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
