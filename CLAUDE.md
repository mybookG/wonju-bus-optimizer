# 원주시 스마트 버스 노선 최적화 서비스

## 프로젝트 개요

원주시 공공데이터(버스 노선·배차, 인구 통계, 건축물 대장, 복지시설 위치)를 교차 분석해
수요 대비 노선이 부족한 구간(사각지대)을 탐지하고, AI로 개선 노선을 제안하는 서비스.
시민 불편 제보 기능 포함. 2026 원주시 공공데이터·AI 활용 아이디어 공모전 출품작.

## 기술 스택

- **백엔드**: Spring Boot 3.x (Java 21) — 단일 스택
- **AI**: Google Gemini API — 수요 분석, 노선 최적화 제안, 시민 제보 NLP 분류 (spring-ai-vertex-ai-gemini 또는 google-cloud-aiplatform 의존성)
- **AI 연동 방식**: 공공데이터 전처리 후 구조화된 프롬프트로 Gemini에 전달 → JSON 응답 파싱 → 서비스 로직 처리
- **DB**: PostgreSQL + PostGIS — 공간 데이터, ST_DWithin 반경 쿼리
- **캐시**: Redis — 공공API 응답 캐싱, SMS 인증 코드 TTL 저장
- **배치**: Spring Batch + @Scheduled — 매일 새벽 3시 공공데이터 갱신, 재분석 트리거
- **보안**: Spring Security + JWT — 관리자 로그인, 시민 제보 API는 permitAll
- **SMS 인증**: 네이버 클라우드 SENS — 시민 제보 휴대폰 인증 (6자리 난수, Redis TTL 3분)
- **프론트**: React + Kakao Maps SDK
- **인프라**: Docker + GitHub Actions

## 팀 구성

- 백엔드 A: 데이터 레이어 — 공공API 배치 수집, PostGIS 쿼리, Gemini API 프롬프트 설계 및 연동
- 백엔드 B: 서비스 레이어 — REST API, 시민 제보, Spring Security
- 프론트엔드: React + Kakao Maps 지도 시각화, 시민 제보 UI

## 아키텍처 원칙 (SOLID 엄수)

**SRP** — 도메인별 서비스 분리
- `BusRouteService`: 노선·배차 조회 및 캐싱
- `DemandAnalysisService`: 공공데이터 전처리 후 Gemini API 호출, 수요 점수 파싱
- `RouteRecommendService`: 개선 노선 제안 관리
- `ComplaintService`: 시민 제보 수신·저장·NLP 분류
- `SmsVerificationService`: SENS 연동, Redis 인증 코드 관리

**OCP** — 데이터 소스 확장 가능
- `PublicDataFetcher` 인터페이스 → `TagoFetcher`, `KosisFetcher` 구현체로 분리
- 새 공공데이터 소스 추가 시 구현체만 추가, 기존 코드 수정 없음

**LSP / ISP**
- `NotificationPort`: 이메일·SMS·Push 알림 분리
- `AdminDashboardPort` / `CitizenApiPort` 별도 분리

**DIP** — 외부 시스템 추상화
- `AiAnalysisPort` 인터페이스 → `GeminiAdapter` 구현체 주입 (테스트 시 Mock 교체 가능)
- `MapPort` 인터페이스 → `KakaoMapAdapter` 구현체
- `SmsPort` 인터페이스 → `NaverSensAdapter` 구현체

## 주요 도메인 및 기능

### 수요 예측 (DemandAnalysisService)
- Input: 읍·면·동 인구 밀도, 건축물 용도 비율, 복지시설 반경 인구, 배차 간격 → JSON으로 직렬화
- Gemini 프롬프트: 구조화된 데이터 + "구간별 수요 점수(0~100)를 JSON으로 반환하라" 형식
- 출력: Gemini JSON 응답 파싱 → 시간대별·구간별 수요 점수
- 사각지대 판정: 수요 점수 높음 + 공급 지수(배차 간격 역수) 낮음 → 서비스 레이어에서 판정

### 노선 최적화 (RouteRecommendService)
- Gemini 프롬프트: 사각지대 구간 목록 + 기존 노선 정보 → "최적 노선 개선 방안 3가지를 JSON으로 반환하라"
- 제안 유형: 노선 신설 / 기존 노선 연장 / 배차 간격 단축
- 경로 탐색: PostGIS 도로망 기반 자체 다익스트라로 구체적 경로 보완
- 우선순위 계산: Gemini 제안 점수 × 수혜 인구 / 예상 비용 (서비스 레이어)

