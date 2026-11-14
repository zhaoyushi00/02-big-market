# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20050
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: 127.0.0.1 (MySQL 5.6.39)
# 数据库: big_market
# 生成时间: 2026-11-13 10:19:11 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE database if NOT EXISTS `big_market` default character set utf8mb4;
use `big_market`;

# 转储表 award
# ------------------------------------------------------------

DROP TABLE IF EXISTS `award`;

CREATE TABLE `award` (
                         `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                         `award_id` int(8) NOT NULL COMMENT '抽奖奖品ID - 内部流转使用',
                         `award_key` varchar(32) NOT NULL COMMENT '奖品对接标识 - 每一个都是一个对应的发奖策略',
                         `award_config` varchar(32) NOT NULL COMMENT '奖品配置信息',
                         `award_desc` varchar(128) NOT NULL COMMENT '奖品内容描述',
                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖品表';

LOCK TABLES `award` WRITE;
/*!40000 ALTER TABLE `award` DISABLE KEYS */;

INSERT INTO `award` (`id`, `award_id`, `award_key`, `award_config`, `award_desc`, `create_time`, `update_time`)
VALUES
    (1,101,'user_credit_random','1,100','用户积分【优先透彻规则范围，如果没有则走配置】','2026-09-17 23:01:19','2026-09-17 23:07:10'),
    (2,102,'openai_use_count','5','OpenAI 增加使用次数','2026-09-17 23:01:19','2026-09-17 23:03:42'),
    (3,103,'openai_use_count','10','OpenAI 增加使用次数','2026-09-17 23:01:19','2026-09-17 23:03:42'),
    (4,104,'openai_use_count','20','OpenAI 增加使用次数','2026-09-17 23:01:19','2026-09-17 23:03:42'),
    (5,105,'openai_model','gpt-4','OpenAI 增加模型','2026-09-17 23:01:19','2026-09-17 23:03:19'),
    (6,106,'openai_model','dall-e-2','OpenAI 增加模型','2026-09-17 23:01:19','2026-09-17 23:03:22'),
    (7,107,'openai_model','dall-e-3','OpenAI 增加模型','2026-09-17 23:01:19','2026-09-17 23:03:22'),
    (8,108,'openai_use_count','100','OpenAI 增加使用次数','2026-09-17 23:01:19','2026-09-17 23:03:41'),
    (9,109,'openai_model','gpt-4,dall-e-2,dall-e-3','OpenAI 增加模型','2026-09-17 23:01:19','2026-09-17 23:03:34'),
    (10,100,'user_credit_blacklist','1','黑名单积分','2026-09-29 07:59:45','2026-09-29 07:59:47');

/*!40000 ALTER TABLE `award` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 daily_behavior_rebate
# ------------------------------------------------------------

DROP TABLE IF EXISTS `daily_behavior_rebate`;

CREATE TABLE `daily_behavior_rebate` (
                                         `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                         `behavior_type` varchar(16) NOT NULL COMMENT '行为类型（sign 签到、openai_pay 支付）',
                                         `rebate_desc` varchar(128) NOT NULL COMMENT '返利描述',
                                         `rebate_type` varchar(16) NOT NULL COMMENT '返利类型（sku 活动库存充值商品、integral 用户活动积分）',
                                         `rebate_config` varchar(32) NOT NULL COMMENT '返利配置',
                                         `state` varchar(12) NOT NULL COMMENT '状态（open 开启、close 关闭）',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         PRIMARY KEY (`id`),
                                         KEY `idx_behavior_type` (`behavior_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日常行为返利活动配置';

LOCK TABLES `daily_behavior_rebate` WRITE;
/*!40000 ALTER TABLE `daily_behavior_rebate` DISABLE KEYS */;

INSERT INTO `daily_behavior_rebate` (`id`, `behavior_type`, `rebate_desc`, `rebate_type`, `rebate_config`, `state`, `create_time`, `update_time`)
VALUES
    (1,'sign','签到返利-sku额度','sku','9011','open','2026-11-13 09:32:46','2026-11-13 18:05:23'),
    (2,'sign','签到返利-积分','integral','10','open','2026-11-13 09:32:46','2026-11-13 18:05:27');

/*!40000 ALTER TABLE `daily_behavior_rebate` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 raffle_activity
# ------------------------------------------------------------

DROP TABLE IF EXISTS `raffle_activity`;

CREATE TABLE `raffle_activity` (
                                   `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                   `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
                                   `activity_name` varchar(64) NOT NULL COMMENT '活动名称',
                                   `activity_desc` varchar(128) NOT NULL COMMENT '活动描述',
                                   `begin_date_time` datetime NOT NULL COMMENT '开始时间',
                                   `end_date_time` datetime NOT NULL COMMENT '结束时间',
                                   `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
                                   `state` varchar(8) NOT NULL DEFAULT 'create' COMMENT '活动状态',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uq_activity_id` (`activity_id`),
                                   UNIQUE KEY `uq_strategy_id` (`strategy_id`),
                                   KEY `idx_begin_date_time` (`begin_date_time`),
                                   KEY `idx_end_date_time` (`end_date_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动表';

LOCK TABLES `raffle_activity` WRITE;
/*!40000 ALTER TABLE `raffle_activity` DISABLE KEYS */;

INSERT INTO `raffle_activity` (`id`, `activity_id`, `activity_name`, `activity_desc`, `begin_date_time`, `end_date_time`, `strategy_id`, `state`, `create_time`, `update_time`)
VALUES
    (1,100301,'测试活动','测试活动','2026-10-24 20:00:03','2036-10-24 20:00:03',100006,'open','2026-10-24 20:00:03','2026-11-02 09:04:02');

/*!40000 ALTER TABLE `raffle_activity` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 raffle_activity_count
# ------------------------------------------------------------

DROP TABLE IF EXISTS `raffle_activity_count`;

CREATE TABLE `raffle_activity_count` (
                                         `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                         `activity_count_id` bigint(12) NOT NULL COMMENT '活动次数编号',
                                         `total_count` int(8) NOT NULL COMMENT '总次数',
                                         `day_count` int(8) NOT NULL COMMENT '日次数',
                                         `month_count` int(8) NOT NULL COMMENT '月次数',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uq_activity_count_id` (`activity_count_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动次数配置表';

LOCK TABLES `raffle_activity_count` WRITE;
/*!40000 ALTER TABLE `raffle_activity_count` DISABLE KEYS */;

INSERT INTO `raffle_activity_count` (`id`, `activity_count_id`, `total_count`, `day_count`, `month_count`, `create_time`, `update_time`)
VALUES
    (1,11101,10000,1000,1000,'2026-10-24 20:00:16','2026-11-13 18:58:35');

/*!40000 ALTER TABLE `raffle_activity_count` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 raffle_activity_sku
# ------------------------------------------------------------

DROP TABLE IF EXISTS `raffle_activity_sku`;

CREATE TABLE `raffle_activity_sku` (
                                       `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                       `sku` bigint(12) NOT NULL COMMENT '商品sku - 把每一个组合当做一个商品',
                                       `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
                                       `activity_count_id` bigint(12) NOT NULL COMMENT '活动个人参与次数ID',
                                       `stock_count` int(11) NOT NULL COMMENT '商品库存',
                                       `stock_count_surplus` int(11) NOT NULL COMMENT '剩余库存',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uq_sku` (`sku`),
                                       KEY `idx_activity_id_activity_count_id` (`activity_id`,`activity_count_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `raffle_activity_sku` WRITE;
/*!40000 ALTER TABLE `raffle_activity_sku` DISABLE KEYS */;

INSERT INTO `raffle_activity_sku` (`id`, `sku`, `activity_id`, `activity_count_id`, `stock_count`, `stock_count_surplus`, `create_time`, `update_time`)
VALUES
    (1,9011,100301,11101,100000,100000,'2026-10-27 16:41:02','2026-11-13 18:58:30');

/*!40000 ALTER TABLE `raffle_activity_sku` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 rule_tree
# ------------------------------------------------------------

DROP TABLE IF EXISTS `rule_tree`;

CREATE TABLE `rule_tree` (
                             `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                             `tree_id` varchar(32) NOT NULL COMMENT '规则树ID',
                             `tree_name` varchar(64) NOT NULL COMMENT '规则树名称',
                             `tree_desc` varchar(128) DEFAULT NULL COMMENT '规则树描述',
                             `tree_node_rule_key` varchar(32) NOT NULL COMMENT '规则树根入口规则',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uq_tree_id` (`tree_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则表-树';

LOCK TABLES `rule_tree` WRITE;
/*!40000 ALTER TABLE `rule_tree` DISABLE KEYS */;

INSERT INTO `rule_tree` (`id`, `tree_id`, `tree_name`, `tree_desc`, `tree_node_rule_key`, `create_time`, `update_time`)
VALUES
    (1,'tree_lock_1','规则树','规则树','rule_lock','2026-10-07 19:17:53','2026-10-15 11:15:19'),
    (2,'tree_luck_award','规则树-兜底奖励','规则树-兜底奖励','rule_stock','2026-10-15 11:09:17','2026-10-15 11:15:27'),
    (3,'tree_lock_2','规则树','规则树','rule_lock','2026-10-07 19:17:53','2026-10-15 11:15:19'),
    (4,'tree_lock_3','规则树','规则树','rule_lock','2026-10-07 19:17:53','2026-10-15 11:15:19');

/*!40000 ALTER TABLE `rule_tree` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 rule_tree_node
# ------------------------------------------------------------

DROP TABLE IF EXISTS `rule_tree_node`;

CREATE TABLE `rule_tree_node` (
                                  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                  `tree_id` varchar(32) NOT NULL COMMENT '规则树ID',
                                  `rule_key` varchar(32) NOT NULL COMMENT '规则Key',
                                  `rule_desc` varchar(64) NOT NULL COMMENT '规则描述',
                                  `rule_value` varchar(128) DEFAULT NULL COMMENT '规则比值',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则表-树节点';

LOCK TABLES `rule_tree_node` WRITE;
/*!40000 ALTER TABLE `rule_tree_node` DISABLE KEYS */;

INSERT INTO `rule_tree_node` (`id`, `tree_id`, `rule_key`, `rule_desc`, `rule_value`, `create_time`, `update_time`)
VALUES
    (1,'tree_lock_1','rule_lock','限定用户已完成N次抽奖后解锁','1','2026-10-07 19:18:21','2026-10-15 11:15:42'),
    (2,'tree_lock_1','rule_luck_award','兜底奖品随机积分','101:1,100','2026-10-07 19:18:21','2026-10-15 11:15:44'),
    (3,'tree_lock_1','rule_stock','库存扣减规则',NULL,'2026-10-07 19:18:59','2026-10-15 11:15:44'),
    (4,'tree_luck_award','rule_stock','库存扣减规则',NULL,'2026-10-15 11:09:37','2026-10-15 11:10:59'),
    (5,'tree_luck_award','rule_luck_award','兜底奖品随机积分','101:1,100','2026-10-15 11:09:37','2026-10-15 11:11:01'),
    (6,'tree_lock_2','rule_lock','限定用户已完成N次抽奖后解锁','2','2026-10-07 19:18:21','2026-10-15 11:16:16'),
    (7,'tree_lock_2','rule_luck_award','兜底奖品随机积分','101:1,100','2026-10-07 19:18:21','2026-10-12 20:04:59'),
    (8,'tree_lock_2','rule_stock','库存扣减规则',NULL,'2026-10-07 19:18:59','2026-10-10 15:39:34'),
    (9,'tree_lock_3','rule_lock','限定用户已完成N次抽奖后解锁','3','2026-10-07 19:18:21','2026-11-13 17:52:33'),
    (10,'tree_lock_3','rule_luck_award','兜底奖品随机积分','101:1,100','2026-10-07 19:18:21','2026-11-13 17:52:36'),
    (11,'tree_lock_3','rule_stock','库存扣减规则',NULL,'2026-10-07 19:18:59','2026-10-10 15:39:34');

/*!40000 ALTER TABLE `rule_tree_node` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 rule_tree_node_line
# ------------------------------------------------------------

DROP TABLE IF EXISTS `rule_tree_node_line`;

CREATE TABLE `rule_tree_node_line` (
                                       `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                       `tree_id` varchar(32) NOT NULL COMMENT '规则树ID',
                                       `rule_node_from` varchar(32) NOT NULL COMMENT '规则Key节点 From',
                                       `rule_node_to` varchar(32) NOT NULL COMMENT '规则Key节点 To',
                                       `rule_limit_type` varchar(8) NOT NULL COMMENT '限定类型；1:=;2:>;3:<;4:>=;5<=;6:enum[枚举范围];',
                                       `rule_limit_value` varchar(32) NOT NULL COMMENT '限定值（到下个节点）',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则表-树节点连线';

LOCK TABLES `rule_tree_node_line` WRITE;
/*!40000 ALTER TABLE `rule_tree_node_line` DISABLE KEYS */;

INSERT INTO `rule_tree_node_line` (`id`, `tree_id`, `rule_node_from`, `rule_node_to`, `rule_limit_type`, `rule_limit_value`, `create_time`, `update_time`)
VALUES
    (1,'tree_lock_1','rule_lock','rule_stock','EQUAL','ALLOW','0000-00-00 00:00:00','2026-10-15 11:17:24'),
    (2,'tree_lock_1','rule_lock','rule_luck_award','EQUAL','TAKE_OVER','0000-00-00 00:00:00','2026-10-15 11:17:25'),
    (3,'tree_lock_1','rule_stock','rule_luck_award','EQUAL','ALLOW','0000-00-00 00:00:00','2026-10-15 11:17:26'),
    (4,'tree_luck_award','rule_stock','rule_luck_award','EQUAL','ALLOW','2026-10-15 11:10:16','2026-10-15 11:11:03'),
    (5,'tree_lock_2','rule_lock','rule_stock','EQUAL','ALLOW','0000-00-00 00:00:00','2026-10-15 11:17:24'),
    (6,'tree_lock_2','rule_lock','rule_luck_award','EQUAL','TAKE_OVER','0000-00-00 00:00:00','2026-10-15 11:17:25'),
    (7,'tree_lock_2','rule_stock','rule_luck_award','EQUAL','ALLOW','0000-00-00 00:00:00','2026-10-15 11:17:26'),
    (8,'tree_lock_3','rule_lock','rule_luck_award','EQUAL','ALLOW','0000-00-00 00:00:00','2026-11-13 17:52:55'),
    (9,'tree_lock_3','rule_lock','rule_luck_award','EQUAL','TAKE_OVER','0000-00-00 00:00:00','2026-10-15 11:17:25'),
    (10,'tree_lock_3','rule_stock','rule_luck_award','EQUAL','ALLOW','0000-00-00 00:00:00','2026-10-15 11:17:26');

/*!40000 ALTER TABLE `rule_tree_node_line` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 strategy
# ------------------------------------------------------------

DROP TABLE IF EXISTS `strategy`;

CREATE TABLE `strategy` (
                            `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                            `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
                            `strategy_desc` varchar(128) NOT NULL COMMENT '抽奖策略描述',
                            `rule_models` varchar(256) DEFAULT NULL COMMENT '规则模型，rule配置的模型同步到此表，便于使用',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_strategy_id` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖策略';

LOCK TABLES `strategy` WRITE;
/*!40000 ALTER TABLE `strategy` DISABLE KEYS */;

INSERT INTO `strategy` (`id`, `strategy_id`, `strategy_desc`, `rule_models`, `create_time`, `update_time`)
VALUES
    (1,100001,'抽奖策略','rule_blacklist,rule_weight','2026-09-17 22:24:56','2026-10-04 23:51:14'),
    (2,100003,'抽奖策略-验证lock',NULL,'2026-10-02 03:18:38','2026-11-04 01:34:32'),
    (3,100002,'抽奖策略-非完整1概率',NULL,'2026-09-17 22:24:56','2026-10-10 15:29:00'),
    (4,100004,'抽奖策略-随机抽奖',NULL,'2026-09-17 22:24:56','2026-10-05 02:58:22'),
    (5,100005,'抽奖策略-测试概率计算',NULL,'2026-09-17 22:24:56','2026-10-05 13:44:30'),
    (6,100006,'抽奖策略-规则树',NULL,'2026-10-10 15:20:39','2026-10-10 15:20:39');

/*!40000 ALTER TABLE `strategy` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 strategy_award
# ------------------------------------------------------------

DROP TABLE IF EXISTS `strategy_award`;

CREATE TABLE `strategy_award` (
                                  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
                                  `award_id` int(8) NOT NULL COMMENT '抽奖奖品ID - 内部流转使用',
                                  `award_title` varchar(128) NOT NULL COMMENT '抽奖奖品标题',
                                  `award_subtitle` varchar(128) DEFAULT NULL COMMENT '抽奖奖品副标题',
                                  `award_count` int(8) NOT NULL DEFAULT '0' COMMENT '奖品库存总量',
                                  `award_count_surplus` int(8) NOT NULL DEFAULT '0' COMMENT '奖品库存剩余',
                                  `award_rate` decimal(6,4) NOT NULL COMMENT '奖品中奖概率',
                                  `rule_models` varchar(256) DEFAULT NULL COMMENT '规则模型，rule配置的模型同步到此表，便于使用',
                                  `sort` int(2) NOT NULL DEFAULT '0' COMMENT '排序',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_strategy_id_award_id` (`strategy_id`,`award_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖策略奖品概率';

LOCK TABLES `strategy_award` WRITE;
/*!40000 ALTER TABLE `strategy_award` DISABLE KEYS */;

INSERT INTO `strategy_award` (`id`, `strategy_id`, `award_id`, `award_title`, `award_subtitle`, `award_count`, `award_count_surplus`, `award_rate`, `rule_models`, `sort`, `create_time`, `update_time`)
VALUES
    (1,100001,101,'随机积分',NULL,80000,79998,0.3000,'tree_luck_award',1,'2026-09-17 22:25:25','2026-10-19 18:33:47'),
    (2,100001,102,'5次使用',NULL,10000,9999,0.2000,'tree_luck_award',2,'2026-09-17 22:25:44','2026-10-19 18:33:49'),
    (3,100001,103,'10次使用',NULL,5000,4998,0.2000,'tree_luck_award',3,'2026-09-17 22:27:04','2026-10-19 18:33:44'),
    (4,100001,104,'20次使用',NULL,4000,3999,0.1000,'tree_luck_award',4,'2026-09-17 22:27:20','2026-10-19 18:33:26'),
    (5,100001,105,'增加gpt-4对话模型',NULL,600,600,0.1000,'tree_luck_award',5,'2026-09-17 22:27:33','2026-10-15 11:12:10'),
    (6,100001,106,'增加dall-e-2画图模型',NULL,200,200,0.0500,'tree_luck_award',6,'2026-09-17 22:27:46','2026-10-15 11:12:10'),
    (7,100001,107,'增加dall-e-3画图模型','抽奖1次后解锁',200,200,0.0400,'tree_luck_award',7,'2026-09-17 22:28:18','2026-10-15 11:12:11'),
    (8,100001,108,'增加100次使用','抽奖2次后解锁',199,199,0.0099,'tree_luck_award',8,'2026-09-17 22:28:28','2026-10-15 11:12:13'),
    (9,100001,109,'解锁全部模型','抽奖6次后解锁',1,1,0.0001,'tree_luck_award',9,'2026-09-17 22:28:43','2026-10-15 11:12:15'),
    (10,100002,101,'随机积分',NULL,1,1,0.5000,'tree_luck_award',1,'2026-09-17 22:28:43','2026-10-15 11:12:16'),
    (11,100002,102,'5次使用',NULL,1,1,0.1000,'tree_luck_award',2,'2026-09-17 22:28:43','2026-10-15 11:12:18'),
    (12,100002,106,'增加dall-e-2画图模型',NULL,1,1,0.0100,'tree_luck_award',3,'2026-09-17 22:28:43','2026-10-15 11:12:19'),
    (13,100003,107,'增加dall-e-3画图模型','抽奖1次后解锁',200,200,0.0400,'tree_luck_award',7,'2026-09-17 22:28:18','2026-10-15 11:12:20'),
    (14,100003,108,'增加100次使用','抽奖2次后解锁',199,199,0.0099,'tree_luck_award',8,'2026-09-17 22:28:28','2026-10-15 11:12:21'),
    (15,100003,109,'解锁全部模型','抽奖6次后解锁',1,1,0.0001,'tree_luck_award',9,'2026-09-17 22:28:43','2026-10-15 11:12:22'),
    (16,100004,109,'解锁全部模型','抽奖6次后解锁',1,1,1.0000,'tree_luck_award',9,'2026-09-17 22:28:43','2026-10-15 11:12:23'),
    (17,100005,101,'随机积分',NULL,80000,80000,0.0300,'tree_luck_award',1,'2026-09-17 22:25:25','2026-10-15 11:12:24'),
    (18,100005,102,'随机积分',NULL,80000,80000,0.0300,'tree_luck_award',1,'2026-09-17 22:25:25','2026-10-15 11:12:24'),
    (19,100005,103,'随机积分',NULL,80000,80000,0.0300,'tree_luck_award',1,'2026-09-17 22:25:25','2026-10-15 11:12:25'),
    (20,100005,104,'随机积分',NULL,80000,80000,0.0300,'tree_luck_award',1,'2026-09-17 22:25:25','2026-10-15 11:12:25'),
    (21,100005,105,'随机积分',NULL,80000,80000,0.0010,'tree_luck_award',1,'2026-09-17 22:25:25','2026-10-15 11:12:26'),
    (22,100006,101,'随机积分',NULL,100,61,0.0200,'tree_luck_award',1,'2026-09-17 22:25:25','2026-11-13 18:05:15'),
    (23,100006,102,'OpenAI会员卡',NULL,100,19,0.0300,'tree_luck_award',2,'2026-09-17 22:25:25','2026-11-13 18:38:36'),
    (24,100006,103,'支付优惠券',NULL,100,43,0.0300,'tree_luck_award',3,'2026-09-17 22:25:25','2026-11-13 18:01:49'),
    (25,100006,104,'小米台灯',NULL,100,36,0.0300,'tree_luck_award',4,'2026-09-17 22:25:25','2026-11-13 18:38:40'),
    (26,100006,105,'小米su7周体验','抽奖3次后解锁',100,38,0.0300,'tree_lock_3',5,'2026-09-17 22:25:25','2026-11-13 17:53:10'),
    (27,100006,106,'轻奢办公椅','抽奖2次后解锁',100,25,0.0300,'tree_lock_2',6,'2026-09-17 22:25:25','2026-11-13 18:02:01'),
    (28,100006,107,'小霸王游戏机','抽奖1次后解锁',100,24,0.0300,'tree_lock_1',7,'2026-09-17 22:25:25','2026-11-13 18:04:39'),
    (29,100006,108,'暴走玩偶',NULL,100,33,0.0300,'tree_luck_award',8,'2026-09-17 22:25:25','2026-11-13 17:57:49');

/*!40000 ALTER TABLE `strategy_award` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 strategy_rule
# ------------------------------------------------------------

DROP TABLE IF EXISTS `strategy_rule`;

CREATE TABLE `strategy_rule` (
                                 `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                 `strategy_id` int(8) NOT NULL COMMENT '抽奖策略ID',
                                 `award_id` int(8) DEFAULT NULL COMMENT '抽奖奖品ID【规则类型为策略，则不需要奖品ID】',
                                 `rule_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '抽象规则类型；1-策略规则、2-奖品规则',
                                 `rule_model` varchar(16) NOT NULL COMMENT '抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】',
                                 `rule_value` varchar(256) NOT NULL COMMENT '抽奖规则比值',
                                 `rule_desc` varchar(128) NOT NULL COMMENT '抽奖规则描述',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_strategy_id_award_id` (`strategy_id`,`award_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖策略规则';

LOCK TABLES `strategy_rule` WRITE;
/*!40000 ALTER TABLE `strategy_rule` DISABLE KEYS */;

INSERT INTO `strategy_rule` (`id`, `strategy_id`, `award_id`, `rule_type`, `rule_model`, `rule_value`, `rule_desc`, `create_time`, `update_time`)
VALUES
    (13,100001,NULL,1,'rule_weight','4000:102,103,104,105 5000:102,103,104,105,106,107 6000:102,103,104,105,106,107,108,109','消耗6000分，必中奖范围','2026-09-17 22:46:34','2026-09-26 22:34:34'),
    (14,100001,NULL,1,'rule_blacklist','101:user001,user002,user003','黑名单抽奖，积分兜底','2026-09-17 23:46:59','2026-10-15 05:45:29');

/*!40000 ALTER TABLE `strategy_rule` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
