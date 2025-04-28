# 酒店管理系统API文档

## 目录

1. [API概述](#api概述)
2. [认证与授权](#认证与授权)
3. [通用数据结构](#通用数据结构)
4. [用户管理](#用户管理)
5. [会员系统](#会员系统)
6. [房间管理](#房间管理)
7. [预订管理](#预订管理)
8. [入住和退房](#入住和退房)
9. [清洁管理](#清洁管理)
10. [统计数据](#统计数据)

## API概述

### 基础URL

```
https://api.hotelmanagement.com/api
```

### 请求格式

所有API请求应使用JSON格式传递数据，并设置以下HTTP头：

```
Content-Type: application/json
```

### 认证头

对于需要认证的API，需要在HTTP头中包含JWT令牌：

```
Authorization: Bearer {token}
```

### 响应格式

所有API响应都是JSON格式，并包含以下基本结构：

```json
{
  "success": true,          // 请求是否成功
  "code": 200,              // 状态码
  "message": "成功",        // 描述信息
  "data": { }               // 响应数据，可能是对象或数组
}
```

### 常用状态码

| 状态码 | 描述 |
| ------ | ---- |
| 200 | 成功 |
| 400 | 请求错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

## 认证与授权

### 1. 用户登录

**接口描述**：用户登录并获取访问令牌

**请求方法**：POST

**接口URL**：`/login`

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "userInfo": {
      "id": "1001",
      "username": "john",
      "realName": "张三",
      "role": "user",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "level": "银牌会员",
      "points": 1200
    }
  }
}
```

**错误响应**：

```json
{
  "success": false,
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

### 2. 管理员登录

**接口描述**：管理员、前台和保洁人员登录

**请求方法**：POST

**接口URL**：`/admin/login`

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "staffInfo": {
      "id": "2001",
      "username": "admin",
      "realName": "李四",
      "role": "admin", // 可能是 "admin", "reception", "cleaning"
      "phone": "13900139000",
      "email": "lisi@example.com",
      "department": "管理部"
    }
  }
}
```

### 3. 刷新Token

**接口描述**：使用refreshToken刷新访问令牌

**请求方法**：POST

**接口URL**：`/refresh-token`

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| refreshToken | String | 是 | 刷新令牌 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
}
```

### 4. 登出

**接口描述**：用户登出，使当前token失效

**请求方法**：POST

**接口URL**：`/logout`

**请求头**：

```
Authorization: Bearer {token}
```

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

## 用户管理

### 1. 用户注册

**接口描述**：新用户注册

**请求方法**：POST

**接口URL**：`/users/register`

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| confirmPassword | String | 是 | 确认密码 |
| realName | String | 是 | 真实姓名 |
| phone | String | 是 | 手机号码 |
| email | String | 否 | 电子邮箱 |
| gender | String | 否 | 性别(male/female/unknown) |
| birthday | String | 否 | 生日(YYYY-MM-DD) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": "1001",
    "username": "john"
  }
}
```

### 2. 获取用户信息

**接口描述**：获取当前登录用户信息

**请求方法**：GET

**接口URL**：`/users/profile`

**请求头**：

```
Authorization: Bearer {token}
```

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "1001",
    "username": "john",
    "realName": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "gender": "male",
    "birthday": "1990-01-01",
    "registerTime": "2023-01-01 12:00:00",
    "level": "银牌会员",
    "points": 1200,
    "totalSpent": 5000,
    "nextLevel": "金牌会员",
    "nextLevelProgress": 60
  }
}
```

### 3. 更新用户信息

**接口描述**：更新当前登录用户的基本信息

**请求方法**：PUT

**接口URL**：`/users/profile`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| realName | String | 否 | 真实姓名 |
| phone | String | 否 | 手机号码 |
| email | String | 否 | 电子邮箱 |
| gender | String | 否 | 性别 |
| birthday | String | 否 | 生日(YYYY-MM-DD) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": "1001",
    "username": "john",
    "realName": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "gender": "male",
    "birthday": "1990-01-01"
  }
}
```

### 4. 修改密码

**接口描述**：修改当前登录用户的密码

**请求方法**：PUT

**接口URL**：`/users/password`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码 |
| confirmPassword | String | 是 | 确认新密码 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

### 5. 管理员获取用户列表

**接口描述**：管理员获取所有用户列表

**请求方法**：GET

**接口URL**：`/admin/users`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| keyword | String | 否 | 搜索关键词(用户名/手机号) |
| level | String | 否 | 会员等级 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 100,
    "list": [
      {
        "id": "1001",
        "username": "john",
        "realName": "张三",
        "phone": "13800138000",
        "email": "zhangsan@example.com",
        "level": "银牌会员",
        "points": 1200,
        "totalSpent": 5000,
        "registerTime": "2023-01-01 12:00:00",
        "lastLoginTime": "2023-05-01 12:00:00",
        "status": "active"
      },
      // ...更多用户
    ]
  }
}
```

## 会员系统

### 1. 获取会员信息

**接口描述**：获取当前用户的会员信息

**请求方法**：GET

**接口URL**：`/membership/info`

**请求头**：

```
Authorization: Bearer {token}
```

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "userId": "1001",
    "level": "银牌会员",
    "points": 1200,
    "totalSpent": 5000,
    "discount": 0.9,
    "nextLevel": "金牌会员",
    "nextLevelThreshold": 8000,
    "progress": 62.5,
    "registerTime": "2023-01-01",
    "specialPrivileges": [
      "预订免押金",
      "生日礼遇",
      "延迟退房"
    ]
  }
}
```

### 2. 获取会员等级列表

**接口描述**：获取所有会员等级及其权益

**请求方法**：GET

**接口URL**：`/membership/levels`

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "bronze",
      "name": "铜牌会员",
      "discount": 0.95,
      "pointRate": 1,
      "threshold": 0,
      "nextLevel": "银牌会员",
      "nextLevelThreshold": 3000,
      "privileges": [
        "基础折扣9.5折",
        "积分兑换"
      ]
    },
    {
      "id": "silver",
      "name": "银牌会员",
      "discount": 0.9,
      "pointRate": 1.2,
      "threshold": 3000,
      "nextLevel": "金牌会员",
      "nextLevelThreshold": 8000,
      "privileges": [
        "折扣9折",
        "预订免押金",
        "生日礼遇"
      ]
    },
    {
      "id": "gold",
      "name": "金牌会员",
      "discount": 0.85,
      "pointRate": 1.5,
      "threshold": 8000,
      "nextLevel": "钻石会员",
      "nextLevelThreshold": 20000,
      "privileges": [
        "折扣8.5折",
        "预订免押金",
        "生日礼遇",
        "专属客服"
      ]
    },
    {
      "id": "diamond",
      "name": "钻石会员",
      "discount": 0.8,
      "pointRate": 2,
      "threshold": 20000,
      "nextLevel": null,
      "nextLevelThreshold": null,
      "privileges": [
        "折扣8折",
        "预订免押金",
        "生日礼遇",
        "专属客服",
        "机场接送",
        "免费升级房型"
      ]
    }
  ]
}
```

### 3. 获取积分历史记录

**接口描述**：获取用户积分变动历史

**请求方法**：GET

**接口URL**：`/membership/points/history`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 50,
    "list": [
      {
        "id": "1",
        "userId": "1001",
        "points": 200,
        "type": "earn",
        "description": "预订入住",
        "orderNo": "BO2023050100001",
        "balance": 1200,
        "createTime": "2023-05-01 12:00:00"
      },
      {
        "id": "2",
        "userId": "1001",
        "points": -100,
        "type": "redeem",
        "description": "积分兑换优惠券",
        "orderNo": null,
        "balance": 1000,
        "createTime": "2023-04-01 12:00:00"
      }
      // ...更多记录
    ]
  }
}
```

### 4. 计算会员折扣价格

**接口描述**：根据会员等级计算折扣后的价格

**请求方法**：POST

**接口URL**：`/membership/calculate-discount`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| originalPrice | Number | 是 | 原始价格 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "计算成功",
  "data": {
    "originalPrice": 1000,
    "discountRate": 0.9,
    "discountPrice": 900,
    "discountAmount": 100,
    "memberLevel": "银牌会员",
    "estimatedPoints": 900
  }
}
```