### 시민 제보 (ComplaintService)
- 휴대폰 인증: SENS SMS → 6자리 난수 → Redis TTL 3분 → 검증
- Rate Limiting: 동일 번호 하루 3회 제보 제한
- NLP 분류: Gemini API → 제보 텍스트 분류 (노선 없음/배차/혼잡/정류장) + 불편 강도 점수
- 환류: 반경 200m 내 제보 PostGIS ST_DWithin 집계 → 임계치 초과 시 @ApplicationEvent 발행 → 수요 재분석 트리거

### 인증 (관리자)
- Spring Security + JWT
- ROLE_ADMIN 단일 역할
- 시민 제보 API: `permitAll()` + Rate Limiting

## 활용 공공데이터 API

| 데이터 | 출처 | 용도 |
|---|---|---|
| 버스 노선·정류장 | 국토부 TAGO API | 현행 노선 맵핑 |
| 버스 실시간 배차 | 원주시 버스정보시스템 | 배차 간격 분석 |
| 인구 통계 | 통계청 KOSIS | 잠재 수요 산출 |
| 건축물 대장 | 국토부 건축데이터 민간개방 | 주거·상업 밀도 |
| 복지시설 위치 | 보건복지부 복지로 API | 취약계층 접근성 |
| 도로망·POI | 국토부 VWorld | 경로 최적화 |

## 데이터 흐름

```
공공API 수집 (Spring Batch, 매일 03:00)
  → 전처리 (좌표계 변환 EPSG:5179→WGS84, @ItemProcessor)
  → DemandAnalysisService (데이터 JSON 직렬화 → Gemini API 호출 → 수요 점수 파싱)
  → 사각지대 탐지 결과 저장 (JPA)
  → RouteRecommendService (사각지대 정보 → Gemini API 호출 → 노선 제안 파싱)
  → REST API 노출 (관리자 대시보드 / 시민 앱)
  → 시민 제보 환류 (@ApplicationEvent → 수요 재분석 트리거)
```

## 패키지 구조

```
com.wonju.bus
├── interfaces          # Controller, DTO (Request/Response)
│   ├── admin           # 관리자 API
│   └── citizen         # 시민 제보 API
├── application         # Service, 유스케이스 조합
├── domain              # Entity, Repository 인터페이스, 도메인 규칙
│   ├── route
│   ├── demand
│   ├── complaint
│   └── common          # BaseEntity, 공통 도메인
├── infrastructure      # Repository 구현체, Adapter, Batch, 외부 연동
│   ├── persistence     # JPA Repository 구현체, QueryDSL
│   ├── gemini          # GeminiAdapter, GeminiPromptBuilder
│   ├── sens            # NaverSensAdapter
│   ├── publicdata      # TagoFetcher, KosisFetcher
│   └── batch           # Spring Batch Job/Step
└── common              # ApiResponse, ErrorCode, GeoUtils, HttpClientSupport 등 공통 유틸
```

## 커밋 컨벤션

### 커밋 유형 (Commit Type)

| 태그 | 설명 |
|---|---|
| `feat` | 기능 개발 |
| `fix` | 버그 수정 |
| `style` | UI 스타일 수정 (기능 변경 없음) |
| `docs` | 문서 작업 (주로 main에서 README.md) |
| `refactor` | 리팩토링 (기능 변경 없이 구조/설계 개선) |
| `chore` | 설정 변경, 파일 이동/이름 변경 등 빌드/기능에 영향 없는 작업 |

### 커밋 메시지 형식

```
[태그] #이슈번호: 설명
```

**예시**
```
[FEAT] #12: 버스 노선 수요 분석 Service 구현
[FIX] #15: 시민 제보 중복 전화번호 에러 수정
[DOCS] #20: README.md 컨벤션 정리
[CHORE] #0: build.gradle QueryDSL 의존성 추가
```

**규칙**
- 커밋 유형은 반드시 영문 대문자 `[태그]` 형식으로 작성
- 커밋 메시지는 한글로 작성, 라이브러리명/기술 용어/에러 메시지는 영어 허용
- 커밋은 "논리적으로 독립적인 작업" 단위로 작성 (독립 빌드·테스트 가능, 롤백 최소 영향)

## 브랜치 전략

### 브랜치 구조

| 브랜치 | 용도 |
|---|---|
| `main` | 배포용. 실제 서비스 코드만 포함. 직접 push 금지 |
| `develop` | 개발 통합 브랜치. feature에서 작업 후 이곳으로 병합. 충분한 테스트 후 main 배포 |
| `feat/#이슈번호-깃허브아이디` | 신규 기능 개발. develop에서 분기, 완료 후 develop으로 병합 |
| `fix/#이슈번호-깃허브아이디` | 버그 수정. develop에서 분기, 완료 후 develop으로 병합 |
| `hotfix/#이슈번호-깃허브아이디` | 운영 긴급 버그. main에서 분기, 완료 후 main·develop 모두 병합 |

