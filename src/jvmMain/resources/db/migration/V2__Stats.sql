ALTER TABLE `device`
  ADD COLUMN `type` ENUM('bulb', 'plug', 'relay') NOT NULL DEFAULT 'bulb',
  ADD COLUMN `name` TEXT NOT NULL;

UPDATE device SET name = hostname;
UPDATE device SET hostname = CONCAT("lb130-", name, ".guest.kirkstall.top-cat.me") WHERE type = 'bulb';
UPDATE device SET hostname = CONCAT("hs110-", name, ".guest.kirkstall.top-cat.me") WHERE type = 'plug';
