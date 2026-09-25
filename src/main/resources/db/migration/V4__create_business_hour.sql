-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Database: reservation_system
-- ------------------------------------------------------
-- Server version	8.0.44

--
-- Table structure for table `business_hour`
--

CREATE TABLE `business_hour` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stylist_id` bigint NOT NULL,
  `day_of_week` enum('Mon','Tue','Wed','Thu','Fri','Sat','Sun') DEFAULT NULL COMMENT '曜日',
  `start_time` time DEFAULT NULL,
  `end_time` time DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `business_hour_ibfk_1` (`stylist_id`),
  CONSTRAINT `business_hour_ibfk_1` FOREIGN KEY (`stylist_id`) REFERENCES `stylists` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
