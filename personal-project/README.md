# 취미 위시리스트 · 외부 상품 연동과 장바구니

원하는 상품을 직접 등록하거나 DummyJSON에서 가져와 위시리스트에 저장하고, 예산별 추천과 장바구니 금액 미리보기를 제공하는 Spring Boot 프로젝트입니다.

기존 위시리스트·예산 추천·이름 부분 검색을 유지하면서 외부 상품 연동, 장바구니 수량 관리, 쿠폰·배송비 계산을 추가했습니다. 작업 위치는 개인 Fork의 `personal-project/`입니다.

## 개발 환경과 실행

| 항목 | 환경 |
|---|---|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 4.1.2 |
| 빌드 | Gradle Wrapper 9.8.2, Groovy DSL |
| 테스트 | JUnit Jupiter, Mockito, Spring Boot Test, MockMvc |
| 외부 HTTP 호출 | Spring RestClient |
| 저장 방식 | 메모리 저장소, 별도 DB 없음 |
| 서버 포트 | 8081 |
| 기존 화면 | HTML·CSS·JavaScript, Fetch API |

저장소의 `personal-project` 폴더에서 실행합니다.

```powershell
.\gradlew.bat bootRun
```

테스트와 빌드 명령입니다.

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

macOS / Linux에서는 `./gradlew bootRun`, `./gradlew test`, `./gradlew build`를 사용합니다. 실행 권한이 없다면 `chmod +x gradlew`를 먼저 실행합니다.

