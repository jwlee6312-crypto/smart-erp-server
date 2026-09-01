SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- [1] ARS 안내멘트 시나리오
DROP TABLE IF EXISTS `ars_scripts`;
CREATE TABLE `ars_scripts` (
  `id` varchar(40) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `script_text` text,
  `upd_user` varchar(40) DEFAULT NULL,
  `upd_dtime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `ars_scripts` VALUES
 ('01_welcome','기본 인사말','안녕하세요. 씨알엠뱅크입니다.',NULL,'2026-08-28 04:45:53'),
 ('02_select_menu','부서 선택 안내','영업팀은 1번, 기술팀은 2번을 눌러주세요.',NULL,'2026-08-28 04:45:53'),
 ('05_callback_confirm','콜백 접수 확인','상담 예약이 접수되었습니다. 곧 연락드리겠습니다.',NULL,'2026-08-28 04:45:53'),
 ('07_company_info','회사 정보 안내','씨알엠뱅크는 경기도 안산시에 위치하고 있습니다.',NULL,'2026-08-28 04:45:53'),
 ('08_night_greeting','업무 종료 안내','지금은 업무 종료 시간입니다. 내일 다시 전화주세요.',NULL,'2026-08-28 04:45:53');

-- [2] 다이얼플랜 및 인바운드 로직
DROP TABLE IF EXISTS `extensions`;
CREATE TABLE `extensions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `context` varchar(40) DEFAULT NULL,
  `exten` varchar(40) DEFAULT NULL,
  `priority` int DEFAULT NULL,
  `app` varchar(40) DEFAULT NULL,
  `appdata` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4;
INSERT INTO `extensions` VALUES
 (1,'from-internal','999',1,'Set','CALLERID(num)=01032043901'),
 (2,'from-internal','999',2,'Answer',''),
 (3,'from-internal','999',3,'Set','MODE=${TRIM(${REALTIME_FIELD(global_vars,var_name,BUSINESS_MODE,var_value)})}'),
 (4,'from-internal','999',4,'GotoIf','$[\"${MODE}\" = \"OPEN\"]?ivr-main,s,1'),
 (5,'from-internal','999',5,'Goto','after-hours,s,1'),
 (6,'ivr-main','s',1,'Answer',''),
 (7,'ivr-main','s',2,'Background','custom/01_welcome'),
 (8,'ivr-main','s',3,'Background','custom/02_select_menu'),
 (9,'ivr-main','s',4,'WaitExten','5'),
 (10,'ivr-main','1',1,'Queue','8000,tT'),
 (11,'ivr-main','_1XX',1,'Dial','PJSIP/${EXTEN},20,tT'),
 (12,'after-hours','s',1,'Playback','custom/08_night_greeting'),
 (13,'after-hours','s',2,'Hangup','');

-- [3] 전역 변수
DROP TABLE IF EXISTS `global_vars`;
CREATE TABLE `global_vars` (
  `var_name` varchar(40) NOT NULL,
  `var_value` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`var_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `global_vars` VALUES ('BUSINESS_MODE','OPEN');

-- [4] PJSIP 인프라 전체
DROP TABLE IF EXISTS `ps_aors`, `ps_auths`, `ps_endpoints`, `ps_endpoints_custom`, `ps_transports`, `ps_contacts`;
CREATE TABLE `ps_aors` (id varchar(40) PRIMARY KEY, max_contacts int, remove_existing varchar(3), qualify_frequency int) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `ps_aors` VALUES ('101',5,'yes',30),('102',5,'yes',30),('103',5,'yes',30),('121',5,'yes',30),('122',5,'yes',30),('131',5,'yes',30),('132',5,'yes',30);

CREATE TABLE `ps_auths` (id varchar(40) PRIMARY KEY, auth_type varchar(40), password varchar(80), username varchar(80)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `ps_auths` VALUES ('101','userpass','Crmbank123!','101'),('102','userpass','Crmbank123!','102'),('103','userpass','Crmbank123!','103'),('121','userpass','Crmbank123!','121'),('122','userpass','Crmbank123!','122'),('131','userpass','Crmbank123!','131'),('132','userpass','Crmbank123!','132');

CREATE TABLE `ps_endpoints` (id varchar(40) PRIMARY KEY, transport varchar(40), aors varchar(40), auth varchar(40), context varchar(40), disallow varchar(200), allow varchar(200), callerid varchar(80)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `ps_endpoints` VALUES ('101','transport-udp','101','101','from-internal','all','ulaw,alaw,gsm','상담원101'),('102','transport-udp','102','102','from-internal','all','ulaw,alaw,gsm','상담원102'),('103','transport-udp','103','103','from-internal','all','ulaw,alaw,gsm','상담원103'),('121','transport-udp','121','121','from-internal','all','ulaw,alaw,gsm','상담원121'),('122','transport-udp','122','122','from-internal','all','ulaw,alaw,gsm','상담원122'),('131','transport-udp','131','131','from-internal','all','ulaw,alaw,gsm','상담원131'),('132','transport-udp','132','132','from-internal','all','ulaw,alaw,gsm','상담원132');

CREATE TABLE `ps_endpoints_custom` (id varchar(40) PRIMARY KEY, record_yn varchar(1) DEFAULT 'Y', mobile_no varchar(20), routing_mode varchar(2) DEFAULT '10', status varchar(2) DEFAULT '10', duty_yn varchar(1) DEFAULT 'N') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `ps_endpoints_custom` (id) VALUES ('101'),('102'),('103'),('121'),('122'),('131'),('132');

CREATE TABLE `ps_transports` (id varchar(40) PRIMARY KEY, bind varchar(40), protocol varchar(40)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `ps_transports` VALUES ('transport-udp','0.0.0.0:5060','udp');

-- [5] 큐(Queue) 및 멤버 전체 (요청하신 부분)
DROP TABLE IF EXISTS `queues`;
CREATE TABLE `queues` (
  `name` varchar(128) NOT NULL,
  `strategy` varchar(128) DEFAULT NULL,
  `timeout` int DEFAULT NULL,
  `ringinuse` varchar(3) DEFAULT NULL,
  `context` varchar(128) DEFAULT NULL,
  `musiconhold` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `queues` VALUES 
 ('8000','linear',15,'no','from-internal','default'),
 ('8001','linear',15,'no','from-internal','default'),
 ('8002','linear',15,'no','from-internal','default');

DROP TABLE IF EXISTS `queue_members`;
CREATE TABLE `queue_members` (
  `interface` varchar(128) NOT NULL,
  `queue_name` varchar(128) NOT NULL,
  `membername` varchar(128) DEFAULT NULL,
  `penalty` int DEFAULT NULL,
  PRIMARY KEY (`interface`,`queue_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO `queue_members` VALUES 
 ('PJSIP/101','8000','상담원101',1), ('PJSIP/102','8000','상담원102',2),
 ('PJSIP/121','8001','상담원121',1), ('PJSIP/122','8001','상담원122',2),
 ('PJSIP/131','8002','상담원131',1), ('PJSIP/132','8002','상담원132',2);

SET FOREIGN_KEY_CHECKS=1;
