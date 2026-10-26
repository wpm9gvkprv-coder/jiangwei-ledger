# 智能个人记账与消费分析系统 后端交付

组名：酱味大鸡；王天昊（241310124），负责后端、数据库与智能算法。
技术栈：Java 17、Spring Boot 3.5.7、MyBatis-Plus 3.5.12、Spring Security、JWT、MySQL 8。

## 内容
* database：建表、默认分类、演示数据脚本。
* src/main：全部后端源码，24个HTTP接口。
* src/test：算法单元测试与API集成测试，可在H2或独立MySQL测试库运行。
* docs：需求/ER图、接口、算法与联调、测试与答辩材料。
* postman：可导入的请求集合；evidence：实际执行记录。
* release：编译后的可执行JAR（完成打包后提供）。

## 开发启动
1. 安装JDK 17、Maven 3.6.3以上、MySQL 8.0。IntelliJ IDEA打开pom.xml。
2. 用数据库管理工具建立空数据库ledger，字符集utf8mb4；依次执行database/01_schema.sql、02_defaults.sql。可选执行03_demo_data.sql装载演示数据，仅用于新建演示库。
3. 创建可访问ledger的数据库账号；设置下列环境变量（示例口令须替换）：
```powershell
$env:DB_URL='jdbc:mysql://127.0.0.1:3306/ledger?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai'
$env:DB_USERNAME='ledger_app'
$env:DB_PASSWORD='你的数据库密码'
$env:JWT_SECRET='替换为至少32字节的随机密钥，不提交到代码仓库'
mvn spring-boot:run
```
已提供JAR时，可使用 `java -jar release/ledger-backend-1.0.0.jar`，仍需设置相同环境变量。
默认端口8080，可设置SERVER_PORT；前端默认5173。访问/api/health返回UP；数据库连接通过实际业务接口验证。
项目不会自动建表，不会覆盖现有数据库。所有演示数据均为人工构造。

## 验证
```powershell
mvn test
mvn package
```
默认测试使用内存H2，不访问个人MySQL数据。使用MySQL验证时，建立专用空测试库ledger_test，执行01_schema.sql及02_defaults.sql，然后：
```powershell
$env:TEST_DB_URL='jdbc:mysql://127.0.0.1:3307/ledger_test?serverTimezone=Asia/Shanghai'
$env:TEST_DB_USERNAME='你的测试数据库账号'
$env:TEST_DB_PASSWORD='你的测试数据库密码'
mvn -Dledger.mysql=true test
```
API集成测试采用事务回滚；仍应只使用独立测试库。默认测试密钥仅在测试配置中，不用于正式启动。

## 四阶段对应
1. 需求与基础：docs/01、database/01和02、pom.xml、领域模型与Mapper。
2. 核心业务：认证、用户、分类、账单、统计和预算接口，docs/02。
3. 智能分析：KeywordClassifier、IqrDetector、IntelligenceService，docs/03。
4. 测试交付：src/test、evidence、Postman集合、运行说明及docs/04和05。

四份DOCX阶段报告单独放在交付包的“阶段报告”目录，周期为第一周至第四周。
这些周期用于课程阶段归档，不代表代码在四个真实自然周内分别完成。真实执行时间记录在evidence中。
当前交付为王天昊的后端部分；没有收到庞润泽的Vue工程，因此未宣称完成真实前后端联调或课堂答辩。

## 官方参考
* Spring Boot系统要求：https://docs.spring.io/spring-boot/3.5/system-requirements.html
* MyBatis-Plus安装：https://baomidou.com/en/getting-started/install/
具体依赖版本固定于pom.xml，以本项目测试结果为准。