## 房间管理

### 1. 获取房间类型列表

**接口描述**：获取所有可用的房间类型

**请求方法**：GET

**接口URL**：`/rooms/types`

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "standard",
      "name": "标准间",
      "basePrice": 299,
      "capacity": 2,
      "bedType": "双床",
      "area": 25,
      "amenities": ["电视", "空调", "热水", "WiFi"],
      "description": "舒适标准间，配备基础设施",
      "images": [
        "https://example.com/images/standard1.jpg",
        "https://example.com/images/standard2.jpg"
      ]
    },
    {
      "id": "deluxe",
      "name": "豪华间",
      "basePrice": 499,
      "capacity": 2,
      "bedType": "大床",
      "area": 35,
      "amenities": ["电视", "空调", "热水", "WiFi", "迷你吧", "浴缸"],
      "description": "豪华客房，配备高级设施",
      "images": [
        "https://example.com/images/deluxe1.jpg",
        "https://example.com/images/deluxe2.jpg"
      ]
    }
    // ...更多房型
  ]
}
```

### 2. 获取房间详情

**接口描述**：获取特定房间类型的详细信息

**请求方法**：GET

**接口URL**：`/rooms/types/{typeId}`

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| typeId | String | 是 | 房间类型ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "deluxe",
    "name": "豪华间",
    "basePrice": 499,
    "weekendPrice": 599,
    "holidayPrice": 699,
    "capacity": 2,
    "maxCapacity": 3,
    "extraBedPrice": 100,
    "bedType": "大床",
    "bedSize": "2x2m",
    "area": 35,
    "floor": "2-8层",
    "amenities": ["电视", "空调", "热水", "WiFi", "迷你吧", "浴缸"],
    "description": "豪华客房，配备高级设施，享受舒适入住体验。...",
    "longDescription": "豪华客房拥有35平米的宽敞空间，配备豪华特大床，提供舒适的睡眠体验。房间内设有独立空调、高速WiFi、50寸智能电视、豪华浴缸等设施...",
    "images": [
      {
        "url": "https://example.com/images/deluxe1.jpg",
        "title": "卧室全景"
      },
      {
        "url": "https://example.com/images/deluxe2.jpg",
        "title": "豪华浴室"
      },
      {
        "url": "https://example.com/images/deluxe3.jpg",
        "title": "窗外景观"
      }
    ],
    "policies": {
      "cancellation": "预订后24小时内可免费取消",
      "prepayment": "需预付30%房费",
      "checkIn": "14:00后",
      "checkOut": "12:00前"
    },
    "availableCount": 5
  }
}
```

### 3. 检查房间可用性

**接口描述**：检查特定日期范围内房间的可用性

**请求方法**：GET

