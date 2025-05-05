# 酒店管理系统
现在这个项目是前后端都有的一个酒店管理系统是定制化的一个酒店管理系统
本项目是一个完整的酒店管理系统，提供了三种角色（管理员、前台、清洁人员）的API接口。

## 项目结构

项目使用Spring Boot框架构建，采用经典的三层架构：

- Controller：API控制器层
- Service：业务逻辑层
- Repository：数据访问层
- Entity：数据实体层
- DTO：数据传输对象

## 主要API概述

### 清洁人员API

```
GET /api/cleaning/tasks/statistics - 获取清洁任务统计信息
GET /api/cleaning/tasks - 获取清洁任务列表
GET /api/cleaning/tasks/{id} - 获取特定任务详情
POST /api/cleaning/tasks/{id}/start - 开始处理任务
POST /api/cleaning/tasks/{id}/complete - 完成清洁任务
GET /api/cleaning/available-rooms - 获取可用于分配任务的房间列表
GET /api/cleaning/available-cleaners - 获取可用保洁人员列表
POST /api/cleaning/tasks - 分配清洁任务
```

### 前台API

```
GET /api/reception/rooms/statistics - P获取房间状态统计
GET /api/reception/rooms - 获取所有房间列表
GET /api/reception/rooms/status/{status} - 根据状态获取房间列表
GET /api/reception/rooms/reserved-today - 获取今日预订的房间
GET /api/reception/booking/{bookingNo} - 根据预订号查询预订信息
POST /api/reception/checkin - 办理入住
POST /api/reception/checkout/{roomNumber} - 办理退房
POST /api/reception/visitors - 访客登记
GET /api/reception/visitors - 获取访客列表
```

### 管理员API

```
GET /api/admin/dashboard/statistics - 获取仪表盘统计数据
GET /api/admin/users - 获取所有用户列表
GET /api/admin/users/{id} - 获取用户详情
POST /api/admin/users - 添加新用户
PUT /api/admin/users/{id} - 更新用户信息
DELETE /api/admin/users/{id} - 删除用户
GET /api/admin/roomtypes - 获取所有房间类型
POST /api/admin/roomtypes - 添加房间类型
POST /api/admin/rooms - 添加新房间
PUT /api/admin/rooms/{id} - 更新房间信息
GET /api/admin/staff - 获取员工列表
POST /api/admin/staff - 添加员工
```

## 遗留问题

在实现过程中，仍有一些问题需要解决：

1. 需要在ReservationRepository中添加findByBookingNo、findByCheckInDateBetween、countByCheckInDateBetween等方法
2. 需要更新Reservation实体类，添加bookingNo、phone、checkInDate、checkOutDate等字段
3. 需要在ReservationRepository中实现findOverlappingReservations方法
4. 需要在CheckInRecordRepository中添加findTopByRoomNumberOrderByCheckInTimeDesc方法
5. 需要完善VisitorRecordService接口和实现

## 如何完成剩余工作

1. 检查并更新所有实体类的字段，确保与前端需求一致
2. 实现所有缺失的Repository方法
3. 实现所有Service接口的方法
4. 完成API控制器的实现
5. 添加适当的异常处理和日志记录
6. 实现数据验证和安全控制

## 数据库配置

项目使用了JPA/Hibernate作为ORM框架，可以自动创建数据库结构。在application.properties中配置数据库连接信息：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_management
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## 安全配置

项目使用Spring Security进行身份验证和授权。每个API路径都有相应的角色权限控制，确保不同角色只能访问其权限范围内的功能。

## 项目运行

使用Maven构建并运行项目：

```bash
mvn clean package
java -jar target/hotel-management-1.0.0.jar
```

或者使用Spring Boot Maven插件：

```bash
mvn spring-boot:run
``` 
