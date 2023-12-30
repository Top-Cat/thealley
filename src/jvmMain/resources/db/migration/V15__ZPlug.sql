ALTER TABLE `device`
    MODIFY COLUMN `type` ENUM('bulb', 'plug', 'relay', 'blind', 'zplug') NOT NULL DEFAULT 'bulb';