# Venus Server

**让每一次触达，都充满吸引力**

Venus 是基于 Spring Boot 2.7 构建的高性能企业微信私域运营管理系统。

## 1. 架构与功能实现介绍

### 技术栈
- **核心框架**: Spring Boot 2.7.12
- **持久层**: Spring Data JPA
- **数据源管理**: `dynamic-datasource-spring-boot-starter` (支持多租户/多企业动态切换)
- **数据库**: MySQL 8.0 / H2 (开发模式)
- **缓存**: Redis (包含 Embedded Redis 用于开发调试)
- **安全与认证**: Spring Security + JWT (jjwt)
- **企微/微信集成**: `weixin-java-cp` & `weixin-java-mp` (BinaryWang SDK)
- **便捷工具**: Lombok, Maven

### 架构设计
- **多租户架构**: 利用动态数据源技术，系统可以根据当前操作的企业上下文自动切换数据源，支持多 CorpID 并在隔离的数据库环境中运行。
- **任务调度**: 内置 `task` 包处理定时任务，如定期从企业微信同步客户列表、标签库及群聊信息。
- **事件驱动**: 预留 `handler` 模块，用于处理企业微信的回调事件（如客户添加、删除、属性变更等）。
- **统一响应**: 提供标准化的异常处理与响应封装，确保前端接口调试的一致性。

### 核心功能模块
- **企业管理**: 维护企业的 CorpID, Secret 等核心凭证。
- **客户/群聊同步**: 全量及增量同步企业微信侧的客户及外部群数据。
- **标签系统**: 同步企微标签组及标签，并支持批量打标。
- **消息群发引擎**: 对接企微群发接口，实现客户单聊与群聊的自动推送。
- **朋友圈助手**: 管理内部员工的朋友圈发布任务。
- **公众号协同**: 通过 UnionID 实现企微客户与公众号粉丝的数据对齐。

---

## 2. 运行、打包与部署指引

### 环境要求
- **JDK**: 8 或更高版本 (推荐 JDK 11)。
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Redis**: 5.0+

### 本地开发测试
1. **数据库初始化**: 
   在 MySQL 中创建数据库 (例如 `wecom_scrm`)，并执行 `src/main/resources/db/schema.sql`。
2. **配置文件**: 
   修改 `src/main/resources/application.properties`，配置 MySQL 连接、Redis 地址以及 JWT 密钥：
   ```properties
   spring.datasource.dynamic.datasource.master.url=jdbc:mysql://localhost:3306/wecom_scrm...
   spring.redis.host=localhost
   jwt.secret=your_super_secret_key_at_least_32_chars
   ```
3. **运行**: 
   执行 `com.wecom.scrm.WecomScrmServerApplication` 中的 `main` 方法，或使用 Maven 命令：
   ```bash
   mvn spring-boot:run
   ```

### 打包构建
1. **执行打包**:
   ```bash
   mvn clean package -DskipTests
   ```
2. **产物**: 构建完成后，在 `target/` 目录下生成 `server-1.0.0-SNAPSHOT.jar`。

### 部署说明
1. **JAR 运行**:
   ```bash
   java -jar server-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
   ```
2. **反向代理**: 
   建议在生产环境下前端页面与后端接口同域部署，通过 Nginx 转发 `/api` 路径到后端服务的 8080 端口。

---

## 3. 核心目录结构
```text
wecom-scrm-server/
├── src/main/java/com/wecom/scrm/
│   ├── config/      # 全局配置 (Security, Redis, Wecom SDK)
│   ├── controller/  # 控制器层 (RESTful APIs)
│   ├── service/     # 业务逻辑层 (多租户处理、企微对接)
│   ├── repository/  # 数据访问层 (JPA Repositories)
│   ├── entity/      # 数据库实体类
│   ├── dto/vo/      # 数据传输对象与视图对象
│   ├── task/        # 定时同步任务
│   ├── handler/     # 企微事件回调处理
│   └── security/    # JWT 认证过滤器与授权逻辑
├── src/main/resources/
│   ├── db/          # 数据库初始化脚本 (schema.sql)
│   └── application.properties # 核心配置文件
└── pom.xml          # Maven 依赖配置
```
