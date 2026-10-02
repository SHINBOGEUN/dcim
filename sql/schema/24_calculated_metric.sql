CREATE TABLE IF NOT EXISTS `calculated_metric` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `calculation_cron` varchar(64) NOT NULL DEFAULT '0 */5 * * * *',
  `collection_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `config_version` int(11) NOT NULL DEFAULT 1,
  `collector_job_id` varchar(100) NULL,
  `formula` varchar(500) NOT NULL,
  `result_unit` varchar(32) NULL,
  `created_dt` timestamp(6) NULL DEFAULT current_timestamp(6),
  `updated_dt` timestamp(6) NULL DEFAULT current_timestamp(6) ON UPDATE current_timestamp(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_calculated_metric_name` (`name`),
  CONSTRAINT `chk_calculated_metric_enabled` CHECK (`collection_enabled` in (0,1)),
  CONSTRAINT `chk_calculated_metric_version` CHECK (`config_version` >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
