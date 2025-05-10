-- 为users表添加索引
CREATE INDEX IF NOT EXISTS idx_users_member_level ON users(member_level);
CREATE INDEX IF NOT EXISTS idx_users_create_time ON users(create_time);

-- 为rooms表添加索引
CREATE INDEX IF NOT EXISTS idx_rooms_status ON rooms(status);
CREATE INDEX IF NOT EXISTS idx_rooms_need_cleaning ON rooms(need_cleaning);

-- 为reservations表添加索引
CREATE INDEX IF NOT EXISTS idx_reservations_check_in_time ON reservations(check_in_time);
CREATE INDEX IF NOT EXISTS idx_reservations_check_out_time ON reservations(check_out_time);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);
CREATE INDEX IF NOT EXISTS idx_reservations_payment_status ON reservations(payment_status);

-- 为bookings表添加复合索引
CREATE INDEX IF NOT EXISTS idx_bookings_date_status ON bookings(check_in_date, check_out_date, status);

-- 为check_in_records表添加索引
CREATE INDEX IF NOT EXISTS idx_check_in_records_status ON check_in_records(status);
CREATE INDEX IF NOT EXISTS idx_check_in_records_check_in_date ON check_in_records(check_in_date);
CREATE INDEX IF NOT EXISTS idx_check_in_records_check_out_date ON check_in_records(check_out_date);

-- 为consumption_records表添加索引
CREATE INDEX IF NOT EXISTS idx_consumption_records_time ON consumption_records(consumption_time);
CREATE INDEX IF NOT EXISTS idx_consumption_records_type ON consumption_records(type);

-- 为tasks表添加索引
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);

-- 为visitor_records表添加索引
CREATE INDEX IF NOT EXISTS idx_visitor_records_visit_time ON visitor_records(visit_time);
CREATE INDEX IF NOT EXISTS idx_visitor_records_status ON visitor_records(status);

-- 为marketing_campaigns表添加索引
CREATE INDEX IF NOT EXISTS idx_marketing_campaigns_date ON marketing_campaigns(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_marketing_campaigns_status ON marketing_campaigns(status);
CREATE INDEX IF NOT EXISTS idx_marketing_campaigns_type ON marketing_campaigns(type);

-- 为points_expiry_records表添加索引
CREATE INDEX IF NOT EXISTS idx_points_expiry_records_date ON points_expiry_records(expiry_date);
CREATE INDEX IF NOT EXISTS idx_points_expiry_records_notified ON points_expiry_records(notified); 