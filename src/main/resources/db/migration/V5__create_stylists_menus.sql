-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Database: reservation_system
-- ------------------------------------------------------
-- Server version	8.0.44
--
-- Table structure for table `stylists_menus`
--

CREATE TABLE `stylists_menus` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stylist_id` bigint DEFAULT NULL,
  `menu_id` bigint DEFAULT NULL,
  `duration_minutes` int DEFAULT NULL COMMENT 'スタイリストの所要時間',
  `price` int DEFAULT NULL COMMENT 'スタイリストの料金',
  PRIMARY KEY (`id`),
  UNIQUE KEY `stylist_id` (`stylist_id`,`menu_id`),
  KEY `menu_id` (`menu_id`),
  CONSTRAINT `stylists_menus_ibfk_1` FOREIGN KEY (`stylist_id`) REFERENCES `stylists` (`user_id`),
  CONSTRAINT `stylists_menus_ibfk_2` FOREIGN KEY (`menu_id`) REFERENCES `menus` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
