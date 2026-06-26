# DDD 아키텍처 가이드

`new-manager-server` 프로젝트에서 Domain-Driven Design(DDD)을 **실제로 지키며** 개발하기 위한 규칙과 패턴을 정리한 문서입니다.

> 관련 문서: [FEATURES.md](./FEATURES.md) — 기존 기능·API 목록

---

## 1. 왜 DDD를 쓰는가

DCIM manager-server는 다음 특성을 가집니다.

- 디바이스·자산·전력·냉각·알람 등 **여러 업무 영역**이 공존
- MariaDB(메타데이터) + InfluxDB(시계열) **이종 저장소** 사용
- 대시보드·분석 API는 **조회·집계 비중**이 큼
- 단일 배포(모놀리스)이지만 **기능 단위 확장** 가능성이 있음

이런 프로젝트에서 폴더만 `controller / service / repository`로 나누면, 시간이 지날수록 Service가 비대해지고 모듈 간 의존이 꼬입니다.  
DDD는 **업무 경계(바운디드 컨텍스트)** 와 **책임 분리**로 이 문제를 막기 위한 설계 방법입니다.

---

## 2. 우리가 쓰는 DDD 스타일

이 프로젝트는 **Pure DDD**가 아니라, Spring Boot 팀에 맞는 **Pragmatic DDD**를 따릅니다.

| 항목 | 우리의 선택 |
|------|-------------|
| 배포 단위 | 모놀리스 (추후 모듈 분리 가능) |
| 계층 | Light Hexagonal (api / application / domain / infrastructure) |
| CQRS | 라이트 적용 — Command와 Query 서비스 분리 |
| 이벤트 | 필요할 때만 (과도한 도메인 이벤트 지양) |
| JPA | 실용적으로 허용 — 단, `domain`이 JPA에 의존하지 않도록 주의 |

**목표:** 이론 완벽함보다 **읽기 쉽고, 경계가 명확하고, 테스트 가능한** 코드.

---

## 3. 패키지 구조

### 3.1 최상위 구조

```
net.vivans.dcim
├── bootstrap/                 # ManagerServerApplication, 전역 Bean 조립
├── shared/                    # 기술 공통 (비즈니스 로직 금지)
│   ├── api/                   # ApiResponse, 공통 DTO
│   ├── exception/
│   ├── security/              # JWT, SecurityConfig, Filter
│   └── persistence/           # BaseEntity, Auditing
└── module/                    # 바운디드 컨텍스트 (= 업무 모듈)
    ├── identity/
    ├── asset/
    ├── device/
    ├── telemetry/
    ├── dashboard/
    ├── analytics/
    ├── alert/
    ├── notification/
    ├── report/
    └── setting/
```

### 3.2 모듈 내부 구조 (모든 모듈 동일)

```
module/{name}/
├── api/                       # Presentation — HTTP 진입점
│   ├── XxxController.java
│   └── dto/
│       ├── XxxCreateRequest.java
│       └── XxxResponse.java
│
├── application/               # Use Case — 흐름 조율
│   ├── XxxCommandService.java   # 생성·수정·삭제
│   └── XxxQueryService.java     # 조회
│
├── domain/                    # Core — 비즈니스 규칙
│   ├── model/                   # Entity, Value Object
│   ├── repository/              # Repository 인터페이스 (Port)
│   └── service/                 # Domain Service (규칙이 한 엔티티에 안 들어갈 때)
│
└── infrastructure/            # Adapter — 외부 기술 구현
    ├── persistence/             # JPA Repository 구현
    └── client/                  # REST, MQTT, Influx, Slack 등
```

### 3.3 의존 방향 (필수)

```
api  →  application  →  domain  ←  infrastructure
```

| 계층 | 할 수 있는 것 | 하면 안 되는 것 |
|------|---------------|-----------------|
| `api` | HTTP 요청/응답, DTO 변환 | DB 직접 접근, 도메인 규칙 구현 |
| `application` | 유스케이스 조율, 트랜잭션 경계 | HTTP, Influx 쿼리 문자열 작성 |
| `domain` | 비즈니스 규칙, 불변식 검증 | Spring, JPA, Influx 의존 |
| `infrastructure` | DB·외부 API 구현 | Controller 반환, API DTO 사용 |

