# 此刻有名（Named Moment）

一个小而美的情感概念匹配应用。用户写下一段日记或想法，系统会生成结构化“情感指纹”，再从 100 个有来源的世界语言词汇与文化概念中返回匹配度最高的 3 个。用户可以选出最像自己的一个，并把这一刻留进私人情感档案馆。

前端使用原生 HTML、CSS 与 JavaScript，直接随 Spring Boot 打包和启动，不需要单独安装 Node 依赖或运行前端服务。

## MVP 能力

- Spring AI 把自由文本转换为 `EmotionFingerprint` 结构化对象。
- Embedding + PostgreSQL/pgvector 召回候选概念。
- Chat Model 对候选重排，最终事实字段始终从数据库回填。
- RAG 失败时，Advisor 强制模型先调用数据库工具，再对工具返回的真实候选做结构化重排。
- 用户可保存、查看和删除自己选中的情感记录。
- 100 条概念种子全部包含中文含义、场景描述和 HTTPS 来源。
- 一体化响应式页面覆盖输入、等待、三个候选、保存、档案列表、删除、空状态与错误状态。

## 技术栈

- Java 21（启用虚拟线程，业务代码保持 Java 8 风格）
- Spring Boot 4.1.1
- Spring AI 2.0.1
- PostgreSQL 17 + pgvector
- MyBatis、Lombok、Maven
- SpringDoc OpenAPI / Swagger UI

## 本地启动

### 1. 前置条件

- JDK 21
- Maven 3.9+
- Docker Desktop
- 已启动的 OpenAI 兼容模型服务（聊天模型与 Embedding 模型）

确认版本：

```bash
java -version
mvn -version
docker info
```

### 2. 准备环境变量

```bash
cp .env.example .env
```

默认连接 `http://127.0.0.1:8000/v1`。把 `.env` 中的 `AI_API_KEY` 替换为本地服务密钥。Spring AI 2.0.1 使用 OpenAI SDK，`AI_BASE_URL` 需要包含 `/v1`。然后让当前终端加载配置：

```bash
set -a
source .env
set +a
```

`.env` 已被 Git 忽略，不要提交真实密钥。

### 3. 启动 PostgreSQL

```bash
docker compose up -d postgres
docker compose ps
```

这一步只启动数据库。`vector` 扩展、两张表和 100 条概念数据由下一步的 Spring Boot 初始化脚本创建。

### 4. 启动应用

```bash
mvn spring-boot:run
```

启动时会先执行 `schema.sql` 和 `data.sql`，再为缺少向量的概念调用 Embedding API。初始化按每批 20 条查询、逐条写入；失败项会记录警告，并在下一次启动时重试。未设置 `AI_API_KEY` 时会跳过向量初始化并只记录一次警告，记录类接口仍可使用，但匹配接口不可用。全部完成后可执行：

```bash
docker exec named-moment-postgres psql -U postgres -d named_moment \
  -c "select count(*) from emotion_concept where embedding is null;"
```

预期结果为 `0`。

## 使用入口

- 此刻有名：[http://localhost:8080/](http://localhost:8080/)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- 完整请求示例：[docs/api-examples.md](docs/api-examples.md)

## 验证

运行全部测试：

```bash
mvn clean verify
```

前端纯逻辑测试使用 Node.js 内置测试运行器，不需要安装依赖：

```bash
node --test src/test/frontend/*.test.mjs
```

`FrontendResourceTest` 会随 Maven 测试一起验证首页、响应式与无障碍契约、ES Modules 和玻璃纹理素材是否被正确打包。

默认测试不会连接本地模型。需要额外验证真实 Spring AI 工具调用时，在已加载 `.env` 的终端运行：

```bash
RUN_LOCAL_AI_IT=true mvn -Dtest=EmotionFallbackLocalTest test
```

该测试会确认模型生成检索关键词、Advisor 强制首轮工具调用、`emotion_concept` 查询以及最终三个合法 ID 的结构化重排。

运行 20 条真实匹配质量评测：

```bash
RUN_LOCAL_AI_EVAL=true mvn -Dtest=EmotionMatchEvaluationLocalTest test
```

测试会执行完整的情感指纹、Embedding、向量召回、重排和必要的工具兜底链路。它要求全部结果通过数据库事实与结构检查，并且至少 16 条在 Top 3 中命中人工标注的可接受概念。详细结果写入 `target/emotion-evaluation-results.json`，固定评测集和人工复核记录见 [docs/evaluation-cases.md](docs/evaluation-cases.md)。

同时验证全部单元测试、真实工具调用和 20 条评测：

```bash
RUN_LOCAL_AI_IT=true RUN_LOCAL_AI_EVAL=true mvn test
```

运行数据库质量门禁：

```bash
docker exec -i named-moment-postgres psql -U postgres -d named_moment \
  < docs/sql/concept-quality-check.sql
```

必须满足：概念数为 100、缺字段与重复查询为空；完成首次向量初始化后，缺失向量数为 0，向量维度只有 512。

## 停止服务

```bash
docker compose down
```

命名卷会保留数据库数据。只有明确需要清空本地数据库时才使用 `docker compose down -v`，它会删除全部本地记录和已生成向量。

## 当前边界

- 仅支持纯文字输入。
- 无登录与多用户隔离，情感记录是本机共享数据。
- 不把匹配结果当作心理诊断或治疗建议。
- 来源当前以研究者策展的 Positive Lexicography 为统一基线；正式发布前可优先为高频概念补充语言权威词典链接。
