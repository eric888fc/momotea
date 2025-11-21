CREATE DATABASE  IF NOT EXISTS `bigwork` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `bigwork`;
-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: bigwork
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '儲存雜湊後的密碼',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'BUYER',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `default_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `admin_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `admin_code` (`admin_code`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (3,'奸商','s1@gmail.com','$2a$10$dgjC6LR0WxbPS5fsCIHVG.ERBUH8ymbv6N4IjzO4zbJuyF41N0Z6u','SELLER','0912345678','高雄市','2025-11-03 08:35:32',NULL),(4,'雅買家','b1@gmail.com','$2a$10$BOC63u0wrLAhYplmMl/WGO6XVrcDghRJTwYO7CWNcRmHRx19AZbSu','BUYER','0912345678','taipei','2025-11-05 01:45:22',NULL),(9,'eeee','s2@gmail.com','$2a$10$160tyoa5IlXKf2NSDlcCjeHVgOrpmk25NpnrWQJ6FWYaS29.wX3oe','SELLER','0912345678','taipei','2025-11-10 01:38:11',NULL),(10,'666','b2@gmail.com','$2a$10$72OZsDAG/qysJr9p8POlougYwZZyLyWcAA/DckPDROqrIVGCP/UWy','BUYER','0912345678','台中','2025-11-12 09:32:02',NULL),(11,'奧客','b3@gmail.com','$2a$10$9kXPf.KTRlWGABZxFNjAIujMObU77tjOrNyJ.XQ0s7YPfHi9fU4nS','BUYER','0912345678','花蓮','2025-11-14 06:48:10',NULL),(16,'Admin','Admin01@gmail.com','$2a$10$winZjMMyiegDcQn9a7wNXOgcDYhtzBQV.lUav8pw/52WoFYGBwrmq','ADMIN','','台南','2025-11-14 07:47:50','ADM001'),(17,'Admin','Admin02@gmail.com','abc123456','ADMIN',NULL,NULL,'2025-11-20 07:09:45','ADM002'),(18,'Admin','Admin01+@mail.com','$2a$10$dfef7kLDdcdOXnelztHSVueDe4e/TZuVd1fxf.lhzpRTVrYvwSWyG','ADMIN','0987654321','TW','2025-11-20 07:30:38','ADM003');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-21 16:15:17
