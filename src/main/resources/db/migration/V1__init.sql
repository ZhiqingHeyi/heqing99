-- 初始化数据库脚本
-- 创建用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    last_login_time DATETIME,
    member_level VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    points INT NOT NULL,
    points_validity_days INT,
    total_spent DECIMAL(19,2) NOT NULL,
    birthday DATETIME(6),
    gender VARCHAR(255),
    id_number VARCHAR(255),
    id_type VARCHAR(255)
);

-- 创建房间类型表
CREATE TABLE room_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL,
    amenities TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    area INT,
    bed_size VARCHAR(255),
    bed_type VARCHAR(255),
    extra_bed_price DECIMAL(19,2),
    floor VARCHAR(255),
    holiday_price DECIMAL(19,2),
    long_description VARCHAR(255),
    max_capacity INT,
    policies VARCHAR(255),
    weekend_price DECIMAL(19,2),
    images TEXT
);

-- 创建房间表
CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    room_type_id BIGINT NOT NULL,
    floor VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    description VARCHAR(255),
    need_cleaning BIT(1),
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);

-- 创建预订表
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    guest_count INT NOT NULL,
    special_requests TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    INDEX (check_in_date),
    INDEX (status)
);

-- 创建预订表
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    check_in_actual_time DATETIME(6),
    check_in_time DATETIME(6) NOT NULL,
    check_out_actual_time DATETIME(6),
    check_out_time DATETIME(6) NOT NULL,
    create_time DATETIME(6),
    guest_name VARCHAR(255) NOT NULL,
    guest_phone VARCHAR(255) NOT NULL,
    room_count INT NOT NULL,
    special_requests VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    total_price DECIMAL(19,2) NOT NULL,
    update_time DATETIME(6),
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    payment_status VARCHAR(255) NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建入住记录表
CREATE TABLE check_in_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    actual_check_out DATETIME,
    deposit_amount DECIMAL(10,2),
    deposit_status VARCHAR(20),
    notes TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    actual_check_in_time DATETIME(6) NOT NULL,
    actual_check_out_time DATETIME(6),
    check_in_date DATE NOT NULL,
    check_in_number VARCHAR(255) UNIQUE,
    check_out_date DATE NOT NULL,
    deposit DECIMAL(19,2) NOT NULL,
    guest_count INT NOT NULL,
    guest_id_number VARCHAR(255) NOT NULL,
    guest_id_type VARCHAR(255) NOT NULL,
    guest_mobile VARCHAR(255) NOT NULL,
    guest_name VARCHAR(255) NOT NULL,
    operator_id BIGINT,
    operator_name VARCHAR(255),
    payment_method VARCHAR(255) NOT NULL,
    remarks VARCHAR(255),
    room_id BIGINT NOT NULL,
    room_number VARCHAR(255),
    room_type VARCHAR(255),
    special_requests VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- 创建清洁记录表
CREATE TABLE cleaning_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    verify_time DATETIME,
    notes TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    remarks VARCHAR(255),
    task_id BIGINT,
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (staff_id) REFERENCES users(id),
    INDEX (status)
);

-- 创建邀请码表
CREATE TABLE invitation_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    create_time DATETIME(6),
    current_uses INT NOT NULL,
    enabled BIT(1) NOT NULL,
    expiry_date DATETIME(6) NOT NULL,
    max_uses INT NOT NULL,
    role VARCHAR(255) NOT NULL,
    update_time DATETIME(6)
);

-- 创建消费记录表
CREATE TABLE consumption_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(19,2) NOT NULL,
    consumption_time DATETIME(6),
    description VARCHAR(255),
    discount_rate DOUBLE NOT NULL,
    discounted_amount DECIMAL(19,2) NOT NULL,
    points_earned INT NOT NULL,
    reservation_id BIGINT,
    room_id BIGINT,
    type VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建额外费用表
CREATE TABLE additional_charges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(19,2) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    description VARCHAR(255) NOT NULL,
    notes VARCHAR(255),
    operator_id BIGINT,
    operator_name VARCHAR(255),
    payment_method VARCHAR(255),
    type VARCHAR(255) NOT NULL,
    check_in_record_id BIGINT NOT NULL,
    FOREIGN KEY (check_in_record_id) REFERENCES check_in_records(id)
);

-- 创建访客表
CREATE TABLE visitors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    check_in_record_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    id_type VARCHAR(20) NOT NULL,
    id_number VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    leave_time DATETIME(6),
    purpose VARCHAR(255),
    room_number VARCHAR(255),
    visit_time DATETIME(6),
    FOREIGN KEY (check_in_record_id) REFERENCES check_in_records(id)
);

-- 创建访客记录表
CREATE TABLE visitor_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    create_time DATETIME(6),
    id_card VARCHAR(255),
    leave_time DATETIME(6),
    phone VARCHAR(255),
    purpose VARCHAR(255) NOT NULL,
    remarks VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    update_time DATETIME(6),
    visit_time DATETIME(6) NOT NULL,
    visitor_name VARCHAR(255) NOT NULL,
    visited_user_id BIGINT,
    FOREIGN KEY (visited_user_id) REFERENCES users(id)
);

