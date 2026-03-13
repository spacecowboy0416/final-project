## 🏗️ 1. 모듈 구조 (Module Structure)

com.finalproject.coordi
├── config
│ ├── SecurityConfig.java # [보안 설정] 시큐리티 정책 및 접근 권한 제어 총괄
│ └── WebConfig.java # [웹 설정] @LoginUser 등 어노테이션 등록
├── auth # [인증 도메인]
│ ├── handler
│ │ └── OAuth2SuccessHandler.java # [JWT 발급] 로그인 성공 시 보안 쿠키 생성
│ ├── jwt
│ │ ├── JwtProvider.java # [토큰 엔진] JWT 생성, 검증 및 파싱
│ │ └── JwtAuthenticationFilter.java # [인증 필터] 매 요청마다 토큰 유효성 검사
│ └── oauth
│ └── CustomOAuth2UserService.java # [정보 추출] 소셜 서비스별 데이터 통합 추출
├── user # [사용자 관리]
│ ├── annotation # [커스텀] @LoginUser 관련 로직
│ ├── dto
│ │ └── UserDto.java # [데이터] 사용자 정보 객체
│ ├── service
│ │ └── UserService.java # [로직] 회원가입, 중복 방지, 관리자 지정
│ └── mapper
│ └── UserMapperInter.java # [DB 통신] 유저 정보 SQL 실행 인터페이스
└── exception # [예외 관리]
├── auth # 인증 관련 커스텀 예외들
└── user # 사용자 관련 커스텀 예외들

## 🔄 2. 주요 프로세스 흐름 (Process Flow)

1. 소셜 로그인: 구글/카카오 로그인 성공 시 유저 정보 추출 (CustomOAuth2UserService)
2. 회원 관리: 이메일 중복 체크 및 신규 가입/기존 유저 업데이트 (UserService, UserMapperInter)
3. JWT 발급 및 저장: Access Token(30분)과 Refresh Token(7일)을 생성함. 보안을 위해 HttpOnly 및 SameSite=Lax 속성이 적용된 브라우저 쿠키에 토큰 저장 (OAuth2SuccessHandler, JwtProvider)
4. 인증 유지: 브라우저 요청 시 쿠키에 담긴 JWT를 검사하여 로그인 상태 유지 (JwtAuthenticationFilter, JwtProvider)
5. 자동 토큰 재발급: Access Token 만료 시 Refresh Token을 확인하여 자동으로 새 토큰을 발급(JwtAuthenticationFilter, JwtProvider)
6. 로그아웃: /logout 요청 시 모든 토큰 쿠키를 삭제하여 인증을 해제 (SecurityConfig)

## 📄 3. 파일별 상세 역할

- SecurityConfig: 서비스 전체의 보안 정책 설정 (권한 제어, 로그인/로그아웃 경로 등)
- WebConfig: @LoginUser 어노테이션을 인식하기 위한 리졸버 등록
- OAuth2SuccessHandler: 소셜 로그인 성공 시 JWT를 발급 받아 보안 쿠키에 저장 후 사용자에게 지급
- JwtProvider: JWT 생성, 유효성 검증 및 데이터 추출을 담당하는 핵심 컴포넌트
- JwtAuthenticationFilter: 모든 요청에서 쿠키에 담긴 토큰을 검사하여 인증 처리
- CustomOAuth2UserService: 구글/카카오/네이버 등에서 받은 유저 데이터를 공통 규격으로 변환
- UserService: DB와 연동하여 회원가입, 정보 수정 및 중복 가입 방지 로직 수행
- UserDto: 시스템 내에서 유저 정보를 전달하기 위한 데이터 객체
- UserMapperInter: MyBatis를 사용하여 실제 DB 쿼리를 실행하는 인터페이스

> 💡 팀원 가이드:
>
> - 컨트롤러에서 유저 정보가 필요하면 @LoginUser UserDto user를 사용하세요.
