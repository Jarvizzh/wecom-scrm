# WeCom SCRM UI

基于 Vue 3 + TypeScript + Vite + Element Plus 构建的企业微信私域运营管理系统前端。

## 1. 架构与功能实现介绍

### 技术栈
- **核心框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **语言**: TypeScript
- **UI 组件库**: Element Plus
- **路由管理**: Vue Router 4
- **状态管理**: 浏览器本地存储 (LocalStorage) + Vue 响应式数据
- **图表库**: ECharts
- **网络请求**: Axios

### 架构设计
- **布局系统**: 采用响应式侧边栏布局 (`MainLayout.vue`)，支持折叠，集成面包屑导航与用户信息管理。
- **动态代理**: 开发环境下通过 Vite Proxy 解决跨域问题，将 `/api` 请求转发至后端服务。
- **权限控制**: 路由层面集成 `beforeEach` 守卫，通过 Token 校验强制登录。
- **组件化设计**: 抽离常用的功能组件，如移动端预览 (`MobilePreview.vue`)、企业表单 (`EnterpriseFormDialog.vue`) 等。

### 项目目录结构
```text
wecom-scrm-ui/
├── public/              # 公共静态资源
├── src/
│   ├── api/             # 后端请求模块，按业务拆分（customer, tag, user等）
│   ├── assets/          # 本地静态资源（图片、SVG等）
│   ├── layout/          # 全局布局组件（侧边栏、顶部导航、主视图区域）
│   ├── router/          # 路由配置与权限导航守卫
│   ├── views/           # 页面级组件
│   │   ├── mp/          # 公众号管理相关页面
│   │   └── ...          # 客户/群聊/消息/企业等管理页面
│   ├── App.vue          # 根组件
│   ├── main.ts          # 入口文件（初始化Vue、插件挂载）
│   └── style.css        # 全局设计系统样式与 CSS 变量
├── index.html           # 应用入口 HTML
├── package.json         # 项目依赖与执行脚本
├── tsconfig.json        # TypeScript 配置
└── vite.config.ts       # Vite 构建与开发服务器配置
```

### 核心功能
- **仪表盘**: 数据概览，集成 ECharts 展示客户增长趋势等关键指标。
- **客户管理**: 
  - 支持多维度筛选（姓名、UnionID、归属人、来源公众号、标签）。
  - 支持批量打标签、手动触发数据同步。
- **客户群管理**: 统计与查看企业微信外部群聊。
- **消息推送**: 
  - **客户群发**: 支持图文、链接等多种类型的单聊群发。
  - **客户群群发**: 支持对群聊进行批量推送。
  - **朋友圈运营**: 统一管理员工朋友圈发布任务。
- **自动化运营**: 
  - **欢迎语管理**: 配置新客户添加后的自动回复，支持手机端实时预览。
- **多企业管理**: 支持多租户架构，可动态切换和维护不同的企业配置（CorpID、Secret）。
- **公众号集成**: 关联微信公众号，实现 UnionID 级别的客户打通与粉丝同步。
- **内部管理**: 员工列表与同步日志，确保数据一致性。

---

## 2. 运行、打包与部署指引

### 环境准备
- **Node.js**: 推荐 v18.0 或更高版本。
- **npm**: 推荐 9.0 或更高版本。

### 本地开发运行
1. **克隆项目**:
   ```bash
   git clone <repository-url>
   cd wecom-scrm-ui
   ```

2. **安装依赖**:
   ```bash
   npm install
   ```

3. **配置后端接口地址**:
   在 `vite.config.ts` 中修改 proxy 配置，确保指向正确的后端服务：
   ```typescript
   proxy: {
     '/api': {
       target: 'http://localhost:8080', // 填入你的后端地址
       changeOrigin: true
     }
   }
   ```

4. **启动服务**:
   ```bash
   npm run dev
   ```
   启动后访问 `http://localhost:3000`。

### 打包生产版本
1. **执行构建**:
   ```bash
   npm run build
   ```
2. **构建产物**: 生成的静态文件位于 `dist` 目录。

### 部署指引 (Nginx)
建议使用 Nginx 部署 `dist` 目录，并配置 API 转发以解决生产环境跨域及路由指向问题。

**Nginx 配置示例**:
```nginx
server {
    listen       80;
    server_name  your-domain.com;

    location / {
        try_files $uri /index.html;
    }
    
    # 代理后端 API
    location /api {
        proxy_pass http://backend-service:8080; # 后端服务地址
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 项目开发注意事项
- **Icon 使用**: 项目集成 `@element-plus/icons-vue`，推荐直接按需引入。
- **代码规范**: 请遵循 TypeScript 严格模式，确保类型定义清晰。
- **响应式**: UI 适配主流桌面分辨率，侧边栏支持自动/手动收缩。
