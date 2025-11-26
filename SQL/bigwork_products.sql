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
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `seller_id` bigint NOT NULL,
  `category_id` int NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `price` decimal(10,2) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`),
  KEY `fk_products_users_idx` (`seller_id`),
  KEY `fk_products_categories_idx` (`category_id`),
  CONSTRAINT `fk_products_categories` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_products_users` FOREIGN KEY (`seller_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (3,3,4,'粉塵','這是一個低品質的商品',88.00,2,'/uploads/60e59636-f44e-443d-a181-2944f3ffd64e.png','2025-11-05 08:28:44','2025-11-25 03:06:28'),(4,3,3,'軟糖','11111',50.00,100,'/uploads/96ed2859-dde3-44bc-a341-06ee1540caa0.png','2025-11-10 06:50:14','2025-11-23 13:51:22'),(5,3,3,'薯片','22222',30.00,34,'/uploads/465b9841-92b3-4380-b152-86ccb2704055.png','2025-11-10 07:26:42','2025-11-25 06:47:19'),(6,9,8,'大綠茶','很綠',30.00,50,'/uploads/a1398940-0d13-4b5d-8291-491f53432101.png','2025-11-11 09:20:41','2025-11-25 06:51:29'),(7,9,8,'熱飲','很燙',30.00,45,'/uploads/808736d5-3c77-4761-88ce-0a96be98eec1.png','2025-11-14 02:27:42','2025-11-25 02:53:57'),(8,9,5,'油泥蝦','生猛海鮮',60.00,42,'/uploads/cb5959c6-5117-40cb-959f-8887672bd660.png','2025-11-14 02:28:23','2025-11-25 03:23:57'),(9,3,6,'箱子','很大的箱子',10.00,11,'/uploads/ed073a63-ceb8-4abc-9be7-01fdd746e3eb.png','2025-11-14 06:42:54','2025-11-24 09:17:08'),(10,16,5,'大蒜','味道很重的大蒜',20.00,54,'/uploads/b6c49e53-e1ea-4f22-bb46-a9695f7c95f2.png','2025-11-14 07:48:50','2025-11-26 01:24:38'),(11,3,6,'77','1111111',100.00,28,'/uploads/ec66d9f2-4cc6-4dff-bdbb-76736afd3107.png','2025-11-17 02:12:06','2025-11-23 16:23:05'),(12,16,6,'桶子','很大的桶子',40.00,37,'/uploads/b30afa9f-3b78-49f1-abfd-e37161fb8ce5.png','2025-11-17 02:40:30','2025-11-24 09:09:29'),(13,16,5,'肉乾','1111111111',20.00,16,'/uploads/7e39e3f3-6e73-43e5-b769-5ca351bfa635.png','2025-11-19 04:00:16','2025-11-25 08:27:38'),(14,9,3,'起司','多力多滋',40.00,47,'/uploads/cbd71bde-0cd6-4795-885f-cc4b0323b09f.png','2025-11-25 06:52:30','2025-11-25 07:35:59');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-26  9:26:17
