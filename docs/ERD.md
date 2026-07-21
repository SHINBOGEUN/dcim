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

    location_node {
        varchar code PK "10자 Base62, 서버 자동 생성"
        varchar parent_code FK "nullable, self-ref"
        int location_type_id FK "common_code.id"
        varchar name "노드 표시명"
        timestamp created_dt "생성 시각"
        timestamp updated_dt "수정 시각"
    }

    device_model {
        int id PK "AUTO_INCREMENT"
        varchar name UK "모델/제품명"
        varchar manufacturer UK "제조사"
        varchar description "설명 (nullable)"
        timestamp created_dt "생성 시각"
        timestamp updated_dt "수정 시각"
    }

    device_model_protocol {
        int id PK "AUTO_INCREMENT"
        int model_id FK "device_model.id"
        int protocol_type_id FK "common_code.id (PROTOCOL_TYPE)"
        timestamp created_dt "생성 시각"
        timestamp updated_dt "수정 시각"
    }

    device_model_snmp_point {
        int id PK "AUTO_INCREMENT"
        int model_protocol_id FK "device_model_protocol.id"
        varchar name UK "식별자·표시명"
        varchar oid "OID 또는 템플릿"
        tinyint requires_instance "boolean, 기본 0"
        varchar unit "단위 nullable"
        tinyint enabled "boolean, 기본 1"
        timestamp created_dt "생성 시각"
        timestamp updated_dt "수정 시각"
    }

    code_group ||--o{ common_code : "group_id"
    common_code ||--o{ location_node : "location_type_id"
    location_node ||--o{ location_node : "parent_code"
    device_model ||--o{ device_model_protocol : "model_id"
    common_code ||--o{ device_model_protocol : "protocol_type_id"
    device_model_protocol ||--o{ device_model_snmp_point : "model_protocol_id"
```

| 모듈 | 테이블 | 관계 |
|------|--------|------|
| identity | `users` | 독립 |
| common | `code_group` | 1 |
| common | `common_code` | N → `code_group` |
| location | `location_node` | N → `common_code` (LOCATION_TYPE), 자기참조 `parent_code` |
| devicemodel | `device_model` | 장비 SKU 카탈로그 (UK: name+manufacturer) |
| devicemodel | `device_model_protocol` | 모델 ↔ PROTOCOL_TYPE N:M (UK: model_id+protocol_type_id) |
| devicemodel | `device_model_snmp_point` | ✅ SNMP point (UK: model_protocol_id+name) |
| device | `devices` | ⏳ 스켈레톤만 (DDL·API 미구현) |

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

### `location_node` — 위치 트리 노드 (location 모듈)

| 컬럼 | 타입 | NULL | 키 | 설명 |
|------|------|------|-----|------|
| `code` | CHAR(10) | N | PK | 노드 식별자 (**10자 Base62**, 서버 자동 생성, 불변) |
| `parent_code` | CHAR(10) | Y | FK | 부모 노드 code. **루트는 NULL** |
| `location_type_id` | INT | N | FK | 위치 유형 (`common_code.id`, **LOCATION_TYPE만 허용**) |
| `name` | VARCHAR(255) | N | UK* | 노드 표시명 (사용자 입력) |
| `created_dt` | TIMESTAMP(6) | Y | | 최초 생성 시각 |
| `updated_dt` | TIMESTAMP(6) | Y | | 최종 수정 시각 |

\* UK: `(parent_code, name)` — 자식 노드 이름 중복 방지. 루트는 애플리케이션에서 검증

**엔티티:** `module/location/domain/model/LocationNode.java`  
**API 설계:** [LOCATION_NODE_API.md](../location/LOCATION_NODE_API.md)  
**상속:** `BaseEntity`  
**연관:**
- `@ManyToOne` → `LocationNode` (`@JoinColumn(name = "parent_code")`) — 자기 참조
- `@ManyToOne` → `CommonCode` (`@JoinColumn(name = "location_type_id")`)

**DDL:** [V004__create_location_node_table.sql](../sql/history/V004__create_location_node_table.sql)

**FK 제약**

| FK | 참조 | ON DELETE | ON UPDATE |
|----|------|-----------|-----------|
| `fk_location_node_parent_code` | `location_node(code)` | RESTRICT | CASCADE |
| `fk_location_node_location_type_id` | `common_code(id)` | RESTRICT | CASCADE |

**향후 연동 (devices)**

| 컬럼 | 설명 |
|------|------|
| `devices.location_node_code` | 장비가 속한 위치 노드 FK → `location_node(code)` (`CHAR(10)`, CONTAINER/ROW/RACK 등 모든 유형 가능) |

**트리 규칙 (애플리케이션)**

| 구분 | 조건 |
|------|------|
| 루트 노드 | `parent_code IS NULL` |
| 리프 노드 | `parent_code = 이 노드 code` 인 행이 없음 |
| 위치 유형 | `location_type_id` → `common_code` 중 `group_key = 'LOCATION_TYPE'`만 허용 (DB FK는 `common_code`만 검증) |
| `code` | 생성 시 10자 Base62 자동 부여, 변경 불가 |
| 순환 참조 | 금지 (애플리케이션 검증) |
| 자식 있는 노드 삭제 | 리프만 단건 삭제 / 서브트리 cascade 삭제 API로 분리 ([API 설계](../location/LOCATION_NODE_API.md#5-삭제-api)) |
| 유형 삽입 시 재구성 | 중간 유형 등록 시 기존 직접 자식 재부모화 ([API 설계](../location/LOCATION_NODE_API.md#자식-등록-시-트리-재구성-핵심-규칙)) |

**예시 데이터**

`LOCATION_TYPE` common_code: `CONTAINER`, `ZONE`, `ROW`, `RACK` …

| code | parent_code | location_type_id | name |
|------|-------------|------------------|------|
| `K7mN2pQx9L` | NULL | 1 | 컨테이너 A |
| `A1b2C3d4E5` | `K7mN2pQx9L` | 2 | Zone 1 |
| `Z9y8X7w6V5` | `A1b2C3d4E5` | 3 | A열 |
| `M4n3B2v1C0` | `Z9y8X7w6V5` | 4 | Rack-01 |

> `location_type_id`는 `common_code.id` (LOCATION_TYPE 그룹)를 가리킵니다.

---

### `device_model` — 장비 제품 모델 (devicemodel 모듈)

**구현 상태:** ✅ 구현 완료

| 컬럼 | 타입 | NULL | 키 | 설명 |
|------|------|------|-----|------|
| `id` | INT | N | PK | 모델 ID (AUTO_INCREMENT) |
| `name` | VARCHAR(255) | N | UK | 모델/제품명 |
| `manufacturer` | VARCHAR(255) | N | UK | 제조사 |
| `description` | VARCHAR(1000) | Y | | 설명 |
| `created_dt` | TIMESTAMP(6) | Y | | 최초 생성 시각 |
| `updated_dt` | TIMESTAMP(6) | Y | | 최종 수정 시각 |

\* UK: `(name, manufacturer)`

**엔티티:** `module/devicemodel/domain/model/DeviceModel.java`  
**API 설계:** [DEVICE_MODEL_API.md](devicemodel/DEVICE_MODEL_API.md)  
**상속:** `BaseEntity`  
**연관:** `@OneToMany` → `DeviceModelProtocol` (`mappedBy = "deviceModel"`, cascade ALL)

**DDL:** [V005__create_device_model_tables.sql](../sql/history/V005__create_device_model_tables.sql)

**범위**

| 참조 주체 | model FK |
|-----------|----------|
| `devices` (향후) | ✅ |
| `assets` (향후, 장비류) | ✅ 검토 |
| `location_node` | ❌ |

---

### `device_model_protocol` — 모델별 프로토콜 (devicemodel 모듈)

**구현 상태:** ✅ 구현 완료

| 컬럼 | 타입 | NULL | 키 | 설명 |
|------|------|------|-----|------|
| `id` | INT | N | PK | 연결 ID |
| `model_id` | INT | N | FK | `device_model.id` |
| `protocol_type_id` | INT | N | FK | `common_code.id` (**PROTOCOL_TYPE**만) |
| `created_dt` | TIMESTAMP(6) | Y | | |
| `updated_dt` | TIMESTAMP(6) | Y | | |

| 제약 | 규칙 |
|------|------|
| UK | `(model_id, protocol_type_id)` |

**엔티티:** `module/devicemodel/domain/model/DeviceModelProtocol.java`  
**연관:**
- `@ManyToOne` → `DeviceModel` (`model_id`)
- `@ManyToOne` → `CommonCode` (`protocol_type_id`)

**FK 제약**

| FK | 참조 | ON DELETE | ON UPDATE |
|----|------|-----------|-----------|
| `fk_device_model_protocol_model_id` | `device_model(id)` | RESTRICT | CASCADE |
| `fk_device_model_protocol_protocol_type_id` | `common_code(id)` | RESTRICT | CASCADE |

**관계 (N:M)**

```
device_model ←—— device_model_protocol ——→ common_code (PROTOCOL_TYPE)
```

**PROTOCOL_TYPE 시드 (V005)**

V005에서 `code_group` + `common_code` 모두 INSERT (없을 때만).

| group_key | code | name | sort_order |
|-----------|------|------|------------|
| PROTOCOL_TYPE | snmp | SNMP | 1 |
| PROTOCOL_TYPE | modbus | Modbus | 2 |
| PROTOCOL_TYPE | mqtt | MQTT | 3 |

> ERD §code_group 예시(id=4 등)는 문서용. 실제 id는 DB마다 다름.

---

### `device_model_snmp_point` — 모델별 SNMP 수집 point (devicemodel 모듈)

**구현 상태:** ✅ 구현 완료

| 컬럼 | 타입 | NULL | 키 | 기본값 | 설명 |
|------|------|------|-----|--------|------|
| `id` | INT | N | PK | AUTO_INCREMENT | point ID |
| `model_protocol_id` | INT | N | FK | | `device_model_protocol.id` (SNMP만) |
| `name` | VARCHAR(255) | N | UK* | | 식별자·표시명 (`V`, `전압`, `PRI-FLOW`) |
| `oid` | VARCHAR(512) | N | | | OID 또는 `{instanceId}` 템플릿 |
| `requires_instance` | TINYINT(1) | N | | `0` | OID `{instanceId}` 치환 필요 여부 (boolean) |
| `unit` | VARCHAR(50) | Y | | | 단위 (`V`, `A`, `L/min`) |
| `enabled` | TINYINT(1) | N | | `1` | 사용 여부 (boolean) |
| `created_dt` | TIMESTAMP(6) | Y | | | |
| `updated_dt` | TIMESTAMP(6) | Y | | | |

\* UK: `(model_protocol_id, name)` — 같은 SNMP protocol 연결 안에서만 name 유일. **모델 간 `V` 중복은 허용**

**엔티티:** `module/devicemodel/domain/model/DeviceModelSnmpPoint.java`  
**API 설계:** [DEVICE_MODEL_SNMP_POINT_API.md](devicemodel/DEVICE_MODEL_SNMP_POINT_API.md)  
**상속:** `BaseEntity`  
**연관:** `@ManyToOne` → `DeviceModelProtocol` (`model_protocol_id`, LAZY)

**DDL:** [V006__create_device_model_snmp_point.sql](../sql/history/V006__create_device_model_snmp_point.sql)

**FK 제약**

| FK | 참조 | ON DELETE | ON UPDATE |
|----|------|-----------|-----------|
| `fk_device_model_snmp_point_model_protocol_id` | `device_model_protocol(id)` | CASCADE | CASCADE |

**관계도 (devicemodel — SNMP point)**

```mermaid
erDiagram
    device_model {
        int id PK
        varchar name UK
        varchar manufacturer UK
        varchar description
    }

    device_model_protocol {
        int id PK
        int model_id FK
        int protocol_type_id FK
    }

    common_code {
        int id PK
        int group_id FK
        varchar code "snmp, modbus, mqtt"
        varchar name
    }

    device_model_snmp_point {
        int id PK
        int model_protocol_id FK
        varchar name UK
        varchar oid
        tinyint requires_instance
        varchar unit
        tinyint enabled
    }

    device_model ||--o{ device_model_protocol : "model_id"
    common_code ||--o{ device_model_protocol : "protocol_type_id"
    device_model_protocol ||--o{ device_model_snmp_point : "model_protocol_id"
```

```
device_model (1) ──< device_model_protocol (N) >── common_code (PROTOCOL_TYPE)
                            │
                            └──< device_model_snmp_point (N)   ※ protocolCode = snmp 만
```

| 제약 | 규칙 |
|------|------|
| UK | `(model_protocol_id, name)` |
| 프로토콜 | `protocolCode = snmp` 인 `device_model_protocol`만 point 등록 가능 |
| 삭제 | protocol 삭제 시 point CASCADE |

---

## boolean 컬럼 규칙 (프로젝트 공통)

플래그성 컬럼은 **`boolean` (`true` / `false`)** 을 사용합니다.  
**`device_model_snmp_point`가 최초** 적용 대상이며, 이후 Modbus/MQTT point·device 설정 등에도 동일 규칙을 적용합니다.

| 계층 | 규칙 | 예시 |
|------|------|------|
| DB | `TINYINT(1) NOT NULL DEFAULT 0` (또는 `1`) | `requires_instance`, `enabled` |
| Java | `boolean` + `@Column(nullable = false)` | `requiresInstance`, `enabled` |
| API JSON | `true` / `false` | `requiresInstance`, `enabled` |

| 컬럼 (DB) | API 필드 | 기본값 | 의미 |
|-----------|----------|--------|------|
| `requires_instance` | `requiresInstance` | `false` | OID 그대로 사용 (치환 불필요). **instanceId 값을 저장하지 않음** |
| `requires_instance` | `requiresInstance` | `true` | OID의 `{instanceId}`를 장비 `instanceId`로 치환 |
| `enabled` | `enabled` | `true` | 수집·스크립트 생성 대상 |

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

### location — `LocationNode`

| DB 컬럼 | Java 필드 | 출처 |
|---------|-----------|------|
| `code` | `code` | `LocationNode` (`@Id`) |
| `parent_code` | `parent` | `LocationNode` (`@ManyToOne`, self) |
| `location_type_id` | `locationType` | `LocationNode` (`@ManyToOne` → `CommonCode`) |
| `name` | `name` | `LocationNode` |
| `created_dt` | `createdDt` | `BaseEntity` |
| `updated_dt` | `updatedDt` | `BaseEntity` |

### devicemodel — `DeviceModel`

| DB 컬럼 | Java 필드 | 출처 |
|---------|-----------|------|
| `id` | `id` | `DeviceModel` |
| `name` | `name` | `DeviceModel` |
| `manufacturer` | `manufacturer` | `DeviceModel` |
| `description` | `description` | `DeviceModel` |
| `created_dt` | `createdDt` | `BaseEntity` |
| `updated_dt` | `updatedDt` | `BaseEntity` |

### devicemodel — `DeviceModelProtocol`

| DB 컬럼 | Java 필드 | 출처 |
|---------|-----------|------|
| `id` | `id` | `DeviceModelProtocol` |
| `model_id` | `deviceModel` | `DeviceModelProtocol` (`@ManyToOne`) |
| `protocol_type_id` | `protocolType` | `DeviceModelProtocol` (`@ManyToOne` → `CommonCode`) |
| `created_dt` | `createdDt` | `BaseEntity` |
| `updated_dt` | `updatedDt` | `BaseEntity` |

### devicemodel — `DeviceModelSnmpPoint`

| DB 컬럼 | Java 필드 | 출처 |
|---------|-----------|------|
| `id` | `id` | `DeviceModelSnmpPoint` |
| `model_protocol_id` | `modelProtocol` | `DeviceModelSnmpPoint` (`@ManyToOne`) |
| `name` | `name` | `DeviceModelSnmpPoint` |
| `oid` | `oid` | `DeviceModelSnmpPoint` |
| `requires_instance` | `requiresInstance` | `DeviceModelSnmpPoint` (`boolean`) |
| `unit` | `unit` | `DeviceModelSnmpPoint` |
| `enabled` | `enabled` | `DeviceModelSnmpPoint` (`boolean`) |
| `created_dt` | `createdDt` | `BaseEntity` |
| `updated_dt` | `updatedDt` | `BaseEntity` |

Spring Boot 기본 naming strategy 기준으로 camelCase → snake_case 변환됩니다.
