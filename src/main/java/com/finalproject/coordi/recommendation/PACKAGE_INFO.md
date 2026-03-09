# recommendation 패키지 폴더 트리-이해가 용이하게 만들었습니다.

## enum, dto만 완성했고 비즈니스 로직을 담당하는 service는 뼈대만 만들었습니다.

## 용어 경계
- `Blueprint`: AI 생성/검증/중간 산출물(내부 처리용)
- `Coordination`: 최종 코디 결과(외부 응답용)
- `Recommendation`: 추천 과정 전반


## Visual Tree
```text
recommendation
├─ controller
├─ domain ----------- enum과 프롬프트 분리
│  ├─ enums
│  └─ prompts
├─ dto
│  ├─ api ----------- 인/아웃바운드 계약 DTO
│  ├─ internal ------ 파이프라인 내부 계산/검증/계약 DTO
│  └─ persistent ---- DB 매핑용 DTO
├─ exception -------- 추후 Sentry 고도화시 통합 예정
├─ mapper
└─ service ---------- 외부 api는 port/adapter, 오케스트레이터가 component 통제
   ├─ apiport
   ├─ apiadapter
   │  ├─ gemini
   │  ├─ kakaomap
   │  └─ naver
   ├─ Orchestrator.java
   └─ component
   
```
