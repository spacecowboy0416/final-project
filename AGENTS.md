# Team Coding Guidelines v1

항상 한국어로 대답하고 주석을 적는다.

## 0. 적용 범위
- recommendation 모듈의 모든 신규/수정 코드에 적용한다.
- 리팩터링/기능 개발 시 본 문서를 우선 참조한다.

## 1. 지시 전 코드 수정 금지
- 명시적인 수정 지시 전까지는 코드를 변경하지 않는다.
- 분석, 설계, 리뷰, 구조 파악을 우선 수행한다.
- 비즈니스 로직 구현을 시작한 이후 구조 변경이 발생하면 DTO, Mapper, Schema 정합성을 즉시 함께 수정한다.

## 2. 하드코딩 금지
- 문자열, 숫자, 경로, 정책값을 코드에 직접 박아 넣지 않는다.
- 상수, 설정값, enum, 정책 객체로 분리한다.

## 3. 네이밍 전 기존 구조 참조
- 클래스, 메서드, DTO, 패키지 네이밍 전에 기존 파일과 폴더 트리를 먼저 확인한다.
- 신규 이름은 현재 모듈의 패턴과 용어를 우선적으로 따른다.

## 4. 클린 코드 네이밍
- 패키지명이 이미 `recommendation`이면 클래스명에 중복 접두어를 붙이지 않는다.
- 예: `DraftService`, `ScoringService`, `OrchestratorService`.

## 5. 네이밍 일관성
- Port/Adapter는 한 쌍 규칙으로 통일한다.
- 예: `GeminiPort` + `GeminiAdapter`, `ShoppingPort` + `NaverShoppingAdapter`.
- 새 이름을 짓기 전에 같은 성격의 기존 타입이 프로젝트 안에서 어떤 접미사와 용어를 쓰는지 먼저 확인한다.
- 의미가 비슷한데 이름 체계만 다른 타입을 새로 만들지 않는다.
- 후보가 여러 개면 프로젝트의 기존 패턴과 가장 가까운 이름을 우선 채택한다.

## 6. SRP(단일 책임) 엄수
- 한 클래스는 한 책임만 가진다.
- 역할 분리 기준:
  - Context 해석: `ContextResolver`
  - DRAFT 생성: `DraftService`
  - 정규화: `NormalizationService`
  - 점수화: `ScoringService`
  - 상품 연결: `ProductService`
  - 영속화: `PersistenceService`
  - 응답 매핑: `ResponseMapper`

## 7. 팀 변환 용이성(Weather/Coordination)
- weather 입력은 구조화 DTO(`WeatherInput`)를 사용한다.
- coordination 출력은 구조화 DTO(`CoordinationOutput`)를 제공한다.
- 임의 JsonNode 직접 사용을 최소화한다.

## 8. 계약 우선
- 코드보다 입력/출력 계약(DTO/enum/단위)을 먼저 고정한다.
- 계약 변경은 버전 정책과 함께 기록한다.

## 9. 불변 모델 우선
- 공유 계약/도메인 모델은 `record` 또는 불변 객체를 우선한다.
- 가변 setter 모델은 영속 계층에서만 제한적으로 사용한다.

## 10. 예외 코드 표준화
- 문자열 예외 대신 표준 `ErrorCode`를 사용한다.
- 외부 API/검증/DB 오류를 구분한다.

## 11. 경계 분리
- controller: 입출력/검증
- service(application): 오케스트레이션
- domain: 규칙 계산 모델
- outbound/persistence: I/O

## 12. 룰/수식 상수화
- 가중치, 임계치(예: 70) 등 정책값은 상수/정책 객체로 분리한다.
- 하드코딩 분산을 금지한다.

## 13. 관측 가능성
- 요청 단위 식별자(traceId)와 단계별 로그를 남긴다.
- 실패 지점(외부 API/DB/검증)을 구분 가능해야 한다.

## 14. 테스트 계층화
- 단위: 정규화/점수
- 통합: Mapper/DB
- E2E: API 경로
- 계약 테스트: DTO 직렬화/역직렬화

## 15. 호환성 원칙
- 팀 입력 누락 필드는 기본값 처리 규칙을 명시한다.
- 하위호환이 깨지는 필드 변경은 금지한다.