---

## 4. 바운디드 컨텍스트 정의

모듈 하나 = 바운디드 컨텍스트 하나. **같은 단어가 다른 의미를 가질 수 있음**을 인정합니다.

| 모듈 | 책임 | 기존 코드 참고 |
|------|------|----------------|
| `identity` | 로그인, 사용자, JWT | `auth` |
| `asset` | 존·랙·자산·3D 모델 | `asset`, `space`, `model` |
| `device` | 디바이스 계층, PDU 마스터, 수집 스크립트 | `device`, `pduTest` |
| `telemetry` | Influx 조회, MQTT/SNMP 수집 | `pduTelemetry`, Influx Repository |
| `dashboard` | 대시보드 위젯, 실시간 조회 (Read Model) | `dashboard`, `cooling` 일부 |
| `analytics` | 차트, 트렌드, PDU 분석 (Read Model) | `analysis`, `pduAnalysis` |
| `alert` | 알람 이력 | `alert` |
| `notification` | Slack 연동 | `slack` |
| `report` | 리포트 스케줄·생성 | `report` |
| `setting` | 임계값, 랙 색상 등 설정 | `setting` |

### Read Model 모듈 (`dashboard`, `analytics`)

조회·집계 전용 모듈은 **`domain/model`을 갖지 않아도 됩니다.**

```
module/dashboard/
├── api/
├── application/     # QueryService만
└── infrastructure/
    └── influx/      # Influx 전용 조회 어댑터
```

Influx에서 읽은 결과를 DTO로 매핑해 반환하는 것이 이 모듈의 역할입니다.

---

## 5. 핵심 규칙

### 5.1 MUST (반드시 지킬 것)

1. **엔티티는 해당 모듈 `domain/model`에만 둔다.**  
   `shared/entity`, `domain/entity` 같은 공유 엔티티 폴더를 만들지 않는다.

2. **모듈 간 Repository 직접 호출 금지.**  
   `device` 모듈이 `asset`의 JPA Repository를 `@Autowired` 하지 않는다.

3. **API DTO는 `api/dto`에만 둔다.**  
   `domain` 안에 `XxxRequest`, `XxxResponse`를 두지 않는다.

4. **외부 연동은 `infrastructure`에만 둔다.**  
   Influx, MQTT, Collector API, Slack, SNMP 클라이언트는 모두 infrastructure.

5. **변경과 조회를 분리한다.**  
   - `XxxCommandService` — 생성·수정·삭제  
   - `XxxQueryService` — 목록·상세·검색

6. **Security URL 규칙은 구현된 API만 등록한다.**  
   아직 없는 모듈의 permitAll / hasRole 규칙을 미리 넣지 않는다.

7. **`shared`에는 비즈니스 로직을 넣지 않는다.**  
   공통 응답, 예외, 보안, Auditing 같은 기술 코드만 허용.

### 5.2 SHOULD (권장)

1. **유스케이스 하나 = application 메서드 하나**  
   Service 클래스가 500줄 넘어가면 유스케이스 단위로 분리 검토.

2. **도메인 규칙은 Entity/VO 메서드로 표현**  
   예: `device.rename(String name)` — Service에 if문으로 흩어두지 않기.

3. **모듈 간 통신은 application 계층에서**  
   다른 모듈의 `QueryService`를 호출하거나, 필요 시 이벤트 발행.

4. **Influx 쿼리는 infrastructure에 캡슐화**  
   Flux 문자열이 application/service에 흩어지지 않게 한다.

5. **테스트는 domain → application 순으로 우선**  
   Controller 테스트보다 도메인 규칙·유스케이스 테스트를 먼저.

### 5.3 MUST NOT (하지 말 것)

| 안티패턴 | 이유 |
|----------|------|
| `domain/entity` 공유 폴더 | 바운디드 컨텍스트 경계 파괴 |
| God Service (모든 로직이 한 Service) | 변경 영향 범위 폭발 |
| Controller → Repository 직접 호출 | 계층 우회 |
| dashboard 모듈에 Device Entity 보유 | Read/Write 모델 혼재 |
| util에 비즈니스 로직 | 찾을 수 없는 코드 |
| 미구현 API Security 규칙 선등록 | 혼란, 보안 착각 |

