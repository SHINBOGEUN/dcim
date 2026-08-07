-- =============================================================================
-- V012: device_model_snmp_point (model_protocol_id, oid) UK 추가
-- =============================================================================
-- 작성일  : 2026-08-07
-- 대상 DB : MariaDB (dcim_new)
--
-- 적용 방법 (예시):
--   mysql -h HOST -P PORT -u dcim -p dcim_new < sql/history/V012__add_uk_device_model_snmp_point_oid.sql
--
-- 선행 조건: V006 (device_model_snmp_point)
--
-- 목적:
--   - 동일 SNMP protocol 내 OID(템플릿 포함) 중복 방지
--   - name UK와 별도로, WATT/KWH가 같은 OID를 쓰는 실무 오류 차단
--
-- 주의:
--   - 이미 중복 OID가 있으면 ALTER가 실패합니다.
--   - 적용 전 중복 확인:
--       SELECT model_protocol_id, oid, COUNT(*) c
--       FROM device_model_snmp_point
--       GROUP BY model_protocol_id, oid
--       HAVING c > 1;
-- =============================================================================

ALTER TABLE device_model_snmp_point
    ADD CONSTRAINT uk_device_model_snmp_point_protocol_oid
        UNIQUE (model_protocol_id, oid);
