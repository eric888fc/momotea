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
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `order_item_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '對應到 orders(order_id)',
  `product_id` bigint DEFAULT NULL COMMENT '對應到 products(product_id) (設為 NULL 以防商品被刪除)',
  `quantity` int NOT NULL COMMENT '購買數量',
  `price_per_unit` decimal(10,2) NOT NULL COMMENT '購買時的「單價」快照',
  PRIMARY KEY (`order_item_id`),
  KEY `fk_order_items_order_idx` (`order_id`),
  KEY `fk_order_items_product_idx` (`product_id`),
  CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂單明細 (商品快照)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1,NULL,10,99.99),(2,1,3,10,88.00),(3,2,5,4,30.00),(4,3,5,13,30.00),(5,3,4,20,50.00),(6,4,5,2,30.00),(7,4,3,2,88.00),(8,5,5,1,30.00),(9,6,6,5,30.00),(10,7,3,2,88.00),(11,8,6,3,30.00),(12,9,3,1,88.00),(13,10,6,1,30.00),(14,11,6,4,30.00),(15,12,9,5,10.00),(16,12,5,4,30.00),(17,13,10,3,20.00),(18,14,9,3,10.00),(19,15,6,3,30.00),(20,15,8,4,60.00),(21,16,12,5,40.00),(22,17,9,3,10.00),(23,18,7,3,30.00),(24,19,12,3,40.00),(25,20,13,3,20.00),(26,21,9,5,10.00),(27,22,11,2,100.00),(28,23,13,2,20.00),(29,24,12,2,40.00),(30,25,12,2,40.00),(33,28,9,3,10.00),(34,29,7,2,30.00),(35,30,3,3,88.00),(36,31,10,3,20.00),(37,32,8,4,60.00),(38,33,5,2,30.00),(39,34,13,3,20.00),(40,35,13,2,20.00),(41,36,14,3,40.00),(42,37,13,3,20.00);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-26  9:26:16
