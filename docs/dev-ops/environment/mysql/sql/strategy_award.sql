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
