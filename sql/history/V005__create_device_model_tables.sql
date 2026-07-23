-- =============================================================================
-- V005: device_model, device_model_protocol 테이블 생성 (devicemodel 모듈)
-- =============================================================================
-- 작성일  : 2026-07-06
-- 대상 DB : MariaDB (dcim_new)
-- 엔티티  : net.vivans.dcim.module.devicemodel.domain.model.DeviceModel
--           net.vivans.dcim.module.devicemodel.domain.model.DeviceModelProtocol
--           net.vivans.dcim.shared.persistence.BaseEntity
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V005__create_device_model_tables.sql
--
-- 선행 조건: V003 (common_code)
--             본 스크립트 하단에서 MODEL_TYPE · PROTOCOL_TYPE 시드
--
-- 설계 문서: docs/devicemodel/DEVICE_MODEL_API.md
--
-- 비즈니스 규칙 (애플리케이션에서 검증):
--   - (name, manufacturer) UK — 동일 제조사·제품명 중복 불가
--   - device_type_id — MODEL_TYPE 그룹만 허용 (유형은 모델에 귀속, devices에는 두지 않음)
--   - protocols 1개 이상
--   - protocol_type_id — PROTOCOL_TYPE 그룹만 허용
--   - (model_id, protocol_type_id) UK — 모델 내 동일 프로토콜 중복 불가
-- =============================================================================

CREATE TABLE IF NOT EXISTS device_model (
    id               INT           NOT NULL AUTO_INCREMENT COMMENT '장비 모델 ID',
    name             VARCHAR(255)  NOT NULL                COMMENT '모델/제품명',
    manufacturer     VARCHAR(255)  NOT NULL                COMMENT '제조사',
    device_type_id   INT           NOT NULL                COMMENT 'common_code.id (MODEL_TYPE만)',
    description      VARCHAR(1000) NULL                   COMMENT '설명',
    created_dt       TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt       TIMESTAMP(6)  NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_model_name_manufacturer (name, manufacturer),
    KEY idx_device_model_device_type_id (device_type_id),
    CONSTRAINT fk_device_model_device_type_id
        FOREIGN KEY (device_type_id) REFERENCES common_code (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='장비 제품 모델 (SKU/제품군)';

CREATE TABLE IF NOT EXISTS device_model_protocol (
    id                 INT          NOT NULL AUTO_INCREMENT COMMENT '모델-프로토콜 연결 ID',
    model_id           INT          NOT NULL                COMMENT 'device_model.id (FK)',
    protocol_type_id   INT          NOT NULL                COMMENT 'common_code.id (PROTOCOL_TYPE만)',
    created_dt         TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt         TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_model_protocol_model_protocol (model_id, protocol_type_id),
    KEY idx_device_model_protocol_model_id (model_id),
    KEY idx_device_model_protocol_protocol_type_id (protocol_type_id),
    CONSTRAINT fk_device_model_protocol_model_id
        FOREIGN KEY (model_id) REFERENCES device_model (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_device_model_protocol_protocol_type_id
        FOREIGN KEY (protocol_type_id) REFERENCES common_code (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='장비 모델별 지원 프로토콜 (N:M 연결)';

-- -----------------------------------------------------------------------------
-- MODEL_TYPE · PROTOCOL_TYPE code_group + common_code 시드
-- V002는 code_group 테이블만 생성하고 그룹 데이터는 넣지 않음 (ERD 예시 ≠ DB 시드).
-- device_model.device_type_id 는 MODEL_TYPE 그룹만 허용 (애플리케이션 검증).
-- LOCATION_TYPE 등 다른 그룹은 V004 / API에서 별도 등록.
-- -----------------------------------------------------------------------------

INSERT INTO code_group (group_key, group_name)
SELECT 'MODEL_TYPE', 'Model Type'
WHERE NOT EXISTS (
    SELECT 1 FROM code_group WHERE group_key = 'MODEL_TYPE'
);

INSERT INTO code_group (group_key, group_name)
SELECT 'PROTOCOL_TYPE', 'Protocol Type'
WHERE NOT EXISTS (
    SELECT 1 FROM code_group WHERE group_key = 'PROTOCOL_TYPE'
);

INSERT INTO common_code (group_id, code, name, sort_order)
SELECT cg.id, v.code, v.name, v.sort_order
FROM code_group cg
CROSS JOIN (
    SELECT 'PDU'    AS code, 'PDU'    AS name, 1 AS sort_order UNION ALL
    SELECT 'SENSOR' AS code, 'Sensor' AS name, 2 AS sort_order UNION ALL
    SELECT 'CDU'    AS code, 'CDU'    AS name, 3 AS sort_order UNION ALL
    SELECT 'OTHER'  AS code, 'Other'  AS name, 99 AS sort_order
) v
WHERE cg.group_key = 'MODEL_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM common_code cc
      WHERE cc.group_id = cg.id
        AND cc.code = v.code
  );

INSERT INTO common_code (group_id, code, name, sort_order)
SELECT cg.id, v.code, v.name, v.sort_order
FROM code_group cg
CROSS JOIN (
    SELECT 'snmp'   AS code, 'SNMP'   AS name, 1 AS sort_order UNION ALL
    SELECT 'modbus' AS code, 'Modbus' AS name, 2 AS sort_order UNION ALL
    SELECT 'mqtt'   AS code, 'MQTT'   AS name, 3 AS sort_order
) v
WHERE cg.group_key = 'PROTOCOL_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM common_code cc
      WHERE cc.group_id = cg.id
        AND cc.code = v.code
  );
