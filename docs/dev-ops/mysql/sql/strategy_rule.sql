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
