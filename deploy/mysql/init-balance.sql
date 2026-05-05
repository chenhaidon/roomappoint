-- 用户余额功能数据库变更

-- 1. appuser 表新增余额字段
ALTER TABLE `appuser` ADD COLUMN `Balance` decimal(10,2) NOT NULL DEFAULT 0 COMMENT '用户余额';

-- 2. 余额流水表
CREATE TABLE IF NOT EXISTS `balance_record` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `CreationTime` timestamp NULL DEFAULT NULL,
  `CreatorId` int(11) DEFAULT NULL,
  `UserId` int(11) NOT NULL COMMENT '用户ID',
  `Amount` decimal(10,2) NOT NULL COMMENT '变动金额(正=充值,负=消费)',
  `Source` varchar(64) DEFAULT NULL COMMENT '来源: 充值/礼品兑换/管理员调整',
  `RelativeCode` varchar(128) DEFAULT NULL COMMENT '关联号(订单号/兑换ID)',
  `Title` varchar(128) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`Id`),
  KEY `idx_user` (`UserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 支付订单表
CREATE TABLE IF NOT EXISTS `pay_order` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `CreationTime` timestamp NULL DEFAULT NULL,
  `CreatorId` int(11) DEFAULT NULL,
  `UserId` int(11) NOT NULL COMMENT '用户ID',
  `OrderNo` varchar(64) NOT NULL COMMENT '订单号',
  `Amount` decimal(10,2) NOT NULL COMMENT '充值金额',
  `Status` varchar(20) NOT NULL DEFAULT '待支付' COMMENT '待支付/已支付/已取消',
  `PayTime` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `TradeNo` varchar(64) DEFAULT NULL COMMENT '支付宝交易号',
  PRIMARY KEY (`Id`),
  UNIQUE KEY `uk_orderno` (`OrderNo`),
  KEY `idx_user` (`UserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. GiftRedeem 表新增扣减明细字段
ALTER TABLE `GiftRedeem` ADD COLUMN `DeductIntegral` int(11) DEFAULT 0 COMMENT '实际扣减积分';
ALTER TABLE `GiftRedeem` ADD COLUMN `DeductBalance` decimal(10,2) DEFAULT 0 COMMENT '实际扣减余额';

-- 5. 插入支付宝沙箱配置
INSERT INTO `systemconfig` (`ConfigKey`, `ConfigValue`, `Remark`) VALUES
('alipay.appid', '9021000162673485', '支付宝应用ID(沙箱)'),
('alipay.privateKey', 'MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCI2TpEqCDdeKbWodMWy352kQLML2VNMXoVNsamVs22SEgMhw5/UxZTS7AujmtoYuI4dudy0RowwCHvAzwjp4dxxEJFm2q7bjwsUvAktDy0H5HBuQaEj0gm4l0RX9QNl2Exy/oXfVGqzsbnuDH7nSB3uEhMvb5shz7jQos8oeNGJPimeVV0Vlrhxu5dIQMC7bneefUGR8zY4gqkz+MtNZ02Ezas7YlAKuX6lOWl2z4Mqz24YZLd2951prpg+JAd1Sy9jobKXXOqQeSw5ZcXuywNo/zZuuaCjZwq4c6BE27x0emhrAOho8Lw8MU/BKITUGhq2V3Y5BeWeStW1PPGaaGPAgMBAAECggEAbHIwwb22pat6EzaUUEkcw5rw65xXm8+snmkVDTZWuLAZIIxaib3vQD0Hti04orVR7ksT6SIZri5j9cPLgbm+FQLhPcYG09dsqfdjxMJsJJWr7SfSKXReSioydJVLNyYSaOLrIng2IrTQXUrTmUCQPS91ZBMtd1XKGVBfhMIJmEh+2XWhrwV7UYhYuKFmPVrZpBympcnxqFX8G5zuu6kHUccjLsVDTEc1pmO1mm6SYgxNaRRzLa6+9nlyr0ei7LQZRz371f6gXC1xNaBKiae6VaeD+kOEabPG8qSQJ3Az3nMPpob/sa/vgKCEz8POs7qlBZFjzP/VpccxGfATA4ee+QKBgQDnG7jy1MXuMQoGYYyj6MtCjOBqs40oUbB/DHww6jbVtmOAg9RXLqitK+gpyfYYS+wMQ/bo0wC2Cu6UMpSA7aRoZd5477+xFMzBo0FDjh4gvBtLJE1PDlPQ0wkoeyE2AjsVQUgmn06Xd+zVOOkzsBrsll+vhuzx1Nr9jb6yCsc3BQKBgQCXloKizwHcBafSlIDw6kQv4SXJx66kiV053EGu5aQ9OETxx7bVENc6Gw5zejJdam64mkxtUEPbTYL2PaPtLME0HwZa1sOk/BXVR4iI0qiVZ3/X090bfM08B0U7FVkpwWwaOxUZEYlKZ+ViTo3WJnlcTTC59MjuxIRetSHsrQuygwKBgDTJTzgfw0nBZ7HHbDEwBeUgek3EzxQLfI24U1F03at4qkd7VO/I13weUDvjRlOMW9D9YRMSxtHNs2qkFulP3kJ+Kg9ja1xODOqZmFmOx3DgpjdGhmXtNtW2rhvR6+cDIW2caJdxwai1GqIlCpbkvXGWcyqLXelqnc5WJt1RuujlAoGBAIZf3nfSRxSyUr1B9Tcag81pqSTAhNjIB8MVs4gFe1zld4BYiINBRXj1qQ7GoMTcZz/vT5aLU0ug31nG9Hx3cuDwK6L0xEOJTxVcLvUSwGyIbfR1k4gdTpVK4JzqZLq2Ak0SWLZ7LBewF8/YULUoL+aZans9Oa0muSx3JpZ2I2tfAoGBAJsuRf5FoYx7KgaGV5HaKywXMU2qidqZturzxAw/o2jQNku1+bYZN7tgo73nIydEEl9I39zuDdC7reAR4ks7Yu4c4w7Oo0nnDBXxW/x66C2bmdHag0xqpzNGXXBl59IriuJ5Or0YKb7h9vxtgdiU83UDrfxN/sUWiuqo0wynSgi1', '支付宝应用私钥(沙箱)'),
('alipay.alipayPublicKey', 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr099c4zggu0ljrRyNIK/JFsqo4untPRZ2yc8tRaBbOtiG8ydTV16teU2mry09NiRx8LqdlHs7UqaiqlDG4GdTq33whGkttduJFy5wHep13NaIFyyMUKze5cwJBYEXLySAssD48EJE0QVmUI0OZNiriyVOCMfqKMnm/s76jJdtWC6cSw6d+bHoqW5z9pEi/KiOEx+Ilk/bap/euFhbfRespR53KVJwpY04ecFlZAkN9Iq6tWCZWOqC7ajiZROc1Z7GoA8cCdx0wjMGrbiLTAfew+hQLxRthZmv6Ad/8ttW9HwWNttF/C18irSxXnAxiIIlLpjPq9pQYNlCjw4gcpnAwIDAQAB', '支付宝公钥(沙箱)'),
('alipay.gateway', 'https://openapi-sandbox.dl.alipaydev.com/gateway.do', '支付宝网关(沙箱)'),
('alipay.notifyUrl', 'http://124.220.70.121/api/Pay/Notify', '异步回调地址'),
('alipay.returnUrl', 'http://124.220.70.121/#/Front/BalanceRecordList', '同步跳转地址');
