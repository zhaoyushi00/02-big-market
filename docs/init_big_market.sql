# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20050
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: 127.0.0.1 (MySQL 5.6.39)
# 数据库: big_market
# 生成时间: 2024-02-15 05:21:27 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE database if NOT EXISTS `big_market` default character set utf8mb4 collate utf8mb4_0900_ai_ci;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `award` WRITE;
/*!40000 ALTER TABLE `award` DISABLE KEYS */;

INSERT INTO `award` (`id`, `award_id`, `award_key`, `award_config`, `award_desc`, `create_time`, `update_time`)
VALUES
	(1,101,'user_credit_random','1,100','用户积分【优先透彻规则范围，如果没有则走配置】','2023-12-09 11:07:06','2023-12-09 11:21:31'),
	(2,102,'openai_use_count','5','OpenAI 增加使用次数','2023-12-09 11:07:06','2023-12-09 11:12:59'),
	(3,103,'openai_use_count','10','OpenAI 增加使用次数','2023-12-09 11:07:06','2023-12-09 11:12:59'),
	(4,104,'openai_use_count','20','OpenAI 增加使用次数','2023-12-09 11:07:06','2023-12-09 11:12:58'),
	(5,105,'openai_model','gpt-4','OpenAI 增加模型','2023-12-09 11:07:06','2023-12-09 11:12:01'),
	(6,106,'openai_model','dall-e-2','OpenAI 增加模型','2023-12-09 11:07:06','2023-12-09 11:12:08'),
	(7,107,'openai_model','dall-e-3','OpenAI 增加模型','2023-12-09 11:07:06','2023-12-09 11:12:10'),
	(8,108,'openai_use_count','100','OpenAI 增加使用次数','2023-12-09 11:07:06','2023-12-09 11:12:55'),
	(9,109,'openai_model','gpt-4,dall-e-2,dall-e-3','OpenAI 增加模型','2023-12-09 11:07:06','2023-12-09 11:12:39'),
	(10,100,'user_credit_blacklist','1','黑名单积分','2024-01-06 12:30:40','2024-01-06 12:30:46');

/*!40000 ALTER TABLE `award` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `rule_tree` WRITE;
/*!40000 ALTER TABLE `rule_tree` DISABLE KEYS */;

INSERT INTO `rule_tree` (`id`, `tree_id`, `tree_name`, `tree_desc`, `tree_node_rule_key`, `create_time`, `update_time`)
VALUES
	(1,'tree_lock_1','规则树','规则树','rule_lock','2024-01-27 10:01:59','2024-02-15 07:49:59'),
	(2,'tree_luck_award','规则树-兜底奖励','规则树-兜底奖励','rule_stock','2024-02-15 07:35:06','2024-02-15 07:50:20'),
	(3,'tree_lock_2','规则树','规则树','rule_lock','2024-01-27 10:01:59','2024-02-15 07:49:59');

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `rule_tree_node` WRITE;
/*!40000 ALTER TABLE `rule_tree_node` DISABLE KEYS */;

INSERT INTO `rule_tree_node` (`id`, `tree_id`, `rule_key`, `rule_desc`, `rule_value`, `create_time`, `update_time`)
VALUES
	(1,'tree_lock_1','rule_lock','限定用户已完成N次抽奖后解锁','1','2024-01-27 10:03:09','2024-02-15 07:50:57'),
	(2,'tree_lock_1','rule_luck_award','兜底奖品随机积分','101:1,100','2024-01-27 10:03:09','2024-02-15 07:51:00'),
	(3,'tree_lock_1','rule_stock','库存扣减规则',NULL,'2024-01-27 10:04:43','2024-02-15 07:51:02'),
	(4,'tree_luck_award','rule_stock','库存扣减规则',NULL,'2024-02-15 07:35:55','2024-02-15 07:39:19'),
	(5,'tree_luck_award','rule_luck_award','兜底奖品随机积分','101:1,100','2024-02-15 07:35:55','2024-02-15 07:39:23'),
	(6,'tree_lock_2','rule_lock','限定用户已完成N次抽奖后解锁','2','2024-01-27 10:03:09','2024-02-15 07:52:20'),
	(7,'tree_lock_2','rule_luck_award','兜底奖品随机积分','101:1,100','2024-01-27 10:03:09','2024-02-08 19:59:43'),
	(8,'tree_lock_2','rule_stock','库存扣减规则',NULL,'2024-01-27 10:04:43','2024-02-03 10:40:21');

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `rule_tree_node_line` WRITE;
/*!40000 ALTER TABLE `rule_tree_node_line` DISABLE KEYS */;

