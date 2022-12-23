ALTER TABLE `device`
    MODIFY COLUMN `type` ENUM('bulb', 'plug', 'relay', 'blind') NOT NULL DEFAULT 'bulb';