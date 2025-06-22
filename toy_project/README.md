# 🇰🇷 Kordle Clone - 한글 단어 맞추기 게임

Wordle의 한글 버전인 Kordle을 클론한 웹 게임입니다. 6개의 자모로 이루어진 한글 단어를 맞춰보세요!

## 📝 프로젝트 소개

Kordle Clone은 Spring Boot와 Kotlin으로 개발된 한글 단어 맞추기 게임입니다. 원본 [Kordle](https://kordle.kr/)을 참고하여 제작되었으며, 한글 자모 분해/조합 기능과 단계별 게임 진행, 통계 기능을 제공합니다.

### 주요 기능

- 🎮 **6번의 기회로 한글 단어 맞추기**
- 📊 **개인 게임 통계 및 대시보드**
- 🏆 **단계별 게임 진행 시스템**
- 🔤 **한글 자모 분해/조합 엔진**
- 💾 **H2 데이터베이스를 통한 데이터 관리**
- 🎯 **실시간 피드백 시스템**

## 🛠 기술 스택

### Backend
- **Kotlin** 1.9.21
- **Spring Boot** 3.2.1
- **Spring Data JPA**
- **H2 Database**
- **Gradle** (Kotlin DSL)

### Frontend
- **HTML5/CSS3**
- **Vanilla JavaScript**
- **Responsive Design**

### Test
- **JUnit 5**
- **MockK**
- **Spring Boot Test**

## 📂 프로젝트 구조

```
toy_project/
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/kordle/
│   │   │   ├── controller/           # REST API 컨트롤러
│   │   │   │   └── GameController.kt
│   │   │   ├── dto/                  # 데이터 전송 객체
│   │   │   │   └── GameDto.kt
│   │   │   ├── entity/               # JPA 엔티티
│   │   │   │   ├── GameSession.kt
│   │   │   │   ├── Guess.kt
│   │   │   │   ├── LetterFeedback.kt
│   │   │   │   ├── UserStats.kt
│   │   │   │   └── Word.kt
│   │   │   ├── repository/           # 데이터 접근 계층
│   │   │   │   ├── GameSessionRepository.kt
│   │   │   │   ├── GuessRepository.kt
│   │   │   │   ├── UserStatsRepository.kt
│   │   │   │   └── WordRepository.kt
│   │   │   ├── service/              # 비즈니스 로직
│   │   │   │   ├── GameService.kt
│   │   │   │   ├── StatsService.kt
│   │   │   │   └── WordService.kt
│   │   │   ├── util/                 # 유틸리티
│   │   │   │   └── HangulUtils.kt
│   │   │   └── KordleApplication.kt  # 메인 애플리케이션
│   │   └── resources/
│   │       ├── static/               # 정적 웹 리소스
│   │       │   ├── css/style.css
│   │       │   ├── js/kordle.js
│   │       │   └── index.html
│   │       └── application.yml       # 설정 파일
│   └── test/                         # 테스트 코드
├── data/                             # 데이터베이스 및 CSV 파일
│   ├── words.csv                     # 단어 데이터
│   └── word.mv.db                    # H2 데이터베이스
└── build.gradle.kts                  # 빌드 설정
```

## 🎯 게임 규칙

1. **목표**: 6번의 기회 안에 한글 단어를 맞춰보세요
2. **입력**: 6개의 자모(초성, 중성, 종성 포함)로 구성된 단어
3. **피드백**:
   - 🟩 **초록색**: 올바른 위치의 올바른 자모
   - 🟨 **노란색**: 단어에 포함되지만 잘못된 위치의 자모
   - ⬜ **회색**: 단어에 포함되지 않은 자모

## 🚀 실행 방법

### 1. 저장소 클론
```bash
git clone <repository-url>
cd toy_project
```

### 2. 애플리케이션 실행
```bash
# Gradle을 이용한 실행
./gradlew bootRun

# 또는 JAR 파일 빌드 후 실행
./gradlew build
java -jar build/libs/kordle-0.0.1-SNAPSHOT.jar
```

### 3. 웹 브라우저에서 접속
```
http://localhost:8080
```

## 📊 API 엔드포인트

### 게임 관련
- `POST /api/game/start` - 새 게임 세션 시작
- `POST /api/game/{sessionId}/guess` - 단어 추측 제출
- `GET /api/game/{sessionId}/history` - 게임 히스토리 조회

### 통계 관련
- `GET /api/game/stats/{nickname}` - 사용자 통계 조회
- `GET /api/game/dashboard` - 전체 대시보드 조회

## 🧪 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests GameServiceTest
```

## 📈 한글 처리 엔진

이 프로젝트의 핵심 기능인 한글 처리는 `HangulUtils` 클래스에서 담당합니다:

- **자모 분해**: 한글 단어를 초성, 중성, 종성으로 분해
- **복합 자음/모음 처리**: ㅘ, ㅙ, ㅚ 등의 복합 모음과 ㄳ, ㄵ 등의 겹받침 처리
- **6자리 정규화**: 모든 단어를 정확히 6개 자모로 변환
- **유효성 검증**: 입력된 자모의 유효성 검사

### 예시
```
꼬들 → [ㄱ, ㄱ, ㅗ, ㄷ, ㅡ, ㄹ]
너구리 → [ㄴ, ㅓ, ㄱ, ㅜ, ㄹ, ㅣ]
```

## 📋 데이터베이스 스키마

### Words 테이블
- `text`: 한글 단어 (PK)
- `stage`: 게임 단계
- `answer`: 자모 배열 (JSON 형태)

### GameSession 테이블
- `id`: 세션 ID (PK)
- `nickname`: 플레이어 닉네임
- `answer`: 정답 자모 배열
- `stage`: 현재 단계
- `attempts_left`: 남은 시도 횟수
- `is_cleared`: 클리어 여부

### UserStats 테이블
- `nickname`: 플레이어 닉네임 (PK)
- `games_played`: 총 게임 수
- `games_won`: 승리한 게임 수
- `current_stage`: 현재 단계
- `win_rate`: 승률

## 🎨 UI/UX 특징

- **반응형 디자인**: 모바일과 데스크톱 모두 지원
- **실시간 피드백**: 즉시 색상 변화로 결과 확인
- **한글 키보드**: 게임 전용 한글 자모 키보드
- **통계 시각화**: 게임 결과를 차트로 표시
- **직관적인 인터페이스**: 원본 Kordle과 유사한 UI/UX

## 🔧 환경 설정

### application.yml 주요 설정
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/word
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
server:
  port: 8080
```

### H2 Console 접속
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/word`
- Username: `sa`
- Password: (비어있음)
