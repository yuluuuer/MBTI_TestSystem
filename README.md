# MBTI_TestSystem

这是一个基于 Java Spring Boot 和 Vue 3 实现的 MBTI 性格测试系统，包含用户测试、结果分析、历史记录和管理后台。

## 当前技术栈

- 后端：Java 17、Spring Boot 3.2.5、Spring Security、Spring Data JPA、Hibernate、JWT、Maven
- 前端：Vue 3、Vite 5、Vue Router、Pinia、Axios、Tailwind CSS
- 数据库：MySQL 5.7+ / 8.0+

## 功能模块

- 用户注册、登录、退出和个人资料维护
- MBTI 测试答题、提交评分和结果展示
- 测试历史记录查询
- 管理后台
  - 题库管理：题目、选项、排序、启用/禁用
  - 测试管理：测试题库、开放时间、参与人员、启用/禁用
  - 维度管理：维度名称和描述
  - 用户管理：用户列表、重置密码、删除用户
  - 数据统计：用户和测试数据统计

## 目录结构

```text
.
├── backend/                 # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/mbti/   # Java 源码
│       └── resources/       # application.yml 和种子数据
├── frontend/                # Vue 3 + Vite 前端
│   ├── package.json
│   └── src/                 # 前端源码
└── DEPLOY.md                # 部署说明
```

## 本地运行

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS mbti CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

后端默认读取环境变量，未设置时会使用本地默认值：

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/mbti?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true}
    username: ${SPRING_DATASOURCE_USERNAME:root}
    password: ${SPRING_DATASOURCE_PASSWORD:root}
```

如本机 MySQL 账号不同，可设置环境变量，或参考 `backend/.env.example`。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

Vite 开发服务器会把 `/api` 请求代理到 `http://localhost:8080`。

## 管理员账号

新注册账号默认是普通用户。管理员账号需要通过已有管理员后台或数据库维护。

账号字段当前作为登录账号使用，不再限制必须是邮箱格式；数据库字段名仍沿用 `email`。

管理员在后台给用户重置密码时，默认重置为 `123456`。

## 常用命令

```bash
# 后端开发运行
cd backend
# 使用项目内 Maven
./apache-maven-3.9.6/bin/mvn spring-boot:run

# 后端打包
cd backend
mvn clean package -DskipTests

# 前端开发运行
cd frontend
npm run dev

# 前端生产构建
cd frontend
npm run build
```

详细部署步骤见 [DEPLOY.md](DEPLOY.md)。
