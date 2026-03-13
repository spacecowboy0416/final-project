package com.finalproject.coordi.config;

import com.finalproject.coordi.auth.handler.OAuth2SuccessHandler;
import com.finalproject.coordi.auth.jwt.JwtAuthenticationFilter;
import com.finalproject.coordi.auth.jwt.JwtProvider;
import com.finalproject.coordi.auth.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 및 기본 인증 비활성화
            .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (API 개발 시 보통 끔)
            .formLogin(form -> form.disable()) // 기본 로그인 페이지 비활성화
            .httpBasic(basic -> basic.disable()) // HTTP Basic 인증 비활성화(이거 없애면 로그인 창 뜸)
            
            // 2. 세션 정책 설정: STATELESS (JWT 사용)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Stateless 환경에서도 필터에서 설정한 SecurityContext를 유지하도록 설정
            .securityContext(context -> context
                .requireExplicitSave(false)
            )
            
            // 3. 인가 설정
            .authorizeHttpRequests(auth -> auth
                // 마스터만
                .requestMatchers("/admin/super/**").hasRole("MASTER")
                // 관리자 및 마스터만
                .requestMatchers("/admin/**","/admin-management/**").hasAnyRole("ADMIN", "MASTER")
                // 인증 없이 누구나 접근 가능
                .requestMatchers("/", "/login/**", "/oauth2/**", "/static/**", "/css/**", "/js/**", "/common/**").permitAll()
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            
            // 4. OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2SuccessHandler) // 성공 핸들러 등록
                .failureHandler((request, response, exception) -> {
                    String encodedMsg = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                    response.sendRedirect("/?error=true&message=" + encodedMsg);
                })
            )
            
            // 5. 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("accessToken", "refreshToken")
            )
            
            // 6. JWT 필터 등록 (UsernamePasswordAuthenticationFilter 이전에 실행)
            .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}