# recommendation 패키지 폴더 트리-이해가 용이하게 만들었습니다.

## 3.18 커밋 변경점
- 하부 컴포넌트들이 너무 많아짐에 따라 기능별로 stage를 묶었습니다. 따라서 메인 로직이 작동하는 방식은 orchestrator와 각 feature의 stage만 보시면 됩니다.
- 외부 api가 많아짐에 따라 infra를 따로 만들었습니다.
- 그 외 prompt나 도메인 계약, 스키마 계약, properties를 계층에 맞게 infra나 domain에 넣었습니다.
- 현재 filter에 대한 이해는 없는 상태입니다. 프로젝트 진행 스케쥴에 맞춰 이 과정을 삭제하고 영상에 잘 나오게끔 search query만 조절해서 결과가 나오게 하려고 하고 추후 보강하려고 합니다.

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
├─ domain ----------- enum과 추후 ai 호출시 사용할 프롬프트 정의 레이어 입니다. 비즈니스에서 고립하기 위해 만들었습니다.
│  ├─ imagefilter ---- 이미지 필터 판정 결과/사유 도메인 모델
│  ├─ enums
│  └─ prompt
├─ dto
│  ├─ api ----------- 인/아웃바운드 계약 DTO
│  ├─ internal ------ 파이프라인 내부 계산/검증/계약 DTO
│  └─ persistent ---- DB 매핑용 DTO
├─ exception -------- 추후 Sentry 고도화시 통합 예정
├─ mapper
├─ config
├─ infra ------------ 외부 연동 구현체(adapter/properties)
│  ├─ gemini
│  ├─ imageAnalysis - YOLO/ONNX 기반 이미지 분석 어댑터
│  └─ navershopping/s3 등 adapter 구현
└─service
    ├─ productSearch - 검색 + 사용자 출력 조립
    ├─ persistent - 저장 전용 계층
    └─ Orchestrator.java 각 stage 선언

