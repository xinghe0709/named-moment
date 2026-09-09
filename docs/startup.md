# 此刻有名：本地启动与排错

本文适用于项目根目录 `~/named-moment`。前端已经随 Spring Boot 打包，不需要另外启动前端服务。

## 本次“点击寻找名字无反应”的原因

日志中的以下三行已经给出了完整因果链：

```text
Started NamedMomentApplication
stage=embedding-init status=skipped reason=missing-api-key
stage=fingerprint status=failed type=UnauthorizedException
```

这不是 Spring Boot 启动失败：应用和 PostgreSQL 都已经启动成功。真正失败的是点击按钮后的 AI 请求。

启动命令没有把项目根目录的 `.env` 加载到 Spring 环境，应用使用了占位 API Key，模型服务因此返回 HTTP 401。项目现在会自动读取根目录 `.env`，不再要求每次打开终端后手动执行 `source .env`。

## 1. 检查本地依赖

需要：

- Java 21 或更高版本；
- Maven 3.9 或更高版本；
- Docker Desktop；
- 一个兼容 OpenAI API 的本地模型服务；
- 聊天模型和 Embedding 模型均已加载。

```bash
java -version
mvn -version
docker info
```

项目按 Java 21 编译。即使本机使用 Java 23 或 Java 25，只要 `mvn -version` 显示的 Java 版本不低于 21，也可以正常构建。

## 2. 准备 `.env`

始终从项目根目录执行：

```bash
cd ~/named-moment
cp .env.example .env
```

编辑 `.env`，确认以下配置与本地模型服务一致：

```dotenv
AI_BASE_URL=http://127.0.0.1:8000/v1
AI_API_KEY=填写本地服务密钥
AI_CHAT_MODEL=Qwen3.6-35B-A3B-4bit
AI_EMBEDDING_MODEL=Qwen3-Embedding-0.6B-8bit
```

不要提交 `.env`。它已被 `.gitignore` 忽略。

应用通过下面的配置自动加载项目根目录 `.env`：

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

因此正常启动时不需要执行 `source .env`。如果从其他目录启动打包后的 JAR，需要在那个工作目录准备 `.env`，或显式设置系统环境变量。

## 3. 启动 PostgreSQL

先确保 Docker Desktop 已运行：

```bash
cd ~/named-moment
docker compose up -d postgres
docker compose ps
```

`docker compose ps` 中的 `named-moment-postgres` 应显示为 `healthy`。

## 4. 检查本地模型服务

确认模型管理工具中两个模型都处于已加载状态，然后执行：

```bash
cd ~/named-moment
set -a
source .env
set +a
curl -sS \
  -H "Authorization: Bearer $AI_API_KEY" \
  "$AI_BASE_URL/models"
```

这里临时加载 `.env` 只是为了让 `curl` 使用相同配置，不是 Spring Boot 启动所必需的。请求成功时会返回可用模型列表；401 表示密钥不匹配，连接失败表示本地模型服务未监听配置的端口。

## 5. 启动应用

```bash
cd ~/named-moment
mvn spring-boot:run
```

启动成功后重点检查三类日志：

```text
Started NamedMomentApplication
stage=startup-diagnostics status=ready aiKeyConfigured=true ...
stage=embedding-init status=complete processedCount=... failedCount=0 ...
```

第一次启动会为缺少向量的概念调用 Embedding API，耗时会比后续启动长。`processedCount=0` 表示已有概念都存在向量，不是错误。

打开：

- 页面：<http://localhost:8080/>
- Swagger UI：<http://localhost:8080/swagger-ui/index.html>
- OpenAPI：<http://localhost:8080/v3/api-docs>

## 6. 验证接口

```bash
curl -i http://localhost:8080/api/emotions/records
```

响应头会包含：

```text
X-Request-Id: ...
```

每次 API 请求的所有业务日志都会带相同的 `requestId`。定位一次失败请求时，直接复制这个 ID 搜索整段日志即可。请求日志会记录路径、HTTP 状态、总耗时、匹配阶段、候选数量、模型异常根因和堆栈，但不会记录用户日记正文或 API Key。

匹配成功时，核心链路类似：

```text
event=http-request outcome=started method=POST path=/api/emotions/match
stage=match status=started inputLength=...
stage=fingerprint status=success durationMs=...
stage=vector-recall status=success recalledCount=... validCount=...
stage=rerank status=success attempt=1 matchCount=3
stage=match status=success mode=RAG candidateCount=3 durationMs=...
event=http-request outcome=completed ... httpStatus=200 durationMs=...
```

## 常见错误对照

| 日志或现象 | 原因 | 处理方式 |
|---|---|---|
| `aiKeyConfigured=false` | `.env` 不存在、Key 为空或不是从项目根目录启动 | 检查 `~/named-moment/.env`，然后重新启动应用 |
| `missing-api-key` | AI Key 没有进入 Spring 配置 | 检查 `.env` 后重启，不能只刷新浏览器 |
| `UnauthorizedException` / HTTP 401 | 模型服务收到错误或占位 Key | 用上面的 `/models` 命令验证 `.env` 中的 Key |
| PostgreSQL `Connection refused` | Docker Desktop 或数据库容器未运行 | 执行 `docker compose up -d postgres` |
| `127.0.0.1:8000 Connection refused` | 本地模型服务未启动或端口不一致 | 启动模型服务并核对 `AI_BASE_URL` |
| `Port 8080 was already in use` | 旧的应用进程仍在运行 | 在旧终端按 `Ctrl+C`，再重新启动 |
| `embedding-init ... failedCount` 大于 0 | 部分概念向量生成失败 | 根据同一行附近的 `rootType`、`rootMessage` 检查模型和维度配置，修复后重启会自动重试 |
| 页面能打开但匹配失败 | 静态页面正常，不代表 AI 调用正常 | 搜索该请求的 `requestId`，从 `stage=fingerprint` 开始查看 |

## 停止服务

应用终端按：

```text
Ctrl+C
```

停止 PostgreSQL 容器但保留数据：

```bash
docker compose down
```

不要随意执行 `docker compose down -v`，它会删除本地数据库记录和已生成的向量。