**接口URL**：`/rooms/availability`

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| checkIn | String | 是 | 入住日期(YYYY-MM-DD) |
| checkOut | String | 是 | 离店日期(YYYY-MM-DD) |
| roomType | String | 否 | 房间类型ID |
| guests | Integer | 否 | 客人数量 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "typeId": "standard",
      "typeName": "标准间",
      "basePrice": 299,
      "available": true,
      "availableCount": 10,
      "dailyPrices": [
        {
          "date": "2023-06-01",
          "price": 299,
          "available": true
        },
        {
          "date": "2023-06-02",
          "price": 399,
          "available": true
        },
        {
          "date": "2023-06-03",
          "price": 399,
          "available": true
        }
      ],
      "totalPrice": 1097
    },
    {
      "typeId": "deluxe",
      "typeName": "豪华间",
      "basePrice": 499,
      "available": true,
      "availableCount": 5,
      "dailyPrices": [
        {
          "date": "2023-06-01",
          "price": 499,
          "available": true
        },
        {
          "date": "2023-06-02",
          "price": 599,
          "available": true
        },
        {
          "date": "2023-06-03",
          "price": 599,
          "available": true
        }
      ],
      "totalPrice": 1697
    }
    // ...更多房型
  ]
}
```

### 4. 管理员获取所有房间列表

**接口描述**：管理员获取所有房间列表及状态

**请求方法**：GET

**接口URL**：`/admin/rooms`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| floor | Integer | 否 | 楼层 |
| roomType | String | 否 | 房间类型ID |
| status | String | 否 | 房间状态 |
| keyword | String | 否 | 关键词搜索(房间号) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 100,
    "list": [
      {
        "id": "1001",
        "roomNumber": "301",
        "floor": 3,
        "roomType": {
          "id": "standard",
          "name": "标准间",
          "basePrice": 299
        },
        "status": "vacant",
        "needCleaning": false,
        "lastCleanTime": "2023-06-01 10:00:00",
        "notes": "",
        "updateTime": "2023-06-01 12:00:00"
      },
      {
        "id": "1002",
        "roomNumber": "302",
        "floor": 3,
        "roomType": {
          "id": "deluxe",
          "name": "豪华间",
          "basePrice": 499
        },
        "status": "occupied",
        "needCleaning": true,
        "lastCleanTime": "2023-05-30 10:00:00",
        "notes": "客户要求额外枕头",
        "updateTime": "2023-06-01 12:00:00"
      }
      // ...更多房间
    ]
  }
}
```

### 5. 管理员添加房间

**接口描述**：管理员添加新房间

**请求方法**：POST

**接口URL**：`/admin/rooms`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| roomNumber | String | 是 | 房间号 |
| floor | Integer | 是 | 楼层 |
| roomTypeId | String | 是 | 房间类型ID |
| status | String | 是 | 房间状态 |
| needCleaning | Boolean | 否 | 是否需要清洁 |
| notes | String | 否 | 备注 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": "1003",
    "roomNumber": "303",
    "floor": 3,
    "roomType": {
      "id": "standard",
      "name": "标准间",
      "basePrice": 299
    },
    "status": "vacant",
    "needCleaning": false,
    "notes": "",
    "createTime": "2023-06-01 12:00:00",
    "updateTime": "2023-06-01 12:00:00"
  }
}
```

### 6. 管理员更新房间信息

**接口描述**：管理员更新房间信息

**请求方法**：PUT

**接口URL**：`/admin/rooms/{roomId}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| roomId | String | 是 | 房间ID |

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| roomNumber | String | 否 | 房间号 |
| floor | Integer | 否 | 楼层 |
| roomTypeId | String | 否 | 房间类型ID |
| status | String | 否 | 房间状态 |
| needCleaning | Boolean | 否 | 是否需要清洁 |
| notes | String | 否 | 备注 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": "1001",
    "roomNumber": "301",
    "floor": 3,
    "roomType": {
      "id": "deluxe",
      "name": "豪华间",
      "basePrice": 499
    },
    "status": "vacant",
    "needCleaning": false,
    "notes": "已更新房型",
    "updateTime": "2023-06-01 14:00:00"
  }
}
```

### 7. 管理员删除房间

**接口描述**：管理员删除房间

**请求方法**：DELETE

**接口URL**：`/admin/rooms/{roomId}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| roomId | String | 是 | 房间ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 8. 管理员添加房间类型

**接口描述**：管理员添加新房间类型

**请求方法**：POST

**接口URL**：`/admin/rooms/types`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| name | String | 是 | 房型名称 |
| basePrice | Number | 是 | 基础价格 |
| capacity | Integer | 是 | 可住人数 |
| amenities | String | 否 | 设施，逗号分隔 |
| description | String | 否 | 描述 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": "suite",
    "name": "套房",
    "basePrice": 899,
    "capacity": 4,
    "amenities": "电视,空调,热水,WiFi,迷你吧,浴缸,客厅",
    "description": "高级套房，拥有独立客厅和卧室",
    "createTime": "2023-06-01 12:00:00"
  }
}
```

## 预订管理

### 1. 创建预订

**接口描述**：用户创建新预订

**请求方法**：POST

**接口URL**：`/reservations`

**请求头**：

```
Authorization: Bearer {token} (可选，登录用户提供)
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| checkIn | String | 是 | 入住日期(YYYY-MM-DD) |
| checkOut | String | 是 | 离店日期(YYYY-MM-DD) |
| roomType | String | 是 | 房间类型ID |
| roomCount | Integer | 是 | 预订房间数量 |
| contactName | String | 是 | 联系人姓名 |
| phone | String | 是 | 联系电话 |
| email | String | 否 | 电子邮箱 |
| guestCount | Integer | 是 | 客人数量 |
| remarks | String | 否 | 备注 |
| paymentMethod | Integer | 是 | 支付方式(1:在线支付,2:到店支付) |
| depositType | Integer | 否 | 预付类型(1:全额,2:30%,3:免预付) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "预订成功",
  "data": {
    "reservationId": "BO2023060100001",
    "checkIn": "2023-06-10",
    "checkOut": "2023-06-12",
    "roomType": "豪华间",
    "roomCount": 1,
    "nightCount": 2,
    "guestCount": 2,
    "contactName": "张三",
    "phone": "13800138000",
    "totalAmount": 998,
    "discountAmount": 100,
    "finalAmount": 898,
    "depositAmount": 269.4,
    "paymentMethod": 1,
    "paymentStatus": "pending",
    "status": "pending",
    "createTime": "2023-06-01 12:00:00",
    "paymentUrl": "https://payment.example.com/pay/BO2023060100001"
  }
}
```

### 2. 获取预订详情

**接口描述**：获取预订详细信息

**请求方法**：GET

**接口URL**：`/reservations/{reservationId}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| reservationId | String | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "BO2023060100001",
    "userId": "1001",
    "checkIn": "2023-06-10",
    "checkOut": "2023-06-12",
    "roomType": {
      "id": "deluxe",
      "name": "豪华间",
      "basePrice": 499
    },
    "roomCount": 1,
    "nightCount": 2,
    "guestCount": 2,
    "contactName": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "remarks": "希望有安静的房间",
    "totalAmount": 998,
    "discountAmount": 100,
    "finalAmount": 898,
    "depositAmount": 269.4,
    "paymentMethod": 1,
    "paymentStatus": "paid",
    "status": "confirmed",
    "createTime": "2023-06-01 12:00:00",
    "updateTime": "2023-06-01 12:30:00",
    "confirmTime": "2023-06-01 14:00:00",
    "cancelTime": null,
    "cancelReason": null,
    "assignedRooms": [
      {
        "roomNumber": "503",
        "floor": 5,
        "roomType": "豪华间"
      }
    ],
    "member": {
      "level": "银牌会员",
      "discount": 0.9,
      "points": 898
    }
  }
}
```

### 3. 获取用户预订列表

**接口描述**：获取当前用户的所有预订

**请求方法**：GET

**接口URL**：`/reservations/user`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| status | String | 否 | 预订状态(pending/confirmed/checked-in/cancelled/completed) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 5,
    "list": [
      {
        "id": "BO2023060100001",
        "checkIn": "2023-06-10",
        "checkOut": "2023-06-12",
        "roomType": "豪华间",
        "roomCount": 1,
        "nightCount": 2,
        "totalAmount": 898,
        "status": "confirmed",
        "createTime": "2023-06-01 12:00:00"
      },
      {
        "id": "BO2023050100001",
        "checkIn": "2023-05-15",
        "checkOut": "2023-05-17",
        "roomType": "标准间",
        "roomCount": 1,
        "nightCount": 2,
        "totalAmount": 598,
        "status": "completed",
        "createTime": "2023-05-01 12:00:00"
      }
      // ...更多预订
    ]
  }
}
```

