# 酒店管理系统API文档

## 目录

1. [API概述](#api概述)
2. [版本控制](#版本控制)
3. [环境信息](#环境信息)
4. [API调用示例](#api调用示例)
5. [认证与授权](#认证与授权)
6. [安全策略](#安全策略)
7. [通用数据结构](#通用数据结构)
8. [用户管理](#用户管理)
9. [会员系统](#会员系统)
10. [房间管理](#房间管理)
11. [预订管理](#预订管理)
12. [入住和退房](#入住和退房)
13. [清洁管理](#清洁管理)
14. [统计数据](#统计数据)
15. [API变更日志](#api变更日志)

## API概述

### 基础URL

```
https://api.hotelmanagement.com/api/v1
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
| 400 | 请求错误 - 通常是参数验证失败 |
| 401 | 未授权 - 用户未登录或Token已过期 |
| 403 | 禁止访问 - 用户无权限访问此资源 |
| 404 | 资源不存在 - 请求的资源未找到 |
| 409 | 冲突 - 例如创建已存在的资源 |
| 422 | 请求格式正确，但语义错误导致无法处理 |
| 429 | 请求过于频繁 - 超过API调用频率限制 |
| 500 | 服务器内部错误 - 服务器处理请求时出错 |
| 503 | 服务不可用 - 服务器暂时无法处理请求 |

### 错误码详细说明

API除了返回HTTP状态码，还会在响应体中包含更具体的业务错误码和消息：

| 错误码 | 描述 | 可能原因 |
| ------ | ---- | -------- |
| 10001 | 用户名或密码错误 | 登录时提供的凭证无效 |
| 10002 | 用户不存在 | 尝试访问不存在的用户 |
| 10003 | 用户已存在 | 注册时用户名已被占用 |
| 10004 | 密码错误 | 修改密码时旧密码验证失败 |
| 10005 | 用户已禁用 | 尝试访问已被管理员禁用的账号 |
| 20001 | 预订不存在 | 尝试访问不存在的预订 |
| 20002 | 无法取消预订 | 预订已确认或已过取消期限 |
| 20003 | 房间不可用 | 预订时所选房间已满 |
| 20004 | 预订时间无效 | 入住/退房日期无效或冲突 |
| 30001 | 会员级别不存在 | 尝试访问不存在的会员级别 |
| 40001 | 权限不足 | 尝试访问无权限的资源 |
| 40002 | Token已过期 | 认证Token已过期，需要刷新 |
| 40003 | Token无效 | 提供的Token格式错误或已被撤销 |
| 50001 | 服务器内部错误 | 服务器处理请求时遇到意外错误 |

### 错误响应示例

```json
{
  "success": false,
  "message": "用户名或密码错误",
  "code": 10001
}
```

## 版本控制

API使用URL路径版本控制方式，所有API请求路径以`/api/v{version}`开头，其中`{version}`表示API版本号。

### 当前版本

当前API版本为v1，完整基础URL为：
```
https://api.hotelmanagement.com/api/v1
```

### 版本策略

- 主要版本号（v1, v2等）：表示不向后兼容的变更
- API变更将在[API变更日志](#api变更日志)中记录
- 废弃的API将至少保留6个月，并在文档中明确标注为"已废弃"

## 环境信息

系统提供以下环境的API访问：

### 开发环境

- 基础URL: `https://dev-api.hotelmanagement.com/api/v1`
- 说明: 用于开发和测试，数据随时可能重置
- 访问限制: 仅内部网络可访问

### 测试环境

- 基础URL: `https://test-api.hotelmanagement.com/api/v1`
- 说明: 用于集成测试和验收测试
- 访问限制: 需要特定的测试账号

### 生产环境

- 基础URL: `https://api.hotelmanagement.com/api/v1`
- 说明: 正式环境，访问真实数据
- 访问限制: 需要正式授权

## API调用示例

本节提供常见编程语言的API调用示例，帮助开发人员快速集成本系统。

### JavaScript (Axios)

#### 用户登录

```javascript
import axios from 'axios';

// 创建API请求客户端
const apiClient = axios.create({
  baseURL: 'https://api.hotelmanagement.com/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// 用户登录
async function login(username, password) {
  try {
    const response = await apiClient.post('/users/login', {
      username,
      password
    });
    
    // 登录成功，保存token
    if (response.data.success) {
      localStorage.setItem('token', response.data.token);
      return response.data;
    } else {
      throw new Error(response.data.message);
    }
  } catch (error) {
    console.error('登录失败:', error.message);
    throw error;
  }
}

// 使用示例
login('john', 'password123')
  .then(data => console.log('登录成功:', data))
  .catch(error => console.error('登录失败:', error.message));
```

#### 获取用户预订列表

```javascript
// 添加带认证的请求拦截器
apiClient.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// 获取用户预订列表
async function getUserReservations(userId) {
  try {
    const response = await apiClient.get(`/reservations/user/${userId}`);
    
    if (response.data.success) {
      return response.data.data;
    } else {
      throw new Error(response.data.message);
    }
  } catch (error) {
    console.error('获取预订列表失败:', error.message);
    throw error;
  }
}

// 使用示例
getUserReservations(1001)
  .then(reservations => console.log('用户预订列表:', reservations))
  .catch(error => console.error('获取失败:', error.message));
```

### Java (Spring RestTemplate)

#### 用户登录

```java
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class HotelApiClient {
    private final String baseUrl = "https://api.hotelmanagement.com/api/v1";
    private final RestTemplate restTemplate;
    private String token;
    
    public HotelApiClient() {
        this.restTemplate = new RestTemplate();
    }
    
    public boolean login(String username, String password) {
        try {
            // 创建请求体
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String requestJson = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
            
            // 发送登录请求
            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                baseUrl + "/users/login",
                request,
                LoginResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                LoginResponse loginResponse = response.getBody();
                if (loginResponse.isSuccess()) {
                    // 保存token
                    this.token = loginResponse.getToken();
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("登录失败: " + e.getMessage());
            return false;
        }
    }
    
    // 创建带Token的请求头
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        return headers;
    }
    
    // 响应类
    private static class LoginResponse {
        private boolean success;
        private String message;
        private String token;
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
    }
}
```

#### 创建预订

```java
public class HotelApiClient {
    // ... 前面的代码 ...
    
    public Reservation createReservation(ReservationRequest request) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<ReservationRequest> requestEntity = new HttpEntity<>(request, headers);
            
            ResponseEntity<ReservationResponse> response = restTemplate.postForEntity(
                baseUrl + "/reservations",
                requestEntity,
                ReservationResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ReservationResponse reservationResponse = response.getBody();
                if (reservationResponse.isSuccess()) {
                    System.out.println("预订创建成功: ID=" + reservationResponse.getReservationId());
                    
                    // 返回预订详情
                    return getReservation(reservationResponse.getReservationId());
                } else {
                    throw new RuntimeException("创建预订失败: " + reservationResponse.getMessage());
                }
            }
            throw new RuntimeException("创建预订失败: 服务器响应异常");
        } catch (Exception e) {
            System.err.println("创建预订失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    public Reservation getReservation(Long id) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<GetReservationResponse> response = restTemplate.exchange(
                baseUrl + "/reservations/" + id,
                HttpMethod.GET,
                requestEntity,
                GetReservationResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GetReservationResponse reservationResponse = response.getBody();
                if (reservationResponse.isSuccess()) {
                    return reservationResponse.getData();
                } else {
                    throw new RuntimeException("获取预订失败: " + reservationResponse.getMessage());
                }
            }
            throw new RuntimeException("获取预订失败: 服务器响应异常");
        } catch (Exception e) {
            System.err.println("获取预订失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    // 请求和响应类
    // ... 其他类定义 ...
}
```

### Python (Requests)

```python
import requests

class HotelApiClient:
    def __init__(self):
        self.base_url = "https://api.hotelmanagement.com/api/v1"
        self.token = None
    
    def login(self, username, password):
        """用户登录并获取token"""
        url = f"{self.base_url}/users/login"
        payload = {
            "username": username,
            "password": password
        }
        
        try:
            response = requests.post(url, json=payload)
            data = response.json()
            
            if data.get("success"):
                self.token = data.get("token")
                print(f"登录成功，用户: {data.get('username')}")
                return True
            else:
                print(f"登录失败: {data.get('message')}")
                return False
        except Exception as e:
            print(f"登录出错: {str(e)}")
            return False
    
    def get_headers(self):
        """获取带认证的请求头"""
        headers = {"Content-Type": "application/json"}
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"
        return headers
    
    def get_user_info(self, user_id):
        """获取用户信息"""
        url = f"{self.base_url}/users/{user_id}"
        
        try:
            response = requests.get(url, headers=self.get_headers())
            data = response.json()
            
            if data.get("success"):
                return data.get("data")
            else:
                print(f"获取用户信息失败: {data.get('message')}")
                return None
        except Exception as e:
            print(f"获取用户信息出错: {str(e)}")
            return None
    
    def create_reservation(self, reservation_data):
        """创建预订"""
        url = f"{self.base_url}/reservations"
        
        try:
            response = requests.post(url, json=reservation_data, headers=self.get_headers())
            data = response.json()
            
            if data.get("success"):
                print(f"预订创建成功，ID: {data.get('reservationId')}")
                return data
            else:
                print(f"创建预订失败: {data.get('message')}")
                return None
        except Exception as e:
            print(f"创建预订出错: {str(e)}")
            return None

# 使用示例
if __name__ == "__main__":
    client = HotelApiClient()
    
    # 登录
    client.login("john", "password123")
    
    # 获取用户信息
    user_info = client.get_user_info(1001)
    print(f"用户信息: {user_info}")
    
    # 创建预订
    reservation_data = {
        "userId": 1001,
        "roomType": 1,
        "checkIn": "2023-06-10T14:00:00",
        "checkOut": "2023-06-12T12:00:00",
        "roomCount": 1,
        "contactName": "张三",
        "phone": "13800138000",
        "remarks": "希望有安静的房间",
        "totalAmount": 998
    }
    
    result = client.create_reservation(reservation_data)
    print(f"预订结果: {result}")
```

## 认证与授权

### 1. 用户登录

**接口描述**：用户登录并获取访问令牌

**请求方法**：POST

**接口URL**：`/api/users/login`

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| username | String | 是 | 用户名或手机号 |
| password | String | 是 | 密码 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "1001",
    "username": "john",
    "name": "张三",
    "role": "USER"
  }
}
```

**错误响应**：

```json
{
  "success": false,
  "message": "登录失败: 用户名或密码错误"
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

## 安全策略

### 数据加密

所有API通信必须使用HTTPS协议，确保传输数据的安全性。敏感信息（如密码）在传输前应进行加密处理。

### 密码安全

- 密码在服务器端使用BCrypt算法加密存储
- 密码复杂度要求：至少8个字符，包含字母、数字和特殊字符
- 密码在传输过程中不能以明文形式出现

### 访问控制

API使用基于角色的访问控制（RBAC）机制：

| 角色 | 权限范围 |
| ---- | -------- |
| 游客(GUEST) | 浏览公开信息，注册账号 |
| 用户(USER) | 管理个人资料，预订房间，查看历史记录 |
| 前台(RECEPTION) | 处理入住/退房，管理预订，处理客户请求 |
| 保洁人员(CLEANING) | 查看和更新房间清洁状态 |
| 管理员(ADMIN) | 完全访问权限，包括用户管理、报表等 |

特定权限的控制逻辑：

- 个人用户仅能访问自己的资源（预订、账户信息等）
- 前台可以查看预订信息并处理入住/退房
- 保洁人员只能访问房间清洁相关功能
- 管理员可以访问所有API功能和数据

### Token安全

- JWT令牌过期时间为1小时
- 刷新令牌过期时间为7天
- 令牌必须通过HTTPS传输
- 敏感操作(修改密码、修改账户信息等)可能需要重新验证身份

### 频率限制

为防止滥用，API实施了请求频率限制：

- 普通用户: 100次请求/分钟
- 管理员: 300次请求/分钟

超过限制时会返回`429 Too Many Requests`状态码，响应头包含：

```
Retry-After: 60
```

表示需要等待60秒后再尝试请求。

### CORS策略

API支持跨域资源共享（CORS），允许以下来源的请求：

```
https://hotelmanagement.com
https://*.hotelmanagement.com
```

针对不同HTTP方法的CORS处理：

- GET请求：支持跨域
- POST/PUT/DELETE请求：需要进行预检请求(OPTIONS)

### 日志审计

所有API调用都会被记录，包括：

- 调用者IP地址
- 用户标识(如果已登录)
- 请求的API和方法
- 请求时间
- 操作结果

敏感操作(修改用户信息、支付相关等)会记录更详细的日志，用于审计和安全监控。

## 通用数据结构

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

## 用户管理

> **标签:** `用户` `认证`

### 1. 用户注册

**接口描述**：新用户注册

**请求方法**：POST

**接口URL**：`/api/users/register`

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |
| confirmPassword | String | 是 | 确认密码 |
| name | String | 是 | 真实姓名 |
| phone | String | 是 | 手机号码 |
| email | String | 否 | 电子邮箱 |
| gender | String | 否 | 性别(male/female/unknown) |
| birthday | String | 否 | 生日(YYYY-MM-DD) |

**成功响应**：

```json
{
  "success": true,
  "message": "注册成功",
  "userId": "1001",
  "username": "john"
}
```

**错误响应**：

```json
{
  "success": false,
  "message": "注册失败: 用户名已存在"
}
```

### 2. 获取用户信息

**接口描述**：获取指定用户的信息

**请求方法**：GET

**接口URL**：`/api/users/{id}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**成功响应**：

```json
{
  "success": true,
  "data": {
    "id": 1001,
    "username": "john",
    "name": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "gender": "male",
    "birthday": "1990-01-01",
    "registerTime": "2023-01-01 12:00:00",
    "role": "USER"
  }
}
```

**错误响应**：

```json
{
  "success": false,
  "message": "获取用户信息失败: 用户不存在"
}
```

### 3. 通过用户名获取用户信息

**接口描述**：通过用户名获取用户信息

**请求方法**：GET

**接口URL**：`/api/users/username/{username}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| username | String | 是 | 用户名 |

**成功响应**：

```json
{
  "success": true,
  "data": {
    "id": 1001,
    "username": "john",
    "name": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "gender": "male",
    "birthday": "1990-01-01",
    "registerTime": "2023-01-01 12:00:00",
    "role": "USER"
  }
}
```

### 4. 更新用户信息

**接口描述**：更新用户的基本信息

**请求方法**：PUT

**接口URL**：`/api/users/{id}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**请求体参数**：

```json
{
  "name": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "gender": "male",
  "birthday": "1990-01-01"
}
```

**成功响应**：

```json
{
  "success": true,
  "message": "用户信息更新成功",
  "data": {
    "id": 1001,
    "username": "john",
    "name": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "gender": "male",
    "birthday": "1990-01-01"
  }
}
```

### 5. 修改密码

**接口描述**：修改用户的密码

**请求方法**：PUT

**接口URL**：`/api/users/{id}/password`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**请求参数**：

```json
{
  "oldPassword": "旧密码",
  "newPassword": "新密码"
}
```

**成功响应**：

```json
{
  "success": true,
  "message": "密码修改成功"
}
```

### 6. 删除用户

**接口描述**：删除指定用户

**请求方法**：DELETE

**接口URL**：`/api/users/{id}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**成功响应**：

```json
{
  "success": true,
  "message": "用户已删除"
}
```

### 7. 获取所有用户

**接口描述**：获取所有用户的列表（管理员权限）

**请求方法**：GET

**接口URL**：`/api/users`

**请求头**：

```
Authorization: Bearer {token}
```

**成功响应**：

```json
{
  "success": true,
  "data": [
    {
      "id": 1001,
      "username": "john",
      "name": "张三",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "role": "USER",
      "registerTime": "2023-01-01 12:00:00"
    },
    // ... 更多用户
  ]
}
```

### 8. 按角色获取用户

**接口描述**：获取指定角色的所有用户（管理员权限）

**请求方法**：GET

**接口URL**：`/api/users/role/{role}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| role | UserRole | 是 | 用户角色(USER/ADMIN/RECEPTION/CLEANING) |

**成功响应**：

```json
{
  "success": true,
  "data": [
    {
      "id": 1001,
      "username": "john",
      "name": "张三",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "role": "USER",
      "registerTime": "2023-01-01 12:00:00"
    },
    // ... 更多用户
  ]
}
```

### 9. 切换用户状态

**接口描述**：启用或禁用用户账号（管理员权限）

**请求方法**：PUT

**接口URL**：`/api/users/{id}/status?enabled={enabled}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 用户ID |

**查询参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| enabled | Boolean | 是 | 是否启用账号 |

**成功响应**：

```json
{
  "success": true,
  "message": "用户状态已更新"
}
```

### 10. 获取活跃员工

**接口描述**：获取所有活跃的员工（管理员权限）

**请求方法**：GET

**接口URL**：`/api/users/staff/active`

**请求头**：

```
Authorization: Bearer {token}
```

**成功响应**：

```json
{
  "success": true,
  "data": [
    {
      "id": 2001,
      "username": "reception1",
      "name": "李四",
      "phone": "13900139000",
      "email": "lisi@example.com",
      "role": "RECEPTION",
      "registerTime": "2023-01-01 12:00:00"
    },
    // ... 更多员工
  ]
}
```

### 11. 获取特定角色的用户数量

**接口描述**：获取特定角色的用户数量（管理员权限）

**请求方法**：GET

**接口URL**：`/api/users/count/{role}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| role | UserRole | 是 | 用户角色(USER/ADMIN/RECEPTION/CLEANING) |

**成功响应**：

```json
{
  "success": true,
  "data": 50
}
```

## 会员系统

> **标签:** `会员` `用户` `积分`

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

## 消费记录

> **标签:** `消费` `会员` `用户`

### 1. 获取用户消费记录

**接口描述**：获取用户的消费记录列表

**请求方法**：GET

**接口URL**：`/api/consumption-records/user/{userId}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| userId | Long | 是 | 用户ID |

**查询参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页条数，默认10 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "content": [
      {
        "id": 1001,
        "amount": 1299.00,
        "discountedAmount": 1169.10,
        "discountRate": 0.9,
        "pointsEarned": 1169,
        "type": "房费",
        "description": "豪华大床房两晚",
        "reservationId": 5001,
        "roomId": 301,
        "consumptionTime": "2023-07-01T12:00:00"
      },
      {
        "id": 1002,
        "amount": 258.00,
        "discountedAmount": 232.20,
        "discountRate": 0.9,
        "pointsEarned": 232,
        "type": "餐饮",
        "description": "西餐厅晚餐",
        "reservationId": 5001,
        "roomId": 301,
        "consumptionTime": "2023-07-01T19:30:00"
      }
      // ...更多记录
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalPages": 3,
      "totalElements": 28
    }
  }
}
```

### 2. 获取消费记录详情

**接口描述**：获取特定消费记录的详细信息

**请求方法**：GET

**接口URL**：`/api/consumption-records/{id}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 消费记录ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1001,
    "userId": 2001,
    "userName": "张三",
    "amount": 1299.00,
    "discountedAmount": 1169.10,
    "discountRate": 0.9,
    "pointsEarned": 1169,
    "type": "房费",
    "description": "豪华大床房两晚",
    "reservationId": 5001,
    "roomId": 301,
    "roomNumber": "301",
    "roomType": "豪华大床房",
    "consumptionTime": "2023-07-01T12:00:00",
    "paymentMethod": "信用卡",
    "invoiceNo": "INV202307010001",
    "operatorId": 3001,
    "operatorName": "李四",
    "remarks": "会员9折优惠"
  }
}
```

### 3. 按时间范围查询消费记录

**接口描述**：获取指定时间范围内的消费记录

**请求方法**：GET

**接口URL**：`/api/consumption-records/user/{userId}/time-range`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| userId | Long | 是 | 用户ID |

**查询参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| start | String | 是 | 开始日期(YYYY-MM-DD) |
| end | String | 是 | 结束日期(YYYY-MM-DD) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1001,
      "amount": 1299.00,
      "discountedAmount": 1169.10,
      "discountRate": 0.9,
      "pointsEarned": 1169,
      "type": "房费",
      "description": "豪华大床房两晚",
      "consumptionTime": "2023-07-01T12:00:00"
    },
    {
      "id": 1002,
      "amount": 258.00,
      "discountedAmount": 232.20,
      "discountRate": 0.9,
      "pointsEarned": 232,
      "type": "餐饮",
      "description": "西餐厅晚餐",
      "consumptionTime": "2023-07-01T19:30:00"
    }
    // ...更多记录
  ]
}
```

### 4. 按消费类型查询记录

**接口描述**：获取指定类型的消费记录

**请求方法**：GET

**接口URL**：`/api/consumption-records/user/{userId}/type/{type}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| userId | Long | 是 | 用户ID |
| type | String | 是 | 消费类型(房费/餐饮/会议室/SPA等) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1001,
      "amount": 1299.00,
      "discountedAmount": 1169.10,
      "discountRate": 0.9,
      "pointsEarned": 1169,
      "type": "房费",
      "description": "豪华大床房两晚",
      "consumptionTime": "2023-07-01T12:00:00"
    },
    {
      "id": 1005,
      "amount": 1499.00,
      "discountedAmount": 1349.10,
      "discountRate": 0.9,
      "pointsEarned": 1349,
      "type": "房费",
      "description": "家庭套房一晚",
      "consumptionTime": "2023-07-15T14:00:00"
    }
    // ...更多记录
  ]
}
```

### 5. 创建消费记录

**接口描述**：为用户创建新的消费记录

**请求方法**：POST

**接口URL**：`/api/consumption-records`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| userId | Long | 是 | 用户ID |
| amount | BigDecimal | 是 | 消费金额 |
| type | String | 是 | 消费类型 |
| description | String | 否 | 消费描述 |
| reservationId | Long | 否 | 关联的预订ID |
| roomId | Long | 否 | 关联的房间ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1010,
    "userId": 2001,
    "amount": 580.00,
    "discountedAmount": 522.00,
    "discountRate": 0.9,
    "pointsEarned": 522,
    "type": "餐饮",
    "description": "中餐厅午餐",
    "reservationId": 5001,
    "roomId": 301,
    "consumptionTime": "2023-07-03T13:15:00"
  }
}
```

### 6. : 获取消费统计数据

**接口描述**：获取用户的消费统计数据

**请求方法**：GET

**接口URL**：`/api/consumption-records/user/{userId}/statistics`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| userId | Long | 是 | 用户ID |

**查询参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| period | String | 否 | 统计周期(month/quarter/year)，默认month |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalAmount": 6788.50,
    "totalDiscountedAmount": 6109.65,
    "totalPointsEarned": 6110,
    "totalSavings": 678.85,
    "byType": [
      {
        "type": "房费",
        "amount": 4998.00,
        "percentage": 73.62
      },
      {
        "type": "餐饮",
        "amount": 1250.50,
        "percentage": 18.42
      },
      {
        "type": "SPA",
        "amount": 540.00,
        "percentage": 7.96
      }
    ],
    "byMonth": [
      {
        "month": "2023-04",
        "amount": 2100.00
      },
      {
        "month": "2023-05",
        "amount": 1980.50
      },
      {
        "month": "2023-06",
        "amount": 2708.00
      }
    ],
    "averageConsumption": 2262.83,
    "consumptionCount": 12
  }
}
```

## 入住和退房

> **标签:** `入住` `退房` `前台` `客房`

### 1. 办理入住

**接口描述**：为预订客人办理入住手续

**请求方法**：POST

**接口URL**：`/api/check-in`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| reservationId | Long | 是 | 预订ID |
| guestIdInfo | Object | 是 | 客人身份信息 |
| guestIdInfo.idType | String | 是 | 证件类型(身份证/护照/驾照等) |
| guestIdInfo.idNumber | String | 是 | 证件号码 |
| guestIdInfo.name | String | 是 | 证件姓名 |
| roomId | Long | 是 | 分配的房间ID |
| checkInTime | String | 是 | 入住时间(YYYY-MM-DDTHH:MM:SS) |
| estimatedCheckOutTime | String | 是 | 预计退房时间(YYYY-MM-DDTHH:MM:SS) |
| depositAmount | BigDecimal | 是 | 押金金额 |
| depositMethod | String | 是 | 押金方式(现金/信用卡预授权) |
| remarks | String | 否 | 备注信息 |
| additionalGuests | Array | 否 | 其他入住人信息 |

**请求示例**：

```json
{
  "reservationId": 5001,
  "guestIdInfo": {
    "idType": "身份证",
    "idNumber": "110101199001011234",
    "name": "张三"
  },
  "roomId": 301,
  "checkInTime": "2023-07-01T14:00:00",
  "estimatedCheckOutTime": "2023-07-03T12:00:00",
  "depositAmount": 1000.00,
  "depositMethod": "信用卡预授权",
  "remarks": "客人要求高层安静房间",
  "additionalGuests": [
    {
      "idType": "身份证",
      "idNumber": "110101199001011235",
      "name": "李四"
    }
  ]
}
```

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "入住办理成功",
  "data": {
    "checkInId": 6001,
    "reservationId": 5001,
    "guestName": "张三",
    "roomNumber": "301",
    "roomType": "豪华大床房",
    "checkInTime": "2023-07-01T14:00:00",
    "estimatedCheckOutTime": "2023-07-03T12:00:00",
    "actualCheckOutTime": null,
    "depositAmount": 1000.00,
    "depositMethod": "信用卡预授权",
    "checkInStatus": "已入住",
    "operatorId": 3001,
    "operatorName": "前台-李四"
  }
}
```

### 2. 获取入住详情

**接口描述**：获取入住记录详情

**请求方法**：GET

**接口URL**：`/api/check-in/{id}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 入住记录ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInId": 6001,
    "reservationId": 5001,
    "guestInfo": {
      "userId": 2001,
      "name": "张三",
      "idType": "身份证",
      "idNumber": "110101199001011234",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "memberLevel": "银牌会员"
    },
    "roomInfo": {
      "roomId": 301,
      "roomNumber": "301",
      "roomType": "豪华大床房",
      "floor": 3,
      "building": "主楼"
    },
    "checkInTime": "2023-07-01T14:00:00",
    "estimatedCheckOutTime": "2023-07-03T12:00:00",
    "actualCheckOutTime": null,
    "depositAmount": 1000.00,
    "depositMethod": "信用卡预授权",
    "depositStatus": "已收取",
    "depositRefundStatus": null,
    "checkInStatus": "已入住",
    "additionalGuests": [
      {
        "name": "李四",
        "idType": "身份证",
        "idNumber": "110101199001011235"
      }
    ],
    "consumptionRecords": [
      {
        "id": 1001,
        "amount": 1299.00,
        "type": "房费",
        "description": "豪华大床房两晚",
        "consumptionTime": "2023-07-01T12:00:00"
      },
      {
        "id": 1002,
        "amount": 258.00,
        "type": "餐饮",
        "description": "西餐厅晚餐",
        "consumptionTime": "2023-07-01T19:30:00"
      }
    ],
    "totalConsumption": 1557.00,
    "discountedTotalConsumption": 1401.30,
    "operatorInfo": {
      "operatorId": 3001,
      "operatorName": "前台-李四",
      "operationTime": "2023-07-01T14:00:00"
    },
    "remarks": "客人要求高层安静房间"
  }
}
```

### 3. 查询当前入住客人列表

**接口描述**：获取当前所有入住客人列表

**请求方法**：GET

**接口URL**：`/api/check-in/current`

**请求头**：

```
Authorization: Bearer {token}
```

**查询参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页条数，默认10 |
| sortBy | String | 否 | 排序字段(checkInTime/roomNumber/guestName等) |
| direction | String | 否 | 排序方向(asc/desc)，默认desc |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "content": [
      {
        "checkInId": 6001,
        "guestName": "张三",
        "roomNumber": "301",
        "roomType": "豪华大床房",
        "checkInTime": "2023-07-01T14:00:00",
        "estimatedCheckOutTime": "2023-07-03T12:00:00",
        "memberLevel": "银牌会员",
        "phone": "13800138000"
      },
      {
        "checkInId": 6002,
        "guestName": "王五",
        "roomNumber": "305",
        "roomType": "行政套房",
        "checkInTime": "2023-07-01T16:30:00",
        "estimatedCheckOutTime": "2023-07-05T12:00:00",
        "memberLevel": "金牌会员",
        "phone": "13900139000"
      }
      // ...更多记录
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalPages": 5,
      "totalElements": 42
    }
  }
}
```

### 4. 查询客房入住状态

**接口描述**：获取酒店所有客房的入住状态

**请求方法**：GET

**接口URL**：`/api/rooms/status`

**请求头**：

```
Authorization: Bearer {token}
```

**查询参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| floor | Integer | 否 | 楼层过滤 |
| building | String | 否 | 楼栋过滤 |
| roomType | String | 否 | 房型过滤 |
| status | String | 否 | 状态过滤(空闲/已预订/已入住/清扫中/维修中) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalRooms": 120,
    "occupiedRooms": 85,
    "availableRooms": 25,
    "cleaningRooms": 7,
    "maintenanceRooms": 3,
    "occupancyRate": 70.8,
    "rooms": [
      {
        "roomId": 301,
        "roomNumber": "301",
        "roomType": "豪华大床房",
        "floor": 3,
        "building": "主楼",
        "status": "已入住",
        "guestName": "张三",
        "checkInTime": "2023-07-01T14:00:00",
        "checkOutTime": "2023-07-03T12:00:00"
      },
      {
        "roomId": 302,
        "roomNumber": "302",
        "roomType": "豪华大床房",
        "floor": 3,
        "building": "主楼",
        "status": "空闲",
        "guestName": null,
        "checkInTime": null,
        "checkOutTime": null
      },
      {
        "roomId": 303,
        "roomNumber": "303",
        "roomType": "豪华双床房",
        "floor": 3,
        "building": "主楼",
        "status": "清扫中",
        "guestName": null,
        "checkInTime": null,
        "checkOutTime": null
      }
      // ...更多房间
    ]
  }
}
```

### 5. 更新入住信息

**接口描述**：更新入住客人的信息

**请求方法**：PUT

**接口URL**：`/api/check-in/{id}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 入住记录ID |

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| estimatedCheckOutTime | String | 否 | 更新预计退房时间 |
| remarks | String | 否 | 更新备注信息 |
| additionalGuests | Array | 否 | 更新其他入住人信息 |

**请求示例**：

```json
{
  "estimatedCheckOutTime": "2023-07-04T12:00:00",
  "remarks": "客人延长入住一天",
  "additionalGuests": [
    {
      "idType": "身份证",
      "idNumber": "110101199001011235",
      "name": "李四"
    },
    {
      "idType": "身份证",
      "idNumber": "110101199001011236",
      "name": "王五"
    }
  ]
}
```

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "更新成功",
  "data": {
    "checkInId": 6001,
    "guestName": "张三",
    "roomNumber": "301",
    "checkInTime": "2023-07-01T14:00:00",
    "estimatedCheckOutTime": "2023-07-04T12:00:00",
    "remarks": "客人延长入住一天",
    "additionalGuests": [
      {
        "name": "李四",
        "idType": "身份证",
        "idNumber": "110101199001011235"
      },
      {
        "name": "王五",
        "idType": "身份证",
        "idNumber": "110101199001011236"
      }
    ]
  }
}
```

### 6. 变更房间

**接口描述**：为已入住客人更换房间

**请求方法**：POST

**接口URL**：`/api/check-in/{id}/change-room`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 入住记录ID |

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| newRoomId | Long | 是 | 新房间ID |
| reason | String | 是 | 换房原因 |
| additionalCharge | BigDecimal | 否 | 额外费用(升级房型时) |
| remarks | String | 否 | 备注信息 |

**请求示例**：

```json
{
  "newRoomId": 401,
  "reason": "客人投诉空调噪音",
  "additionalCharge": 0,
  "remarks": "免费升级以安抚客人"
}
```

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "换房成功",
  "data": {
    "checkInId": 6001,
    "guestName": "张三",
    "oldRoomNumber": "301",
    "newRoomNumber": "401",
    "newRoomType": "豪华大床房",
    "changeTime": "2023-07-01T20:30:00",
    "reason": "客人投诉空调噪音",
    "additionalCharge": 0,
    "remarks": "免费升级以安抚客人",
    "operatorId": 3002,
    "operatorName": "前台-王五"
  }
}
```

### 7. 办理退房

**接口描述**：为入住客人办理退房手续

**请求方法**：POST

**接口URL**：`/api/check-out`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| checkInId | Long | 是 | 入住记录ID |
| checkOutTime | String | 是 | 退房时间(YYYY-MM-DDTHH:MM:SS) |
| paymentMethod | String | 是 | 结算方式(现金/信用卡/支付宝/微信等) |
| depositRefund | BigDecimal | 是 | 退还押金金额 |
| remarks | String | 否 | 备注信息 |

**请求示例**：

```json
{
  "checkInId": 6001,
  "checkOutTime": "2023-07-03T11:30:00",
  "paymentMethod": "信用卡",
  "depositRefund": 1000.00,
  "remarks": "房间设施完好，全额退还押金"
}
```

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "退房成功",
  "data": {
    "checkOutId": 7001,
    "checkInId": 6001,
    "guestName": "张三",
    "roomNumber": "301",
    "checkInTime": "2023-07-01T14:00:00",
    "checkOutTime": "2023-07-03T11:30:00",
    "stayDuration": "2天",
    "depositAmount": 1000.00,
    "depositRefund": 1000.00,
    "totalConsumption": 1557.00,
    "discountedAmount": 1401.30,
    "paymentMethod": "信用卡",
    "billDetails": [
      {
        "type": "房费",
        "description": "豪华大床房两晚",
        "amount": 1299.00,
        "discountedAmount": 1169.10
      },
      {
        "type": "餐饮",
        "description": "西餐厅晚餐",
        "amount": 258.00,
        "discountedAmount": 232.20
      }
    ],
    "discount": {
      "rate": 0.9,
      "type": "会员折扣",
      "description": "银牌会员9折"
    },
    "pointsEarned": 1401,
    "operatorId": 3002,
    "operatorName": "前台-王五",
    "invoiceNo": "INV202307030001",
    "remarks": "房间设施完好，全额退还押金"
  }
}
```

### 8. 获取退房详情

**接口描述**：获取退房记录详情

**请求方法**：GET

**接口URL**：`/api/check-out/{id}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 退房记录ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutId": 7001,
    "checkInId": 6001,
    "guestInfo": {
      "userId": 2001,
      "name": "张三",
      "idType": "身份证",
      "idNumber": "110101199001011234",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "memberLevel": "银牌会员"
    },
    "roomInfo": {
      "roomId": 301,
      "roomNumber": "301",
      "roomType": "豪华大床房",
      "floor": 3,
      "building": "主楼"
    },
    "checkInTime": "2023-07-01T14:00:00",
    "checkOutTime": "2023-07-03T11:30:00",
    "stayDuration": "2天",
    "depositAmount": 1000.00,
    "depositRefund": 1000.00,
    "depositMethod": "信用卡预授权",
    "totalConsumption": 1557.00,
    "discountedAmount": 1401.30,
    "discountSavings": 155.70,
    "paymentMethod": "信用卡",
    "billDetails": [
      {
        "type": "房费",
        "description": "豪华大床房两晚",
        "amount": 1299.00,
        "discountedAmount": 1169.10
      },
      {
        "type": "餐饮",
        "description": "西餐厅晚餐",
        "amount": 258.00,
        "discountedAmount": 232.20
      }
    ],
    "discount": {
      "rate": 0.9,
      "type": "会员折扣",
      "description": "银牌会员9折"
    },
    "pointsEarned": 1401,
    "operatorInfo": {
      "operatorId": 3002,
      "operatorName": "前台-王五",
      "operationTime": "2023-07-03T11:30:00"
    },
    "invoiceInfo": {
      "invoiceNo": "INV202307030001",
      "invoiceType": "增值税普通发票",
      "invoiceTitle": "张三",
      "invoiceAmount": 1401.30,
      "issuedTime": "2023-07-03T11:35:00"
    },
    "remarks": "房间设施完好，全额退还押金"
  }
}
```

### 9. 查询退房历史

**接口描述**：查询历史退房记录

**请求方法**：GET

**接口URL**：`/api/check-out/history`

**请求头**：

```
Authorization: Bearer {token}
```

**查询参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| startDate | String | 否 | 开始日期(YYYY-MM-DD) |
| endDate | String | 否 | 结束日期(YYYY-MM-DD) |
| guestName | String | 否 | 客人姓名(模糊查询) |
| roomNumber | String | 否 | 房间号 |
| page | Integer | 否 | 页码，默认0 |
| size | Integer | 否 | 每页条数，默认10 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "content": [
      {
        "checkOutId": 7001,
        "checkInId": 6001,
        "guestName": "张三",
        "roomNumber": "301",
        "checkInTime": "2023-07-01T14:00:00",
        "checkOutTime": "2023-07-03T11:30:00",
        "stayDuration": "2天",
        "totalConsumption": 1557.00,
        "discountedAmount": 1401.30,
        "memberLevel": "银牌会员"
      },
      {
        "checkOutId": 7002,
        "checkInId": 6002,
        "guestName": "王五",
        "roomNumber": "305",
        "checkInTime": "2023-07-01T16:30:00",
        "checkOutTime": "2023-07-05T10:00:00",
        "stayDuration": "4天",
        "totalConsumption": 5280.00,
        "discountedAmount": 4488.00,
        "memberLevel": "金牌会员"
      }
      // ...更多记录
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "totalPages": 12,
      "totalElements": 115
    }
  }
}
```

