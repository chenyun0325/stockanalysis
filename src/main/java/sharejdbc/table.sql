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

CREATE TABLE `k_st_his_data` (
  `id` bigint(20) NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `date` varchar(255) DEFAULT NULL,
  `ktype` varchar(255) DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `close` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `volume` double DEFAULT NULL,
  `price_change` double DEFAULT NULL,
  `p_change` double DEFAULT NULL,
  `ma5` double DEFAULT NULL,
  `ma10` double DEFAULT NULL,
  `ma20` double DEFAULT NULL,
  `v_ma5` double DEFAULT NULL,
  `v_ma10` double DEFAULT NULL,
  `v_ma20` double DEFAULT NULL,
  `turnover` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `analysis_zl_filter_fs_data` (
  `id` bigint(20) NOT NULL,
  `code` char(6) NOT NULL COMMENT '股票代码',
  `filter_amount` int(11) NOT NULL COMMENT '过滤数量',
  `type` tinyint(4) NOT NULL COMMENT '0:卖,1:买',
  `begin_time_l` bigint(20) DEFAULT NULL COMMENT '开始时间的毫秒数',
  `end_time_l` bigint(20) DEFAULT NULL COMMENT '结束时间的毫秒数',
  `begin_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `time_index_str` varchar(255) DEFAULT NULL COMMENT '20170101d|20170101d-01h，20170101d-02h|20170101d-01h-01m|20170101d-01h-02m',
  `amount` int(11) DEFAULT NULL COMMENT '交易量',
  `time_type` char(1) DEFAULT NULL COMMENT 'D/H/M|天/小时/分钟',
  `time_peried` int(11) DEFAULT '1' COMMENT '周期大小',
  PRIMARY KEY (`id`),
  KEY `idx_stock` (`code`) USING BTREE,
  KEY `idx_filter` (`filter_amount`) USING BTREE,
  KEY `idx_time` (`time_index_str`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;