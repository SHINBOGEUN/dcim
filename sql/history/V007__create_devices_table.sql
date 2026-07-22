-- =============================================================================
-- V007: devices 테이블 생성 (device 모듈 — 1차 인스턴스층)
-- =============================================================================
-- 작성일  : 2026-07-21
-- 대상 DB : MariaDB (dcim_new)
-- 엔티티  : net.vivans.dcim.module.device.domain.model.Device
--           net.vivans.dcim.shared.persistence.BaseEntity
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V007__create_devices_table.sql
--
-- 선행 조건: V004 (location_node + UNASSIGNED 시드), V005 (device_model)
--
-- 설계 문서:
--   docs/device/DEVICE_API.md          — 1차 CRUD·API
--   docs/device/DEVICE_ARCHITECTURE.md — 4층 아키텍처·로드맵
--
-- 1차 devices 설계 원칙 (DEVICE_ARCHITECTURE §6):
--   - 인스턴스 공통 필드만 — model, location, name, enabled, description
--   - location_node_code — 필수 (미지정 시 V004 시드 'UNASSIGNED' 참조)
--   - host/port, community, instanceId → 2차 device_protocol_endpoint (V008)
--   - OID/point 정의 → devicemodel 카탈로그 (V005/V006)
--   - parent_device_id, collection_script → 6차 이후 (V010+)
--   - attributes JSON 컬럼 금지
--
-- 비즈니스 규칙 (애플리케이션에서 검증):
--   - model_id — device_model.id (필수)
--   - location_node_code — location_node.code (필수)
--     · 장비만 먼저 등록 → 'UNASSIGNED' (미배정)
--     · 이후 수정 API로 실제 Rack/Zone 등으로
--   - (location_node_code, name) UK — 같은 위치 아래 표시명 중복 불가
--   - enabled — 0=false, 1=true
--   - device_model 삭제 — devices 참조 시 RESTRICT (앱 409)
--   - location_node 삭제 — devices 참조 시 RESTRICT (앱 409)
--   - 'UNASSIGNED' 노드 삭제·이름 변경 금지
-- =============================================================================

CREATE TABLE IF NOT EXISTS devices (
    id                   INT           NOT NULL AUTO_INCREMENT COMMENT '장비 ID (API {deviceId}, Influx tag device_id)',
    model_id             INT           NOT NULL                COMMENT 'device_model.id (FK) — 제품 카탈로그',
    location_node_code   CHAR(10)      NOT NULL                COMMENT 'location_node.code (FK, 필수 — 미지정 시 UNASSIGNED)',
    name                 VARCHAR(255)  NOT NULL                COMMENT '현장 표시명',
    description          VARCHAR(1000) NULL                    COMMENT '설명',
    enabled              TINYINT(1)    NOT NULL DEFAULT 1      COMMENT '사용 여부 (0=false, 1=true)',
    created_dt           TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt           TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_devices_location_node_code_name (location_node_code, name),
    KEY idx_devices_model_enabled (model_id, enabled),
    KEY idx_devices_location_enabled (location_node_code, enabled),
    CONSTRAINT fk_devices_model_id
        FOREIGN KEY (model_id) REFERENCES device_model (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_devices_location_node_code
        FOREIGN KEY (location_node_code) REFERENCES location_node (code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_devices_enabled
        CHECK (enabled IN (0, 1))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='장비 인스턴스 (현장 1대 = 1행, 얇은 인스턴스층)';