## 预订管理

> **标签:** `预订` `客房` `前台`

### 1. 获取预订列表

**接口描述**：获取所有预订记录

**请求方法**：GET

**接口URL**：`/api/reservations`

**请求头**：

```
Authorization: Bearer {token}
```

**查询参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| status | String | 否 | 状态过滤(待确认/已确认/已取消/已完成) |
| roomType | String | 否 | 房间类型过滤 |
| guestName | String | 否 | 客人姓名关键词 |
| startDate | String | 否 | 预订日期起始(YYYY-MM-DD) |
| endDate | String | 否 | 预订日期结束(YYYY-MM-DD) |

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
        "id": 1001,
        "bookingNumber": "B202306120001",
        "roomType": "豪华大床房",
        "guestName": "张三",
        "checkInDate": "2023-07-01",
        "checkOutDate": "2023-07-03",
        "status": "待确认",
        "totalAmount": 998.00,
        "operatorId": 5001,
        "operatorName": "张经理",
        "createTime": "2023-06-12 10:00:00"
      },
      {
        "id": 1002,
        "bookingNumber": "B202306130001",
        "roomType": "行政套房",
        "guestName": "李四",
        "checkInDate": "2023-07-02",
        "checkOutDate": "2023-07-04",
        "status": "已确认",
        "totalAmount": 1498.00,
        "operatorId": 5002,
        "operatorName": "前台-王五",
        "createTime": "2023-06-13 11:00:00"
      }
      // ...更多预订
    ]
  }
}
```

### 2. 创建预订

**接口描述**：为用户创建新的预订

**请求方法**：POST

**接口URL**：`/api/reservations`

**请求头**：

```
Authorization: Bearer {token}
```

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| roomType | String | 是 | 房间类型 |
| guestName | String | 是 | 客人姓名 |
| checkInDate | String | 是 | 入住日期(YYYY-MM-DD) |
| checkOutDate | String | 是 | 离店日期(YYYY-MM-DD) |
| roomCount | Integer | 是 | 房间数量 |
| contactName | String | 是 | 联系人姓名 |
| phone | String | 是 | 联系人手机号 |
| remarks | String | 否 | 备注信息 |
| totalAmount | BigDecimal | 是 | 总金额 |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "预订创建成功",
  "data": {
    "id": 1003,
    "bookingNumber": "B202306140001",
    "roomType": "豪华大床房",
    "guestName": "张三",
    "checkInDate": "2023-07-01",
    "checkOutDate": "2023-07-03",
    "status": "待确认",
    "totalAmount": 998.00,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-14 12:00:00"
  }
}
```

