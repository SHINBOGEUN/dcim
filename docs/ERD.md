# DB ERD

`new-manager-server` 데이터베이스 스키마를 모듈 단위로 정리합니다.  
테이블·컬럼이 추가될 때마다 이 문서를 갱신합니다.

> 기준 DB: MariaDB `dcim_new` (test 프로파일)  
> 엔티티 위치: `module/{name}/domain/model`  
> 공통 컬럼: `shared/persistence/BaseEntity` (`created_dt`, `updated_dt`)

---

## 전체 관계도 (현재)

```mermaid
erDiagram
    users {
        int id PK "AUTO_INCREMENT"
        varchar username UK "로그인 아이디"
        varchar password "BCrypt 해시"
        varchar role "기본값 USER"
        varchar refresh_token "JWT refresh (nullable)"
        timestamp created_dt "생성 시각"
        timestamp updated_dt "수정 시각"
    }

    code_group {
        int id PK "AUTO_INCREMENT"
        varchar group_key UK "DEVICE_TYPE 등"
        varchar group_name "그룹 표시명"
        timestamp created_dt "생성 시각"
        timestamp updated_dt "수정 시각"
    }

    common_code {
        int id PK "AUTO_INCREMENT"
        int group_id FK "code_group.id"
        varchar code "ups, pdu 등"
        varchar name "코드 표시명"
        int sort_order "정렬 순서"
        timestamp created_dt "생성 시각"
        timestamp updated_dt "수정 시각"
    }

    code_group ||--o{ common_code : "group_id"
```

| 모듈 | 테이블 | 관계 |
|------|--------|------|
| identity | `users` | 독립 |
| common | `code_group` | 1 |
| common | `common_code` | N → `code_group` |

---

## 테이블 상세

### `users` — 사용자 (identity 모듈)

| 컬럼 | 타입 | NULL | 키 | 설명 |
|------|------|------|-----|------|
| `id` | INT | N | PK | 사용자 ID |
| `username` | VARCHAR(255) | N | UK | 로그인 아이디 |
| `password` | VARCHAR(255) | N | | BCrypt 인코딩 비밀번호 |
| `role` | VARCHAR(50) | Y | | 권한 (`USER` 등) |
| `refresh_token` | VARCHAR(512) | Y | | 리프레시 토큰 저장 |
| `created_dt` | TIMESTAMP(6) | Y | | 최초 생성 시각 |
| `updated_dt` | TIMESTAMP(6) | Y | | 최종 수정 시각 |

**엔티티:** `module/identity/domain/model/User.java`  
**상속:** `BaseEntity`  
**DDL:** [V001__create_users_table.sql](../sql/history/V001__create_users_table.sql)

**참고 (애플리케이션 규칙)**

- 신규 가입 시 `role` = `USER` (API 입력 없음, `User.createNew()`에서 고정)
- `password`는 평문 저장하지 않음 (`PasswordEncoder` 사용)
- `refresh_token`은 로그인·토큰 갱신 시 갱신

#### `role` (권한)

| 항목 | 내용 |
|------|------|
| API에서 입력? | 아니요 — `AuthRequest`는 `username`, `password`만 |
| 어디서 설정? | `User.createNew()`에서 `"USER"` 하드코딩 |
| 종류 정의 | 별도 enum 없음 (현재 `"USER"`만) |
| Spring Security | `CustomUserDetails`가 `USER` → `ROLE_USER` 변환 |

---

### `code_group` — 코드 그룹 (common 모듈)

| 컬럼 | 타입 | NULL | 키 | 설명 |
|------|------|------|-----|------|
| `id` | INT | N | PK | 코드 그룹 ID |
| `group_key` | VARCHAR(100) | N | UK | 그룹 키 (예: `DEVICE_TYPE`) |
| `group_name` | VARCHAR(255) | N | | 그룹 표시명 |
| `created_dt` | TIMESTAMP(6) | Y | | 최초 생성 시각 |
| `updated_dt` | TIMESTAMP(6) | Y | | 최종 수정 시각 |

**엔티티:** `module/common/domain/model/CodeGroup.java`  
**상속:** `BaseEntity`  
**DDL:** [V002__create_code_group_table.sql](../sql/history/V002__create_code_group_table.sql)

