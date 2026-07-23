-- =============================================================================
-- V009: device_protocol_endpoint 테이블 생성 (device 모듈 — 2차 공통 전송층)
-- =============================================================================
-- 작성일  : 2026-07-23
-- 대상 DB : MariaDB (dcim_new)
-- 엔티티  : net.vivans.dcim.module.device.domain.model.DeviceProtocolEndpoint
--           net.vivans.dcim.shared.persistence.BaseEntity
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V009__create_device_protocol_endpoint.sql
--
-- 선행 조건: V003 (common_code), V005 (device_model_protocol), V007 (devices)
--
-- 설계 문서:
--   docs/device/DEVICE_ARCHITECTURE.md §2.2 — 엔드포인트 패턴
--   docs/device/DEVICE_ENDPOINT_API.md     — API (구현 시)
--
-- 범위 (이번 DDL):
--   - device_protocol_endpoint 만 (host, port 공통)
--   - device_endpoint_snmp / modbus / mqtt 확장은 이후 버전
--
-- 설계 원칙 (DEVICE_ARCHITECTURE §6):
--   - host/port는 프로토콜 공통 → 본 테이블에만 둔다 (중복 금지)
--   - SNMP community·instanceId 등은 확장 테이블 (미포함)
--   - attributes JSON 컬럼 금지
--
-- 비즈니스 규칙 (애플리케이션에서 검증):
--   - device_id — devices.id (필수)
--   - protocol_type_id — PROTOCOL_TYPE common_code만 허용
--   - 해당 device의 model이 device_model_protocol에 그 프로토콜을 가져야 함
--   - (device_id, protocol_type_id) UK — 장비당 프로토콜 1엔드포인트
--   - host — IP/hostname 필수
--   - port — 1~65535
--   - enabled — 0=false, 1=true
--   - devices 삭제 시 endpoint CASCADE
-- =============================================================================

CREATE TABLE IF NOT EXISTS device_protocol_endpoint (
    id                 INT           NOT NULL AUTO_INCREMENT COMMENT '엔드포인트 ID',
    device_id          INT           NOT NULL                COMMENT 'devices.id (FK)',
    protocol_type_id   INT           NOT NULL                COMMENT 'common_code.id (PROTOCOL_TYPE만)',
    host               VARCHAR(255)  NOT NULL                COMMENT 'IP 또는 hostname',
    port               INT           NOT NULL                COMMENT '포트 (1~65535)',
    enabled            TINYINT(1)    NOT NULL DEFAULT 1      COMMENT '사용 여부 (0=false, 1=true)',
    created_dt         TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt         TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_protocol_endpoint_device_protocol (device_id, protocol_type_id),
    KEY idx_device_protocol_endpoint_device_id (device_id),
    KEY idx_device_protocol_endpoint_protocol_type_id (protocol_type_id),
    CONSTRAINT fk_device_protocol_endpoint_device_id
        FOREIGN KEY (device_id) REFERENCES devices (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_device_protocol_endpoint_protocol_type_id
        FOREIGN KEY (protocol_type_id) REFERENCES common_code (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_device_protocol_endpoint_enabled
        CHECK (enabled IN (0, 1)),
    CONSTRAINT chk_device_protocol_endpoint_port
        CHECK (port >= 1 AND port <= 65535)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='장비 프로토콜 엔드포인트 (host/port 공통 전송층)';
