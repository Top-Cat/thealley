DROP TABLE `switch`;

CREATE TABLE IF NOT EXISTS `switch` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `button` smallint(1) NOT NULL,
  `scene` int(10) NOT NULL,
  `state` smallint(3) NOT NULL,
  PRIMARY KEY (`id`, `button`)
);