### 3. 获取预订详情

**接口描述**：获取特定预订记录的详细信息

**请求方法**：GET

**接口URL**：`/api/reservations/{id}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1001,
    "bookingNumber": "B202306120001",
    "roomType": "豪华大床房",
    "guestName": "张三",
    "checkInDate": "2023-07-01",
    "checkOutDate": "2023-07-03",
    "status": "待确认",
    "totalAmount": 998.00,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 4. 更新预订状态

**接口描述**：更新预订状态

**请求方法**：PUT

**接口URL**：`/api/reservations/{id}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**请求参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| status | String | 是 | 状态(待确认/已确认/已取消/已完成) |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "状态更新成功",
  "data": {
    "id": 1001,
    "bookingNumber": "B202306120001",
    "roomType": "豪华大床房",
    "guestName": "张三",
    "checkInDate": "2023-07-01",
    "checkOutDate": "2023-07-03",
    "status": "已确认",
    "totalAmount": 998.00,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 5. 取消预订

**接口描述**：取消特定预订

**请求方法**：DELETE

**接口URL**：`/api/reservations/{id}`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "预订取消成功",
  "data": null
}
```

### 6. 确认预订

**接口描述**：确认特定预订

**请求方法**：PUT

**接口URL**：`/api/reservations/{id}/confirm`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "预订确认成功",
  "data": null
}
```