INSERT INTO `rule_tree_node_line` (`id`, `tree_id`, `rule_node_from`, `rule_node_to`, `rule_limit_type`, `rule_limit_value`, `create_time`, `update_time`)
VALUES
	(1,'tree_lock_1','rule_lock','rule_stock','EQUAL','ALLOW','0000-00-00 00:00:00','2024-02-15 07:55:08'),
	(2,'tree_lock_1','rule_lock','rule_luck_award','EQUAL','TAKE_OVER','0000-00-00 00:00:00','2024-02-15 07:55:11'),
	(3,'tree_lock_1','rule_stock','rule_luck_award','EQUAL','ALLOW','0000-00-00 00:00:00','2024-02-15 07:55:13'),
	(4,'tree_luck_award','rule_stock','rule_luck_award','EQUAL','ALLOW','2024-02-15 07:37:31','2024-02-15 07:39:28'),
	(5,'tree_lock_2','rule_lock','rule_stock','EQUAL','ALLOW','0000-00-00 00:00:00','2024-02-15 07:55:08'),
	(6,'tree_lock_2','rule_lock','rule_luck_award','EQUAL','TAKE_OVER','0000-00-00 00:00:00','2024-02-15 07:55:11'),
	(7,'tree_lock_2','rule_stock','rule_luck_award','EQUAL','ALLOW','0000-00-00 00:00:00','2024-02-15 07:55:13');

