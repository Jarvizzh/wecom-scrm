# Venus WeCom SCRM

**Venus** 是一个专为企业微信设计的全链路私域运营管理系统（SCRM），旨在帮助企业高效管理客户关系、自动化运营流程并深度挖掘私域流量价值。

---

## 🚀 项目概览

Venus 采用前后端分离架构，集成了企业微信、微信公众号及多租户管理能力，能够助力企业快速搭建属于自己的私域运营基础设施。

### 核心功能
- **多租户/多企业支持**: 动态数据源架构，支持同时管理多个企业微信实例（多 CorpID）。
- **客户与群聊同步**: 自动化同步全量客户数据、外部群聊信息及标签体系。
- **消息群发引擎**: 支持对客户及客户群进行精准的消息推送（图文、链接、小程序等）。
- **自动化运营**: 提供欢迎语自动化回复、朋友圈任务管理等功能，提升触达效率。
- **公私域联动**: 结合微信公众号，通过 UnionID 实现粉丝与客户的数据打通。
- **数据可视化**: 内置仪表盘，实时监控客户增长趋势与运营成效。

---

## 🛠 技术栈

### 后端 (wecom-scrm-server)
- **框架**: Spring Boot 2.7, Spring Security
- **数据层**: Spring Data JPA, MyBatis, Dynamic Datasource
- **中间件**: MySQL 8.0, Redis
- **SDK**: [WxJava](https://github.com/binarywang/weixin-java-mp-multi) (WeChat Java SDK)
- **安全**: JWT (JSON Web Token)

### 前端 (wecom-scrm-ui)
- **框架**: Vue 3 (Composition API)
- **构建**: Vite, TypeScript
- **UI 组件**: Element Plus
- **图表**: ECharts
- **网络**: Axios

---

## 📂 项目结构

```text
wecom-scrm/
├── wecom-scrm-server/    # Spring Boot 后端服务
│   ├── src/main/java     # Java 业务源码
│   ├── src/main/resources # 配置文件及数据库脚本
│   └── pom.xml           # Maven 依赖管理
├── wecom-scrm-ui/        # Vue 3 前端应用
│   ├── src/              # 前端源码 (Vue/TS)
│   ├── vite.config.ts    # Vite 配置
│   └── package.json      # 前端依赖管理
└── DEVELOPMENT_GUIDES.md # 核心开发规范与准则
```

---

## 🚥 快速开始

### 1. 环境准备
- **Java**: JDK 8+ (推荐 JDK 11)
- **Node.js**: v18.0+
- **MySQL**: 8.0+
- **Redis**: 5.0+

### 2. 后端启动
1. 创建 MySQL 数据库（如 `wecom_scrm`）。
2. 执行 `wecom-scrm-server/src/main/resources/db/schema.sql` 初始化表结构。
3. 修改 `wecom-scrm-server/src/main/resources/application.properties` 中的数据库与 Redis 连接配置。
4. 运行 `WecomScrmServerApplication` 或执行 `mvn spring-boot:run`。

### 3. 前端启动
1. 进入目录：`cd wecom-scrm-ui`
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`
4. 访问 `http://localhost:3000` (默认配置)。

---

## 📝 开发规范

为了保证代码质量与系统稳定性，请在开发前务必阅读 [DEVELOPMENT_GUIDES.md](./DEVELOPMENT_GUIDES.md)。核心要点包括：
- **禁止使用泛型 Map**: 数据库查询与 API 传输必须使用明确的 Entity/DTO/VO 类。
- **显式导入**: 禁止在代码中使用全路径类名引用。
- **多租户意识**: 业务逻辑需考虑多企业上下文切换。

---

## 📜 开源协议
[MIT License](LICENSE)
