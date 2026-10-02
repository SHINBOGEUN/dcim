CREATE TABLE IF NOT EXISTS `calculated_metric_source` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `calculated_metric_id` int(11) NOT NULL,
  `device_id` int(11) NOT NULL,
  `alias` varchar(32) NOT NULL,
  `protocol` varchar(16) NOT NULL,
  `point_name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_calculated_metric_source_alias` (`calculated_metric_id`,`alias`),
  KEY `idx_calculated_metric_source_device` (`device_id`),
  CONSTRAINT `fk_calculated_metric_source_metric` FOREIGN KEY (`calculated_metric_id`) REFERENCES `calculated_metric` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_calculated_metric_source_device` FOREIGN KEY (`device_id`) REFERENCES `devices` (`id`),
  CONSTRAINT `chk_calculated_metric_source_protocol` CHECK (`protocol` in ('snmp','modbus'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
