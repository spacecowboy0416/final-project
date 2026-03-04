# Coordi Project

Coordi 프로젝트에 오신 것을 환영합니다! 팀원들이 처음 개발을 시작할 때 다음 단계를 따라주세요.

## 🛠 필수 환경 설정
- **Java 21**: 이 프로젝트는 Java 21 LTS 버전을 사용합니다.
- **Gradle**: 별도의 설치 없이 `./gradlew` (Linux/Mac) 또는 `gradlew.bat` (Windows)를 사용하여 빌드할 수 있습니다.
- **MySQL**: 로컬에 `final_coordi` 데이터베이스가 생성되어 있어야 합니다.

## 🚀 시작하기 전 준비 사항 (Secret 설정)
보안을 위해 `application-secret.yml` 파일은 Git에 포함되지 않습니다. 다음 순서대로 로컬 설정을 완료하세요.

1. `src/main/resources/application-secret.yml.sample` 파일을 같은 위치에 복사합니다.
2. 파일 이름을 `application-secret.yml`로 바꿉니다.
3. 공유된 실제 API 키와 DB 정보를 입력합니다.

## 🏃 실행 방법
터미널에서 아래 명령어를 실행하여 애플리케이션을 구동할 수 있습니다.

```bash
./gradlew bootRun
```

테스트 코드를 실행하여 환경이 정상인지 확인하세요.
```bash
./gradlew test
```

## 📦 패키지 구조 유의사항
- 예약어 충돌 방지를 위해 기본 패키지는 `com.finalproject.coordi`를 사용합니다.(final 쓰고 싶었으나 final은 예약어라 사용할 수 없음 ㅠㅠ )
- 모든 도메인, 서비스, 컨트롤러는 이 패키지 하위에 생성해 주세요.
