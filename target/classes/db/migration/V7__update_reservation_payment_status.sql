-- 添加支付状态到预订表（如果不存在）
ALTER TABLE reservations
ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) NOT NULL DEFAULT 'UNPAID';

-- 更新现有记录的支付状态
UPDATE reservations
SET payment_status = 'PAID'
WHERE status = 'CHECKED_OUT' AND payment_status = 'UNPAID'; 