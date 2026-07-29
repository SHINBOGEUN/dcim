-- =============================================================================
-- V010: device_model_modbus_point 테이블 생성 (devicemodel 모듈)
-- =============================================================================
-- 작성일  : 2026-07-24
-- 대상 DB : MariaDB (dcim_new)
-- 엔티티  : net.vivans.dcim.module.devicemodel.domain.model.DeviceModelModbusPoint
--           net.vivans.dcim.shared.persistence.BaseEntity
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V010__create_device_model_modbus_point.sql
--
-- 선행 조건: V005 (device_model, device_model_protocol)
--
-- 설계 문서: docs/devicemodel/DEVICE_MODEL_MODBUS_POINT_API.md
--
-- 개념: SNMP의 oid 카탈로그(V006)에 대응하는 Modbus 레지스터 카탈로그.
--       "무엇을 어떻게 읽나"(register/data/byte order)는 모델 공통으로 여기 저장하고,
--       "어디서 읽나"(unit_id, 인스턴스 주소)는 device_endpoint_modbus(향후)에 둔다.
--       register_type / data_type / byte_order 는 Modbus 표준 고정값 → 앱 enum
--       (@Enumerated STRING)으로 관리하며 DB에는 문자열로 저장한다.
--
-- 비즈니스 규칙 (애플리케이션에서 검증):
--   - model_protocol_id — device_model_protocol.id (Modbus 프로토콜 연결만)
--   - (model_protocol_id, name) UK — 동일 protocol 내 point 이름 중복 불가
--   - register_type — COIL / DISCRETE / HOLDING / INPUT (ModbusRegisterType enum)
--   - data_type — INT16 / UINT16 / INT32 / UINT32 / FLOAT32 (ModbusDataType enum)
--   - byte_order — 멀티 레지스터(INT32/UINT32/FLOAT32)일 때만 필수, 단일일 때 NULL
--                  (ABCD / CDAB / BADC / DCBA, ModbusByteOrder enum)
--   - address / requires_instance — 상호 배타
--       · requires_instance = 0 → address 필수 (고정 주소, 예: 쿨러 온도 레지스터)
--       · requires_instance = 1 → address NULL (분전반 회로 등, 인스턴스가 주소 제공)
--   - address — 0 ~ 65535
--   - enabled — 수집·스크립트 생성 대상 여부 (0=false, 1=true)
-- =============================================================================

CREATE TABLE IF NOT EXISTS device_model_modbus_point (
    id                  INT           NOT NULL AUTO_INCREMENT COMMENT 'Modbus point ID',
    model_protocol_id   INT           NOT NULL                COMMENT 'device_model_protocol.id (FK)',
    name                VARCHAR(255)  NOT NULL                COMMENT '식별자·표시명 (TOTAL_WT, ONTO-TEMP 등)',
    register_type       VARCHAR(30)   NOT NULL                COMMENT '레지스터 종류 (COIL/DISCRETE/HOLDING/INPUT)',
    data_type           VARCHAR(20)   NOT NULL                COMMENT '값 해석 타입 (INT16/UINT16/INT32/UINT32/FLOAT32)',
    byte_order          VARCHAR(10)   NULL                    COMMENT '멀티 레지스터 바이트 순서 (ABCD/CDAB/BADC/DCBA), 단일이면 NULL',
    address             INT           NULL                    COMMENT '레지스터 주소 (0~65535). requires_instance=1이면 NULL',
    requires_instance   TINYINT(1)    NOT NULL DEFAULT 0      COMMENT '주소를 인스턴스가 제공하는지 (0=false, 1=true)',
    scale               DOUBLE        NULL                    COMMENT '원시값에 곱할 배율 (NULL이면 1)',
    unit                VARCHAR(50)   NULL                    COMMENT '단위 (W, A, °C, % 등)',
    enabled             TINYINT(1)    NOT NULL DEFAULT 1      COMMENT '사용 여부 (0=false, 1=true)',
    created_dt          TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt          TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_model_modbus_point_protocol_name (model_protocol_id, name),
    KEY idx_device_model_modbus_point_model_protocol_id (model_protocol_id),
    CONSTRAINT fk_device_model_modbus_point_model_protocol_id
        FOREIGN KEY (model_protocol_id) REFERENCES device_model_protocol (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_device_model_modbus_point_requires_instance
        CHECK (requires_instance IN (0, 1)),
    CONSTRAINT chk_device_model_modbus_point_enabled
        CHECK (enabled IN (0, 1)),
    CONSTRAINT chk_device_model_modbus_point_address
        CHECK (
            (requires_instance = 1 AND address IS NULL)
            OR (requires_instance = 0 AND address IS NOT NULL AND address BETWEEN 0 AND 65535)
        )
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='장비 모델별 Modbus 수집 point (레지스터 카탈로그)';
