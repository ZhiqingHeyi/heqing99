-- 添加性别和生日字段到users表
ALTER TABLE users ADD COLUMN gender VARCHAR(10) NULL;
ALTER TABLE users ADD COLUMN birthday DATE NULL; 