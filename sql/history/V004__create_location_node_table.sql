-- =============================================================================
-- V004: location_node 테이블 생성 (location 모듈)
-- =============================================================================
-- 작성일  : 2026-07-01
-- 대상 DB : MariaDB (dcim_new)
-- 엔티티  : net.vivans.dcim.module.location.domain.model.LocationNode
--           net.vivans.dcim.shared.persistence.BaseEntity
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V004__create_location_node_table.sql
--
-- 선행 조건: V003 (common_code)
--
-- 비즈니스 규칙 (애플리케이션에서 검증):
--   - location_type_id는 common_code 중 LOCATION_TYPE 그룹만 허용
--   - parent_id IS NULL → 루트 노드
--   - parent_id를 참조하는 자식이 없으면 리프 노드
-- =============================================================================

CREATE TABLE IF NOT EXISTS location_node (
    id                INT          NOT NULL AUTO_INCREMENT COMMENT '위치 노드 ID',
    parent_id         INT          NULL                     COMMENT '부모 노드 ID (루트는 NULL)',
    location_type_id  INT          NOT NULL                 COMMENT '위치 유형 ID (FK → common_code, LOCATION_TYPE만)',
    name              VARCHAR(255) NOT NULL                 COMMENT '노드 표시명',
    code              VARCHAR(100) NULL                      COMMENT '내부 식별 코드',
    depth             INT          NOT NULL DEFAULT 0         COMMENT '트리 깊이 (루트=0)',
    created_dt        TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt        TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uk_location_node_code (code),
    UNIQUE KEY uk_location_node_parent_id_name (parent_id, name),
    KEY idx_location_node_parent_id (parent_id),
    KEY idx_location_node_location_type_id (location_type_id),
    CONSTRAINT fk_location_node_parent_id
        FOREIGN KEY (parent_id) REFERENCES location_node (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_location_node_location_type_id
        FOREIGN KEY (location_type_id) REFERENCES common_code (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='위치 트리 노드';
