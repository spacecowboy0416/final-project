# recommendation 패키지 폴더 트리-이해가 용이하게 만들었습니다.

## enum, dto만 완성했고 비즈니스 로직을 담당하는 service는 뼈대만 만들었습니다.

## 용어 경계
- `Blueprint`: AI 생성/검증/중간 산출물(내부 처리용)
- `Coordination`: 최종 코디 결과(외부 응답용)
- `Recommendation`: 추천 과정 전반
- `slot`: 카테고리가 어디에 들어가야할 지 정하는 키. 
- `category`: 옷의 종류. 코트와 자켓은 같은 outer slot에 들어간다. 
- `product`: item(closet_item을 포함한) 개별 옷의 전체집합. 명시적이지는 않으나 원시 스키마 구조가 협의되었으므로 이대로 가겠습니다.
  
## Visual Tree
```
recommendation
├─ controller
├─ domain ----------- enum과 프롬프트 비즈니스에서 고립하기 위해 만들었습니다
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
