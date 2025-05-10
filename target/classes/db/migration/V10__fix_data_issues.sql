-- 修复可能为NULL的必填字段
UPDATE users 
SET 
    points = 0 WHERE points IS NULL,
    total_spent = 0.00 WHERE total_spent IS NULL,
    member_level = 'REGULAR' WHERE member_level IS NULL;

-- 修复check_in_records表中可能存在的问题
UPDATE check_in_records
SET 
    deposit = 0.00 WHERE deposit IS NULL,
    total_amount = 0.00 WHERE total_amount IS NULL;

-- 修复消费记录表中的数据
UPDATE consumption_records
SET 
    points_earned = 0 WHERE points_earned IS NULL,
    discount_rate = 1.0 WHERE discount_rate IS NULL,
    discounted_amount = amount WHERE discounted_amount IS NULL;

-- 修复统计数据表中的NULL值
UPDATE statistics
SET 
    available_rooms = 0 WHERE available_rooms IS NULL,
    occupied_rooms = 0 WHERE occupied_rooms IS NULL,
    reserved_rooms = 0 WHERE reserved_rooms IS NULL,
    daily_revenue = 0.00 WHERE daily_revenue IS NULL,
    occupancy_rate = 0.00 WHERE occupancy_rate IS NULL,
    visitor_count = 0 WHERE visitor_count IS NULL,
    reservation_count = 0 WHERE reservation_count IS NULL,
    rooms_cleaned = 0 WHERE rooms_cleaned IS NULL,
    rooms_need_cleaning = 0 WHERE rooms_need_cleaning IS NULL,
    total_rooms = (SELECT COUNT(*) FROM rooms) WHERE total_rooms IS NULL; 