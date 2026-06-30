# MBTI 性格测试系统 - 后端说明

## 技术栈

- **Java 17** + **Spring Boot 3.2.5**
- **Spring Data JPA** + **Hibernate** (MySQL)
- **Spring Security** + **JWT** (jjwt)
- **Maven** 构建

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 5.7+ 或 8.0+

### 2. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS mbti CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置

编辑 `src/main/resources/application.yml` 或设置环境变量：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mbti?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

### 4. 运行

```bash
cd backend
mvn spring-boot:run
```

首次启动会自动：
- 创建数据库表结构（JPA ddl-auto=update）
- 执行 `data/seed.sql` 初始化题库数据（121 道默认题目 + 36 道职场版题目）

### 5. 访问

后端 API: http://localhost:8080

### 6. 管理员

注册接口默认创建普通用户。登录账号不再要求邮箱格式，但数据库字段仍沿用 `email`。

管理员重置用户密码时，默认重置为 `123456`。

## API 端点

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 注册（默认普通用户） |
| POST | /api/auth/login | 登录 |
| POST | /api/auth/logout | 登出 |
| GET | /api/auth/me | 获取当前用户 |

### 公开接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/exam-types | 获取当前用户可参与的测试列表 |
| GET | /api/questions?examTypeId=X | 获取随机题目 |

### 测试会话
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/session/start | 开始测试 |
| POST | /api/session/answer | 保存答案 |
| POST | /api/session/submit | 提交并评分 |
| GET | /api/history?examTypeId=X | 测试历史 |
| PUT | /api/user/profile | 更新昵称 |

### 管理后台（需要 ADMIN 角色）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | /api/admin/questions | 题目管理 |
| GET/POST/PUT/DELETE | /api/admin/exam-types | 测试管理 |
| GET/PUT | /api/admin/dimensions | 维度描述管理 |
| GET/PUT/DELETE | /api/admin/users | 用户管理 |
| GET | /api/admin/stats | 数据统计 |

## 项目结构

```
src/main/java/com/mbti/
├── MbtiApplication.java          # 启动类
├── config/                        # 配置类
│   ├── SecurityConfig.java        # Spring Security 配置
│   ├── WebConfig.java             # CORS 配置
│   └── DataInitializer.java       # 数据初始化
├── entity/                        # JPA 实体
├── enums/                         # 枚举类
├── repository/                    # Spring Data JPA 仓库
├── service/                       # 业务逻辑层
├── controller/                    # REST 控制器
│   └── admin/                     # 管理后台控制器
├── dto/                           # 数据传输对象
│   ├── auth/                      # 认证相关 DTO
│   ├── request/                   # 请求 DTO
│   └── response/                  # 响应 DTO
└── security/                      # JWT 安全组件
```