### 7. 完成预订

**接口描述**：完成特定预订

**请求方法**：PUT

**接口URL**：`/api/reservations/{id}/complete`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "预订完成成功",
  "data": null
}
```

### 8. 获取预订统计数据

**接口描述**：获取预订统计数据

**请求方法**：GET

**接口URL**：`/api/reservations/statistics`

**请求头**：

```
Authorization: Bearer {token}
```

**查询参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| period | String | 否 | 统计周期(month/quarter/year)，默认month |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalReservations": 100,
    "totalConfirmed": 80,
    "totalCanceled": 10,
    "totalCompleted": 10,
    "byStatus": [
      {
        "status": "待确认",
        "count": 20
      },
      {
        "status": "已确认",
        "count": 60
      },
      {
        "status": "已取消",
        "count": 10
      },
      {
        "status": "已完成",
        "count": 10
      }
    ],
    "byRoomType": [
      {
        "roomType": "豪华大床房",
        "count": 50
      },
      {
        "roomType": "行政套房",
        "count": 30
      },
      {
        "roomType": "豪华双床房",
        "count": 20
      }
    ],
    "byDate": [
      {
        "date": "2023-06",
        "count": 100
      },
      {
        "date": "2023-07",
        "count": 100
      }
    ]
  }
}
```

