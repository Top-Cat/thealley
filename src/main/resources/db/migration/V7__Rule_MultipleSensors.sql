CREATE TABLE IF NOT EXISTS `rule_sensor` (
  `rule_id` int(10) UNSIGNED NOT NULL,
  `sensor_id` varchar(32) NOT NULL,
  PRIMARY KEY (`rule_id`, `sensor_id`)
);

INSERT INTO rule_sensor SELECT rule_id, sensor_id FROM rule;

ALTER TABLE `rule`
  DROP COLUMN `sensor_id`;

UPDATE `scene` SET `light_id` = '0';

ALTER TABLE `scene`
  MODIFY `light_id` INT(10) UNSIGNED NOT NULL;