### 4. 取消预订

**接口描述**：取消现有预订

**请求方法**：POST

**接口URL**：`/reservations/{reservationId}/cancel`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| reservationId | String | 是 | 预订ID |

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| reason | String | 否 | 取消原因 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "取消成功",
  "data": {
    "id": "BO2023060100001",
    "status": "cancelled",
    "cancelTime": "2023-06-02 10:00:00",
    "cancelReason": "行程变更",
    "refundAmount": 269.4,
    "refundStatus": "processing"
  }
}
```

### 5. 管理员获取所有预订

**接口描述**：管理员或前台获取所有预订

**请求方法**：GET

**接口URL**：`/admin/reservations`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| status | String | 否 | 预订状态 |
| bookingNo | String | 否 | 预订号 |
| customerName | String | 否 | 客户姓名 |
| phone | String | 否 | 手机号 |
| dateRange | Array | 否 | 入住日期范围 [开始日期, 结束日期] |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 100,
    "list": [
      {
        "id": "BO2023060100001",
        "bookingNo": "BO2023060100001",
        "userId": "1001",
        "customerName": "张三",
        "phone": "13800138000",
        "roomType": "豪华间",
        "roomNumber": "503",
        "roomPrice": 499,
        "checkInDate": "2023-06-10",
        "checkOutDate": "2023-06-12",
        "nightCount": 2,
        "totalAmount": 898,
        "paymentStatus": "paid",
        "status": "confirmed",
        "createTime": "2023-06-01 12:00:00"
      },
      // ...更多预订
    ]
  }
}
```

### 6. 管理员确认预订

**接口描述**：管理员或前台确认预订并分配房间

**请求方法**：POST

**接口URL**：`/admin/reservations/{reservationId}/confirm`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| reservationId | String | 是 | 预订ID |

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| roomNumbers | Array | 否 | 分配的房间号数组 |
| notes | String | 否 | 备注 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "确认成功",
  "data": {
    "id": "BO2023060100001",
    "status": "confirmed",
    "confirmTime": "2023-06-02 10:00:00",
    "confirmedBy": "admin",
    "assignedRooms": [
      {
        "roomNumber": "503",
        "floor": 5,
        "roomType": "豪华间"
      }
    ],
    "notes": "已通知客人"
  }
}
```

### 7. 管理员取消预订

**接口描述**：管理员或前台取消预订

**请求方法**：POST

**接口URL**：`/admin/reservations/{reservationId}/cancel`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| reservationId | String | 是 | 预订ID |

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| reason | String | 是 | 取消原因 |
| refundAmount | Number | 否 | 退款金额 |
| notes | String | 否 | 备注 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "取消成功",
  "data": {
    "id": "BO2023060100001",
    "status": "cancelled",
    "cancelTime": "2023-06-02 10:00:00",
    "cancelReason": "客户要求取消",
    "cancelledBy": "admin",
    "refundAmount": 269.4,
    "refundStatus": "processing",
    "notes": "已通知客人"
  }
}
```

### 8. 管理员创建预订

**接口描述**：管理员或前台创建新预订

**请求方法**：POST

