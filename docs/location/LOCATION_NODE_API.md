# LocationNode API 설계

`location` 모듈의 위치 트리 노드(`location_node`) API·비즈니스 규칙을 정리한 문서입니다.

> API prefix: `/api/manager/location-node`  
> 관련 ERD: [ERD.md — location_node](../ERD.md#location_node--위치-트리-노드-location-모듈)  
> DDL: [V004__create_location_node_table.sql](../../sql/history/V004__create_location_node_table.sql)

---

## 1. 개요

위치 노드는 **트리 구조**로 관리합니다. 루트가 여러 개인 **포레스트**도 허용합니다.

| 개념 | 설명 |
|------|------|
| 루트 노드 | `parent_code = null` |
| 자식 노드 | `parent_code`로 부모 노드에 연결 |
| 위치 유형 | `common_code` 중 `group_key = 'LOCATION_TYPE'`만 허용 |
| 리프 노드 | 자식이 없는 노드 |
| `code` | **PK**. 서버가 **10자 Base62** 랜덤 문자열로 자동 생성. 불변. API·FK 식별자 |

트리 구조의 진실 원천은 **`parent_code`** 입니다.  
사용자는 **`name`** 만 입력합니다.

### 1.1 공통 제약

| 항목 | 규칙 |
|------|------|
| `name` | 필수. 같은 부모 아래에서 중복 불가 (`uk_location_node_parent_code_name`) |
| `code` | 서버 자동 생성 (10자 Base62, `[0-9A-Za-z]`). 사용자 입력 없음. 변경 불가 |
| `location_type_id` | 필수. `LOCATION_TYPE` 그룹 소속만 허용 |
| 순환 참조 | 금지 (자기 자신·자손을 부모로 지정 불가) |

### 1.2 향후 devices 연동

장비(`devices`)는 위치 노드의 **`code`를 FK**(`CHAR(10)`)로 참조합니다.

- CONTAINER에 슬라이딩 도어·배연창 → `location_node_code` = CONTAINER의 `code`
- ROW/RACK 장비도 동일 — **유형 제한 없음**

---

### 1.3 위치 유형(LOCATION_TYPE) 순서

`common_code.sort_order`(또는 동등한 유형 순서 정의)로 **유형 간 계층 순서**를 판단합니다. (**예정**)

예시:

| code | name | sort_order (유형 순서) |
|------|------|------------------------|
| CONTAINER | 컨테이너 | 0 |
| ZONE | 존 | 1 |
| ROW | 열 | 2 |
| RACK | 랙 | 3 |

유형 순서는 트리에서 **부모 → 자식 방향으로 단조 증가**해야 합니다.

---

## 2. 등록 API

### 2.1 단건 등록 — `POST /api/manager/location-node`

**구현 상태:** ✅ 구현됨 (유형 순서 검증·트리 재구성은 미구현)

#### 요청

```json
{
  "parentCode": null,
  "locationTypeId": 1,
  "name": "컨테이너 A"
}
```

| 필드 | 필수 | 설명 |
|------|------|------|
| `parentCode` | 조건부 | `null`이면 **루트** 등록. 값이 있으면 해당 `code`를 부모로 **자식** 등록 |
| `locationTypeId` | O | `LOCATION_TYPE` common_code ID |
| `name` | O | 노드 표시명 |

#### 응답

서버가 생성한 `code`를 포함합니다.

```json
{
  "code": "K7mN2pQx9L",
  "parentCode": null,
  "locationTypeId": 1,
  "name": "컨테이너 A"
}
```

#### 자식 등록 시 트리 재구성 (핵심 규칙, 예정)

등록 전:

```
CONTAINER
└── ROW
```

`CONTAINER` 아래에 `ZONE` 유형 노드를 추가하면:

```
CONTAINER
└── ZONE
    └── ROW    ← 기존 ROW의 parent_code가 ZONE으로 변경
```

1. 새 노드 `N`을 부모 `P` 아래에 등록한다.
2. `P`의 **직접 자식** 중, `location_type` 순서가 `N`보다 **큰** 노드들을 찾는다.
3. 해당 노드들의 `parent_code`를 `N.code`로 변경한다.

#### 오류

| 조건 | HTTP | 메시지(예) |
|------|------|------------|
| 부모 없음 | 404 | `LocationNode not found: {parentCode}` |
| 위치 유형 없음 | 404 | `CommonCode not found: {locationTypeId}` |
| LOCATION_TYPE 아님 | 400 | `locationType must belong to LOCATION_TYPE group` |
| 형제 name 중복 | 400 | `name already exists under parent` |
| 유형 순서 위반 | 400 | `child location type must be deeper than parent` |

---

### 2.2 일괄 등록 — `POST /api/manager/location-node/bulk`

**구현 상태:** ⬜ 미구현

```json
{
  "nodes": [
    {
      "parentCode": null,
      "locationTypeId": 1,
      "name": "컨테이너 A"
    },
    {
      "parentCode": "K7mN2pQx9L",
      "locationTypeId": 2,
      "name": "ZONE 1"
    }
  ]
}
```

- 단일 트랜잭션, 배열 순서대로 처리
- 앞서 등록된 노드의 응답 `code`를 다음 노드의 `parentCode`로 사용

---

## 3. 수정 API

### 3.1 기본 수정 — `PUT /api/manager/location-node/{code}`

**구현 상태:** ✅ 구현됨

`locationType`, `name`만 수정. **`code`·`parentCode`는 변경 불가.**

#### 요청

```json
{
  "locationTypeId": 2,
  "name": "컨테이너 A (수정)"
}
```

| 필드 | 수정 |
|------|------|
| `locationType` | O |
| `name` | O |
| `code` | X (PK, 불변) |
| `parent` | X (별도 API) |

---

### 3.2 부모 변경 — `PATCH /api/manager/location-node/{code}/parent`

**구현 상태:** ⬜ 미구현

```json
{
  "parentCode": "A1b2C3d4E5"
}
```

| 필드 | 설명 |
|------|------|
| `parentCode` | 새 부모 `code`. `null`이면 루트 승격 |

---

### 3.3 일괄 수정 — 검토 중

**구현 상태:** ⬜ 미구현 · 필요 여부 미확정

---

## 4. 조회 API

### 4.1 목록 조회 — `GET /api/manager/location-node`

**구현 상태:** ⬜ 미구현

| 파라미터 | 설명 |
|----------|------|
| `name` | 이름 부분 일치 검색 |
| `parentCode` | 특정 부모의 직접 자식만 |
| `locationTypeId` | 위치 유형 필터 |

---

## 5. 삭제 API

### 5.1 단건 삭제 (리프만) — `DELETE /api/manager/location-node/{code}`

**구현 상태:** ⬜ 미구현

- 자식 없음 → 삭제 성공
- 자식 있음 → 400
- 트리 비우기: 리프부터 `DELETE` 반복

### 5.2 서브트리 전체 삭제 — `DELETE /api/manager/location-node/{code}/subtree`

**구현 상태:** ⬜ 미구현

- 해당 노드 + 모든 자손 cascade 삭제
- 향후 `devices.location_node_code` 참조 시 RESTRICT/409 검토

---

## 6. API 요약

| Method | Path | 설명 | 상태 |
|--------|------|------|------|
| `POST` | `/api/manager/location-node` | 단건 등록 | ✅ |
| `POST` | `/api/manager/location-node/bulk` | 일괄 등록 | ⬜ |
| `PUT` | `/api/manager/location-node/{code}` | 메타 수정 | ✅ |
| `PATCH` | `/api/manager/location-node/{code}/parent` | 부모 변경 | ⬜ |
| `GET` | `/api/manager/location-node` | 목록 조회 | ⬜ |
| `DELETE` | `/api/manager/location-node/{code}` | 리프만 삭제 | ⬜ |
| `DELETE` | `/api/manager/location-node/{code}/subtree` | 서브트리 전체 삭제 | ⬜ |

---

## 7. 구현 현황

| 구분 | 내용 |
|------|------|
| 도메인 | `createRoot`, `createChild` (10자 Base62 `code` 자동 생성), `update` |
| DTO | `LocationNodeCreateRequest`, `LocationNodeUpdateRequest` 분리 |
| 미구현 | 부모 변경, 유형 순서·재부모화, 조회, 삭제, bulk |

---

## 8. 갱신 이력

| 날짜 | 변경 |
|------|------|
| 2026-07-02 | 최초 작성 |
| 2026-07-02 | `depth` 제거, 하위 순차 삭제 API 제거 |
| 2026-07-02 | `id` 제거, `code` PK·서버 자동 생성, `parentCode` 기준으로 전환 |
| 2026-07-02 | `code` 형식을 UUID → **10자 Base62** 로 변경 (`LocationNodeCodeGenerator`) |
