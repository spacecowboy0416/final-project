package com.finalproject.coordi.config;

import com.finalproject.coordi.auth.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 및 폼 로그인 등 기본 인증 방식 비활성화
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            
            // 2. 세션 정책 설정 (테스트를 위해 필요 시 세션 생성)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            
            // 3. 인가(권한) 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login/**", "/oauth2/**", "/static/**", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            
            // 4. OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .defaultSuccessUrl("/", true) // 성공 시 메인 페이지
                .failureHandler((request, response, exception) -> {
                    // 로그인 실패(중복 가입 등) 시 원인 메시지를 쿼리 파라미터로 전달
                    String encodedMsg = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                    response.sendRedirect("/?error=true&message=" + encodedMsg);
                })
            );

        return http.build();
    }
}
