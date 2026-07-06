# 원주시 버스 최적화 시스템 API 명세서

- **Base URL**: `https://{host}/api/v1`
- **Content-Type**: `application/json`
- **인증 방식**: JWT Bearer Token (관리자 전용 API)
- **버전**: v1

---

## 목차

1. [인증 (Auth)](#1-인증-auth)
2. [SMS 인증 (SMS)](#2-sms-인증-sms)
3. [시민 제보 (Complaint)](#3-시민-제보-complaint)
4. [버스 노선 조회 (Route)](#4-버스-노선-조회-route)
5. [수요 분석 (Demand)](#5-수요-분석-demand)
6. [사각지대 (BlindSpot)](#6-사각지대-blindspot)
7. [노선 추천 공개 조회 (Recommendation)](#7-노선-추천-공개-조회-recommendation)
8. [관리자 - 노선 추천 (Admin Recommendation)](#8-관리자---노선-추천-admin-recommendation)
9. [관리자 - 제보 관리 (Admin Complaint)](#9-관리자---제보-관리-admin-complaint)
10. [배치 작업 (Batch) - 관리자](#10-배치-작업-batch---관리자)

---

## Security 권한 매핑

| URL 패턴 | 접근 권한 |
|----------|----------|
| `POST /sms/**` | `permitAll` |
| `POST /complaints` | `permitAll` (SMS verifiedToken 필요) |
| `GET /complaints/{id}` | `permitAll` |
| `GET /routes/**` | `permitAll` |
| `GET /demand/**` | `permitAll` |
| `GET /recommendations` | `permitAll` (승인된 추천만 반환) |
| `POST /auth/login` | `permitAll` |
| `/admin/**` | `hasRole(ADMIN)` |

---

## 공통 응답 포맷

```json
{
  "success": true,
  "data": {},
  "message": "OK"
}
```

### 오류 응답

```json
{
  "success": false,
  "data": null,
  "message": "에러 메시지"
}
```

### HTTP 상태 코드

| 코드 | 의미 |
|------|------|
| 200  | 성공 |
| 201  | 생성 성공 |
| 400  | 잘못된 요청 (유효성 검사 실패) |
| 401  | 인증 실패 (토큰 없음/만료) |
| 403  | 권한 없음 (ROLE_ADMIN 필요) |
| 404  | 리소스 없음 |
| 429  | 요청 한도 초과 (일일 제한) |
| 500  | 서버 내부 오류 |

---

## 1. 인증 (Auth)

### POST `/auth/login`

관리자 로그인. JWT 토큰을 발급합니다.

**Request Body**

```json
{
  "username": "admin",
  "password": "password123!"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "message": "로그인 성공"
}
```

**Response 401**

```json
{
  "success": false,
  "data": null,
  "message": "아이디 또는 비밀번호가 올바르지 않습니다."
}
```

---

## 2. SMS 인증 (SMS)

> 인증 불필요 (permitAll)

### POST `/sms/send`

시민 제보 전 휴대폰 인증코드를 발송합니다.

- 동일 번호 재발송 대기: 60초
- 인증코드 유효시간: 3분 (Redis TTL)

**Request Body**

```json
{
  "phoneNumber": "01012345678"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "expiresIn": 180
  },
  "message": "인증코드가 발송되었습니다."
}
```

**Response 400**

```json
{
  "success": false,
  "data": null,
  "message": "유효하지 않은 전화번호 형식입니다."
}
```

**Response 429** — 60초 쿨다운 중 재요청

```json
{
  "success": false,
  "data": {
    "retryAfterSeconds": 42
  },
  "message": "잠시 후 다시 시도해주세요."
}
```

---

### POST `/sms/verify`

발송된 인증코드를 검증합니다. 성공 시 단기 인증 토큰을 반환합니다.

**Request Body**

```json
{
  "phoneNumber": "01012345678",
  "code": "123456"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "verifiedToken": "eyJhbGci...",
    "expiresIn": 600
  },
  "message": "인증되었습니다."
}
```

**Response 400**

```json
{
  "success": false,
  "data": null,
  "message": "인증코드가 일치하지 않거나 만료되었습니다."
}
```

---

## 3. 시민 제보 (Complaint)

> 인증 불필요 (permitAll) — SMS 인증 토큰 필요

### POST `/complaints`

시민이 버스 불편사항을 제보합니다.

- 동일 번호 일일 최대 3회
- 위치 기반 200m 내 유사 제보 중복 감지
- Gemini AI 자동 분류 (트랜잭션 커밋 후 비동기)

**Request Body**

```json
{
  "verifiedToken": "eyJhbGci...",
  "content": "삼산동 삼거리에서 버스가 너무 뜸합니다. 출퇴근 시간에 30분 이상 기다립니다.",
  "latitude": 37.3428,
  "longitude": 127.9201
}
```

**Response 201**

```json
{
  "success": true,
  "data": {
    "complaintId": 42,
    "status": "RECEIVED",
    "message": "제보가 접수되었습니다."
  },
  "message": "제보 접수 완료"
}
```

**Response 401** — verifiedToken 만료 또는 무효

```json
{
  "success": false,
  "data": null,
  "message": "SMS 인증이 만료되었습니다. 다시 인증해주세요."
}
```

**Response 429** — 일일 한도 초과

```json
{
  "success": false,
  "data": null,
  "message": "일일 제보 한도(3건)를 초과하였습니다."
}
```

---

### GET `/complaints/{complaintId}`

제보 상태를 조회합니다.

**Path Variable**: `complaintId` (Long)

**Response 200**

```json
{
  "success": true,
  "data": {
    "complaintId": 42,
    "content": "삼산동 삼거리에서...",
    "category": "INSUFFICIENT_FREQUENCY",
    "severityScore": 4,
    "status": "CLASSIFIED",
    "createdAt": "2026-07-06T09:30:00"
  },
  "message": "OK"
}
```

> **Note**: `content`는 공개 조회이므로 개인정보(전화번호)는 응답에 포함하지 않습니다.

---

## 4. 버스 노선 조회 (Route)

> 인증 불필요 (공개)

### GET `/routes`

지역코드 기준 노선 목록을 조회합니다.

**Query Parameters**

| 파라미터 | 필수 | 기본값 | 설명 |
|---------|------|-------|------|
| `areaCode` | O | — | 지역코드 (예: `42220`) |
| `routeType` | X | — | 노선 유형 필터 (`간선`, `지선`, `마을`) |
| `page` | X | 0 | 페이지 번호 |
| `size` | X | 20 | 페이지 크기 |

**Response 200**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "routeId": "41-1",
        "routeName": "41-1번",
        "routeType": "간선",
        "startStop": "원주역",
        "endStop": "신림면사무소",
        "intervalMinutes": 25,
        "areaCode": "42220"
      }
    ],
    "totalElements": 35,
    "totalPages": 2,
    "page": 0,
    "size": 20
  },
  "message": "OK"
}
```

---

### GET `/routes/{routeId}`

특정 노선 상세 정보를 조회합니다.

**Path Variable**: `routeId` (String, 예: `"41-1"`)

**Response 200**

```json
{
  "success": true,
  "data": {
    "routeId": "41-1",
    "routeName": "41-1번",
    "routeType": "간선",
    "startStop": "원주역",
    "endStop": "신림면사무소",
    "intervalMinutes": 25,
    "areaCode": "42220"
  },
  "message": "OK"
}
```

**Response 404**

```json
{
  "success": false,
  "data": null,
  "message": "노선을 찾을 수 없습니다. routeId=41-1"
}
```

---

### GET `/routes/{routeId}/stops`

특정 노선의 정류장 목록을 순서대로 조회합니다.

**Path Variable**: `routeId` (String, 예: `"41-1"`)

**Response 200**

```json
{
  "success": true,
  "data": {
    "routeId": "41-1",
    "routeName": "41-1번",
    "stops": [
      {
        "sequence": 1,
        "direction": "상행",
        "stopId": "WJ001",
        "stopName": "원주역",
        "latitude": 37.3384,
        "longitude": 127.9202
      }
    ]
  },
  "message": "OK"
}
```

---

## 5. 수요 분석 (Demand)

> 인증 불필요 (공개 조회) — Gemini AI 분석 결과

### GET `/demand/scores`

지역별 수요 점수 목록을 조회합니다.

**Query Parameters**

| 파라미터 | 필수 | 기본값 | 설명 |
|---------|------|-------|------|
| `areaCode` | X | 42220 | 지역코드 |
| `date` | X | 오늘 | 분석 날짜 (yyyy-MM-dd) |
| `sort` | X | `demand_score,desc` | 정렬 기준 |

**Response 200**

```json
{
  "success": true,
  "data": [
    {
      "areaCode": "42220",
      "areaName": "원주시 단계동",
      "demandScore": 87.5,
      "supplyIndex": 32.0,
      "population": 12500,
      "welfareFacilityCount": 8,
      "analyzedDate": "2026-07-06"
    }
  ],
  "message": "OK"
}
```

---

### GET `/demand/scores/{areaCode}`

특정 지역의 수요 점수 상세 및 Gemini 분석 내용을 조회합니다.

**Path Variable**: `areaCode` (String)

**Response 200**

```json
{
  "success": true,
  "data": {
    "areaCode": "42220",
    "areaName": "원주시 단계동",
    "demandScore": 87.5,
    "supplyIndex": 32.0,
    "population": 12500,
    "buildingCount": 3200,
    "welfareFacilityCount": 8,
    "analyzedDate": "2026-07-06",
    "aiAnalysisSummary": "해당 지역은 고령 인구 비율이 높고 복지시설 밀도 대비 대중교통 공급이 현저히 부족합니다."
  },
  "message": "OK"
}
```

---

## 6. 사각지대 (BlindSpot)

> 인증 불필요 (공개 조회)

### GET `/demand/blind-spots`

교통 사각지대 목록을 조회합니다.

**Query Parameters**

| 파라미터 | 필수 | 기본값 | 설명 |
|---------|------|-------|------|
| `areaCode` | X | 42220 | 지역코드 |
| `resolved` | X | false | 해소 여부 필터 |
| `sort` | X | `demand_score,desc` | 정렬 기준 |
| `page` | X | 0 | 페이지 번호 |
| `size` | X | 20 | 페이지 크기 |

**Response 200**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 3,
        "areaCode": "42220",
        "areaName": "원주시 판부면",
        "demandScore": 91.2,
        "supplyIndex": 15.0,
        "centerLatitude": 37.4012,
        "centerLongitude": 127.8871,
        "detectedDate": "2026-07-01",
        "resolved": false
      }
    ],
    "totalElements": 7,
    "totalPages": 1,
    "page": 0,
    "size": 20
  },
  "message": "OK"
}
```

---

## 7. 노선 추천 공개 조회 (Recommendation)

> 인증 불필요 (공개) — 승인된 추천만 반환

### GET `/recommendations`

시민에게 공개되는 **승인된** 노선 추천 목록을 조회합니다.

**Query Parameters**

| 파라미터 | 필수 | 기본값 | 설명 |
|---------|------|-------|------|
| `areaCode` | X | 42220 | 지역코드 |
| `sort` | X | `priority_score,desc` | 정렬 기준 |
| `page` | X | 0 | 페이지 번호 |
| `size` | X | 20 | 페이지 크기 |

**Response 200**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "recommendationId": 12,
        "areaName": "원주시 판부면",
        "recommendType": "NEW_ROUTE",
        "description": "판부면사무소 ↔ 원주시청 직통 노선 신설을 권장합니다.",
        "priorityScore": 88.7,
        "estimatedBeneficiaries": 1200,
        "status": "APPROVED"
      }
    ],
    "totalElements": 3,
    "totalPages": 1,
    "page": 0,
    "size": 20
  },
  "message": "OK"
}
```

---

## 8. 관리자 - 노선 추천 (Admin Recommendation)

> **Authorization**: `Bearer {accessToken}` (ROLE_ADMIN 필요)  
> **URL Prefix**: `/admin/recommendations/...` → SecurityConfig `/admin/**` 보호 범위

### POST `/admin/recommendations/blind-spots/{blindSpotId}/generate`

특정 사각지대에 대한 노선 추천을 Gemini AI로 생성합니다.

**Path Variable**: `blindSpotId` (Long)

**Response 201**

```json
{
  "success": true,
  "data": {
    "recommendationId": 12,
    "blindSpotId": 3,
    "areaName": "원주시 판부면",
    "recommendType": "NEW_ROUTE",
    "description": "판부면사무소 ↔ 원주시청 직통 노선 신설을 권장합니다.",
    "priorityScore": 88.7,
    "estimatedBeneficiaries": 1200,
    "status": "PENDING"
  },
  "message": "노선 추천 생성 완료"
}
```

**Response 404**

```json
{
  "success": false,
  "data": null,
  "message": "해당 사각지대를 찾을 수 없습니다. blindSpotId=3"
}
```

---

### GET `/admin/recommendations`

모든 상태의 노선 추천 목록을 조회합니다 (관리자 전용).

**Query Parameters**

| 파라미터 | 필수 | 기본값 | 설명 |
|---------|------|-------|------|
| `status` | X | — | 상태 필터 (`PENDING`, `APPROVED`, `REJECTED`) |
| `areaCode` | X | 42220 | 지역코드 |
| `sort` | X | `priority_score,desc` | 정렬 기준 |
| `page` | X | 0 | 페이지 번호 |
| `size` | X | 20 | 페이지 크기 |

**Response 200**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "recommendationId": 12,
        "blindSpotId": 3,
        "areaName": "원주시 판부면",
        "recommendType": "NEW_ROUTE",
        "description": "판부면사무소 ↔ 원주시청 직통 노선 신설을 권장합니다.",
        "priorityScore": 88.7,
        "estimatedBeneficiaries": 1200,
        "status": "PENDING",
        "createdAt": "2026-07-06T10:00:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "page": 0,
    "size": 20
  },
  "message": "OK"
}
```

---

### PATCH `/admin/recommendations/{recommendationId}/approve`

노선 추천을 승인합니다.

**Path Variable**: `recommendationId` (Long)

**Response 200**

```json
{
  "success": true,
  "data": {
    "recommendationId": 12,
    "status": "APPROVED",
    "approvedAt": "2026-07-06T11:30:00"
  },
  "message": "노선 추천이 승인되었습니다."
}
```

---

### PATCH `/admin/recommendations/{recommendationId}/reject`

노선 추천을 반려합니다.

**Path Variable**: `recommendationId` (Long)

**Request Body**

```json
{
  "reason": "예산 부족으로 당분간 신규 노선 운영 불가"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "recommendationId": 12,
    "status": "REJECTED",
    "rejectedAt": "2026-07-06T11:35:00"
  },
  "message": "노선 추천이 반려되었습니다."
}
```

---

## 9. 관리자 - 제보 관리 (Admin Complaint)

> **Authorization**: `Bearer {accessToken}` (ROLE_ADMIN 필요)

### GET `/admin/complaints`

전체 제보 목록을 조회합니다.

**Query Parameters**

| 파라미터 | 필수 | 기본값 | 설명 |
|---------|------|-------|------|
| `status` | X | — | 상태 필터 (`RECEIVED`, `CLASSIFIED`, `PROCESSING`, `RESOLVED`) |
| `category` | X | — | 분류 필터 |
| `sort` | X | `created_at,desc` | 정렬 기준 |
| `page` | X | 0 | 페이지 번호 |
| `size` | X | 20 | 페이지 크기 |

**Response 200**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "complaintId": 42,
        "maskedPhone": "010****5678",
        "content": "삼산동 삼거리에서 버스가 너무 뜸합니다.",
        "latitude": 37.3428,
        "longitude": 127.9201,
        "category": "INSUFFICIENT_FREQUENCY",
        "severityScore": 4,
        "status": "CLASSIFIED",
        "createdAt": "2026-07-06T09:30:00"
      }
    ],
    "totalElements": 28,
    "totalPages": 2,
    "page": 0,
    "size": 20
  },
  "message": "OK"
}
```

---

### PATCH `/admin/complaints/{complaintId}/status`

제보 처리 상태를 변경합니다.

**Path Variable**: `complaintId` (Long)

**Request Body**

```json
{
  "status": "RESOLVED",
  "note": "41-1번 노선 배차 간격 15분으로 단축 조치 완료"
}
```

**Response 200**

```json
{
  "success": true,
  "data": {
    "complaintId": 42,
    "status": "RESOLVED",
    "updatedAt": "2026-07-06T14:00:00"
  },
  "message": "제보 상태가 업데이트되었습니다."
}
```

---

## 10. 배치 작업 (Batch) - 관리자

> **Authorization**: `Bearer {accessToken}` (ROLE_ADMIN 필요)

### POST `/admin/batch/public-data`

공공데이터 수집 및 수요 분석 배치를 수동으로 실행합니다.

> **Note**: 매일 자정 Spring Batch가 자동 실행됩니다. 이 API는 수동 재실행용입니다.

**Request Body**

```json
{
  "areaCode": "42220",
  "forceRerun": false
}
```

**Response 202**

```json
{
  "success": true,
  "data": {
    "jobExecutionId": 15,
    "status": "STARTED",
    "startedAt": "2026-07-06T11:40:00"
  },
  "message": "배치 작업이 시작되었습니다."
}
```

---

### GET `/admin/batch/status/{jobExecutionId}`

배치 실행 상태를 조회합니다.

**Path Variable**: `jobExecutionId` (Long)

**Response 200**

```json
{
  "success": true,
  "data": {
    "jobExecutionId": 15,
    "jobName": "publicDataBatchJob",
    "status": "COMPLETED",
    "startedAt": "2026-07-06T11:40:00",
    "endedAt": "2026-07-06T11:42:30",
    "steps": [
      {
        "stepName": "collectBusRouteStep",
        "status": "COMPLETED",
        "readCount": 35,
        "writeCount": 35
      },
      {
        "stepName": "analyzeDemandStep",
        "status": "COMPLETED",
        "readCount": 42,
        "writeCount": 42
      }
    ]
  },
  "message": "OK"
}
```

---

## Enum 값 정리

### ComplaintCategory

| 값 | 설명 |
|----|------|
| `INSUFFICIENT_FREQUENCY` | 배차 간격 부족 |
| `ROUTE_MISSING` | 노선 미운행 지역 |
| `STOP_MISSING` | 정류장 없음 |
| `INCONVENIENT_TRANSFER` | 환승 불편 |
| `SAFETY_ISSUE` | 안전 문제 |
| `OTHER` | 기타 |

### ComplaintStatus

| 값 | 설명 |
|----|------|
| `RECEIVED` | 접수됨 |
| `CLASSIFIED` | AI 분류 완료 |
| `PROCESSING` | 처리 중 |
| `RESOLVED` | 해결됨 |

### RecommendationType

| 값 | 설명 |
|----|------|
| `NEW_ROUTE` | 신규 노선 신설 |
| `FREQUENCY_INCREASE` | 배차 간격 단축 |
| `ROUTE_EXTENSION` | 기존 노선 연장 |
| `DEMAND_RESPONSIVE` | 수요응답형 버스 |

### RecommendationStatus

| 값 | 설명 |
|----|------|
| `PENDING` | 검토 대기 |
| `APPROVED` | 승인됨 |
| `REJECTED` | 반려됨 |
