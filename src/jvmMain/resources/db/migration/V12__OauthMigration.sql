CREATE TABLE IF NOT EXISTS `oa_access_token` (
    `token_id` varchar(256) NOT NULL,
    `type` varchar(256) NOT NULL,
    `expiration` timestamp NOT NULL,
    `scope` varchar(256) NOT NULL,
    `user_name` varchar(256),
    `client_id` varchar(256) NOT NULL,
    `refresh_token` varchar(256),
    PRIMARY KEY (`token_id`)
);

CREATE TABLE IF NOT EXISTS `oa_refresh_token` (
    `token_id` varchar(256) NOT NULL,
    `scope` varchar(256) NOT NULL,
    `user_name` varchar(256),
    `client_id` varchar(256) NOT NULL,
    PRIMARY KEY (`token_id`)
);