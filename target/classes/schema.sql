-- 创建数据库
DROP DATABASE IF EXISTS heqing;
CREATE DATABASE heqing DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE heqing;

-- 创建用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,  -- ADMIN, STAFF, USER
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, INACTIVE
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    last_login_time DATETIME
);

-- 创建房间类型表
CREATE TABLE room_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL,
    amenities TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

-- 创建房间表
CREATE TABLE rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    room_type_id BIGINT NOT NULL,
    floor VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,  -- AVAILABLE, OCCUPIED, NEEDS_CLEANING, MAINTENANCE
    notes TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);

-- 创建预订表
CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,  -- PENDING, CONFIRMED, CANCELLED, COMPLETED
    total_price DECIMAL(10,2) NOT NULL,
    guest_count INT NOT NULL,
    special_requests TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- 创建入住记录表
CREATE TABLE check_in_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    actual_check_in DATETIME NOT NULL,
    actual_check_out DATETIME,
    deposit_amount DECIMAL(10,2),
    deposit_status VARCHAR(20),  -- PAID, REFUNDED
    notes TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- 创建访客登记表
CREATE TABLE visitors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    check_in_record_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    id_type VARCHAR(20) NOT NULL,
    id_number VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (check_in_record_id) REFERENCES check_in_records(id)
);

-- 创建清洁记录表
CREATE TABLE cleaning_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,  -- PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, VERIFIED
    start_time DATETIME,
    end_time DATETIME,
    verify_time DATETIME,
    notes TEXT,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (staff_id) REFERENCES users(id)
);

-- 创建酒店信息表
CREATE TABLE hotel_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
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

-- 创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_rooms_status ON rooms(status);
CREATE INDEX idx_bookings_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_cleaning_records_status ON cleaning_records(status);