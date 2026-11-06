# Greeting API

Java 21과 Spring Boot를 활용하여 REST API를 구현한 프로젝트입니다.

기본 인사말 API를 시작으로 스터디 참가 신청, 명단 조회, 참가 취소, 정원 변경 기능을 구현했습니다.

이후 AI를 활용하여 HTML·CSS·JavaScript로 스터디 신청 화면을 제작하고, 기존 Spring Boot API와 연결했습니다.

## 1. 개발 환경

| 항목 | 사용 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build Tool | Gradle (Kotlin DSL) |
| IDE | IntelliJ IDEA |
| Frontend | HTML · CSS · JavaScript |
| API 테스트 | Postman, 웹 브라우저 |

## 2. 실행 방법

`greeting-api/` 디렉터리에서 실행합니다.

**Windows (PowerShell)**

```powershell
.\gradlew.bat bootRun
```

**macOS / Linux**

```bash
./gradlew bootRun
```

서버 실행 후 다음 주소에서 확인할 수 있습니다.

- 인사 API: http://localhost:8080/hello
- 스터디 신청 화면: http://localhost:8080/study.html

**빌드 명령어**

Windows:

```powershell
.\gradlew.bat build
```

macOS / Linux:

```bash
./gradlew build
```

빌드 성공 시 `BUILD SUCCESSFUL` 메시지를 확인할 수 있습니다.

## 3. 구현 기능 및 API

### 3.1 API 목록

| 구분 | Method | Endpoint | 기능 |
|---|---|---|---|
| 기본 | GET | `/hello` | 고정 인사말 |
| 기본 | GET | `/greet` | 이름별 인사말 |
| 심화 A | POST | `/study/participants` | 참가 신청 |
| 심화 A | GET | `/study/participants` | 명단 조회 |
| 심화 B | POST | `/study/cancel` | 참가 취소 |
| 심화 C | POST | `/study/capacity` | 정원 변경 |

### 3.2 GET /hello

고정된 인사말을 반환합니다.

**요청**

```http
GET http://localhost:8080/hello
```

**응답**

```text
Hello, Spring!
```

### 3.3 GET /greet

요청 파라미터 `name`에 따라 인사말을 반환합니다.

**이름을 전달한 경우**

```http
GET http://localhost:8080/greet?name=민지
```

```text
안녕하세요, 민지님
```

**이름을 전달하지 않은 경우**

```http
GET http://localhost:8080/greet
```

```text
안녕하세요, 방문자님
```

`name`을 전달하지 않으면 기본값인 `방문자`를 사용합니다.

## 4. 심화 A — 참가 신청 및 명단 조회

### 4.1 참가 신청

**`POST /study/participants`**

요청 파라미터: `name`

**처리 규칙**

- 이름의 앞뒤 공백을 제거합니다.
- 이름이 누락되거나 빈 값, 공백만 있는 경우 신청을 거절합니다.
- 이미 명단에 있는 이름은 중복 신청할 수 없습니다.
- 신청에 성공하면 고유 ID를 발급합니다.
- 정원 안에 신청하면 `CONFIRMED`, 초과하면 `WAITING` 상태로 등록합니다.
- 신청 실패 시 기존 명단을 변경하지 않습니다.

**성공 응답 예시**

```text
신청완료: 1/민지/CONFIRMED
```

**실패 응답 예시**

```text
이름을 입력해주세요.
이미 신청한 이름입니다.
```

### 4.2 참가자 명단 조회

**`GET /study/participants`**

참가자의 ID, 이름, 상태를 신청 순서대로 반환합니다.

**출력 형식**

```text
id/이름/상태
```

**응답 예시**

```text
1/민지/CONFIRMED
2/철수/CONFIRMED
3/영희/CONFIRMED
4/지수/WAITING
```

명단이 비어 있는 경우:

```text
참가자가 없습니다.
```

### 4.3 참가 상태 및 명단 관리

| 상태 | 의미 |
|---|---|
| `CONFIRMED` | 참가 확정 |
| `WAITING` | 대기 |

- 최초 정원은 3명입니다.
- 정원 초과 시 신청 순서대로 대기합니다.
- 참가자 ID는 고유하게 발급되며 연속될 필요는 없습니다.
- 참가자 명단은 Service에서 관리합니다.
- 서버 메모리에 저장하므로 서버를 재시작하면 초기화됩니다.

## 5. 심화 B — 참가 취소 및 대기자 승격

### 5.1 참가 취소

**`POST /study/cancel`**

요청 파라미터: `memberId`

참가 신청 시 발급된 고유 ID를 이용하여 신청을 취소합니다.

**처리 규칙**

