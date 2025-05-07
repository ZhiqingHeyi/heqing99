# 酒店管理系统（Hotel Management System）

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.0-green.svg)
![Vue](https://img.shields.io/badge/Vue-3.3.4-brightgreen.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)

## 📖 项目介绍

酒店管理系统是一个功能完备的前后端分离酒店业务管理平台，专为酒店日常运营设计。系统支持三种角色（管理员、前台、清洁人员）的业务流程，实现了酒店从房间管理、预订入住到清洁维护的全生命周期管理。

### 系统功能亮点

- 🏨 **多角色权限系统**：基于角色的访问控制，为不同岗位提供专属功能界面
- 🛏️ **房间管理**：支持多种房型与状态管理，直观的房态展示
- 📝 **预订与入住**：完整的预订、入住、退房流程，支持预订单号查询
- 🧹 **清洁任务**：清洁任务分配、跟踪与完成记录，提高房间周转效率
- 📊 **数据统计**：多维度业务数据统计与可视化，辅助经营决策
- 👥 **会员管理**：支持会员等级与优惠政策，提升客户忠诚度
- 📱 **响应式界面**：适配PC和移动设备的前端界面，随时随地管理酒店业务

## 🛠️ 技术栈

### 后端技术
- **框架**: Spring Boot 2.7.0
- **安全**: Spring Security + JWT
- **数据持久化**: Spring Data JPA + Hibernate
- **数据库**: MySQL
- **API文档**: Swagger/OpenAPI
- **构建工具**: Maven

### 前端技术
- **框架**: Vue 3 + Vite
- **状态管理**: Pinia
- **UI组件**: Element Plus
- **数据可视化**: ECharts
- **HTTP客户端**: Axios
- **路由**: Vue Router
- **样式**: SCSS

## 🚀 快速开始

### 环境要求
- Java 17+
- Node.js 14+
- MySQL 8.0+
- Maven 3.6+

### 后端部署

1. 克隆仓库
```bash
git clone https://github.com/yourusername/hotel-management.git
cd hotel-management
```

2. 配置数据库
修改 `src/main/resources/application.properties` 文件中的数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_management
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. 构建并运行
```bash
mvn clean package
java -jar target/hotel-management-1.0-SNAPSHOT.jar
```
或者使用Spring Boot Maven插件：
```bash
mvn spring-boot:run
```

### 前端部署

1. 安装依赖
```bash
npm install
```

2. 开发模式运行
```bash
npm run dev
```

3. 构建生产版本
```bash
npm run build
```

## 🌟 系统功能模块

### 1. 管理员模块
- 系统概览与业务数据看板
- 用户账户与权限管理
- 房型与房间配置
- 价格策略设置
- 会员等级管理
- 收入与业务统计报表

### 2. 前台模块
- 房态一览
- 预订管理
- 入住与退房登记
- 访客记录
- 客户信息管理
- 收款与发票

### 3. 清洁人员模块
- 待清洁房间列表
- 清洁任务接收与更新
- 任务完成记录
- 清洁工作统计

## 📸 界面预览

> 请在项目运行后访问以下界面

- 管理员控制台: `http://localhost:3000/admin`
- 前台系统: `http://localhost:3000/reception`
- 清洁管理: `http://localhost:3000/cleaning`

## 📜 API文档

系统提供了完整的API接口文档，详细API请参考 `apidocs.md` 文件或访问运行中系统的Swagger界面：
`http://localhost:8080/swagger-ui/index.html`

### 核心API概览

**清洁人员API**
```
GET /api/cleaning/tasks/statistics - 获取清洁任务统计信息
GET /api/cleaning/tasks - 获取清洁任务列表
POST /api/cleaning/tasks/{id}/complete - 完成清洁任务
```

**前台API**
```
GET /api/reception/rooms/statistics - 获取房间状态统计
POST /api/reception/checkin - 办理入住
POST /api/reception/checkout/{roomNumber} - 办理退房
```

**管理员API**
```
GET /api/admin/dashboard/statistics - 获取仪表盘统计数据
GET /api/admin/users - 获取所有用户列表
POST /api/admin/roomtypes - 添加房间类型
```

## 🔒 安全特性

- 基于JWT的身份验证
- 基于角色的权限控制
- 密码加密存储
- 防SQL注入
- CORS安全配置
- 请求限流防护

## 🤝 贡献指南

欢迎提交问题报告和功能建议！如果你想为项目做出贡献，请遵循以下步骤：

1. Fork 这个仓库
2. 创建你的特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交你的修改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启一个 Pull Request

## 📄 许可证

本项目采用 MIT 许可证。详情请参阅 [LICENSE](LICENSE) 文件。

## 📞 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 项目维护者: 执清鹤一
- Email: 2033842761@qq.com
- GitHub: [ZhiqingHeyi](https://github.com/ZhiqingHeyi)

---

感谢您对本项目的关注！希望这个酒店管理系统能够为您的酒店业务带来价值。 
