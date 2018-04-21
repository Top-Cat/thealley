ALTER TABLE `rule`
  ADD COLUMN `timeout` int(10) UNSIGNED NOT NULL,
  DROP COLUMN `state`;
