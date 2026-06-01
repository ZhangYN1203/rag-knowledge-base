# RAG 智能知识库问答系统

基于 **RAG（Retrieval-Augmented Generation，检索增强生成）** 技术的智能知识库问答平台。用户上传知识库文档后，系统自动解析文档内容并构建向量索引，在问答环节通过语义检索匹配最相关的文档片段，结合大语言模型生成精准回答，同时展示引用来源。

---

## 📋 目录

- [系统功能](#系统功能)
- [技术架构](#技术架构)
- [在线访问](#在线访问)
- [本地运行](#本地运行)
- [环境变量说明](#环境变量说明)
- [部署说明](#部署说明)
- [前端接口转发](#前端接口转发)
- [安全说明](#安全说明)
- [项目目录结构](#项目目录结构)
- [使用说明](#使用说明)
- [常见问题](#常见问题)

---

## ✨ 系统功能

| 功能模块 | 说明 |
|---------|------|
| **用户注册与登录** | 支持用户注册、登录、JWT 令牌自动刷新 |
| **文档上传与知识库管理** | 支持 TXT、PDF、Word 文档上传，文档列表查看与删除 |
| **文档内容解析与向量化** | 后端自动解析文档内容，分块后调用嵌入模型生成向量并存储 |
| **基于知识库的智能问答** | 用户提问时检索相关文档片段，结合 LLM 生成带引用的回答 |
| **对话历史保存** | 每次问答自动保存到数据库，支持历史会话查看与删除 |
| **引用来源展示** | AI 回答中标注引用编号，前端展示对应的文档来源片段 |
| **Prompt 模板管理** | 支持自定义提示词模板，使用 `{{variable}}` 占位符动态注入 |
| **模型配置管理** | 前端页面可查看当前 AI 提供商、模型名称、API 地址等配置信息 |

---

## 🏗️ 技术架构

### 整体架构

```
用户浏览器 ──→ Vercel（前端 SPA） ──→ Railway（后端 API） ──→ SiliconFlow API（AI 服务）
                                            │
                                          H2 数据库
```

### 前端

| 技术 | 用途 |
|------|------|
| Vue 3 | 前端框架 |
| TypeScript | 类型安全 |
| Vite 5 | 构建工具 |
| Pinia | 状态管理 |
| Element Plus | UI 组件库 |
| Axios | HTTP 请求 |
| Marked + highlight.js | Markdown 渲染 |

### 后端

| 技术 | 用途 |
|------|------|
| Spring Boot 3.2.5 | 核心框架 |
| Spring Security + JWT | 用户认证与授权 |
| Spring AI 1.0.0 | AI 模型集成 |
| Spring Data JPA + Hibernate | 数据库 ORM |
| H2 File DB | 关系数据库与向量存储 |
| Apache PDFBox / POI | PDF / Word 文档解析 |
| Maven | 项目构建 |

### AI 服务

| 服务 | 用途 |
|------|------|
| SiliconFlow API | AI 模型调用平台 |
| Qwen/Qwen2.5-7B-Instruct | 对话生成模型 |
| BAAI/bge-m3 | 文本嵌入模型 |

### 部署

| 平台 | 组件 |
|------|------|
| Vercel | 前端静态部署 |
| Railway | 后端容器化部署（Docker） |

---

## 🌐 在线访问

| 组件 | 地址 |
|------|------|
| 🖥️ **前端页面** | [https://rag-knowledge-base-beige.vercel.app](https://rag-knowledge-base-beige.vercel.app) |
| ⚙️ **后端 API** | [https://rag-knowledge-base-api-production.up.railway.app](https://rag-knowledge-base-api-production.up.railway.app) |

> ⚠️ 后端根路径直接访问可能返回 403（属于 Spring Security 的正常访问控制），但前端通过 `/api/**` 路径转发可以正常调用所有接口。Railway 免费实例在无访问时可能进入休眠，首次请求需要等待 30-60 秒唤醒。

---

## 🚀 本地运行

### 前置环境要求

- JDK 17+
- Node.js 18+
- Maven（或使用项目自带的 `mvnw`）

### 后端启动

```bash
# 1. 设置 API Key（硅基流动平台获取）
set OPENAI_API_KEY=your_siliconflow_api_key

# 2. 启动后端（openai profile 使用云端 API）
mvnw spring-boot:run -Dspring-boot.run.profiles=openai
```

后端启动在 **http://localhost:8080**

### 前端启动

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

前端启动在 **http://localhost:3000**

---

## 🔐 环境变量说明

以下环境变量需要在运行后端时配置，**不要将真实值写入代码或仓库**：

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `OPENAI_API_KEY` | SiliconFlow API Key（必填） | `sk-your-key-here` |
| `SPRING_PROFILES_ACTIVE` | Spring 激活 profile | `openai` |
| `JWT_SECRET` | JWT 签名密钥 | `your-jwt-secret` |

---

## 📦 部署说明

### Vercel 前端部署

| 配置项 | 值 |
|--------|-----|
| Framework Preset | **Vite** |
| Root Directory | `frontend` |
| Build Command | `npm run build` |
| Output Directory | `dist` |
| Install Command | `npm install` |

### Railway 后端部署

后端通过项目根目录的 `Dockerfile` 构建 Docker 镜像并启动。在 Railway 控制台中需要配置以下环境变量：

- `OPENAI_API_KEY`
- `SPRING_PROFILES_ACTIVE`（设为 `openai`）
- `JWT_SECRET`

Railway 会自动检测 `Dockerfile` 并执行多阶段构建（Maven 编译 → JRE 运行）。

---

## 🔀 前端接口转发

前端 `frontend/vercel.json` 中配置了 API 请求转发规则，将前端 `/api/*` 路径的请求转发到 Railway 后端：

```json
{
  "rewrites": [
    {
      "source": "/api/(.*)",
      "destination": "https://rag-knowledge-base-api-production.up.railway.app/api/$1"
    }
  ]
}
```

例如前端请求 `/api/auth/login` 会被转发到 `https://rag-knowledge-base-api-production.up.railway.app/api/auth/login`。

---

## 🛡️ 安全说明

- **API Key 保护**：SiliconFlow API Key 仅存储在 Railway 环境变量中，不会写入 GitHub 仓库、README、Dockerfile、配置文件或前端代码
- **AI 请求转发**：所有 AI 请求由后端统一调用 SiliconFlow API，前端不直接接触 API Key
- **用户鉴权**：系统使用 JWT 进行用户认证，注册和登录接口公开，其余接口需要携带有效 Token
- **CORS 安全**：后端配置了跨域访问控制，允许 Vercel 前端域名访问

---

## 📁 项目目录结构

```
rag-knowledge-base/
├── frontend/                          # Vue 3 前端项目
│   ├── src/
│   │   ├── api/                       # API 调用与类型定义
│   │   ├── components/                # 通用组件（ChatMessage、ChatInput 等）
│   │   ├── views/                     # 页面组件（Chat、Documents、Templates、Config）
│   │   ├── stores/                    # Pinia 状态管理
│   │   ├── router/                    # 路由配置
│   │   └── utils/                     # 工具函数（Markdown 渲染、时间格式化）
│   └── vercel.json                    # Vercel 部署与 API 转发配置
├── src/main/java/com/example/app/     # 后端 Java 源码
│   ├── config/                        # 配置类（Security、JWT、AI、CORS）
│   ├── controller/                    # REST API 控制器
│   ├── service/                       # 业务逻辑层
│   ├── repository/                    # 数据访问层
│   ├── entity/                        # JPA 实体类
│   └── dto/                           # 数据传输对象
├── src/main/resources/                # 配置文件（application.yml 等）
├── src/test/                          # 单元测试
├── docs/                              # 项目文档
│   ├── Prompt 报告.md                 # Prompt 设计报告
│   ├── 系统架构图.md                  # 系统架构与交互流程
│   └── 5.23记录.md                    # 问题解决记录
├── Dockerfile                         # 后端 Docker 构建文件
├── render.yml                         # Render 部署配置
├── vercel.json                        # Vercel 部署配置（根路径）
└── README.md                          # 项目说明文档（当前文件）
```

---

## 📖 使用说明

1. **打开前端地址**：浏览器访问 [https://rag-knowledge-base-beige.vercel.app](https://rag-knowledge-base-beige.vercel.app)
2. **注册账号**：点击登录页面的注册链接，输入用户名、邮箱和密码完成注册
3. **登录系统**：使用注册的账号登录
4. **上传知识库文档**：进入知识库管理页面，上传 TXT、PDF 或 Word 文档
5. **智能问答**：进入聊天页面，在输入框中输入问题，系统将基于已上传的文档内容生成回答
6. **查看引用来源**：AI 回答后会标注引用编号，点击可查看对应的文档片段

---

## ❓ 常见问题

**Q：前端页面能打开但登录失败？**
A：检查 Railway 后端是否已启动完成。免费实例在无访问时会休眠，首次请求可能需要等待 30-60 秒。

**Q：直接访问后端根路径返回 403？**
A：这是 Spring Security 的正常访问控制行为，不影响功能使用。前端通过 `/api/**` 路径转发可正常调用所有接口。

**Q：Vercel 构建失败？**
A：检查 `frontend` 目录配置是否正确，确保本地 `npm run build` 可以成功构建。确认 `vercel.json` 中不含 `rootDirectory` 等不支持字段。

**Q：Railway 部署启动失败？**
A：检查环境变量 `OPENAI_API_KEY`、`SPRING_PROFILES_ACTIVE`、`JWT_SECRET` 是否已正确配置。查看 Railway 构建日志确认 Dockerfile 构建是否成功。

---

## 📄 许可证

MIT License

---

## 📬 项目仓库

- 个人仓库：[https://github.com/ZhangYN1203/rag-knowledge-base](https://github.com/ZhangYN1203/rag-knowledge-base)
- 组织仓库：[https://github.com/cs-sbs/personal-project-ZhangYN1203](https://github.com/cs-sbs/personal-project-ZhangYN1203)

如有问题请提交 Issue。