**接口URL**：`/admin/reservations`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| userId | String | 否 | 用户ID(如果是会员) |
| checkIn | String | 是 | 入住日期(YYYY-MM-DD) |
| checkOut | String | 是 | 离店日期(YYYY-MM-DD) |
| roomType | String | 是 | 房间类型ID |
| roomCount | Integer | 是 | 预订房间数量 |
| contactName | String | 是 | 联系人姓名 |
| phone | String | 是 | 联系电话 |
| email | String | 否 | 电子邮箱 |
| guestCount | Integer | 是 | 客人数量 |
| remarks | String | 否 | 备注 |
| paymentMethod | Integer | 是 | 支付方式(1:在线支付,2:到店支付) |
| paymentStatus | String | 是 | 支付状态(unpaid/paid) |
| status | String | 是 | 预订状态(pending/confirmed) |
| roomNumbers | Array | 否 | 分配的房间号 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "预订成功",
  "data": {
    "reservationId": "BO2023060100003",
    "checkIn": "2023-06-15",
    "checkOut": "2023-06-17",
    "roomType": "豪华间",
    "roomCount": 1,
    "nightCount": 2,
    "guestCount": 2,
    "contactName": "李四",
    "phone": "13900139000",
    "totalAmount": 998,
    "discountAmount": 0,
    "finalAmount": 998,
    "paymentMethod": 2,
    "paymentStatus": "unpaid",
    "status": "confirmed",
    "createTime": "2023-06-01 12:00:00",
    "createdBy": "admin",
    "assignedRooms": [
      {
        "roomNumber": "505",
        "floor": 5,
        "roomType": "豪华间"
      }
    ]
  }
}
```

## 入住和退房

### 1. 办理入住

**接口描述**：前台办理入住手续

**请求方法**：POST

**接口URL**：`/admin/checkin`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| reservationId | String | 否 | 预订ID(有预订时) |
| guestName | String | 是 | 客人姓名 |
| idType | String | 是 | 证件类型 |
| idNumber | String | 是 | 证件号码 |
| phone | String | 是 | 联系电话 |
| roomType | String | 否 | 房间类型ID(无预订时) |
| roomNumber | String | 是 | 房间号 |
| checkIn | String | 是 | 入住日期 |
| checkOut | String | 是 | 预计退房日期 |
| guestCount | Integer | 是 | 入住人数 |
| paymentAmount | Number | 否 | 支付金额 |
| paymentMethod | String | 是 | 支付方式 |
| deposit | Number | 是 | 押金 |
| remarks | String | 否 | 备注 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "入住成功",
  "data": {
    "id": "CI2023060100001",
    "reservationId": "BO2023060100001",
    "guestName": "张三",
    "idType": "身份证",
    "idNumber": "110101199001011234",
    "phone": "13800138000",
    "roomNumber": "503",
    "roomType": "豪华间",
    "checkIn": "2023-06-10",
    "checkOut": "2023-06-12",
    "actualCheckIn": "2023-06-10 14:30:00",
    "guestCount": 2,
    "paymentAmount": 898,
    "paymentMethod": "信用卡",
    "deposit": 500,
    "remarks": "",
    "status": "checked-in",
    "createdBy": "reception",
    "createTime": "2023-06-10 14:30:00"
  }
}
```

### 2. 查看入住记录

**接口描述**：获取入住记录详情

**请求方法**：GET

**接口URL**：`/admin/checkin/{checkinId}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| checkinId | String | 是 | 入住记录ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "CI2023060100001",
    "reservationId": "BO2023060100001",
    "guestName": "张三",
    "idType": "身份证",
    "idNumber": "110101199001011234",
    "phone": "13800138000",
    "roomNumber": "503",
    "roomType": "豪华间",
    "checkIn": "2023-06-10",
    "checkOut": "2023-06-12",
    "actualCheckIn": "2023-06-10 14:30:00",
    "actualCheckOut": null,
    "guestCount": 2,
    "paymentAmount": 898,
    "paymentMethod": "信用卡",
    "deposit": 500,
    "totalAmount": 898,
    "extraAmount": 0,
    "refundAmount": 0,
    "status": "checked-in",
    "remarks": "",
    "createdBy": "reception",
    "createTime": "2023-06-10 14:30:00",
    "additionalCharges": []
  }
}
```

### 3. 获取入住列表

**接口描述**：获取入住记录列表

**请求方法**：GET

**接口URL**：`/admin/checkin`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| status | String | 否 | 状态(checked-in/checked-out) |
| guestName | String | 否 | 客人姓名 |
| phone | String | 否 | 联系电话 |
| roomNumber | String | 否 | 房间号 |
| dateRange | Array | 否 | 入住日期范围 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 100,
    "list": [
      {
        "id": "CI2023060100001",
        "reservationId": "BO2023060100001",
        "guestName": "张三",
        "phone": "13800138000",
        "roomNumber": "503",
        "roomType": "豪华间",
        "checkIn": "2023-06-10",
        "checkOut": "2023-06-12",
        "actualCheckIn": "2023-06-10 14:30:00",
        "actualCheckOut": null,
        "status": "checked-in",
        "createTime": "2023-06-10 14:30:00"
      },
      // ...更多记录
    ]
  }
}
```

### 4. 办理退房

**接口描述**：前台办理退房手续

**请求方法**：POST

**接口URL**：`/admin/checkout/{checkinId}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| checkinId | String | 是 | 入住记录ID |

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| additionalCharges | Array | 否 | 额外消费 |
| damages | Array | 否 | 损坏物品 |
| returnDeposit | Number | 是 | 返还押金 |
| paymentMethod | String | 否 | 额外费用支付方式 |
| remarks | String | 否 | 备注 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "退房成功",
  "data": {
    "id": "CI2023060100001",
    "guestName": "张三",
    "roomNumber": "503",
    "checkIn": "2023-06-10",
    "checkOut": "2023-06-12",
    "actualCheckIn": "2023-06-10 14:30:00",
    "actualCheckOut": "2023-06-12 11:45:00",
    "deposit": 500,
    "totalAmount": 898,
    "additionalCharges": [
      {
        "type": "minibar",
        "description": "迷你吧饮料",
        "amount": 50
      }
    ],
    "damages": [],
    "extraAmount": 50,
    "returnDeposit": 450,
    "status": "checked-out",
    "checkoutBy": "reception",
    "checkoutTime": "2023-06-12 11:45:00"
  }
}
```

### 5. 获取今日入住/退房统计

**接口描述**：获取今日入住和退房的统计数据

**请求方法**：GET

**接口URL**：`/admin/checkin/today-stats`

**请求头**：

```
Authorization: Bearer {token}
```

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "todayCheckin": {
      "total": 15,
      "completed": 10,
      "pending": 5,
      "list": [
        {
          "id": "CI2023061200001",
          "reservationId": "BO2023060500001",
          "guestName": "张三",
          "roomNumber": "503",
          "checkIn": "2023-06-12",
          "checkOut": "2023-06-15",
          "status": "checked-in",
          "actualCheckIn": "2023-06-12 14:10:00"
        },
        // ...更多入住
      ]
    },
    "todayCheckout": {
      "total": 12,
      "completed": 8,
      "pending": 4,
      "list": [
        {
          "id": "CI2023061000001",
          "reservationId": "BO2023060100001",
          "guestName": "李四",
          "roomNumber": "505",
          "checkIn": "2023-06-10",
          "checkOut": "2023-06-12",
          "status": "checked-out",
          "actualCheckOut": "2023-06-12 11:30:00"
        },
        // ...更多退房
      ]
    },
    "occupancyRate": 75.5,
    "availableRooms": 25
  }
}
```

