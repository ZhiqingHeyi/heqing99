-- 确保外键约束存在
ALTER TABLE consumption_records
ADD CONSTRAINT IF NOT EXISTS FK_consumption_records_room_id
FOREIGN KEY (room_id) REFERENCES rooms(id),
ADD CONSTRAINT IF NOT EXISTS FK_consumption_records_reservation_id
FOREIGN KEY (reservation_id) REFERENCES reservations(id);

-- 确保tasks表与rooms表的关系
ALTER TABLE tasks
ADD COLUMN IF NOT EXISTS room_id BIGINT,
ADD CONSTRAINT IF NOT EXISTS FK_tasks_room_id
FOREIGN KEY (room_id) REFERENCES rooms(id);

-- 确保cleaning_records与tasks表的外键关系
ALTER TABLE cleaning_records
ADD CONSTRAINT IF NOT EXISTS FK_cleaning_records_task_id
FOREIGN KEY (task_id) REFERENCES tasks(id);

-- 更新统计信息表的字段类型
ALTER TABLE statistics
MODIFY COLUMN occupancy_rate DECIMAL(5,2); 