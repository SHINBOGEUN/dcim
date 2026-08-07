-- =============================================================================
-- V011: device_snmp_instance 테이블 생성 (device 모듈 — SNMP instance 인덱스)
-- =============================================================================
-- 작성일  : 2026-07-28
-- 대상 DB : MariaDB (dcim_new)
-- 엔티티  : net.vivans.dcim.module.device.domain.model.DeviceSnmpInstance
--           net.vivans.dcim.shared.persistence.BaseEntity
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V011__create_device_snmp_instance.sql
--
-- 선행 조건: V009 (device_protocol_endpoint)
--
-- 설계 문서:
--   docs/device/DEVICE_ARCHITECTURE.md §2.2
--
-- 역할:
--   - device_model_snmp_point OID 템플릿의 {instanceId} 치환값 (장비·SNMP endpoint당 1개)
--   - PDU 등 TEMPLATE형 모델 — requires_instance point가 있고 현장 index가 다를 때만 행 추가
--   - community/version — 애플리케이션 기본값 (본 테이블 미포함)
--   - SRC 등 장비별 OID 전체가 다른 경우 — device_snmp_point (별도 V0xx) 예정
--
-- 비즈니스 규칙 (애플리케이션에서 검증):
--   - endpoint_id — device_protocol_endpoint.id (SNMP 프로토콜 endpoint만)
--   - 행 없음 — 고정 OID만 쓰거나 instance 치환 불필요 (정상)
--   - instance_id — 양의 정수 (1 이상)
--   - endpoint 삭제 시 본 행 CASCADE
-- =============================================================================

CREATE TABLE IF NOT EXISTS device_snmp_instance (
    endpoint_id        INT           NOT NULL                COMMENT 'device_protocol_endpoint.id (PK/FK, SNMP endpoint 1:1)',
    instance_id        INT           NOT NULL                COMMENT 'SNMP MIB instance index ({instanceId} 치환값)',
    created_dt         TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt         TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (endpoint_id),
    CONSTRAINT fk_device_snmp_instance_endpoint_id
        FOREIGN KEY (endpoint_id) REFERENCES device_protocol_endpoint (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_device_snmp_instance_id
        CHECK (instance_id >= 1)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='장비 SNMP instance 인덱스 (OID {instanceId} 치환, 필요한 endpoint만)';