## 清洁管理

### 1. 获取清洁任务列表

**接口描述**：获取清洁任务列表，保洁人员和管理员可访问

**请求方法**：GET

**接口URL**：`/admin/cleaning/tasks`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| status | String | 否 | 任务状态(pending/processing/completed) |
| priority | String | 否 | 优先级(high/medium/low) |
| cleanerId | String | 否 | 保洁员ID |
| date | String | 否 | 日期(YYYY-MM-DD) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 50,
    "list": [
      {
        "id": "CT2023061200001",
        "roomNumber": "503",
        "roomType": "豪华间",
        "floor": 5,
        "status": "pending",
        "priority": "high",
        "cleanerId": "3001",
        "cleanerName": "王五",
        "expectedTime": "14:00",
        "assignTime": "2023-06-12 08:00:00",
        "startTime": null,
        "completeTime": null,
        "notes": "客人已退房，请尽快处理"
      },
      {
        "id": "CT2023061200002",
        "roomNumber": "505",
        "roomType": "标准间",
        "floor": 5,
        "status": "processing",
        "priority": "medium",
        "cleanerId": "3001",
        "cleanerName": "王五",
        "expectedTime": "15:00",
        "assignTime": "2023-06-12 08:00:00",
        "startTime": "2023-06-12 13:30:00",
        "completeTime": null,
        "notes": ""
      },
      // ...更多任务
    ]
  }
}
```

### 2. 创建清洁任务

**接口描述**：创建新的清洁任务，管理员可访问

**请求方法**：POST

**接口URL**：`/admin/cleaning/tasks`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| roomNumber | String | 是 | 房间号 |
| cleanerId | String | 是 | 保洁员ID |
| priority | String | 是 | 优先级(high/medium/low) |
| expectedTime | String | 是 | 预计完成时间(HH:mm) |
| notes | String | 否 | 备注 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "CT2023061200003",
    "roomNumber": "507",
    "roomType": "豪华间",
    "floor": 5,
    "status": "pending",
    "priority": "high",
    "cleanerId": "3001",
    "cleanerName": "王五",
    "expectedTime": "16:00",
    "assignTime": "2023-06-12 14:00:00",
    "assignBy": "admin",
    "notes": "客人16:30入住，请尽快处理"
  }
}
```

### 3. 开始清洁任务

**接口描述**：保洁人员开始执行清洁任务

**请求方法**：POST

**接口URL**：`/admin/cleaning/tasks/{taskId}/start`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| taskId | String | 是 | 任务ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "任务已开始",
  "data": {
    "id": "CT2023061200001",
    "roomNumber": "503",
    "status": "processing",
    "startTime": "2023-06-12 14:15:00",
    "estimatedCompleteTime": "2023-06-12 14:45:00"
  }
}
```

### 4. 完成清洁任务

**接口描述**：保洁人员标记清洁任务完成

**请求方法**：POST

**接口URL**：`/admin/cleaning/tasks/{taskId}/complete`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| taskId | String | 是 | 任务ID |

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| notes | String | 否 | 完成备注 |
| items | Array | 否 | 补充的物品 |
| issues | Array | 否 | 发现的问题 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "任务已完成",
  "data": {
    "id": "CT2023061200001",
    "roomNumber": "503",
    "status": "completed",
    "startTime": "2023-06-12 14:15:00",
    "completeTime": "2023-06-12 14:50:00",
    "duration": 35,
    "notes": "已完成清洁",
    "items": [
      {
        "name": "毛巾",
        "quantity": 2
      },
      {
        "name": "矿泉水",
        "quantity": 2
      }
    ],
    "issues": []
  }
}
```

### 5. 获取清洁任务详情

**接口描述**：获取单个清洁任务的详细信息

**请求方法**：GET

**接口URL**：`/admin/cleaning/tasks/{taskId}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| taskId | String | 是 | 任务ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "CT2023061200001",
    "roomNumber": "503",
    "roomType": "豪华间",
    "floor": 5,
    "status": "completed",
    "priority": "high",
    "cleanerId": "3001",
    "cleanerName": "王五",
    "expectedTime": "14:00",
    "assignTime": "2023-06-12 08:00:00",
    "assignBy": "admin",
    "startTime": "2023-06-12 14:15:00",
    "completeTime": "2023-06-12 14:50:00",
    "duration": 35,
    "notes": "已完成清洁",
    "beforeImages": [],
    "afterImages": [],
    "items": [
      {
        "name": "毛巾",
        "quantity": 2
      },
      {
        "name": "矿泉水",
        "quantity": 2
      }
    ],
    "issues": []
  }
}
```

### 6. 获取清洁任务统计

**接口描述**：获取清洁任务统计数据

**请求方法**：GET

