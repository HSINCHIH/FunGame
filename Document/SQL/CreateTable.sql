CREATE TABLE `Game` (
`GameNum` INT(11) NOT NULL AUTO_INCREMENT COMMENT '遊戲編號' ,
`GameName` VARCHAR(255) NOT NULL COMMENT '遊戲名稱' ,
PRIMARY KEY (`GameNum`)
)
COLLATE='utf8_unicode_ci'
ENGINE=InnoDB
;CREATE TABLE `Player` (
`PlayerNum` INT(11) NOT NULL AUTO_INCREMENT COMMENT '玩家編號' ,
`PlayerName` VARCHAR(255) NOT NULL COMMENT '玩家帳號' ,
`PlayerPW` VARCHAR(255) NOT NULL COMMENT '玩家密碼' ,
`RoomNum` INT(11) NOT NULL DEFAULT '-1' COMMENT '房間編號(-1:沒房間)' ,
PRIMARY KEY (`PlayerNum`,`PlayerName`)
)
COLLATE='utf8_unicode_ci'
ENGINE=InnoDB
;CREATE TABLE `Record` (
`RecordNum` INT(11) NOT NULL AUTO_INCREMENT COMMENT '紀錄編號' ,
`RoomNum` INT(11) NOT NULL COMMENT '房間編號' ,
`PlayerNum` INT(11) NOT NULL COMMENT '玩家編號' ,
`Command` TEXT NOT NULL COMMENT '玩家指令' ,
`RecordTime` TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL COMMENT '紀錄時間' ,
PRIMARY KEY (`RecordNum`)
)
COLLATE='utf8_unicode_ci'
ENGINE=InnoDB
;CREATE TABLE `Room` (
`RoomNum` INT(11) NOT NULL AUTO_INCREMENT COMMENT '房間編號' ,
`RoomName` VARCHAR(255) NOT NULL COMMENT '房間名稱' ,
`RoomPW` VARCHAR(255) NOT NULL COMMENT '房間密碼' ,
`Description` VARCHAR(255) NOT NULL COMMENT '房間敘述' ,
`StartTime` TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL COMMENT '開始時間' ,
`StopTime` TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL COMMENT '結束時間' ,
`GameNum` INT(11) NOT NULL COMMENT '遊戲種類' ,
`CreateBy` INT(11) NOT NULL COMMENT '建立者' ,
`Winner` INT(11) NOT NULL DEFAULT '-1' COMMENT '贏家(-1:沒贏家)' ,
`RoomState` INT(11) NOT NULL DEFAULT 1 COMMENT '0:不啟用,1:啟用,2:遊戲中,3:結束' ,
PRIMARY KEY (`RoomNum`)
)
COLLATE='utf8_unicode_ci'
ENGINE=InnoDB
;