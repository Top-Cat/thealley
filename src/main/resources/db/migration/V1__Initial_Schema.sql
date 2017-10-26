CREATE TABLE IF NOT EXISTS `switch` (
  `macAddr` char(8) NOT NULL,
  `hostA` int(10) NOT NULL,
  `hostB` int(10) NOT NULL,
  PRIMARY KEY (`macAddr`)
);

CREATE TABLE IF NOT EXISTS `device` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `hostname` TEXT NOT NULL,
  PRIMARY KEY (`id`)
);
