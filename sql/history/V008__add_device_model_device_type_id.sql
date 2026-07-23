-- =============================================================================
-- V008: device_model.device_type_id 추가 (common_code FK)
-- =============================================================================
-- 작성일  : 2026-07-22
-- 대상 DB : MariaDB (dcim_new) — 이미 V005(구버전)로 device_model이 있는 환경
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V008__add_device_model_device_type_id.sql
--
-- 선행 조건: V003 (common_code), V005 (device_model)
--            common_code id=13, id=3 존재 (MODEL_TYPE 등)
--
-- 신규 DB: V005(신버전)에 이미 device_type_id가 있으면 경우 본 스크립트는
--          ADD COLUMN IF NOT EXISTS / 백필만 수행하고 FK는 없을 때만 추가.
--
-- 백필 (기존 데이터):
--   - device_model.id = 2 (LHT65N-PIR) → device_type_id = 13
--   - device_model.id = 3 (PDU-3상)    → device_type_id = 3
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1) 컬럼 추가 (기존 행 때문에 우선 NULL 허용)
-- -----------------------------------------------------------------------------
ALTER TABLE device_model
    ADD COLUMN IF NOT EXISTS device_type_id INT NULL
        COMMENT 'common_code.id (모델 유형)' AFTER manufacturer;

-- -----------------------------------------------------------------------------
-- 2) 기존 데이터 백필 (고정 ID)
-- -----------------------------------------------------------------------------
UPDATE device_model
SET device_type_id = 13
WHERE id = 2
  AND device_type_id IS NULL;

UPDATE device_model
SET device_type_id = 3
WHERE id = 3
  AND device_type_id IS NULL;

-- -----------------------------------------------------------------------------
-- 3) NOT NULL + 인덱스 + FK
-- -----------------------------------------------------------------------------
ALTER TABLE device_model
    MODIFY COLUMN device_type_id INT NOT NULL
        COMMENT 'common_code.id (모델 유형)';

SET @idx_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'device_model'
      AND index_name = 'idx_device_model_device_type_id'
);
SET @sql := IF(
    @idx_exists = 0,
    'ALTER TABLE device_model ADD KEY idx_device_model_device_type_id (device_type_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exists := (
    SELECT COUNT(1)
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'device_model'
      AND constraint_name = 'fk_device_model_device_type_id'
);
SET @sql := IF(
    @fk_exists = 0,
    'ALTER TABLE device_model
        ADD CONSTRAINT fk_device_model_device_type_id
            FOREIGN KEY (device_type_id) REFERENCES common_code (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
