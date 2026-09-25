-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
--  Database: reservation_system
-- ------------------------------------------------------
-- Server version	8.0.44
--
-- Table structure for table `menus`
--

CREATE TABLE `menus` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL COMMENT 'メニュー名',
  `base_duration_minutes` int DEFAULT NULL COMMENT '標準所要時間',
  `base_price` int DEFAULT NULL COMMENT '標準料金',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;