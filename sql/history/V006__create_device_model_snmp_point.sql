-- =============================================================================
-- V006: device_model_snmp_point 테이블 생성 (devicemodel 모듈)
-- =============================================================================
-- 작성일  : 2026-07-10
-- 대상 DB : MariaDB (dcim_new)
-- 엔티티  : net.vivans.dcim.module.devicemodel.domain.model.DeviceModelSnmpPoint
--           net.vivans.dcim.shared.persistence.BaseEntity
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V006__create_device_model_snmp_point.sql
--
-- 선행 조건: V005 (device_model, device_model_protocol)
--
-- 설계 문서: docs/devicemodel/DEVICE_MODEL_SNMP_POINT_API.md
--
-- 비즈니스 규칙 (애플리케이션에서 검증):
--   - model_protocol_id — device_model_protocol.id (SNMP 프로토콜 연결만)
--   - (model_protocol_id, name) UK — 동일 protocol 내 point 이름 중복 불가
--   - requires_instance — OID {instanceId} 치환 필요 여부 (0=false, 1=true)
--   - enabled — 수집·스크립트 생성 대상 여부 (0=false, 1=true)
-- =============================================================================

CREATE TABLE IF NOT EXISTS device_model_snmp_point (
    id                  INT           NOT NULL AUTO_INCREMENT COMMENT 'SNMP point ID',
    model_protocol_id   INT           NOT NULL                COMMENT 'device_model_protocol.id (FK)',
    name                VARCHAR(255)  NOT NULL                COMMENT '식별자·표시명 (V, 전압, PRI-FLOW 등)',
    oid                 VARCHAR(512)  NOT NULL                COMMENT 'SNMP OID 또는 {instanceId} 템플릿',
    requires_instance   TINYINT(1)    NOT NULL DEFAULT 0      COMMENT 'instanceId 치환 필요 여부 (0=false, 1=true)',
    unit                VARCHAR(50)   NULL                    COMMENT '단위 (V, A, L/min 등)',
    enabled             TINYINT(1)    NOT NULL DEFAULT 1      COMMENT '사용 여부 (0=false, 1=true)',
    created_dt          TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt          TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_model_snmp_point_protocol_name (model_protocol_id, name),
    KEY idx_device_model_snmp_point_model_protocol_id (model_protocol_id),
    CONSTRAINT fk_device_model_snmp_point_model_protocol_id
        FOREIGN KEY (model_protocol_id) REFERENCES device_model_protocol (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='장비 모델별 SNMP 수집 point (OID 카탈로그)';