# 🧠 MBTI 性格测试系统

一个基于 **Java Spring Boot + Vue 3** 的全栈 MBTI 性格测试系统，支持用户测试、结果分析、历史记录和管理后台。

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)
![Vue.js](https://img.shields.io/badge/Vue.js-3.x-42b883.svg)
![MySQL](https://img.shields.io/badge/MySQL-5.7%2B-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

## ✨ 特性

- 🎯 **完整的 MBTI 测试**：支持 93 道专业题目，自动计算 4 维度得分
- 🔐 **安全认证**：JWT + Spring Security，支持用户注册登录
- 📊 **详细结果分析**：可视化展示各维度得分和性格类型
- 📈 **历史记录**：保存测试历史，支持多次测试对比
- 👨‍💼 **管理后台**：题库管理、用户管理、数据统计
- 📱 **响应式设计**：支持 PC 和移动端访问

## 🛠️ 技术栈

### 后端
- **Java 17** + **Spring Boot 3.2.5**
- **Spring Security** + **JWT** 认证
- **Spring Data JPA** + **Hibernate** ORM
- **MySQL 5.7+/8.0+** 数据库
- **Maven** 构建工具

### 前端
- **Vue 3** + **Vite 5** 构建
- **Vue Router** 路由管理
- **Pinia** 状态管理
- **Axios** HTTP 客户端
- **Tailwind CSS** 样式框架

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 5.7+ 或 8.0+

### 1. 克隆项目

```bash
git clone https://github.com/yuluuuer/mbti-java.git
cd mbti-java
```

### 2. 数据库配置

创建 MySQL 数据库：

```sql
CREATE DATABASE IF NOT EXISTS mbti CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改数据库配置 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mbti?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

### 3. 启动后端

```bash
cd backend
# 使用项目内 Maven
./apache-maven-3.9.6/bin/mvn spring-boot:run

# 或使用全局 Maven
mvn spring-boot:run
```

后端运行在：`http://localhost:8080`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在：`http://localhost:5173`

### 5. 访问系统

打开浏览器访问：**http://localhost:5173**

## 📁 项目结构

```
mbti-java/
├── backend/                     # Spring Boot 后端
│   ├── src/main/java/com/mbti/
│   │   ├── controller/          # REST 控制器
│   │   ├── entity/              # JPA 实体
│   │   ├── repository/          # 数据访问层
│   │   ├── service/             # 业务逻辑层
│   │   └── security/            # JWT 和安全配置
│   └── src/main/resources/
│       ├── application.yml      # 配置文件
│       └── data/seed.sql        # 初始数据
├── frontend/                    # Vue 3 前端
│   ├── src/
│   │   ├── components/          # 组件
│   │   ├── views/               # 页面视图
│   │   ├── stores/              # Pinia 状态
│   │   └── api/                 # API 调用
│   └── vite.config.js           # Vite 配置
└── README.md
```

## 🎮 功能模块

### 用户端
- ✅ 用户注册、登录、退出
- ✅ MBTI 性格测试（93 道题目）
- ✅ 测试结果展示（4 维度得分分析）
- ✅ 历史测试记录查询
- ✅ 个人资料管理

### 管理后台
- ✅ 题库管理（增删改查、排序、启用/禁用）
- ✅ 测试管理（创建测试、开放时间、参与人员）
- ✅ 维度管理（维度名称和描述）
- ✅ 用户管理（用户列表、重置密码、删除用户）
- ✅ 数据统计（用户和测试数据统计）

## 📸 截图


```

## 🔧 配置说明

### 环境变量（可选）

创建 `.env` 文件：

```env
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=mbti
DB_USERNAME=root
DB_PASSWORD=123456

# JWT 配置
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000
```

### CORS 配置

如果前后端域名不同，需要在后端配置 CORS：

```yaml
app:
  cors:
    allowed-origins: http://localhost:5173,http://127.0.0.1:5173
```

## 🚢 生产部署

### 后端打包

```bash
cd backend
mvn clean package -DskipTests
java -jar target/mbti-backend-1.0.0.jar
```

### 前端打包

```bash
cd frontend
npm run build
# 将 dist 目录部署到 Nginx 或其他静态服务器
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的改动 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 📝 开发说明

### 数据库重置

如需重置数据库：

```sql
DROP DATABASE IF EXISTS mbti;
CREATE DATABASE mbti CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后重启后端，会自动重建表结构并导入初始数据。

### 管理员账号

- 新注册账号默认是普通用户
- 管理员账号需要通过数据库直接修改 `role` 字段
- 或通过已有管理员在后台创建
- 默认管理员账号:admin@test.com,密码:123456
## 🐛 常见问题

### 前端提示 401/403
- 重新登录或检查管理员权限

### 前端连不上后端
- 检查后端是否运行在 8080 端口
- 检查 Vite 代理配置
- 检查 CORS 配置

### 数据库连接失败
- 确认 MySQL 服务正在运行
- 检查数据库配置是否正确

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [MBTI 官方网站](https://www.mbtionline.com/)

---

⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！
