-- 开始事务
BEGIN;

-- 创建邀请码表
CREATE TABLE IF NOT EXISTS invitation_codes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    max_uses INT NOT NULL,
    current_uses INT NOT NULL DEFAULT 0,
    expiry_date DATETIME NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 为users表添加invitation_code字段（如果不存在）
SET @dbname = 'heqing';
SET @tablename = 'users';
SET @columnname = 'invitation_code';
SET @columntype = 'VARCHAR(255)';

SET @preparedStatement = (
    SELECT IF(
        (SELECT COUNT(*) 
        FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = @dbname
        AND TABLE_NAME = @tablename
        AND COLUMN_NAME = @columnname) = 0,
        CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' ', @columntype),
        'SELECT 1'
    )
);

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 创建初始超级管理员账户（如果不存在）
INSERT INTO users (username, password, name, role, enabled, create_time, update_time)
SELECT 'superadmin', '$2a$10$xVqYxC8qbJ9MgqzVGXJ5.uWH.1.0O8EA0QdqY1f8SHu/MtZPq5Hy.', '超级管理员', 'SUPER_ADMIN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'superadmin');

-- 创建初始管理员账户（如果不存在）
INSERT INTO users (username, password, name, role, enabled, create_time, update_time)
SELECT 'admin', '$2a$10$xVqYxC8qbJ9MgqzVGXJ5.uWH.1.0O8EA0QdqY1f8SHu/MtZPq5Hy.', '管理员', 'ADMIN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- 创建初始邀请码
INSERT INTO invitation_codes (code, role, max_uses, expiry_date)
VALUES 
('ADMIN2024', 'ADMIN', 5, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 30 DAY)),
('STAFF2024', 'STAFF', 10, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 30 DAY));

-- 提交事务
COMMIT;