기본 주소는 `http://localhost:8081`이고, 기존 위시리스트 화면은 [http://localhost:8081/](http://localhost:8081/)에서 사용합니다. 외부 상품·장바구니·금액 미리보기는 현재 HTTP API로 제공합니다. 외부 프론트엔드 라이브러리나 CDN은 사용하지 않습니다.

서버를 재시작하면 위시리스트와 장바구니 데이터가 초기화됩니다. 실제 DummyJSON 조회·가져오기에는 인터넷 연결이 필요하며, 자동 테스트는 실제 외부 서버를 호출하지 않습니다.

## 단계별 완료 범위

| 범위 | 구현 내용 | 상태 |
|---|---|---|
| 기존 기능 | 위시리스트 등록·목록·단건 조회·예산 추천·이름 부분 검색·기존 화면 | 유지 |
| 1단계 | 외부 상품 목록·검색·상세 조회, 위시리스트 가져오기, 중복 방지, 원화 환산 | 완료 |
| 2단계 | 장바구니 담기·조회·수량 변경·삭제, 수량·종류·재고 제한, 복사한 값 보존 | 완료 |
| 2단계 | 쿠폰·배송비 포함 금액 미리보기, 오류 응답과 실패 후 데이터 보존 | 완료 |
| 보조 기능 | 수동 상품 수정·위시리스트 상품 삭제 | 완료 |
| 3단계 선택 | 배송비 포함 최저 금액 쿠폰 추천, 외부 상품·장바구니용 AI 화면 | 미구현 |

## 패키지 구조와 클래스 책임

```text
com.example.personal_project
├── PersonalProjectApplication
├── presentation
│   ├── ItemController / ItemDto
│   ├── ExternalProductController
│   ├── CartController
│   ├── AddCartItemRequest / ChangeQuantityRequest
│   ├── CartItemResponse / CartPreviewResponse
│   └── GlobalExceptionHandler
├── application
│   ├── ItemService
│   ├── CartService
│   └── ValidationService
├── domain
│   ├── Item / SourceType
│   ├── Cart / CartItem
│   ├── Coupon / CartPreview
│   ├── ItemRepository / CartRepository
│   └── EntityNotFoundException
└── infrastructure
    ├── MemoryItemRepository / MemoryCartRepository
    └── DummyJsonClient / DummyJsonProduct / DummyJsonProductList
```

| 클래스 | 책임 |
|---|---|
| Controller | HTTP 요청·응답 처리, 장바구니 요청 DTO의 `@Valid` 검증 |
| ItemService | 위시리스트 조회·등록·수정·삭제·추천, 외부 조회와 가져오기·가격 환산 조율 |
| CartService | 원본 상품과 보관된 Cart 조회, 담기·변경·삭제·미리보기 요청, 응답 DTO 변환 |
| ValidationService | 기존 이름·가격·예산 입력 검증과 이름 공백 제거 |
| Item | 로컬 id·이름·가격·출처·외부 id·재고 보관 |
| Cart | 장바구니 항목 관리, 중복 합산·20종 제한·행 id 생성·금액 계산 |
| CartItem | 처음 복사한 상품 정보 보관, 수량·재고 검사와 수량 변경 |
| Coupon | 쿠폰 종류, 할인 조건과 할인액 계산 |
| CartPreview | Domain의 금액 계산 결과 보관 |
| CartPreviewResponse | 계산 결과를 HTTP 응답으로 전달 |
| Repository 인터페이스 | 필요한 저장·조회 동작 정의 |
| MemoryItemRepository | 메모리 목록에 Item 저장·조회·수정·삭제, 로컬 id 생성 |
| MemoryCartRepository | 서버 메모리에서 하나의 Cart 보관 |
| DummyJsonClient | RestClient로 외부 HTTP 호출, 페이징·검색어 검사, 외부 오류 변환 |
| DummyJsonProduct / DummyJsonProductList | DummyJSON에서 받은 JSON 응답 보관 |
| GlobalExceptionHandler | 입력 오류·대상 없음·규칙 충돌을 400·404·409로 변환 |

Repository 인터페이스와 메모리 구현체를 분리하고, Service에 Repository와 외부 Client를 생성자로 주입합니다. 기존 방식에 맞춰 Service에서 응답 DTO 변환을 담당합니다.

## API 계약

기본 주소: `http://localhost:8081`. JSON 요청에는 `Content-Type: application/json`을 지정합니다. 삭제는 성공 시 `204`와 빈 본문을 반환하고, 아래 나머지 API는 성공 시 `200`을 반환합니다.

### 위시리스트

| 기능 | 메서드 | 경로 | 요청·응답 |
|---|---|---|---|
| 수동 등록 | POST | `/items` | 이름·가격 JSON → ItemDto |
| 전체 조회 | GET | `/items` | ItemDto 배열 |
| 단건 조회 | GET | `/items/{id}` | 로컬 id → ItemDto |
| 예산 추천 | GET | `/items/recommend?budget=5000` | ItemDto 배열, 최대 3개 |
| 이름 부분 검색 | GET | `/items/search?name=노트` | ItemDto 배열 |
| 수동 상품 수정 | PUT | `/items/{id}` | 이름·가격 JSON → ItemDto |
| 상품 삭제 | DELETE | `/items/{id}` | 위시리스트에서 삭제 |
| 외부 상품 가져오기 | POST | `/items/import/{externalId}` | 외부 id만 전달, 본문 없음 → ItemDto |

수동 등록·수정 요청 예시입니다.

```json
{"name": "  노트  ", "price": 12000}
```

응답 예시입니다. 요청으로 id를 보내도 등록 시에는 서버가 새 id를 부여하고, 수정 시에는 경로의 id를 사용합니다.

```json
{"id": 1, "name": "노트", "price": 12000}
```

응답으로 받은 실제 id를 이후 조회·수정·삭제에 사용합니다. 이름은 null·빈 문자열·공백뿐인 값을 거부하고 앞뒤 공백을 제거합니다. 가격은 0 이상의 int이며 무료 상품을 허용합니다. 예산도 0 이상이어야 합니다.

추천은 **각 상품의 가격이 예산 이하인지** 검사하고, 가격 오름차순으로 정렬한 뒤 최대 3개를 반환합니다. 같은 가격은 등록 순서를 유지하며 원본 목록을 변경하지 않습니다. 추천 상품들의 합계를 예산에 맞추는 기능은 아닙니다. 이름 부분 검색은 누락·빈 검색어·공백 검색어를 거부합니다. 추천·이름 검색 결과가 없으면 빈 배열을 반환합니다.

### 외부 상품 조회

| 기능 | 내 서버 경로 | DummyJSON 요청 |
|---|---|---|
| 목록 | `/external-products?limit=10&skip=0` | `/products?limit=10&skip=0` |
| 검색 | `/external-products/search?q=phone&limit=10&skip=0` | `/products/search?q=phone&limit=10&skip=0` |
| 상세 | `/external-products/1` | `/products/1` |

모두 GET 요청이며, 브라우저 또는 Postman → 내 Spring 서버 → `https://dummyjson.com` 순서로 호출합니다. 연결 제한은 3초, 응답 제한은 5초입니다.

- `q`: 필수, 앞뒤 공백 제거 후 빈 값 거부
- `limit`: 기본 10, 1~20
- `skip`: 기본 0, 0 이상
- 외부 상세 id: 양수
- 상세 응답: `id`, `title`, `price`, `category`, `thumbnail`, `stock`
- 목록·검색 응답: `{"products": [...]}`. 결과 없음은 200과 `{"products": []}`

외부 조회 응답의 `price`는 원래 가격이며, 위시리스트로 가져올 때만 수업용 원화로 환산합니다. `discountPercentage`, `minimumOrderQuantity`는 사용하지 않습니다.

### 외부 상품 가져오기 규칙

`POST /items/import/1`처럼 외부 id만 받습니다. 서버가 상세 API를 조회하여 이름·가격·재고를 가져오므로 요청으로 이름·가격을 지정하지 않습니다.

- 로컬 `id`는 서버에서 새로 생성하며, `sourceType=DUMMY_JSON`과 `externalId`를 별도로 보관합니다.
- 수동 등록 상품은 `sourceType=MANUAL`, 외부 id·재고는 null입니다.
- 같은 출처·외부 id가 이미 있으면 409로 거부합니다.
- 재고 0인 품절 상품도 위시리스트에는 저장할 수 있습니다.
- 저장된 외부 상품의 이름·가격은 PUT 요청으로 수정할 수 없고, 외부 API의 변경 내용을 자동으로 갱신하지 않습니다.
- 가져오기 응답은 기존 ItemDto의 `id`, `name`, `price`입니다. 출처·외부 id·재고는 서버의 Item에 보관합니다.

환산은 `BigDecimal`로 1,000을 곱한 뒤 `HALF_UP`으로 원 단위 반올림합니다. 예를 들어 9.99 → 9,990원, 9.9905 → 9,991원입니다. 실제 통화나 실시간 환율이 아닌 수업용 고정 규칙입니다.

### 장바구니

| 기능 | 메서드 | 경로 | 요청·응답 |
|---|---|---|---|
| 조회 | GET | `/cart` | CartItemResponse 배열 |
| 담기 | POST | `/cart/items` | `{"itemId": 1, "quantity": 2}` → CartItemResponse |
| 수량 변경 | PATCH | `/cart/items/{id}` | `{"quantity": 3}` → CartItemResponse |
| 행 삭제 | DELETE | `/cart/items/{id}` | 해당 행 삭제 |

`itemId`는 위시리스트의 로컬 상품 id입니다. PATCH·DELETE 경로의 `id`는 장바구니 행 id이므로 담기 응답 또는 목록에서 확인합니다. 수량 변경은 기존 수량에 더하는 것이 아니라 요청한 수량으로 교체합니다.

장바구니 행 응답 예시입니다. 아래 null 재고는 수동 상품에 재고 제한이 없음을 뜻합니다.

```json
{
  "id": 1,
  "itemId": 10,
  "name": "노트",
  "unitPrice": 12000,
  "sourceType": "MANUAL",
  "stock": null,
  "quantity": 2
}
```

- 추가·변경 요청 수량은 1~5개이며, 같은 상품을 추가하면 기존 행에 합산합니다. 합산 후에도 최대 5개입니다.
- 서로 다른 상품은 최대 20종입니다. 이미 20종이어도 기존 상품에 허용 범위의 수량을 추가할 수 있습니다.
- 외부 상품은 복사한 재고 이하만 허용하며, 품절·재고 정보 누락은 거부합니다. 수동 상품은 재고 제한이 없습니다.
- 처음 담을 때 이름·단가·출처·재고를 복사합니다. 원본 수정·삭제 후에도 장바구니 값은 유지됩니다.
- 중복 추가·수량 변경은 처음 복사한 단가·재고를 사용합니다. 장바구니에서 삭제 후 다시 담으면 현재 위시리스트 값을 새로 복사합니다.
- 새로 담거나 중복 추가할 때는 로컬 원본 상품이 있어야 합니다. 원본 삭제 후에도 기존 장바구니 행의 조회·수량 변경·삭제·미리보기는 가능합니다.

### 금액 미리보기

```http
GET /cart/preview
GET /cart/preview?couponCode=PERCENT10
GET /cart/preview?couponCode=FIXED15000
```

`couponCode`를 생략하면 `NONE`이며, 한 번에 하나만 적용합니다. 쿠폰 코드는 아래 대문자 값을 사용합니다.

| 쿠폰 | 할인 규칙 |
|---|---|
| NONE | 할인 없음 |
| PERCENT10 | 상품 합계의 10%, 원 미만 버림, 최대 20,000원 |
| FIXED15000 | 상품 합계 100,000원 이상일 때 15,000원 |

단가·수량의 곱과 상품 합계·할인·배송비·최종 금액은 long으로 계산합니다. 할인 후 금액이 100,000원 이상이면 무료배송이고, 미만이면 5,000원입니다. 빈 장바구니는 모든 금액이 0이며 NONE만 허용합니다. 0원 상품이 담긴 장바구니는 빈 장바구니가 아니므로 배송비 규칙을 적용합니다.

상품 합계 110,000원에 PERCENT10을 적용한 응답입니다.

```json
{
  "subtotal": 110000,
  "discount": 11000,
  "discountedSubtotal": 99000,
  "shipping": 5000,
  "finalAmount": 104000,
  "coupon": "PERCENT10"
}
```

미리보기는 계산 결과를 반환할 뿐, 결과를 저장하거나 장바구니의 항목·수량·복사한 정보를 변경하지 않습니다.

### 오류 응답과 데이터 보존

| 상태 | 상황 |
|---|---|
| 400 | 잘못된 이름·가격·예산·검색어·페이징·요청 수량, 알 수 없거나 사용할 수 없는 쿠폰 |
| 404 | 없는 로컬 상품·외부 상품·장바구니 행 |
| 409 | 외부 상품 중복 가져오기·이름/가격 수정 시도, 20종 제한·합산 수량·재고 충돌 |
| 502 | 외부 연결 실패·시간 초과·외부 서버 오류·빈 외부 응답 본문 |

요청 수량이 6개이면 입력 검증 오류인 400이고, 기존 3개에 3개를 추가하여 합산 수량이 6개가 되면 규칙 충돌인 409입니다.

검증과 외부 조회를 마친 뒤 저장하고, 수량도 검사 후 변경하여 실패한 가져오기·추가·변경·미리보기가 기존 데이터를 바꾸지 않도록 했습니다. 공통 핸들러의 응답 본문은 예외 메시지 문자열이며, DTO 검증과 `ResponseStatusException`은 Spring의 오류 응답을 사용합니다. 오류 본문 형식을 하나로 통일하지는 않았습니다.

## 자동 테스트와 실제 검증 결과

단위 테스트는 Domain·Service의 규칙을 확인하고, MockMvc 테스트는 요청 DTO 검증과 HTTP 상태·응답·저장소 보존을 함께 확인합니다. `@SpringBootTest`로 Spring이 연결한 Service와 실제 메모리 저장소도 확인합니다.

외부 Client 테스트는 `MockRestServiceServer`로 가짜 응답·연결 실패·시간 초과 예외를 준비합니다. Service·HTTP 연계 테스트의 외부 호출은 Mockito Client 대역을 사용하며 실제 DummyJSON에 접속하지 않습니다. 실제 네트워크에서 3초·5초가 경과하는지 측정한 테스트는 아닙니다.

현재 소스와 설정을 그대로 복사한 검증 폴더에서 `.\gradlew.bat --offline test --console=plain`을 실행하여 `BUILD SUCCESSFUL`을 확인했습니다. 원본 폴더에서 Gradle 프로세스를 시작하는 실행 환경의 권한 제한 때문에 복사본을 사용했으며, 아래 수치는 그 실행의 테스트 결과 XML 기준입니다.

| 테스트 클래스 | 실행 | 실패 | 오류 | 건너뜀 |
|---|---:|---:|---:|---:|
| ItemServiceTest | 23 | 0 | 0 | 0 |
| ItemServiceUnitTest | 11 | 0 | 0 | 0 |
| CartItemTest | 12 | 0 | 0 | 0 |
| CartTest | 22 | 0 | 0 | 0 |
| CartPreviewTest | 8 | 0 | 0 | 0 |
| DummyJsonClientTest | 20 | 0 | 0 | 0 |
| CartControllerTest | 26 | 0 | 0 | 0 |
| CartPreviewControllerTest | 6 | 0 | 0 | 0 |
| CartServiceIntegrationTest | 2 | 0 | 0 | 0 |
| PersonalProjectApplicationTests | 1 | 0 | 0 | 0 |
| **합계** | **131** | **0** | **0** | **0** |

테스트 실행 후 보고서는 실행한 프로젝트의 `build/reports/tests/test/index.html`, XML은 `build/test-results/test/`에서 확인할 수 있습니다. 위 기록은 test 작업 결과이며, 별도 build 작업 완료를 의미하지 않습니다.

### QA 체크리스트와 결과

| 검증 항목 | 실제 결과 |
|---|---|
| 기존 등록·실제 id 조회·검색·예산 추천·원본 목록 보존 | 통과 |
| 외부 상세·목록·검색의 정상/빈 결과·입력 오류·404·502 | 통과 |
| 연결 실패·시간 초과 예외를 502로 변환 | 통과 |
| 9.99 → 9,990원, 9.9905 → HALF_UP 9,991원 | 통과 |
| 로컬 id와 외부 id 구분, 출처·재고 보존 | 통과 |
| 중복 가져오기 409, 품절 가져오기 허용 및 장바구니 담기 거부 | 통과 |
| 요청 수량 1~5·동일 상품 합산·20종·재고 경계 | 통과 |
| 잘못된 추가·수량 변경·없는 행 삭제 후 기존 값 보존 | 통과 |
| 원본 수정·삭제 후 장바구니 정보 유지, 원본 삭제 후 수량 변경·행 삭제 | 통과 |
| 장바구니 삭제 후 재추가 시 현재 이름·단가·재고 복사 | 통과 |
| 원본이 없는 상품의 추가 거부, 다른 위시리스트·장바구니 보존 | 통과 |
| 쿠폰 최소 금액·원 미만 버림·할인 상한·배송비 100,000원 경계 | 통과 |
| 빈 장바구니 NONE만 허용, 모든 금액 0 | 통과 |
| 무료 상품이 담긴 장바구니의 배송비 적용 | 통과 |
| int 범위를 넘는 합계를 long으로 계산 | 통과 |
| 정상·실패 미리보기 전후 장바구니 보존 | 통과 |
| Spring이 주입한 Service의 장바구니·미리보기 실행 | 통과 |

상품 합계 110,000원의 쿠폰별 결과도 자동 테스트에서 확인했습니다.

| 쿠폰 | 할인 | 할인 후 금액 | 배송비 | 최종 금액 |
|---|---:|---:|---:|---:|
| NONE | 0 | 110,000 | 0 | 110,000 |
| PERCENT10 | 11,000 | 99,000 | 5,000 | 104,000 |
| FIXED15000 | 15,000 | 95,000 | 5,000 | 100,000 |

## 제한사항과 남은 작업

- 서버 메모리에 하나의 장바구니를 보관하며 사용자별로 구분하지 않습니다. 재시작하면 데이터가 사라집니다.
- 동시 요청에서 중복 검사와 저장·수량 변경을 하나의 작업으로 보장하는 처리는 추가하지 않았습니다.
- 재고 예약·차감·실제 결제·실시간 환율·외부 상품 자동 갱신은 하지 않습니다.
- 외부 가격은 환산 후 int 범위 안인 값을 전제로 하며, 비정상 외부 필드·환산 범위 초과에 대한 전용 오류 처리는 남아 있습니다.
- 기존 화면은 위시리스트 등록·조회·검색·추천용입니다. 외부 상품·장바구니 화면과 최적 쿠폰 자동 추천은 선택 기능으로 남겨두었습니다.
- 오류 응답 본문 형식 통일은 남은 개선 사항입니다.
- 제출 전 개인 Fork에 코드·테스트·README를 Push하고, 조직 원본을 대상으로 관련 Issue·이전 PR 링크·이번 변경 범위를 포함한 PR을 작성합니다. 원본 main에는 병합하지 않고 제출 브랜치를 보존합니다.
