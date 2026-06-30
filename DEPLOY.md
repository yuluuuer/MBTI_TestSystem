# MBTI 性格测试系统 - 部署文档

本文档对应当前 Java + Vue 版本。

## 1. 架构

- 后端：Spring Boot 3.2.5，默认端口 `8080`
- 前端：Vue 3 + Vite 5，开发端口 `5173`
- 数据库：MySQL 5.7+ / 8.0+
- 鉴权：Spring Security + JWT
- 静态资源部署：前端构建后由 Nginx 或其他静态服务器托管

## 2. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 5.7+ 或 8.0+

Windows 本地如果没有全局 Maven，也可以使用项目内的 `backend/apache-maven-3.9.6/bin/mvn.cmd`。

## 3. 数据库准备

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS mbti CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

默认连接配置在 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mbti?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

生产环境应改成服务器自己的数据库账号和强密码。

后端启动时会：

- 根据 JPA 实体自动创建或更新表结构：`spring.jpa.hibernate.ddl-auto=update`
- 执行 `classpath:data/seed.sql` 初始化默认题库和维度数据

## 4. 本地开发运行

### 启动后端

```bash
cd backend
./apache-maven-3.9.6/bin/mvn spring-boot:run
```

后端地址：`http://localhost:8080`

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

开发环境下，`frontend/vite.config.js` 会将 `/api` 代理到 `http://localhost:8080`。

## 5. 初始化管理员

新注册账号默认是普通用户。管理员账号需要通过已有管理员后台或数据库维护。

账号字段用于登录账号，不要求必须是邮箱格式；数据库字段名仍沿用 `email`。

管理员后台重置用户密码时，默认重置为 `123456`。生产环境首次登录后应立即修改测试密码或删除测试账号。
默认管理员账号：admin@test.com,密码123456

## 6. 生产打包

### 后端打包

```bash
cd backend
mvn clean package -DskipTests
```

启动后端：

```bash
java -jar target/mbti-backend-1.0.0.jar
```

如需覆盖数据库或 JWT 配置，推荐使用环境变量或外部配置文件，不要直接使用开发环境密码。

### 前端打包

```bash
cd frontend
npm install
npm run build
```

构建产物位于：

```text
frontend/dist/
```

## 7. Nginx 示例

将前端 `dist` 目录放到服务器后，可用下面配置托管静态资源并反向代理 API：

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

如果前后端域名不同，需要在后端 `app.cors.allowed-origins` 中加入前端域名。

## 8. 常见问题

### 前端提示 401

通常是登录状态失效或没有管理员权限。重新登录；访问 `/api/admin/**` 必须是管理员账号。

### 前端提示 403

通常是当前账号没有管理员权限。请使用管理员账号登录后台。

### 前端连不上后端

检查：

1. 后端是否运行在 `8080`
2. 前端代理是否指向 `http://localhost:8080`
3. CORS 是否包含当前前端地址，例如 `http://localhost:5173` 或 `http://127.0.0.1:5173`

### 如何重置数据库

删除并重建 `mbti` 数据库，再启动后端即可重新建表并导入种子数据。

### 如何管理题库

登录管理员后台后：

1. 在测试管理中创建测试、设置开放时间并选择参与人员
2. 在题库管理中维护题目、选项、排序和启用状态
3. 在维度管理中维护各维度说明

## 9. 当前不包含

- Docker 镜像和 Compose 编排
- 自动化 CI/CD 脚本
- 多环境配置模板
