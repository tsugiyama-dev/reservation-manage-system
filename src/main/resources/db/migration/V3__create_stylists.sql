-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
--  Database: reservation_system
-- ------------------------------------------------------
-- Server version	8.0.44
--
-- Table structure for table `stylists`
--
CREATE TABLE `stylists` (
  `user_id` bigint NOT NULL,
  `bio` text COMMENT ' 自己紹介など',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `stylists_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

