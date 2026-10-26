-- 人工构造演示数据，先执行01与02，仅导入空演示库。
-- 演示账号 demo_student / demo_other，密码 DemoPass123!。
SET FOREIGN_KEY_CHECKS=0;
INSERT INTO `app_user` VALUES (1,'demo_student','$2a$10$uEa6yPeDPr9I6VQAK456BeM3UMtR60z1bF/gOcAg4OizCFJle/ZJS','课程演示用户',1);
INSERT INTO `app_user` VALUES (2,'demo_other','$2a$10$0pfYTWP.1vNdblxx05vS3OLqKxkwzNE8dN0PfV.fOZtqyU/NLhwly','课程演示用户',0);
INSERT INTO `bill` VALUES (1,1,1,'EXPENSE',10.00,'2026-10-26','人工构造演示账单');
INSERT INTO `bill` VALUES (2,1,1,'EXPENSE',12.00,'2026-10-26','人工构造演示账单');
INSERT INTO `bill` VALUES (3,1,1,'EXPENSE',14.00,'2026-10-26','人工构造演示账单');
INSERT INTO `bill` VALUES (4,1,1,'EXPENSE',16.00,'2026-10-26','人工构造演示账单');
INSERT INTO `bill` VALUES (5,1,1,'EXPENSE',18.00,'2026-10-26','人工构造演示账单');
INSERT INTO `bill` VALUES (6,1,1,'EXPENSE',20.00,'2026-10-26','人工构造演示账单');
INSERT INTO `bill` VALUES (7,1,1,'EXPENSE',22.00,'2026-10-26','人工构造演示账单');
INSERT INTO `bill` VALUES (8,1,1,'EXPENSE',24.00,'2026-10-26','人工构造演示账单');
INSERT INTO `bill` VALUES (9,1,1,'EXPENSE',100.00,'2026-10-26','人工构造演示账单');
INSERT INTO `bill` VALUES (10,1,6,'INCOME',1000.00,'2026-10-26','人工构造演示账单');
INSERT INTO `budget` VALUES (1,1,'2026-10',200.00);
INSERT INTO `consumption_alert` VALUES (2,1,9,100.00,31.0000,8,'支出超过历史四分位上界Q3+1.5×IQR，请核对；该提醒不代表确定异常',0);

SET FOREIGN_KEY_CHECKS=1;
