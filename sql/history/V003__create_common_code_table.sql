-- =============================================================================
-- V003: common_code 테이블 생성 (common 모듈)
-- =============================================================================
-- 작성일  : 2026-06-26
-- 대상 DB : MariaDB (dcim_new)
-- 엔티티  : net.vivans.dcim.module.common.domain.model.CommonCode
--           net.vivans.dcim.shared.persistence.BaseEntity
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V003__create_common_code_table.sql
--
-- 선행 조건: V002 (code_group)
-- =============================================================================

CREATE TABLE IF NOT EXISTS common_code (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT '공통 코드 ID',
    group_id    INT          NOT NULL                COMMENT '코드 그룹 ID (FK)',
    code        VARCHAR(100) NOT NULL                COMMENT '코드 값 (예: ups, pdu)',
    name        VARCHAR(255) NOT NULL                COMMENT '코드 표시명',
    sort_order  INT          NULL                     COMMENT '정렬 순서',
    created_dt  TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt  TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_common_code_group_id_code (group_id, code),
    KEY idx_common_code_group_id (group_id),
    CONSTRAINT fk_common_code_group_id
        FOREIGN KEY (group_id) REFERENCES code_group (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='공통 코드';