### 브랜치 네이밍 규칙

- kebab-case 사용
- 형식: `타입/#이슈번호-깃허브아이디`

**예시**
```
feat/12-mybookG      # 기능 개발
fix/15-mybookG       # 버그 수정
docs/20-mybookG      # 문서 작업
hotfix/33-mybookG    # 긴급 수정
```

### 작업 흐름

```
이슈 생성 → feat/#이슈번호-아이디 브랜치 생성 (develop에서 분기)
  → 커밋 ([FEAT] #이슈번호: 설명)
  → PR (develop으로)
  → 코드 리뷰 (최소 1인)
  → develop 병합
  → (배포 준비 완료 시) develop → main 병합
```

- PR 머지 전 최소 1인 리뷰 필수
- 기능 개발은 반드시 feat 브랜치에서, 긴급 수정은 hotfix 브랜치에서

## 코딩 컨벤션

### Claude 행동 원칙
- **작업 완료 후 보고**: 모든 작업이 끝나면 변경된 파일, 추가된 메서드, 수정 이유를 3줄 이내로 간단명료하게 보고
- **충돌 시 질문 먼저**: 요구사항 간 충돌이 감지되면 임의로 판단하지 않고 어떤 방향으로 할지 먼저 물어볼 것
- **로깅 적극 활용**: 모든 주요 흐름에 `log.info()` 필수 삽입 (아래 로깅 규칙 참고)

### 기본 원칙
- 인터페이스 우선 설계 — 구현체 직접 주입 금지, 항상 Port/Repository 인터페이스 경유
- 레이어 의존 방향: Controller → Service → Repository (역방향 금지)
- 단위 테스트 시 AiAnalysisPort, SmsPort, MapPort는 Mock 구현체로 교체

### 계층별 역할 엄수
각 계층은 자신의 역할만 수행. 역할 외 로직이 보이면 즉시 적절한 계층으로 이동.

- **Controller**: 요청 수신, 입력값 검증(`@Valid`), 응답 반환만 담당. 비즈니스 로직 금지
- **Service**: 비즈니스 로직, 트랜잭션 관리, 도메인 조합만 담당. DB 쿼리 직접 작성 금지
- **Repository**: DB 접근만 담당. 비즈니스 판단 로직 금지
- **Domain(Entity)**: 상태와 핵심 도메인 규칙만 담당. 외부 의존성 주입 금지
- **Adapter**: 외부 시스템(Gemini, SENS, 공공API) 변환만 담당. 비즈니스 로직 금지

### 외부 API 호출 원칙
- 모든 외부 API 호출은 try-catch + fallback 처리 필수 (Gemini, SENS, 공공API 장애 대비)
- fallback 우선순위: Redis 캐시 → 규칙 기반 기본값 → 예외 발생
- **트랜잭션 내부에서 외부 API 호출 금지** — 커넥션 고갈 위험. 트랜잭션 종료 후 호출하거나 `@TransactionalEventListener` 활용

### 로깅 규칙 (log.info 적극 활용)
- 형식: `[클래스명] 작업명 - key=value, key=value`
- INFO: 주요 흐름 진입·완료, 외부 API 호출
- WARN: fallback 발동, 재시도 발생
- ERROR: 예외 발생, 배치 실패
- DEBUG: 쿼리 파라미터, 상세 응답 본문
- 개인정보(전화번호, 이름)는 반드시 마스킹 처리 후 로깅

### 공통 컴포넌트 — 재사용성
새 기능 개발 전 아래 공통 컴포넌트 존재 여부를 먼저 확인하고 활용할 것.
중복 구현 발견 시 즉시 공통 컴포넌트로 추출.

- **BaseEntity**: 모든 엔티티 상속 필수 — `createdAt`, `updatedAt`, `createdBy` 자동 관리
- **ApiResponse\<T\>**: 모든 REST 응답 래퍼. 성공: `ApiResponse.success(data)`, 실패: `ApiResponse.error(code, message)`. 컨트롤러 개별 Map 반환 금지
- **BusServiceException**: 커스텀 예외 베이스 클래스. `@RestControllerAdvice` 전역 핸들러에서 일괄 처리 — 컨트롤러 내 try-catch 금지
- **ErrorCode enum**: 에러 코드 중앙 관리
- **PageResponse\<T\>**: 목록 조회 응답 통일 (`content`, `totalElements`, `totalPages`, `page`). 컨트롤러마다 다른 구조 금지
- **HttpClientSupport**: WebClient 설정(타임아웃, 재시도, 로깅) 공통 관리. 각 Adapter는 상속·활용, 개별 생성 금지
- **GeminiPromptBuilder**: 프롬프트 조립, JSON 강제 지시문, 파싱 로직 단일 관리. 서비스에서 직접 조립 금지
- **GeoUtils**: 좌표계 변환(EPSG:5179 → WGS84), 반경 계산 등 공간 연산 집중. 서비스·쿼리 레이어 중복 구현 금지

