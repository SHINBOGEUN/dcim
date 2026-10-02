-- Local development reset: old PUE definitions/widgets are intentionally discarded.
-- Back up any data that must be retained before applying this file.
-- Stop Manager/Collector/Sensor Data before applying; old in-memory collector jobs must be cleared by restart.
-- One-time migration; do not run again after the new columns have been created.
ALTER TABLE `pue_definition`
  ADD COLUMN `formula` varchar(500) NULL COMMENT 'Restricted arithmetic expression using source aliases',
  ADD COLUMN `result_unit` varchar(32) NULL COMMENT 'Calculated result display unit';

ALTER TABLE `pue_definition_source`
  MODIFY COLUMN `role` varchar(16) NULL,
  ADD COLUMN `alias` varchar(32) NULL,
  ADD COLUMN `protocol` varchar(16) NULL,
  ADD UNIQUE KEY `uk_pue_definition_source_alias` (`pue_definition_id`, `alias`),
  DROP INDEX `uk_pue_definition_source_device`,
  ADD CONSTRAINT `chk_pue_definition_source_protocol`
    CHECK (`protocol` IS NULL OR `protocol` IN ('snmp', 'modbus'));

-- Apply data cleanup only after both schema changes succeed.
DELETE FROM `page_widget` WHERE `id` IN (SELECT `widget_id` FROM `page_widget_pue`);
DELETE FROM `page_widget` WHERE `query_kind` = 'pue';
DELETE FROM `pue_definition`;