**接口URL**：`/admin/cleaning/stats`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| date | String | 否 | 日期(YYYY-MM-DD)，默认今天 |
| cleanerId | String | 否 | 保洁员ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "date": "2023-06-12",
    "summary": {
      "total": 30,
      "pending": 10,
      "processing": 5,
      "completed": 15,
      "highPriority": 8,
      "mediumPriority": 12,
      "lowPriority": 10
    },
    "byCleaners": [
      {
        "cleanerId": "3001",
        "cleanerName": "王五",
        "totalTasks": 15,
        "completedTasks": 10,
        "pendingTasks": 3,
        "processingTasks": 2,
        "avgDuration": 30
      },
      {
        "cleanerId": "3002",
        "cleanerName": "赵六",
        "totalTasks": 15,
        "completedTasks": 5,
        "pendingTasks": 7,
        "processingTasks": 3,
        "avgDuration": 35
      }
    ],
    "byFloors": [
      {
        "floor": 3,
        "totalRooms": 20,
        "needCleaning": 5,
        "inProgress": 2,
        "completed": 13
      },
      {
        "floor": 4,
        "totalRooms": 20,
        "needCleaning": 3,
        "inProgress": 1,
        "completed": 16
      },
      {
        "floor": 5,
        "totalRooms": 20,
        "needCleaning": 2,
        "inProgress": 2,
        "completed": 16
      }
    ]
  }
}
```

### 7. 获取清洁记录历史

**接口描述**：获取历史清洁记录

**请求方法**：GET

**接口URL**：`/admin/cleaning/records`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| roomNumber | String | 否 | 房间号 |
| cleanerId | String | 否 | 保洁员ID |
| dateRange | Array | 否 | 日期范围 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "total": 500,
    "list": [
      {
        "id": "CR2023061200001",
        "taskId": "CT2023061200001",
        "roomNumber": "503",
        "roomType": "豪华间",
        "cleanerId": "3001",
        "cleanerName": "王五",
        "date": "2023-06-12",
        "startTime": "2023-06-12 14:15:00",
        "completeTime": "2023-06-12 14:50:00",
        "duration": 35,
        "items": ["毛巾", "矿泉水"],
        "issues": []
      },
      // ...更多记录
    ]
  }
}
```

## 统计数据

### 1. 获取管理员仪表盘数据

**接口描述**：获取管理员仪表盘所需的统计数据

**请求方法**：GET

**接口URL**：`/admin/dashboard`

**请求头**：

```
Authorization: Bearer {token}
```

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "overview": {
      "occupancyRate": 75.5,
      "todayRevenue": 12500,
      "weekRevenue": 85000,
      "monthRevenue": 350000,
      "todayCheckins": 15,
      "todayCheckouts": 12,
      "newReservations": 20,
      "pendingReservations": 8
    },
    "roomStatus": {
      "total": 100,
      "vacant": 25,
      "occupied": 65,
      "reserved": 5,
      "maintenance": 3,
      "cleaning": 2
    },
    "revenueChart": {
      "labels": ["1月", "2月", "3月", "4月", "5月", "6月"],
      "data": [300000, 320000, 350000, 330000, 340000, 350000]
    },
    "occupancyChart": {
      "labels": ["周一", "周二", "周三", "周四", "周五", "周六", "周日"],
      "data": [70, 75, 77, 80, 85, 90, 85]
    },
    "popularRooms": [
      {
        "typeId": "deluxe",
        "typeName": "豪华间",
        "bookingCount": 150,
        "occupancyRate": 85,
        "averagePrice": 499
      },
      {
        "typeId": "standard",
        "typeName": "标准间",
        "bookingCount": 120,
        "occupancyRate": 75,
        "averagePrice": 299
      }
      // ...更多房型
    ],
    "recentBookings": [
      {
        "id": "BO2023061200001",
        "customerName": "张三",
        "roomType": "豪华间",
        "checkIn": "2023-06-15",
        "checkOut": "2023-06-17",
        "status": "confirmed",
        "createTime": "2023-06-12 10:00:00"
      }
      // ...更多预订
    ]
  }
}
```

### 2. 获取前台仪表盘数据

**接口描述**：获取前台人员仪表盘所需的统计数据

**请求方法**：GET

**接口URL**：`/admin/reception/dashboard`

**请求头**：

```
Authorization: Bearer {token}
```

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "todayCheckins": {
      "total": 15,
      "completed": 10,
      "pending": 5
    },
    "todayCheckouts": {
      "total": 12,
      "completed": 8,
      "pending": 4
    },
    "pendingReservations": 8,
    "roomAvailability": {
      "total": 100,
      "available": 25,
      "occupied": 65,
      "reserved": 5,
      "other": 5
    },
    "todayTasks": [
      {
        "type": "check-in",
        "roomNumber": "505",
        "customerName": "李四",
        "time": "14:00",
        "status": "pending"
      },
      {
        "type": "check-out",
        "roomNumber": "507",
        "customerName": "王五",
        "time": "12:00",
        "status": "pending"
      }
      // ...更多任务
    ],
    "recentActivity": [
      {
        "type": "check-in",
        "roomNumber": "503",
        "customerName": "张三",
        "time": "2023-06-12 13:30:00",
        "operator": "reception"
      },
      {
        "type": "check-out",
        "roomNumber": "501",
        "customerName": "赵六",
        "time": "2023-06-12 11:00:00",
        "operator": "reception"
      }
      // ...更多活动
    ]
  }
}
```

### 3. 获取保洁仪表盘数据

**接口描述**：获取保洁人员仪表盘所需的统计数据

**请求方法**：GET

**接口URL**：`/admin/cleaning/dashboard`

**请求头**：