**예시 데이터**

| id | group_key | group_name |
|----|-----------|------------|
| 1 | DEVICE_TYPE | Device Type |
| 2 | LOCATION_TYPE | Location Type |
| 3 | ASSET_TYPE | Asset Type |
| 4 | PROTOCOL_TYPE | Protocol Type |
| 5 | ALARM_TYPE | Alarm Type |

---

### `common_code` — 공통 코드 (common 모듈)

| 컬럼 | 타입 | NULL | 키 | 설명 |
|------|------|------|-----|------|
| `id` | INT | N | PK | 공통 코드 ID |
| `group_id` | INT | N | FK | `code_group.id` |
| `code` | VARCHAR(100) | N | UK* | 코드 값 (예: `ups`, `pdu`) |
| `name` | VARCHAR(255) | N | | 코드 표시명 |
| `sort_order` | INT | Y | | 목록 정렬 순서 |
| `created_dt` | TIMESTAMP(6) | Y | | 최초 생성 시각 |
| `updated_dt` | TIMESTAMP(6) | Y | | 최종 수정 시각 |

\* UK: `(group_id, code)` 복합 유니크 — 같은 그룹 내 코드 중복 불가

**엔티티:** `module/common/domain/model/CommonCode.java`  
**상속:** `BaseEntity`  
**연관:** `@ManyToOne` → `CodeGroup` (`@JoinColumn(name = "group_id")`)  
**DDL:** [V003__create_common_code_table.sql](../sql/history/V003__create_common_code_table.sql)

**FK 제약**

| FK | 참조 | ON DELETE | ON UPDATE |
|----|------|-----------|-----------|
| `fk_common_code_group_id` | `code_group(id)` | RESTRICT | CASCADE |

**예시 데이터**

| id | group_id | code | name | sort_order |
|----|----------|------|------|------------|
| 1 | 1 | ups | UPS | 1 |
| 2 | 1 | pdu | PDU | 2 |
| 3 | 1 | sensor | Sensor | 3 |
| 4 | 2 | rack | Rack | 1 |
| 5 | 2 | row | Row | 2 |
| 6 | 3 | rack | Rack | 1 |
| 7 | 4 | snmp | SNMP | 1 |

---

## 컬럼 ↔ 엔티티 매핑

### identity — `User`

| DB 컬럼 | Java 필드 | 출처 |
|---------|-----------|------|
| `id` | `id` | `User` |
| `username` | `username` | `User` |
| `password` | `password` | `User` |
| `role` | `role` | `User` |
| `refresh_token` | `refreshToken` | `User` |
| `created_dt` | `createdDt` | `BaseEntity` |
| `updated_dt` | `updatedDt` | `BaseEntity` |

### common — `CodeGroup`

| DB 컬럼 | Java 필드 | 출처 |
|---------|-----------|------|
| `id` | `id` | `CodeGroup` |
| `group_key` | `groupKey` | `CodeGroup` |
| `group_name` | `groupName` | `CodeGroup` |
| `created_dt` | `createdDt` | `BaseEntity` |
| `updated_dt` | `updatedDt` | `BaseEntity` |

### common — `CommonCode`

| DB 컬럼 | Java 필드 | 출처 |
|---------|-----------|------|
| `id` | `id` | `CommonCode` |
| `group_id` | `codeGroup` | `CommonCode` (`@ManyToOne`) |
| `code` | `code` | `CommonCode` |
| `name` | `name` | `CommonCode` |
| `sort_order` | `sortOrder` | `CommonCode` |
| `created_dt` | `createdDt` | `BaseEntity` |
| `updated_dt` | `updatedDt` | `BaseEntity` |

Spring Boot 기본 naming strategy 기준으로 camelCase → snake_case 변환됩니다.

---

## DDL 적용 순서

```
V001 → users
V002 → code_group
V003 → common_code   (V002 선행)
```

---

## 갱신 이력

| 날짜 | 변경 |
|------|------|
| 2026-06-26 | `users` 테이블 최초 등록 |
| 2026-06-26 | `sql/history/V001__create_users_table.sql` 추가 |
| 2026-06-26 | `code_group`, `common_code` 테이블 및 ERD 관계 추가 |
| 2026-06-26 | `sql/history/V002`, `V003` 추가 |
