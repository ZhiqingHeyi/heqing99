-- 开始事务
BEGIN;

-- 为users表添加会员等级相关字段
ALTER TABLE users
    ADD COLUMN member_level VARCHAR(20) DEFAULT 'REGULAR',
    ADD COLUMN points INT DEFAULT 0,
    ADD COLUMN total_spent DECIMAL(10,2) DEFAULT 0.00,
    ADD COLUMN points_validity_days INT DEFAULT 365;

-- 创建会员等级变更记录表
CREATE TABLE IF NOT EXISTS member_level_change_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    old_level VARCHAR(20) NOT NULL,
    new_level VARCHAR(20) NOT NULL,
    reason VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建积分兑换记录表
CREATE TABLE IF NOT EXISTS points_exchange_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    points_used INT NOT NULL,
    cash_value DECIMAL(10,2) NOT NULL,
    exchange_type VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    exchange_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建积分到期记录表
CREATE TABLE IF NOT EXISTS points_expiry_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    points INT NOT NULL,
    expiry_date DATETIME NOT NULL,
    notified BOOLEAN DEFAULT FALSE,
    remarks VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建消费记录表
CREATE TABLE IF NOT EXISTS consumption_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    consumption_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    points_earned INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建营销活动表
CREATE TABLE IF NOT EXISTS marketing_campaigns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    discount_rate DECIMAL(5,2),
    bonus_points INT,
    target_member_level VARCHAR(20),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 提交事务
COMMIT; 