```
Authorization: Bearer {token}
```

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "taskStats": [
      {
        "type": "pending",
        "title": "待处理",
        "count": 10,
        "icon": "Clock"
      },
      {
        "type": "processing",
        "title": "进行中",
        "count": 5,
        "icon": "Loading"
      },
      {
        "type": "completed",
        "title": "已完成",
        "count": 15,
        "icon": "Check"
      },
      {
        "type": "high-priority",
        "title": "高优先级",
        "count": 8,
        "icon": "Warning"
      }
    ],
    "myTasks": [
      {
        "id": "CT2023061200001",
        "roomNumber": "503",
        "roomType": "豪华间",
        "floor": 5,
        "status": "pending",
        "priority": "high",
        "expectedTime": "14:00",
        "notes": "客人已退房，请尽快处理"
      },
      // ...更多任务
    ],
    "performance": {
      "today": {
        "completed": 10,
        "totalAssigned": 15,
        "completionRate": 66.7,
        "avgDuration": 30
      },
      "thisWeek": {
        "completed": 50,
        "totalAssigned": 65,
        "completionRate": 76.9,
        "avgDuration": 32
      }
    },
    "roomsByFloor": [
      {
        "floor": 3,
        "needCleaning": 5,
        "inProgress": 2,
        "completed": 13
      },
      // ...更多楼层
    ]
  }
}
```

### 4. 获取房间占用率统计

**接口描述**：获取房间占用率和收入统计

**请求方法**：GET

**接口URL**：`/admin/stats/occupancy`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| startDate | String | 否 | 开始日期(YYYY-MM-DD) |
| endDate | String | 否 | 结束日期(YYYY-MM-DD) |
| roomType | String | 否 | 房间类型ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "period": {
      "start": "2023-06-01",
      "end": "2023-06-30"
    },
    "summary": {
      "avgOccupancyRate": 75.5,
      "totalRevenue": 350000,
      "avgDailyRevenue": 11666.67,
      "totalBookings": 250
    },
    "daily": [
      {
        "date": "2023-06-01",
        "occupancyRate": 70,
        "revenue": 10500,
        "bookings": 8
      },
      {
        "date": "2023-06-02",
        "occupancyRate": 72,
        "revenue": 11000,
        "bookings": 9
      }
      // ...更多日期
    ],
    "byRoomType": [
      {
        "typeId": "deluxe",
        "typeName": "豪华间",
        "avgOccupancyRate": 85,
        "totalRevenue": 150000,
        "bookings": 100
      },
      {
        "typeId": "standard",
        "typeName": "标准间",
        "avgOccupancyRate": 75,
        "totalRevenue": 100000,
        "bookings": 80
      }
      // ...更多房型
    ]
  }
}
```

### 5. 获取成员分析数据

**接口描述**：获取会员相关的分析数据

**请求方法**：GET

**接口URL**：`/admin/stats/members`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| period | String | 否 | 统计周期(month/quarter/year) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalMembers": 1000,
    "newMembers": 50,
    "activeMembers": 300,
    "memberDistribution": [
      {
        "level": "铜牌会员",
        "count": 600,
        "percentage": 60
      },
      {
        "level": "银牌会员",
        "count": 300,
        "percentage": 30
      },
      {
        "level": "金牌会员",
        "count": 80,
        "percentage": 8
      },
      {
        "level": "钻石会员",
        "count": 20,
        "percentage": 2
      }
    ],
    "memberBookings": {
      "totalBookings": 500,
      "memberBookings": 300,
      "percentage": 60,
      "byLevel": [
        {
          "level": "铜牌会员",
          "bookings": 100,
          "revenue": 50000
        },
        {
          "level": "银牌会员",
          "bookings": 100,
          "revenue": 60000
        },
        {
          "level": "金牌会员",
          "bookings": 70,
          "revenue": 50000
        },
        {
          "level": "钻石会员",
          "bookings": 30,
          "revenue": 40000
        }
      ]
    },
    "memberTrends": {
      "labels": ["1月", "2月", "3月", "4月", "5月", "6月"],
      "newMembers": [30, 35, 40, 45, 50, 50],
      "activeMembers": [200, 220, 250, 270, 290, 300]
    },
    "topMembers": [
      {
        "userId": "1001",
        "username": "zhang",
        "realName": "张三",
        "level": "钻石会员",
        "totalSpent": 50000,
        "bookingCount": 20,
        "registerTime": "2022-01-01"
      },
      // ...更多会员
    ]
  }
}
```

### 6. 获取营收报表

**接口描述**：获取营收相关的报表数据

**请求方法**：GET

**接口URL**：`/admin/stats/revenue`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| startDate | String | 否 | 开始日期(YYYY-MM-DD) |
| endDate | String | 否 | 结束日期(YYYY-MM-DD) |
| groupBy | String | 否 | 分组方式(day/week/month) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "period": {
      "start": "2023-01-01",
      "end": "2023-06-30"
    },
    "summary": {
      "totalRevenue": 2000000,
      "roomRevenue": 1800000,
      "additionalRevenue": 200000,
      "avgDailyRevenue": 11000,
      "comparisonWithPrevPeriod": 8.5
    },
    "chart": {
      "labels": ["1月", "2月", "3月", "4月", "5月", "6月"],
      "revenue": [300000, 320000, 350000, 330000, 340000, 350000],
      "roomRevenue": [270000, 290000, 320000, 300000, 310000, 320000],
      "additionalRevenue": [30000, 30000, 30000, 30000, 30000, 30000]
    },
    "byRoomType": [
      {
        "typeId": "deluxe",
        "typeName": "豪华间",
        "revenue": 900000,
        "percentage": 50,
        "avgPrice": 499
      },
      {
        "typeId": "standard",
        "typeName": "标准间",
        "revenue": 600000,
        "percentage": 33.33,
        "avgPrice": 299
      },
      // ...更多房型
    ],
    "byPaymentMethod": [
      {
        "method": "creditCard",
        "name": "信用卡",
        "amount": 1000000,
        "percentage": 50
      },
      {
        "method": "wechat",
        "name": "微信支付",
        "amount": 600000,
        "percentage": 30
      },
      {
        "method": "alipay",
        "name": "支付宝",
        "amount": 400000,
        "percentage": 20
      }
    ],
    "additionalCharges": [
      {
        "type": "minibar",
        "name": "迷你吧",
        "amount": 100000,
        "percentage": 50
      },
      {
        "type": "roomService",
        "name": "客房服务",
        "amount": 70000,
        "percentage": 35
      },
      {
        "type": "other",
        "name": "其他",
        "amount": 30000,
        "percentage": 15
      }
    ]
  }
}
```

以上是基于前端页面分析的后端API接口文档。该文档涵盖了酒店管理系统所需的各种功能，包括用户认证、会员系统、房间管理、预订管理、入住退房流程、清洁管理以及各类统计数据接口。针对不同角色（管理员、前台接待、保洁人员和普通用户）提供了相应的API接口，以满足酒店运营的各种需求。接口设计遵循RESTful风格，使用JSON格式进行数据交换，并通过JWT令牌进行身份验证。 