package com.finalproject.coordi.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// JWT 토큰 생성, 검증, 인증 정보 추출
@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;//

    private Key key; // JWT 서명에 사용할 키 객체
    private final long ACCESS_TOKEN_EXP = 1000L * 60 * 30; // 30분
    //private final long ACCESS_TOKEN_EXP = 1000L * 60 * 1; // 테스트용: 1분 (중복 로그인 방지 확인 위해 짧게 설정)
    private final long REFRESH_TOKEN_EXP = 1000L * 60 * 60 * 24 * 7; // 7일

    @PostConstruct
    protected void init() {
        // Base64 인코딩된 키를 사용하여 Key 객체 생성
        byte[] keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    //Access Token 생성 (서버 저장 X)
    public String createAccessToken(Long userId, String role) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("role", role);

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXP))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    

    // Refresh Token 생성 (재발급을 위해 정보를 포함)
    public String createRefreshToken(Long userId, String role) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("role", role);

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXP))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 유저 정보 추출하여 Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String userId = claims.getSubject();
        String role = claims.get("role").toString();
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        User principal = new User(userId, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 토큰 유효성 검증(위조 및 만료 여부 확인)
    public boolean validateToken(String token) {
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        return true;
    }

    // 만료 여부와 상관없이 토큰에서 Claims를 추출 (재발급 로직용)
    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료되었어도 내부에 담긴 정보는 꺼낼 수 있음
        }
    }
}
