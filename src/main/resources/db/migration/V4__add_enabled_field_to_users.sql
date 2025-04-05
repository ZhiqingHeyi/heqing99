-- 向users表添加enabled字段，默认值为true
ALTER TABLE users ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE; 