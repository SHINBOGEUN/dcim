-- =============================================================================
-- V004: location_node 테이블 생성 (location 모듈)
-- =============================================================================
-- 작성일  : 2026-07-02
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
--   - code: PK, 서버에서 10자 Base62 랜덤 문자열 자동 생성 (불변)
--   - parent_code IS NULL → 루트 노드
--   - parent_code를 참조하는 자식이 없으면 리프 노드
--   - location_type_id는 common_code 중 LOCATION_TYPE 그룹만 허용
--   - (parent_code, name) 복합 유니크 — 같은 부모 아래 이름 중복 불가
-- =============================================================================

CREATE TABLE IF NOT EXISTS location_node (
    code              CHAR(10)     NOT NULL                 COMMENT '노드 PK (10자 Base62, 서버 자동 생성)',
    parent_code       CHAR(10)     NULL                     COMMENT '부모 노드 code (루트는 NULL)',
    location_type_id  INT          NOT NULL                 COMMENT '위치 유형 ID (FK → common_code, LOCATION_TYPE만)',
    name              VARCHAR(255) NOT NULL                 COMMENT '노드 표시명',
    created_dt        TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_dt        TIMESTAMP(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시각',
    PRIMARY KEY (code),
    UNIQUE KEY uk_location_node_parent_code_name (parent_code, name),
    KEY idx_location_node_parent_code (parent_code),
    KEY idx_location_node_location_type_id (location_type_id),
    CONSTRAINT fk_location_node_parent_code
        FOREIGN KEY (parent_code) REFERENCES location_node (code)
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