/*!40000 ALTER TABLE `rule_tree_node_line` ENABLE KEYS */;
UNLOCK TABLES;
/*
 Navicat Premium Dump SQL

 Source Server         : User
 Source Server Type    : MySQL
 Source Server Version : 80405 (8.4.5)
 Source Host           : localhost:3306
 Source Schema         : big_market

 Target Server Type    : MySQL
 Target Server Version : 80405 (8.4.5)
 File Encoding         : 65001

 Date: 21/09/2026 15:43:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for strategy
-- ----------------------------
DROP TABLE IF EXISTS `strategy`;
CREATE TABLE `strategy`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `strategy_id` int NOT NULL COMMENT '抽奖策略ID',
  `strategy_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '抽奖策略描述',
  `rule_model` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '策略模型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of strategy
-- ----------------------------
INSERT INTO `strategy` VALUES (1, 10001, '抽奖策略A', NULL, '2026-09-19 16:06:03', '2026-09-19 16:06:03');

SET FOREIGN_KEY_CHECKS = 1;
/*
 Navicat Premium Dump SQL

 Source Server         : User
 Source Server Type    : MySQL
 Source Server Version : 80405 (8.4.5)
 Source Host           : localhost:3306
 Source Schema         : big_market

 Target Server Type    : MySQL
 Target Server Version : 80405 (8.4.5)
 File Encoding         : 65001

 Date: 21/09/2026 15:43:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for strategy_award
-- ----------------------------
DROP TABLE IF EXISTS `strategy_award`;
CREATE TABLE `strategy_award`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `strategy_id` int NOT NULL COMMENT '抽奖策略ID',
  `award_id` int NOT NULL COMMENT '抽奖奖品Id',
  `award_title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '抽奖奖品标题',
  `award_subtitle` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '抽奖奖品副标题',
  `award_count` int NOT NULL COMMENT '奖品库存总量',
  `award_count_surplus` int NOT NULL COMMENT '奖品库存剩余',
  `award_rate` decimal(6, 4) NOT NULL COMMENT '奖品中奖概率',
  `rule_models` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '规则模型，rule配置规则记录',
  `sort` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of strategy_award
-- ----------------------------
INSERT INTO `strategy_award` VALUES (1, 10001, 101, '随机积分', NULL, 80000, 80000, 80.0000, 'rule_random', 0000000001, '2026-09-19 16:07:04', '2026-09-19 16:07:04');
INSERT INTO `strategy_award` VALUES (2, 10001, 102, '5次使用', NULL, 10000, 10000, 10.0000, NULL, 0000000002, '2026-09-19 16:08:58', '2026-09-19 16:08:58');
INSERT INTO `strategy_award` VALUES (3, 10001, 103, '10次使用', NULL, 5000, 5000, 5.0000, NULL, 0000000003, '2026-09-19 16:09:43', '2026-09-19 16:09:43');
INSERT INTO `strategy_award` VALUES (4, 10001, 104, '20次使用', NULL, 4000, 4000, 4.0000, NULL, 0000000004, '2026-09-19 16:11:27', '2026-09-19 16:11:27');
INSERT INTO `strategy_award` VALUES (5, 10001, 105, '增加GPT-4对话模型', NULL, 600, 600, 0.6000, NULL, 0000000005, '2026-09-19 16:12:07', '2026-09-19 16:12:07');
INSERT INTO `strategy_award` VALUES (6, 10001, 106, '增加dall-e-2画图模型', '抽奖1次后解锁', 200, 200, 0.2000, NULL, 0000000006, '2026-09-19 16:13:00', '2026-09-19 16:13:00');
INSERT INTO `strategy_award` VALUES (7, 10001, 107, '增加dell-e-3对话模型', '抽奖2次后解锁', 199, 199, 0.1999, 'rule_luck_award,rule_lock', 0000000007, '2026-09-19 16:13:46', '2026-09-19 16:13:46');
INSERT INTO `strategy_award` VALUES (8, 10001, 108, '解锁全部模型', '抽奖6次后解锁', 1, 1, 0.0001, NULL, 0000000008, '2026-09-19 16:14:18', '2026-09-19 16:14:18');

SET FOREIGN_KEY_CHECKS = 1;
/*
 Navicat Premium Dump SQL

 Source Server         : User
 Source Server Type    : MySQL
 Source Server Version : 80405 (8.4.5)
 Source Host           : localhost:3306
 Source Schema         : big_market

 Target Server Type    : MySQL
 Target Server Version : 80405 (8.4.5)
 File Encoding         : 65001

 Date: 21/09/2026 15:43:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for strategy_rule
-- ----------------------------
DROP TABLE IF EXISTS `strategy_rule`;
CREATE TABLE `strategy_rule`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增Id',
  `strategy_id` int NOT NULL COMMENT '抽奖策略ID',
  `award_id` int NULL DEFAULT NULL COMMENT '抽奖奖品ID',
  `rule_type` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '抽奖规则类型【1-策略规则，2-奖品规则】',
  `rule_model` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '抽奖规则类型【rule_lock】',
  `rule_value` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '抽奖规则比值',
  `rule_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '抽奖规则描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of strategy_rule
-- ----------------------------
INSERT INTO `strategy_rule` VALUES (1, 10001, 101, 0000000002, 'rule_random', '1,1000', '随机积分策略', '2026-09-19 16:34:01', '2026-09-19 16:34:01');
INSERT INTO `strategy_rule` VALUES (2, 10001, 107, 0000000002, 'rule_lock', '1', '抽奖1次后解锁', '2026-09-19 16:48:57', '2026-09-19 16:48:57');
INSERT INTO `strategy_rule` VALUES (3, 10001, 108, 0000000002, 'rule_lock', '2', '抽奖2次后解锁', '2026-09-19 16:49:08', '2026-09-19 16:49:08');
INSERT INTO `strategy_rule` VALUES (4, 10001, 109, 0000000002, 'rule_lock', '6', '抽奖6次后解锁', '2026-09-19 16:49:16', '2026-09-19 16:49:16');
INSERT INTO `strategy_rule` VALUES (5, 10001, 107, 0000000002, 'rule_luck_award', '1,100', '随机积分兜底', '2026-09-19 18:07:49', '2026-09-19 18:07:49');
INSERT INTO `strategy_rule` VALUES (6, 10001, NULL, 0000000001, 'rule_weight', '6000:102,103,104,105,106,107,108,109', '礼品兜底', '2026-09-19 18:20:13', '2026-09-19 18:20:13');
INSERT INTO `strategy_rule` VALUES (7, 10001, NULL, 0000000001, 'rule_backlist', '1', '黑名单用户，1积分兜底', '2026-09-19 18:21:56', '2026-09-19 18:21:56');

SET FOREIGN_KEY_CHECKS = 1;
