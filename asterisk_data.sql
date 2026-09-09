-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: asterisk
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ars_scripts`
--

DROP TABLE IF EXISTS `ars_scripts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ars_scripts` (
  `id` varchar(40) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `script_text` text,
  `upd_user` varchar(40) DEFAULT NULL,
  `upd_dtime` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ars_scripts`
--

LOCK TABLES `ars_scripts` WRITE;
/*!40000 ALTER TABLE `ars_scripts` DISABLE KEYS */;
INSERT INTO `ars_scripts` VALUES ('01_welcome','기본 인사말','안녕하세요. 정성을 다하는 씨알엠뱅크입니다.','admin','2026-08-30 06:34:07'),('02_select_menu','부서 선택 안내','영업팀은 1번, 기술팀은 2번을 눌러주세요.','admin','2026-08-30 06:34:07'),('05_callback_confirm','콜백 접수 확인','상담 예약이 접수되었습니다. 곧 연락드리겠습니다.','admin','2026-08-30 06:34:07'),('07_company_info','회사 정보 안내','씨알엠뱅크는 경기도 안산시에 위치하고 있습니다.','admin','2026-08-30 06:34:07'),('08_night_greeting','업무 종료 안내','지금은 업무 종료 시간입니다. 내일 다시 전화주세요.','admin','2026-08-30 06:34:07');
/*!40000 ALTER TABLE `ars_scripts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cdr`
--

DROP TABLE IF EXISTS `cdr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cdr` (
  `id` int NOT NULL AUTO_INCREMENT,
  `calldate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `clid` varchar(80) NOT NULL DEFAULT '',
  `src` varchar(80) NOT NULL DEFAULT '',
  `dst` varchar(80) NOT NULL DEFAULT '',
  `dcontext` varchar(80) NOT NULL DEFAULT '',
  `channel` varchar(80) NOT NULL DEFAULT '',
  `dstchannel` varchar(80) NOT NULL DEFAULT '',
  `lastapp` varchar(80) NOT NULL DEFAULT '',
  `lastdata` varchar(80) NOT NULL DEFAULT '',
  `duration` int NOT NULL DEFAULT '0',
  `billsec` int NOT NULL DEFAULT '0',
  `disposition` varchar(45) NOT NULL DEFAULT '',
  `amaflags` int NOT NULL DEFAULT '0',
  `accountcode` varchar(20) NOT NULL DEFAULT '',
  `uniqueid` varchar(32) NOT NULL DEFAULT '',
  `userfield` varchar(255) NOT NULL DEFAULT '',
  `sequence` int NOT NULL DEFAULT '0',
  `peeraccount` varchar(20) NOT NULL DEFAULT '',
  `linkedid` varchar(32) NOT NULL DEFAULT '',
  `recordingfile` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cdr`
--

LOCK TABLES `cdr` WRITE;
/*!40000 ALTER TABLE `cdr` DISABLE KEYS */;
/*!40000 ALTER TABLE `cdr` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `extensions`
--

DROP TABLE IF EXISTS `extensions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `extensions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `context` varchar(40) DEFAULT NULL,
  `exten` varchar(40) DEFAULT NULL,
  `priority` int DEFAULT NULL,
  `app` varchar(40) DEFAULT NULL,
  `appdata` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extensions`
--

LOCK TABLES `extensions` WRITE;
/*!40000 ALTER TABLE `extensions` DISABLE KEYS */;
INSERT INTO `extensions` VALUES (1,'from-internal','999',1,'Ringing',''),(2,'from-internal','999',2,'Wait','2'),(3,'from-internal','999',3,'Set','MODE=OPEN'),(4,'from-internal','999',4,'GotoIf','$[\"${MODE}\" = \"OPEN\"]?ivr-main,s,1:after-hours,s,1'),(13,'after-hours','s',1,'Answer',''),(14,'after-hours','s',2,'Playback','/var/lib/asterisk/sounds/custom/08_night_greeting'),(15,'after-hours','s',3,'Hangup',''),(18,'from-internal','999',3,'Answer',''),(19,'from-internal','103',1,'Dial','PJSIP/103'),(20,'from-internal','103',1,'Dial','PJSIP/103'),(85,'ivr-main','s',1,'Answer',''),(86,'ivr-main','s',2,'Background','/var/lib/asterisk/sounds/custom/01_welcome'),(87,'ivr-main','s',3,'Background','/var/lib/asterisk/sounds/custom/02_select_menu'),(88,'ivr-main','s',4,'WaitExten','10'),(89,'ivr-main','1',1,'NoOp','Sales Selection Recognized'),(90,'ivr-main','1',2,'Queue','sales_group'),(91,'ivr-main','2',1,'NoOp','Support Selection Recognized'),(92,'ivr-main','2',2,'Queue','support_group');
/*!40000 ALTER TABLE `extensions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `global_vars`
--

DROP TABLE IF EXISTS `global_vars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `global_vars` (
  `var_name` varchar(40) NOT NULL,
  `var_value` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`var_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `global_vars`
--

LOCK TABLES `global_vars` WRITE;
/*!40000 ALTER TABLE `global_vars` DISABLE KEYS */;
INSERT INTO `global_vars` VALUES ('BUSINESS_MODE','OPEN');
/*!40000 ALTER TABLE `global_vars` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ps_aors`
--

DROP TABLE IF EXISTS `ps_aors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ps_aors` (
  `id` varchar(40) NOT NULL,
  `max_contacts` int DEFAULT NULL,
  `remove_existing` varchar(3) DEFAULT NULL,
  `qualify_frequency` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ps_aors`
--

LOCK TABLES `ps_aors` WRITE;
/*!40000 ALTER TABLE `ps_aors` DISABLE KEYS */;
INSERT INTO `ps_aors` VALUES ('101',5,'yes',30),('102',5,'yes',30),('103',5,'yes',30),('121',5,'yes',30),('122',5,'yes',30),('131',5,'yes',30),('132',5,'yes',30);
/*!40000 ALTER TABLE `ps_aors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ps_auths`
--

DROP TABLE IF EXISTS `ps_auths`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ps_auths` (
  `id` varchar(40) NOT NULL,
  `auth_type` varchar(40) DEFAULT NULL,
  `password` varchar(80) DEFAULT NULL,
  `username` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ps_auths`
--

LOCK TABLES `ps_auths` WRITE;
/*!40000 ALTER TABLE `ps_auths` DISABLE KEYS */;
INSERT INTO `ps_auths` VALUES ('101','userpass','101password','101'),('102','userpass','102password','102'),('103','userpass','103password','103'),('121','userpass','121password','121'),('122','userpass','122password','122'),('131','userpass','131password','131'),('132','userpass','132password','132');
/*!40000 ALTER TABLE `ps_auths` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ps_contacts`
--

DROP TABLE IF EXISTS `ps_contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ps_contacts` (
  `id` varchar(255) NOT NULL,
  `uri` varchar(255) DEFAULT NULL,
  `expiration_time` varchar(40) DEFAULT NULL,
  `qualify_frequency` int DEFAULT NULL,
  `outbound_proxy` varchar(40) DEFAULT NULL,
  `path` text,
  `user_agent` varchar(255) DEFAULT NULL,
  `qualify_timeout` float DEFAULT NULL,
  `reg_server` varchar(40) DEFAULT NULL,
  `authenticate_qualify` varchar(3) DEFAULT NULL,
  `via_addr` varchar(40) DEFAULT NULL,
  `via_port` int DEFAULT NULL,
  `call_id` varchar(255) DEFAULT NULL,
  `endpoint` varchar(40) DEFAULT NULL,
  `prune_on_boot` varchar(3) DEFAULT NULL,
  `qualify_2xx_only` varchar(3) DEFAULT 'yes',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ps_contacts`
--

LOCK TABLES `ps_contacts` WRITE;
/*!40000 ALTER TABLE `ps_contacts` DISABLE KEYS */;
/*!40000 ALTER TABLE `ps_contacts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ps_endpoints`
--

DROP TABLE IF EXISTS `ps_endpoints`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ps_endpoints` (
  `id` varchar(40) NOT NULL,
  `transport` varchar(40) DEFAULT NULL,
  `aors` varchar(40) DEFAULT NULL,
  `auth` varchar(40) DEFAULT NULL,
  `context` varchar(40) DEFAULT NULL,
  `disallow` varchar(200) DEFAULT NULL,
  `allow` varchar(200) DEFAULT NULL,
  `callerid` varchar(80) DEFAULT NULL,
  `dtmf_mode` varchar(40) DEFAULT 'rfc4733',
  `rewrite_contact` enum('yes','no') DEFAULT 'yes',
  `force_rport` enum('yes','no') DEFAULT 'yes',
  `direct_media` enum('yes','no') DEFAULT 'no',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ps_endpoints`
--

LOCK TABLES `ps_endpoints` WRITE;
/*!40000 ALTER TABLE `ps_endpoints` DISABLE KEYS */;
INSERT INTO `ps_endpoints` VALUES ('101','transport-udp','101','101','from-internal','all','ulaw,alaw','상담원101','rfc4733','yes','yes','no'),('102','transport-udp','102','102','from-internal','all','ulaw,alaw','상담원102','rfc4733','yes','yes','no'),('103','transport-udp','103','103','from-internal','all','ulaw,alaw','상담원103','rfc4733','yes','yes','no'),('121','transport-udp','121','121','from-internal','all','ulaw,alaw','상담원121','rfc4733','yes','yes','no'),('122','transport-udp','122','122','from-internal','all','ulaw,alaw','상담원122','rfc4733','yes','yes','no'),('131','transport-udp','131','131','from-internal','all','ulaw,alaw','상담원131','rfc4733','yes','yes','no'),('132','transport-udp','132','132','from-internal','all','ulaw,alaw','상담원132','rfc4733','yes','yes','no');
/*!40000 ALTER TABLE `ps_endpoints` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ps_endpoints_custom`
--

DROP TABLE IF EXISTS `ps_endpoints_custom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ps_endpoints_custom` (
  `id` varchar(40) NOT NULL,
  `record_yn` varchar(1) DEFAULT 'Y',
  `mobile_no` varchar(20) DEFAULT NULL,
  `routing_mode` varchar(2) DEFAULT '10',
  `status` varchar(2) DEFAULT '10',
  `duty_yn` varchar(1) DEFAULT 'N',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ps_endpoints_custom`
--

LOCK TABLES `ps_endpoints_custom` WRITE;
/*!40000 ALTER TABLE `ps_endpoints_custom` DISABLE KEYS */;
INSERT INTO `ps_endpoints_custom` VALUES ('101','Y',NULL,'10','10','N'),('102','Y',NULL,'10','10','N'),('103','Y',NULL,'10','10','N'),('121','Y',NULL,'10','10','N'),('122','Y',NULL,'10','10','N'),('131','Y',NULL,'10','10','N'),('132','Y',NULL,'10','10','N');
/*!40000 ALTER TABLE `ps_endpoints_custom` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ps_transports`
--

DROP TABLE IF EXISTS `ps_transports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ps_transports` (
  `id` varchar(40) NOT NULL,
  `bind` varchar(40) DEFAULT NULL,
  `protocol` varchar(40) DEFAULT NULL,
  `external_signaling_address` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ps_transports`
--

LOCK TABLES `ps_transports` WRITE;
/*!40000 ALTER TABLE `ps_transports` DISABLE KEYS */;
INSERT INTO `ps_transports` VALUES ('transport-udp','0.0.0.0:5060','udp','172.30.1.11');
/*!40000 ALTER TABLE `ps_transports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `queue_members`
--

DROP TABLE IF EXISTS `queue_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `queue_members` (
  `uniqueid` int NOT NULL AUTO_INCREMENT,
  `queue_name` varchar(128) NOT NULL,
  `interface` varchar(128) NOT NULL,
  `membername` varchar(128) DEFAULT NULL,
  `state_interface` varchar(128) DEFAULT NULL,
  `penalty` int DEFAULT '0',
  `paused` int DEFAULT '0',
  `wrapuptime` int DEFAULT '0',
  PRIMARY KEY (`uniqueid`),
  KEY `queue_name` (`queue_name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `queue_members`
--

LOCK TABLES `queue_members` WRITE;
/*!40000 ALTER TABLE `queue_members` DISABLE KEYS */;
INSERT INTO `queue_members` VALUES (2,'8001','PJSIP/121','상담원121',NULL,0,0,0),(3,'8002','PJSIP/131','상담원131',NULL,0,0,0),(4,'8000','PJSIP/101','상담원101',NULL,0,0,0);
/*!40000 ALTER TABLE `queue_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `queues`
--

DROP TABLE IF EXISTS `queues`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `queues` (
  `name` varchar(128) NOT NULL,
  `strategy` varchar(128) DEFAULT NULL,
  `timeout` int DEFAULT NULL,
  `ringinuse` varchar(3) DEFAULT NULL,
  `context` varchar(128) DEFAULT NULL,
  `musiconhold` varchar(128) DEFAULT NULL,
  `failover_dest` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `queues`
--

LOCK TABLES `queues` WRITE;
/*!40000 ALTER TABLE `queues` DISABLE KEYS */;
INSERT INTO `queues` VALUES ('8000','linear',15,'no','from-internal','default',NULL),('8001','linear',15,'no','from-internal','default',NULL),('8002','linear',15,'no','from-internal','default',NULL);
/*!40000 ALTER TABLE `queues` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-06 14:23:04
