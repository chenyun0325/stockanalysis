-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `fs_st_his_data_v`()
  BEGIN
    DECLARE `@i` int(11);
    DECLARE `@sqlstr` varchar(2560);
    SET `@i`=0;
    WHILE `@i` < 40 DO
      SET @sqlstr = CONCAT(
          "CREATE TABLE fs_st_his_data_v_",
          `@i`,

          "(
            `index` bigint(20) NOT NULL AUTO_INCREMENT,
            `code` VARCHAR(45) NULL,
            `date` VARCHAR(45) NULL,
            `time` VARCHAR(45) NULL,
            `price` double DEFAULT NULL,
            `change` VARCHAR(45) NULL,
            `volume` bigint(20) DEFAULT NULL,
            `amount` bigint(20) DEFAULT NULL,
            `type` VARCHAR(45) NULL,
            PRIMARY KEY (`index`),
            KEY `idx_code` (`code`(6)),
            KEY `idx_date` (`date`(10)),
            UNIQUE KEY `U_C_D_T` (`code`,`date`,`time`)
          ) ENGINE=InnoDB DEFAULT CHARSET=utf8 "
      );
      prepare stmt from @sqlstr;
      execute stmt;

      SET `@i` = `@i` + 1;
    END WHILE;
  END

    -- --------------------------------------------------------------------------------
    -- Routine DDL
    -- Note: comments before and after the routine body will not be stored by the server
    -- --------------------------------------------------------------------------------
    DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `fs_st_pk_data_v`()
  BEGIN
    DECLARE `@i` int(11);
    DECLARE `@sqlstr` varchar(2560);
    SET `@i`=0;
    WHILE `@i` < 40 DO
      SET @sqlstr = CONCAT(
          "CREATE TABLE fs_st_pk_data_v_",
          `@i`,

          "(
            `id` BIGINT NOT NULL AUTO_INCREMENT,
            `timestamp` BIGINT NULL,
            `date` VARCHAR(45) NULL,
            `time` VARCHAR(45) NULL,
            `code` VARCHAR(45) NULL,
            `name` VARCHAR(45) NULL,
            `open` DOUBLE NULL,
            `pre_close` DOUBLE NULL,
            `price` DOUBLE NULL,
            `high` DOUBLE NULL,
            `low` DOUBLE NULL,
            `bid` DOUBLE NULL,
            `ask` DOUBLE NULL,
            `volume` BIGINT NULL,
            `amount` DOUBLE NULL,
            `b1_v` INT NULL,
            `b1_p` DOUBLE NULL,
            `b2_v` INT NULL,
            `b2_p` DOUBLE NULL,
            `b3_v` INT NULL,
            `b3_p` DOUBLE NULL,
            `b4_v` INT NULL,
            `b4_p` DOUBLE NULL,
            `b5_v` INT NULL,
            `b5_p` DOUBLE NULL,
            PRIMARY KEY (`id`),
            INDEX `IDX_CODE` (`name`(6) ),
            UNIQUE KEY `U_C_D_T` (`code`,`date`,`time`)
          ) ENGINE=InnoDB DEFAULT CHARSET=utf8 "
      );
      prepare stmt from @sqlstr;
      execute stmt;

      SET `@i` = `@i` + 1;
    END WHILE;
  END