---

## 6. 계층별 역할 상세

### 6.1 api (Presentation)

```java
@RestController
@RequestMapping("/api/manager/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceCommandService commandService;
    private final DeviceQueryService queryService;

    @PostMapping
    public ApiResponse<DeviceResponse> create(@RequestBody DeviceCreateRequest request) {
        return ApiResponse.ok(commandService.create(request));
    }
}
```

- HTTP 상태 코드, Swagger 어노테이션, Request/Response DTO
- **비즈니스 판단 없음** — application에 위임

### 6.2 application (Use Case)

```java
@Service
@RequiredArgsConstructor
@Transactional
public class DeviceCommandService {

    private final DeviceRepository deviceRepository;

    public DeviceResponse create(DeviceCreateRequest request) {
        Device device = Device.create(request.name(), request.parentId());
        deviceRepository.save(device);
        return DeviceResponse.from(device);
    }
}
```

- 트랜잭션 경계 (`@Transactional`)
- 여러 Repository·외부 서비스 조율
- **도메인 객체를 생성하고 규칙을 호출** — 규칙 자체는 domain에

### 6.3 domain (Core)

```java
@Getter
public class Device {

    private DeviceId id;
    private String name;
    private DeviceId parentId;

    public static Device create(String name, DeviceId parentId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("device name is required");
        }
        return new Device(name, parentId);
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("device name is required");
        }
        this.name = newName;
    }
}
```

```java
public interface DeviceRepository {
    void save(Device device);
    Optional<Device> findById(DeviceId id);
}
```

- Spring 어노테이션 최소화 (`@Entity`는 실용적으로 허용 가능)
- **프레임워크에 의존하지 않는** 순수 비즈니스 규칙 지향

### 6.4 infrastructure (Adapter)

```java
@Repository
@RequiredArgsConstructor
public class DeviceJpaRepository implements DeviceRepository {

    private final DeviceSpringDataRepository springDataRepository;

    @Override
    public void save(Device device) {
        springDataRepository.save(DeviceJpaEntity.from(device));
    }
}
```

- JPA, Influx, MQTT, HTTP Client 구현
- domain의 Port(인터페이스)를 구현

---

## 7. 모듈 간 통신

### 7.1 허용

```
module/dashboard/application
    → module/device/application/DeviceQueryService
    → module/telemetry/infrastructure (Influx 조회)
```

- **application → application** (다른 모듈의 Query/Command Service)
- DTO 또는 ID(값 객체)만 전달

### 7.2 금지

```
module/dashboard/application
    → module/device/infrastructure/DeviceJpaRepository   ❌
    → module/device/domain/model/Device                  ❌ (직접 조작)
```

### 7.3 순환 의존 방지

모듈 A가 B를 참조하면, B는 A를 참조하지 않는다.  
양방향이 필요하면:

- 공통 ID만 주고 각자 조회
- 또는 `shared`에 **인터페이스(Port)** 만 두고 양쪽 infrastructure에서 구현 (최후 수단)

---

## 8. CQRS 라이트 적용

| 구분 | Command | Query |
|------|---------|-------|
| 목적 | 상태 변경 | 조회·집계 |
| 트랜잭션 | `@Transactional` | `@Transactional(readOnly = true)` |
| 저장소 | MariaDB (주) | MariaDB, Influx |
| 클래스명 | `XxxCommandService` | `XxxQueryService` |
| 모듈 예 | `device`, `asset`, `identity` | `dashboard`, `analytics` |

**한 클래스에 create()와 getChart()를 같이 넣지 않는다** — 작은 프로젝트에서도 습관이 중요합니다.

---

## 9. 네이밍 규칙

| 대상 | 규칙 | 예시 |
|------|------|------|
| 모듈 | 업무 명사, 단수 | `device`, `identity` |
| Controller | `{리소스}Controller` | `DeviceController` |
| Command Service | `{리소스}CommandService` | `DeviceCommandService` |
| Query Service | `{리소스}QueryService` | `DeviceQueryService` |
| Repository (Port) | `{리소스}Repository` | `DeviceRepository` |
| JPA 구현 | `{리소스}JpaRepository` | `DeviceJpaRepository` |
| Request DTO | `{동작}{리소스}Request` | `DeviceCreateRequest` |
| Response DTO | `{리소스}Response` | `DeviceResponse` |
| Domain Service | `{업무}Policy` 또는 `{업무}Service` | `DeviceHierarchyPolicy` |

