# 第二阶段 RESTful API接口说明

默认地址 http://localhost:8080；路径统一以/api开头；JSON使用UTF-8。
除health、register、login外均需Authorization: Bearer <token>。
响应统一为 {"code":0,"message":"成功","data":...}。HTTP 200表示成功，400为参数错误，401为未登录/令牌失效，403为默认分类禁止修改，404为记录不存在或不可访问，409为重复或关联冲突。
任何接口都不允许客户端指定数据所属userId；数据归属由令牌确定。

## 接口清单
|方法|路径|主要参数或返回|用途|
|---|---|---|---|
|GET|/health|status|存活检查|
|POST|/auth/register|username,password,displayName|注册；返回用户信息|
|POST|/auth/login|username,password|返回token、tokenType、user|
|POST|/auth/logout|无|当前用户所有令牌失效|
|GET|/me|id,username,displayName|当前用户|
|PUT|/me|displayName|修改显示名|
|GET|/categories|分类数组|默认和自己的分类|
|POST|/categories|name,type|新建个人分类|
|PUT|/categories/{id}|name,type|修改个人分类|
|DELETE|/categories/{id}|无|删除未使用个人分类|
|GET|/bills|page,size,type,categoryId,from,to,keyword|分页及组合筛选|
|GET|/bills/{id}|账单对象|单笔详情|
|POST|/bills|见下方示例|新增账单并重算提醒|
|PUT|/bills/{id}|同新增，完整替换|修改账单并重算提醒|
|DELETE|/bills/{id}|无|删除账单并重算提醒|
|GET|/statistics/summary|month=yyyy-MM|income,expense,balance|
|GET|/statistics/categories|month,type(默认EXPENSE)|categoryId,name,amount,percentage|
|GET|/statistics/trends|from=yyyy-MM,to=yyyy-MM|含首尾月份，最多24个月，空月补零|
|GET|/budgets/{month}|month为yyyy-MM|amount,spent,remaining,usageRate,status|
|PUT|/budgets/{month}|amount|创建或修改月预算|
|POST|/intelligence/classify|description,type|status,categoryId,categoryName,matchedKeyword,explanation|
|GET|/intelligence/bills/{id}/anomaly|账单ID|status,sampleCount,q1,q3,iqr,upperBound|
|GET|/alerts|page,size|异常提醒分页|
|PUT|/alerts/{id}/read|无|标记个人提醒已读|

## 请求示例
注册：
```json
{"username":"student01","password":"DemoPass123!","displayName":"王天昊"}
```
用户名为3至32位英文字母、数字或下划线，统一转小写。密码为8至64位ASCII可见字符，显示名不能为空且不超过40字符。
新增账单：
```json
{"categoryId":1,"type":"EXPENSE","amount":25.50,"billDate":"2026-10-20","description":"学校食堂午餐"}
```
type只能INCOME或EXPENSE，必须与分类一致；amount为0.01至9999999999.99，最多2位小数；日期不能晚于当天；描述1至200字符。金额可用JSON数字或数值字符串传递。
分类：{"name":"学习资料","type":"EXPENSE"}，名称最多30字符。
预算：{"amount":1000.00}。同一用户同月份修改覆盖金额，不产生重复预算。

分页返回：
```json
{"code":0,"message":"成功","data":{"records":[],"total":0,"page":1,"size":20}}
```
page从1开始，上限1000000；size为1至100，默认20；账单按日期、ID降序。from/to是包含边界的日期。keyword按描述包含查询，用户输入通过SQL参数绑定，不拼接到SQL条件。
默认类别ID：1餐饮、2交通、3购物、4娱乐、5其他支出、6工资、7其他收入。前端仍应读取分类接口，不硬编码ID。

## 联调与安全约定
默认仅允许http://localhost:5173和http://127.0.0.1:5173跨域；额外来源通过CORS_ORIGINS配置。
JWT有效期120分钟，HS256签名密钥由环境变量JWT_SECRET提供（至少32字节）。退出采用token_version递增，旧令牌在进程重启后仍不可恢复使用。
登录不返回密码哈希，错误不透露账号是否存在。密码策略和令牌机制用于课程演示；面向公网前还需HTTPS、登录限流、审计与密钥轮换等部署工作。
