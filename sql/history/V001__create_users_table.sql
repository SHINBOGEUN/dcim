-- =============================================================================
-- V001: users 테이블 생성 (identity 모듈)
-- =============================================================================
-- 작성일  : 2026-06-26
-- 대상 DB : MariaDB (dcim_new)
-- 엔티티  : net.vivans.dcim.module.identity.domain.model.User
--           net.vivans.dcim.shared.persistence.BaseEntity
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V001__create_users_table.sql
--
-- 운영 환경은 ddl-auto: none 이므로 스키마 변경은 이 SQL 이력으로 관리합니다.
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    id             INT          NOT NULL AUTO_INCREMENT COMMENT '사용자 ID',
    username       VARCHAR(255) NOT NULL                COMMENT '로그인 아이디',
    password       VARCHAR(255) NOT NULL                COMMENT 'BCrypt 해시 비밀번호',
    role           VARCHAR(50)  NULL                     COMMENT '권한 (기본 USER)',
    refresh_token  VARCHAR(512) NULL                     COMMENT 'JWT refresh token',
    created_dt     TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt     TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='사용자';
