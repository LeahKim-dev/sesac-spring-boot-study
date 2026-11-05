# Greeting API

Spring Boot를 사용하여 간단한 인사말을 반환하는 REST API 프로젝트입니다.

## 1. 개발 환경

- Java 21
- Spring Boot 4.1.2
- Gradle (Kotlin DSL)

## 2. 실행 방법

`greeting-api` 디렉터리에서 다음 명령어를 실행합니다.

**Windows**
```powershell
.\gradlew.bat bootRun
```

**macOS / Linux**
```bash
./gradlew bootRun
```

## 3. 구현 기능

### GET /hello

고정된 인사말을 반환합니다.

- 요청: `http://localhost:8080/hello`
- 기대 응답: `Hello, Spring!`

### GET /greet

요청 파라미터 `name`을 받아 인사말을 반환합니다.

- 요청: `http://localhost:8080/greet?name=민지`
- 기대 응답: `안녕하세요, 민지님`

`name`을 전달하지 않으면 기본값인 `방문자`를 사용합니다.

- 요청: `http://localhost:8080/greet`
- 기대 응답: `안녕하세요, 방문자님`

## 4. 실행 및 검증 결과

- [x] `GET /hello` — HTTP 200 및 응답 확인
- [x] `GET /greet?name=민지` — HTTP 200 및 응답 확인
- [x] `GET /greet` — HTTP 200 및 기본값 확인
- [x] `GET /not-found` — HTTP 404 확인
- [x] `.\gradlew.bat build` — 빌드 성공 확인
