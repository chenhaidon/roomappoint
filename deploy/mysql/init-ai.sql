-- AI 助手相关表

CREATE TABLE IF NOT EXISTS `systemconfig` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `CreationTime` timestamp NULL DEFAULT NULL,
  `CreatorId` int(11) NULL DEFAULT NULL,
  `ConfigKey` varchar(128) NOT NULL,
  `ConfigValue` text DEFAULT NULL,
  `Description` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`Id`),
  UNIQUE KEY `UK_SystemConfig_Key` (`ConfigKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `systemconfig` (`ConfigKey`, `ConfigValue`, `Description`) VALUES
('ai.api_url', 'https://api.anthropic.com', 'Anthropic API地址（支持中转站）'),
('ai.api_key', '', 'Anthropic API密钥'),
('ai.model', 'claude-sonnet-4-6-20250514', 'AI模型名称'),
('ai.enabled', '0', 'AI助手是否启用（0关闭/1开启）');

CREATE TABLE IF NOT EXISTS `chathistory` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `CreationTime` timestamp NULL DEFAULT NULL,
  `CreatorId` int(11) NULL DEFAULT NULL,
  `UserId` int(11) NOT NULL,
  `Role` varchar(32) NOT NULL,
  `Content` text NOT NULL,
  `ConversationId` varchar(64) NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `IDX_Chathistory_UserId` (`UserId`),
  KEY `IDX_Chathistory_ConversationId` (`ConversationId`),
  KEY `IDX_Chathistory_CreationTime` (`CreationTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户反馈表
CREATE TABLE IF NOT EXISTS `Feedback` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `CreationTime` timestamp NULL DEFAULT NULL,
  `CreatorId` int(11) DEFAULT NULL,
  `UserId` int(11) NOT NULL COMMENT '反馈用户ID',
  `Title` varchar(200) NOT NULL COMMENT '反馈标题',
  `Content` text NOT NULL COMMENT '反馈内容',
  `Status` int(11) NOT NULL DEFAULT 1 COMMENT '状态:1待处理,2已处理,3已关闭',
  `Reply` text DEFAULT NULL COMMENT '管理员回复',
  `ReplyTime` datetime DEFAULT NULL COMMENT '回复时间',
  PRIMARY KEY (`Id`),
  KEY `IDX_Feedback_UserId` (`UserId`),
  KEY `IDX_Feedback_Status` (`Status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
