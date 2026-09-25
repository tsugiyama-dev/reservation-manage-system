-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
--  Database: reservation_system
-- ------------------------------------------------------
-- Server version	8.0.44

--
-- Table structure for table `reservations`
--

CREATE TABLE `reservations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `customer_id` bigint DEFAULT NULL,
  `stylist_id` bigint DEFAULT NULL,
  `menu_id` bigint DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `status` enum('PENDING','CONFIRMED','REJECTED','CANCELLED') DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `customer_id` (`customer_id`),
  KEY `stylist_id` (`stylist_id`),
  KEY `menu_id` (`menu_id`),
  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`stylist_id`) REFERENCES `stylists` (`user_id`),
  CONSTRAINT `reservations_ibfk_3` FOREIGN KEY (`menu_id`) REFERENCES `menus` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