### 9. 获取预订详情统计

**接口描述**：获取特定预订的详细统计信息

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 10. 获取预订详情统计（按房间类型）

**接口描述**：获取特定预订的详细统计信息（按房间类型）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-room-type`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "roomType": "豪华大床房",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 11. 获取预订详情统计（按入住日期）

**接口描述**：获取特定预订的详细统计信息（按入住日期）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 12. 获取预订详情统计（按离店日期）

**接口描述**：获取特定预订的详细统计信息（按离店日期）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 13. 获取预订详情统计（按客人姓名）

**接口描述**：获取特定预订的详细统计信息（按客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 14. 获取预订详情统计（按联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 15. 获取预订详情统计（按联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 16. 获取预订详情统计（按房间号）

**接口描述**：获取特定预订的详细统计信息（按房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 17. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 18. 获取预订详情统计（按离店日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按离店日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 19. 获取预订详情统计（按入住日期和客人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 20. 获取预订详情统计（按入住日期和联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 21. 获取预订详情统计（按入住日期和联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 22. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 23. 获取预订详情统计（按离店日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按离店日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 24. 获取预订详情统计（按入住日期和客人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 25. 获取预订详情统计（按入住日期和联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 26. 获取预订详情统计（按入住日期和联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 27. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 28. 获取预订详情统计（按离店日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按离店日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 29. 获取预订详情统计（按入住日期和客人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 30. 获取预订详情统计（按入住日期和联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 31. 获取预订详情统计（按入住日期和联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 32. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 33. 获取预订详情统计（按离店日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按离店日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 34. 获取预订详情统计（按入住日期和客人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 35. 获取预订详情统计（按入住日期和联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 36. 获取预订详情统计（按入住日期和联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 37. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 38. 获取预订详情统计（按离店日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按离店日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 39. 获取预订详情统计（按入住日期和客人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 40. 获取预订详情统计（按入住日期和联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 41. 获取预订详情统计（按入住日期和联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 42. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 43. 获取预订详情统计（按离店日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按离店日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 44. 获取预订详情统计（按入住日期和客人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 45. 获取预订详情统计（按入住日期和联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 46. 获取预订详情统计（按入住日期和联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 47. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 48. 获取预订详情统计（按离店日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按离店日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 49. 获取预订详情统计（按入住日期和客人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 50. 获取预订详情统计（按入住日期和联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 51. 获取预订详情统计（按入住日期和联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 52. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 53. 获取预订详情统计（按离店日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按离店日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 54. 获取预订详情统计（按入住日期和客人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 55. 获取预订详情统计（按入住日期和联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 56. 获取预订详情统计（按入住日期和联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 57. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 58. 获取预订详情统计（按离店日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按离店日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 59. 获取预订详情统计（按入住日期和客人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 60. 获取预订详情统计（按入住日期和联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 61. 获取预订详情统计（按入住日期和联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 62. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 63. 获取预订详情统计（按离店日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按离店日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 64. 获取预订详情统计（按入住日期和客人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 65. 获取预订详情统计（按入住日期和联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 66. 获取预订详情统计（按入住日期和联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 67. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 68. 获取预订详情统计（按离店日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按离店日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-out-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkOutDate": "2023-07-03",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 69. 获取预订详情统计（按入住日期和客人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和客人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-guest-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "guestName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 70. 获取预订详情统计（按入住日期和联系人姓名）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人姓名）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-name`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactName": "张三",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 71. 获取预订详情统计（按入住日期和联系人手机号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和联系人手机号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-contact-phone`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "contactPhone": "13800138000",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 72. 获取预订详情统计（按入住日期和房间号）

**接口描述**：获取特定预订的详细统计信息（按入住日期和房间号）

**请求方法**：GET

**接口URL**：`/api/reservations/{id}/statistics-by-check-in-date-and-room-number`

**请求头**：

```
Authorization: Bearer {token}
```

**路径参数**：

| 参数名 | 类型 | 必填 | 描述 |
| ------ | ---- | ---- | ---- |
| id | Long | 是 | 预订ID |

**成功响应**：

```json
{
  "success": true,
  "code": 200,
  "message": "获取成功",
  "data": {
    "checkInDate": "2023-07-01",
    "roomNumber": "301",
    "totalAmount": 998.00,
    "discountedAmount": 900.00,
    "discountRate": 0.9,
    "pointsEarned": 900,
    "operatorId": 5001,
    "operatorName": "张经理",
    "createTime": "2023-06-12 10:00:00"
  }
}
```

### 73. 获取预订详情统计（按离店日期和房间号）