-- 创建酒店信息表
CREATE TABLE hotel_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    description TEXT,
    facilities TEXT,
    check_in_time TIME NOT NULL,
    check_out_time TIME NOT NULL,
    images TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

-- 创建会员等级变更表
CREATE TABLE member_level_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    change_reason VARCHAR(255),
    change_type VARCHAR(255) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    new_level VARCHAR(255) NOT NULL,
    previous_level VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL
);

-- 创建会员等级变更记录表
CREATE TABLE member_level_change_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    create_time DATETIME(6),
    new_level VARCHAR(255) NOT NULL,
    old_level VARCHAR(255) NOT NULL,
    reason VARCHAR(255),
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建积分兑换记录表
CREATE TABLE points_exchange_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cash_value DECIMAL(19,2) NOT NULL,
    create_time DATETIME(6),
    description VARCHAR(255),
    exchange_time DATETIME(6),
    exchange_type VARCHAR(255) NOT NULL,
    operator_id BIGINT,
    points_used INT NOT NULL,
    status VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建积分过期记录表
CREATE TABLE points_expiry_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    create_time DATETIME(6),
    expiry_date DATETIME(6) NOT NULL,
    notification_time DATETIME(6),
    notified BIT(1) NOT NULL,
    points INT NOT NULL,
    remarks VARCHAR(255),
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建统计信息表
CREATE TABLE statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    available_rooms INT,
    create_time DATETIME(6),
    daily_revenue DECIMAL(19,2),
    date DATE NOT NULL,
    occupancy_rate DOUBLE,
    occupied_rooms INT,
    reservation_count INT,
    reserved_rooms INT,
    rooms_cleaned INT,
    rooms_need_cleaning INT,
    total_rooms INT,
    type VARCHAR(255),
    visitor_count INT
);

-- 创建任务表
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actual_duration INT,
    clean_items VARCHAR(255),
    cleaner VARCHAR(255) NOT NULL,
    complete_time DATETIME(6),
    create_time DATETIME(6),
    expected_time DATETIME(6),
    issues VARCHAR(255),
    notes VARCHAR(255),
    priority VARCHAR(255) NOT NULL,
    room_number VARCHAR(255) NOT NULL,
    room_type VARCHAR(255) NOT NULL,
    start_time DATETIME(6),
    status VARCHAR(255) NOT NULL,
    supplies VARCHAR(255)
);

-- 创建营销活动表
CREATE TABLE marketing_campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bonus_points INT,
    create_time DATETIME(6),
    description VARCHAR(1000),
    discount_rate DECIMAL(19,2),
    end_date DATETIME(6) NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_date DATETIME(6) NOT NULL,
    status VARCHAR(255) NOT NULL,
    target_member_level VARCHAR(255),
    type VARCHAR(255) NOT NULL,
    update_time DATETIME(6)
);

-- 添加初始管理员用户
INSERT INTO users (username, password, role, status, create_time, update_time, member_level, name, points, total_spent)
VALUES ('admin', '$2a$10$o9rzdxx8KWN7HXHjhmf4XeJpGN5J83mttY1l/cRMNXvN2ggkIWNHS', 'ADMIN', 'ACTIVE', NOW(), NOW(), 'GOLD', '管理员', 0, 0);

-- 添加初始酒店信息
INSERT INTO hotel_info (name, address, phone, email, check_in_time, check_out_time, create_time, update_time)
VALUES ('和清酒店', '四川省成都市武侯区天府大道中段888号', '028-12345678', 'service@hotel.com', '14:00:00', '12:00:00', NOW(), NOW());

-- 添加初始房间类型
INSERT INTO room_types (name, description, base_price, capacity, amenities, create_time, update_time)
VALUES 
('标准单人间', '标准单人间，配备单人床', 199.00, 1, '空调,电视,WiFi,热水', NOW(), NOW()),
('标准双人间', '标准双人间，配备两张单人床', 299.00, 2, '空调,电视,WiFi,热水', NOW(), NOW()),
('豪华大床房', '豪华大床房，配备一张大床', 399.00, 2, '空调,电视,WiFi,热水,迷你吧', NOW(), NOW()),
('商务套房', '商务套房，配备一张大床和办公区域', 599.00, 2, '空调,电视,WiFi,热水,迷你吧,办公桌', NOW(), NOW()),
('总统套房', '总统套房，配备一张特大床和豪华设施', 999.00, 2, '空调,电视,WiFi,热水,迷你吧,办公桌,客厅,健身区', NOW(), NOW());

-- 添加初始房间
INSERT INTO rooms (room_number, room_type_id, floor, status, create_time, update_time, need_cleaning)
VALUES 
('101', 1, '1', 'AVAILABLE', NOW(), NOW(), 0),
('102', 1, '1', 'AVAILABLE', NOW(), NOW(), 0),
('103', 2, '1', 'AVAILABLE', NOW(), NOW(), 0),
('104', 2, '1', 'AVAILABLE', NOW(), NOW(), 0),
('201', 3, '2', 'AVAILABLE', NOW(), NOW(), 0),
('202', 3, '2', 'AVAILABLE', NOW(), NOW(), 0),
('203', 4, '2', 'AVAILABLE', NOW(), NOW(), 0),
('301', 5, '3', 'AVAILABLE', NOW(), NOW(), 0); 