---

## 10. Security와 DDD

Security 설정은 `shared/security`에 둡니다.

```java
// 구현된 API만 등록
.requestMatchers("/api/manager/auth/**").permitAll()
.requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
.anyRequest().authenticated()
```

모듈이 추가될 때마다:

1. Controller 구현
2. 필요 시 Security 규칙 추가
3. `docs/FEATURES.md` API 목록 갱신

---

## 11. 마이그레이션 가이드 (현재 → 목표)

현재 `auth`는 과도기 구조(`domain/auth`, `domain/entity/User`)에 있습니다.

### 목표 구조

```
module/identity/
├── api/AuthController.java
├── api/dto/LoginRequest.java, TokenResponse.java
├── application/AuthCommandService.java
├── application/AuthQueryService.java
├── domain/model/User.java
├── domain/repository/UserRepository.java
└── infrastructure/persistence/UserJpaRepository.java
```

### 이동 순서

1. `shared/` 로 security, exception, ApiResponse, BaseEntity 정리
2. `identity` 모듈 생성 후 auth 코드 이동
3. `domain/entity/User` → `module/identity/domain/model/User`
4. `device` 모듈 — 마스터 데이터 핵심
5. `telemetry` — Influx/MQTT 공통 어댑터
6. `dashboard`, `analytics` — Read Model
7. 나머지 모듈

---

## 12. 코드 리뷰 체크리스트

PR 리뷰 시 아래를 확인합니다.

- [ ] 새 클래스가 올바른 모듈·계층에 있는가?
- [ ] `domain`이 Spring/Influx/JPA에 의존하지 않는가?
- [ ] API DTO가 `api/dto`에 있는가?
- [ ] 다른 모듈 Repository를 직접 주입하지 않았는가?
- [ ] Command와 Query가 섞이지 않았는가?
- [ ] `shared`에 비즈니스 로직이 들어가지 않았는가?
- [ ] Security에 미구현 URL 규칙이 추가되지 않았는가?
- [ ] Influx/Flux 쿼리가 infrastructure에 있는가?

---

## 13. 자주 하는 질문

### Q. Entity에 `@Entity`를 붙여도 되나요?

**가능합니다.** 팀 생산성을 위해 JPA 어노테이션을 domain model에 두는 것을 허용합니다.  
다만 `JpaRepository`를 domain에 두거나, Controller가 Entity를 반환하는 것은 금지입니다.

### Q. 모든 모듈에 domain/model이 필요한가요?

**아닙니다.** `dashboard`, `analytics` 같은 조회 모듈은 application + infrastructure만으로 충분합니다.

### Q. DTO ↔ Entity 변환은 어디서?

- **api → application:** Request DTO를 application 메서드 인자로 전달
- **application → api:** Response DTO는 application 또는 api에서 `from(entity)` 팩토리로 변환
- **infrastructure:** JPA Entity ↔ Domain Model 변환은 infrastructure 책임

### Q. 기존 manager-server 코드를 복사해도 되나요?

로직 참고는 가능하지만, **폴더 구조 그대로 복사는 금지**합니다.  
반드시 이 문서의 모듈·계층 구조에 맞게 재배치합니다.

---

## 14. 참고

- Eric Evans — *Domain-Driven Design* (바운디드 컨텍스트, 애그리거트 개념)
- Vaughn Vernon — *Implementing Domain-Driven Design* (실전 적용)
- Herberto Graca — [DDD, Hexagonal, Onion, CQRS](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/) (계층 관계 시각화)

---

## 15. 요약

```
모듈 = 바운디드 컨텍스트
계층 = api / application / domain / infrastructure
의존 = 안쪽(domain)으로만
조회 = dashboard, analytics (Read Model)
변경 = identity, device, asset, ...
공유 엔티티 폴더 = 사용 금지
```

이 규칙을 지키면, 기능이 늘어나도 **어디에 코드를 넣을지**가 명확해지고, 기존 `manager-server`에서 겪었던 Service 비대화·의존성 꼬임을 피할 수 있습니다.