- 확정 참가자가 취소하면 신청 순서가 가장 빠른 대기자 1명을 확정으로 변경합니다.
- 대기 참가자가 취소하면 해당 참가자만 삭제합니다.
- 승격할 대기자가 없으면 취소만 처리합니다.
- ID 누락, 빈 값, 숫자가 아닌 입력은 거절합니다.
- 존재하지 않거나 이미 취소한 ID는 거절합니다.
- 취소 실패 시 기존 명단을 유지합니다.
- 취소한 참가자는 같은 이름으로 재신청할 수 있으며 새로운 ID가 발급됩니다.

### 5.2 응답 예시

**확정 참가자 취소 및 대기자 승격**

```text
취소 완료: 민지 / 확정 완료: 지수
```

**승격할 대기자가 없는 경우**

```text
취소 완료: 민지
```

**실패 응답**

```text
취소할 참가자의 ID를 입력해주세요.
참가자 ID는 숫자로 입력해주세요.
존재하지 않는 참가자입니다.
```

## 6. 심화 C — 정원 변경 및 자동 승격

### 6.1 정원 변경

**`POST /study/capacity`**

요청 파라미터: `capacity`

스터디 정원을 변경하고 빈자리가 생기면 대기자를 신청 순서대로 확정합니다.

**처리 규칙**

- 초기 정원은 3명입니다.
- 정원은 1~10명 사이의 정수로 변경할 수 있습니다.
- 정원을 늘리면 빈자리 수만큼 대기자를 순서대로 확정합니다.
- 현재 확정 인원보다 작은 정원으로 변경할 수 없습니다.
- 정원을 줄여도 기존 확정 참가자의 상태는 유지됩니다.
- 동일한 정원을 요청하면 변경하지 않습니다.
- 누락, 빈 값, 숫자가 아닌 값, 허용 범위를 벗어난 값은 거절합니다.
- 정원 변경 실패 시 기존 정원과 명단을 유지합니다.

### 6.2 응답 예시

**정원 변경 성공**

```text
정원 변경 완료: 현재 정원 5명 / 이번에 확정된 대기자: 2명
```

**확정 인원보다 작은 정원 요청**

```text
정원 변경 실패: 현재 확정 인원보다 정원을 작게 변경할 수 없습니다. / 현재 정원: 5명
```

**동일한 정원 요청**

```text
현재 정원과 동일합니다. / 현재 정원: 5명
```

**잘못된 입력**

```text
정원 변경 실패: 정원은 1명 이상 10명 이하로 입력해주세요. / 현재 정원: 5명
```

## 7. AI 활용 — 스터디 신청 화면

### 7.1 구현 목적

기존 Spring Boot API를 브라우저에서도 편리하게 사용할 수 있도록 AI를 활용하여 신청 화면을 제작했습니다.

프론트엔드에 익숙하지 않은 상태에서 AI를 활용해 HTML 구조, CSS 디자인, JavaScript API 연결을 구현했습니다.

외부 라이브러리와 CDN을 사용하지 않고 순수 HTML·CSS·JavaScript만 사용했습니다.

### 7.2 파일 위치

```text
greeting-api/
└── src/
    └── main/
        └── resources/
            └── static/
                └── study.html
```

**접속 주소**

http://localhost:8080/study.html

### 7.3 화면 주요 기능

| 기능 | 설명 |
|---|---|
| 참가 신청 | 이름을 입력하여 신청 |
| 참가 취소 | 참가자 ID로 취소 |
| 정원 변경 | 정원을 1~10명 사이로 변경 |
| 명단 조회 | 참가자의 ID·이름·상태 표시 |
| 명단 새로고침 | 최신 참가자 명단 조회 |
| 결과 안내 | 신청·취소·변경 결과 및 오류 표시 |

### 7.4 API 연결 방식

JavaScript의 `fetch()`를 사용하여 Spring Boot API에 요청합니다.

POST 요청은 `URLSearchParams`를 사용하여 `application/x-www-form-urlencoded` 형식으로 전송합니다.

**요청 파라미터**

| 기능 | 파라미터 |
|---|---|
| 참가 신청 | `name` |
| 참가 취소 | `memberId` |
| 정원 변경 | `capacity` |

**POST 요청 예시**

```javascript
const body = new URLSearchParams();
body.set("name", "민지");

fetch("/study/participants", {
    method: "POST",
    headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
    },
    body: body.toString()
})
.then(response => response.text())
.then(console.log);
```

서버가 JSON이 아닌 문자열을 반환하므로 `response.text()`를 사용합니다.

### 7.5 화면 처리 과정

1. 사용자가 신청·취소·정원 변경 버튼을 클릭합니다.
2. JavaScript가 `fetch()`로 API 요청을 전송합니다.
3. Spring Boot의 Controller와 Service가 요청을 처리합니다.
4. 서버에서 처리 결과를 문자열로 반환합니다.
5. 화면에 결과를 표시하고 참가자 명단을 다시 조회합니다.

추가로 버튼 중복 클릭 방지, 입력값 검증, 오류 메시지 표시 기능을 적용했습니다.

## 8. QA 체크리스트
