-- =============================================================================
-- V002: code_group 테이블 생성 (common 모듈)
-- =============================================================================
-- 작성일  : 2026-06-26
-- 대상 DB : MariaDB (dcim_new)
-- 엔티티  : net.vivans.dcim.module.common.domain.model.CodeGroup
--           net.vivans.dcim.shared.persistence.BaseEntity
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V002__create_code_group_table.sql
--
-- 선행 조건: 없음
-- =============================================================================

CREATE TABLE IF NOT EXISTS code_group (
    id          INT          NOT NULL AUTO_INCREMENT COMMENT '코드 그룹 ID',
    group_key   VARCHAR(100) NOT NULL                COMMENT '그룹 키 (예: DEVICE_TYPE)',
    group_name  VARCHAR(255) NOT NULL                COMMENT '그룹 표시명',
    created_dt  TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt  TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code_group_group_key (group_key)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='공통 코드 그룹';
