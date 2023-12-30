CREATE TABLE IF NOT EXISTS `rule` (
  `rule_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `sensor_id` varchar(32) NOT NULL,
  `state` tinyint(1) UNSIGNED NOT NULL,
  `last_active` TIMESTAMP NULL,
  `scene_id` int(10) UNSIGNED NOT NULL,
  PRIMARY KEY (`rule_id`)
);

CREATE TABLE IF NOT EXISTS `scene` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `scene_id` int(10) UNSIGNED NOT NULL,
  `light_id` varchar(255) NOT NULL,
  `brightness` SMALLINT(10) UNSIGNED NOT NULL,
  `hue` SMALLINT(10) UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `scene` (`scene_id`)
);