### Soft Delete
- 제보, 노선 제안 등 이력이 필요한 데이터는 물리 삭제 금지 — `deletedAt` 컬럼으로 소프트 삭제
- `BaseEntity`에 `deletedAt` 포함, `@Where(clause = "deleted_at IS NULL")` 기본 적용
- 물리 삭제가 필요한 경우(임시 데이터 등)는 명시적으로 주석으로 이유 기재

### DB 설계 — N:M 금지
- **`@ManyToMany` 절대 사용 금지** — 중간 엔티티로 반드시 분리
- 예: `BusRoute` ↔ `BusStop` → `RouteStop` 엔티티로 분리
- 중간 엔티티에는 의미 있는 컬럼 추가 (순서, 방향, 등록일 등)
- `@ManyToMany` 발견 시 즉시 리팩토링 대상

### 쿼리 관리
- **단순 조회**: Spring Data JPA 메서드 네이밍 (`findByAreaCodeAndStatus`)
- **복잡한 조건 조회**: QueryDSL 사용 — JPQL/Native 인라인 문자열 금지
- **공간 쿼리**: `@Query` + PostGIS 함수, Repository 인터페이스에 명시적 선언
- **집계·통계 쿼리**: 별도 `*QueryRepository` 클래스로 분리 (일반 Repository 혼재 금지)
- QueryDSL Q클래스 수동 수정 금지, `build/generated` gitignore 처리
- 페이징은 반드시 `Pageable` 사용 — `LIMIT/OFFSET` 직접 작성 금지
- N+1 방지: 연관 엔티티 조회 시 `fetch join` 또는 `@EntityGraph` 명시, 기본 fetch 전략 `LAZY`

### 트랜잭션 관리
- `@Transactional`은 Service 레이어에만 선언 — Controller, Repository 선언 금지
- 조회 메서드: `@Transactional(readOnly = true)` 필수
- 쓰기 메서드: `@Transactional` (기본 전파: REQUIRED)

### DTO 변환 규칙
- **Entity는 Controller까지 올라오면 안 됨** — Service에서 DTO로 변환 후 반환
- 요청 흐름: `{도메인}Request(Controller)` → `Service` → `Entity(Repository)`
- 응답 흐름: `Entity(Repository)` → `Service` → `{도메인}Response(Controller)`
- DTO 네이밍: `{도메인}{동사}Request` / `{도메인}{동사}Response` (예: `ComplaintCreateRequest`, `RouteRecommendResponse`)
- 변환 로직 복잡 시 `{도메인}Mapper` 클래스로 분리, 서비스 인라인 변환 금지

### API 버전 관리
- 모든 API URL: `/api/v1/...` 고정
- 하위 호환 깨지는 변경 시 `/api/v2/...` 신규 추가, 기존 버전 즉시 삭제 금지
- 버전 prefix는 Controller 클래스 레벨 `@RequestMapping`으로 선언

### 환경변수·시크릿 관리
- API 키, 비밀번호, 토큰 코드 하드코딩 절대 금지
- 환경별 설정: `application-{profile}.yml` 분리 (local / dev / prod)
- 시크릿 값은 환경변수로 주입, `application.yml` 직접 기재 금지
- `.gitignore`에 `application-prod.yml`, `.env` 반드시 포함
- 설정 값은 `@Value` 대신 `@ConfigurationProperties` 클래스로 묶어서 관리

### 테스트 규칙
- **단위 테스트 필수**: 모든 Service 클래스 — 외부 의존성은 Mockito Mock 처리
- **통합 테스트**: Repository (`@DataJpaTest`), 외부 API Adapter (`@SpringBootTest` + WireMock)
- Controller: `@WebMvcTest` 사용 — 전체 컨텍스트 로딩 금지
- 클래스 네이밍: `{클래스명}Test` (단위) / `{클래스명}IntegrationTest` (통합)
- 메서드 네이밍: `{메서드명}_{조건}_{기대결과}` (예: `analyzeDemand_whenAreaNotFound_throwsException`)
- given / when / then 주석으로 구분
- 테스트 픽스처는 `{도메인}Fixture` 클래스로 분리, 각 테스트에서 중복 생성 금지

### Redis 키 네이밍
- `sms:verify:{phoneNumber}` — SMS 인증 코드
- `cache:tago:{routeId}` — 공공API 노선 캐시
- `cache:gemini:demand:{areaCode}` — Gemini 수요 분석 결과 캐시
