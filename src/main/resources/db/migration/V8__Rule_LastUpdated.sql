ALTER TABLE `rule`
  ADD COLUMN `last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `daytime` tinyint(1) UNSIGNED NOT NULL DEFAULT 1;
