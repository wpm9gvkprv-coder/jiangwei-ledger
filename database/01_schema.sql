CREATE TABLE app_user (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 username VARCHAR(32) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL,
 display_name VARCHAR(40) NOT NULL,
 token_version INT NOT NULL DEFAULT 0
);
CREATE TABLE category (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NULL,
 name VARCHAR(30) NOT NULL,
 type VARCHAR(7) NOT NULL,
 CONSTRAINT fk_category_user FOREIGN KEY(user_id) REFERENCES app_user(id),
 CONSTRAINT uk_category UNIQUE(user_id,name,type),
 CONSTRAINT ck_category_type CHECK(type IN ('INCOME','EXPENSE'))
);
CREATE TABLE bill (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NOT NULL,
 category_id BIGINT NOT NULL,
 type VARCHAR(7) NOT NULL,
 amount DECIMAL(12,2) NOT NULL,
 bill_date DATE NOT NULL,
 description VARCHAR(200) NOT NULL,
 CONSTRAINT fk_bill_user FOREIGN KEY(user_id) REFERENCES app_user(id),
 CONSTRAINT fk_bill_category FOREIGN KEY(category_id) REFERENCES category(id),
 CONSTRAINT ck_bill_amount CHECK(amount > 0),
 CONSTRAINT ck_bill_type CHECK(type IN ('INCOME','EXPENSE'))
);
CREATE INDEX ix_bill_user_date ON bill(user_id,bill_date,id);
CREATE INDEX ix_bill_category ON bill(category_id);
CREATE TABLE budget (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NOT NULL,
 budget_month CHAR(7) NOT NULL,
 amount DECIMAL(12,2) NOT NULL,
 CONSTRAINT fk_budget_user FOREIGN KEY(user_id) REFERENCES app_user(id),
 CONSTRAINT uk_budget_month UNIQUE(user_id,budget_month),
 CONSTRAINT ck_budget_amount CHECK(amount > 0)
);
CREATE TABLE consumption_alert (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NOT NULL,
 bill_id BIGINT NOT NULL UNIQUE,
 amount DECIMAL(12,2) NOT NULL,
 threshold_amount DECIMAL(16,4) NOT NULL,
 sample_count INT NOT NULL,
 reason VARCHAR(200) NOT NULL,
 read_flag BOOLEAN NOT NULL DEFAULT FALSE,
 CONSTRAINT fk_alert_user FOREIGN KEY(user_id) REFERENCES app_user(id),
 CONSTRAINT fk_alert_bill FOREIGN KEY(bill_id) REFERENCES bill(id) ON DELETE CASCADE
);
CREATE INDEX ix_alert_user ON consumption_alert(user_id,read_flag);
