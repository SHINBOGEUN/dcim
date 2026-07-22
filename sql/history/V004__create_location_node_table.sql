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
--   - code: PK. 일반 노드는 서버가 10자 Base62 랜덤 생성 (불변)
--   - code = 'UNASSIGNED' — 시스템 고정 루트 (시드). 삭제·이름 변경 금지
--   - parent_code IS NULL → 루트 노드
--   - (parent_code, name) UK — 같은 부모 아래 이름 중복 불가 (자식 노드)
--   - 루트 name 중복 — 애플리케이션 검증 (existsByParentIsNullAndName)
--   - location_type_id — LOCATION_TYPE 그룹만 허용
-- =============================================================================

CREATE TABLE IF NOT EXISTS location_node (
    code              CHAR(10)     NOT NULL                 COMMENT '노드 PK (일반: 10자 Base62 / 시스템: UNASSIGNED)',
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

-- -----------------------------------------------------------------------------
-- LOCATION_TYPE code_group + common_code 시드
-- UNASSIGNED 위치 노드가 location_type_id FK를 필요로 하므로 함께 시드한다.
-- CONTAINER/ZONE/ROW/RACK 등은 API에서 추가 등록해도 됨.
-- -----------------------------------------------------------------------------

INSERT INTO code_group (group_key, group_name)
SELECT 'LOCATION_TYPE', 'Location Type'
WHERE NOT EXISTS (
    SELECT 1 FROM code_group WHERE group_key = 'LOCATION_TYPE'
);

INSERT INTO common_code (group_id, code, name, sort_order)
SELECT cg.id, v.code, v.name, v.sort_order
FROM code_group cg
CROSS JOIN (
    SELECT 'UNASSIGNED' AS code, '미배정'     AS name, -1 AS sort_order UNION ALL
    SELECT 'CONTAINER'  AS code, '컨테이너'   AS name,  0 AS sort_order UNION ALL
    SELECT 'ZONE'       AS code, '존'         AS name,  1 AS sort_order UNION ALL
    SELECT 'ROW'        AS code, '열'         AS name,  2 AS sort_order UNION ALL
    SELECT 'RACK'       AS code, '랙'         AS name,  3 AS sort_order
) v
WHERE cg.group_key = 'LOCATION_TYPE'
  AND NOT EXISTS (
      SELECT 1
      FROM common_code cc
      WHERE cc.group_id = cg.id
        AND cc.code = v.code
  );

-- -----------------------------------------------------------------------------
-- 시스템 루트: UNASSIGNED (장비 선등록 시 임시 위치)
-- code = 'UNASSIGNED' (CHAR(10) 고정). 삭제·이름 변경 금지 (앱에서 검증).
-- devices.location_node_code 는 NOT NULL → 위치 미지정 시 이 노드를 참조.
-- -----------------------------------------------------------------------------

INSERT INTO location_node (code, parent_code, location_type_id, name)
SELECT 'UNASSIGNED', NULL, cc.id, '미배정'
FROM common_code cc
INNER JOIN code_group cg ON cg.id = cc.group_id
WHERE cg.group_key = 'LOCATION_TYPE'
  AND cc.code = 'UNASSIGNED'
  AND NOT EXISTS (
      SELECT 1 FROM location_node WHERE code = 'UNASSIGNED'
  );
