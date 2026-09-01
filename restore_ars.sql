SET NAMES utf8mb4;
USE asterisk;

DROP TABLE IF EXISTS `ars_scripts`;
CREATE TABLE `ars_scripts` (
  `id` varchar(40) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `script_text` text,
  `upd_user` varchar(40) DEFAULT NULL,
  `upd_dtime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `ars_scripts` (id, description, script_text) VALUES
 ('01_welcome', '기본 인사말', '안녕하세요. 씨알엠뱅크입니다.'),
 ('02_select_menu', '부서 선택 안내', '영업팀은 1번, 기술팀은 2번을 눌러주세요.'),
 ('05_callback_confirm', '콜백 접수 확인', '상담 예약이 접수되었습니다. 곧 연락드리겠습니다.'),
 ('07_company_info', '회사 정보 안내', '씨알엠뱅크는 경기도 안산시에 위치하고 있습니다.'),
 ('08_night_greeting', '업무 종료 안내', '지금은 업무 종료 시간입니다. 내일 다시 전화주세요.');
