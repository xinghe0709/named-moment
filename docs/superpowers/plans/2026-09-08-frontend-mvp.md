# “此刻有名”前端 MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有 Spring Boot 应用中交付可直接使用的文字匹配与私人档案馆前端。

**Architecture:** 使用 `src/main/resources/static` 中的原生 HTML、CSS 和 ES Modules，不增加前端构建链。`domain.mjs` 负责纯数据校验与格式化，`api.mjs` 封装统一 Result API，`app.mjs` 只负责编排页面状态和安全 DOM 渲染。

**Tech Stack:** Spring Boot 4.1.1、Java 21、原生 HTML/CSS/JavaScript、Node.js 内置测试、JUnit 5、Maven。

**Spec:** `docs/superpowers/specs/2026-09-08-frontend-design.md`

## Global Constraints

- 页面必须随 Spring Boot 一起构建与启动，不引入 npm 依赖或独立开发服务器。
- 首版仅支持 5–2000 字纯文字输入。
- 匹配结果固定展示三个有来源的真实概念。
- 页面不得把模型、用户或数据库文本作为 HTML 注入。
- 用户可保存、查看和删除档案记录。
- 需支持键盘、清晰焦点、移动端与 reduced motion。

---

### Task 1: 建立前端资源契约

**Files:**
- Create: `src/test/java/com/example/namedmoment/FrontendResourceTest.java`
- Create: `src/test/frontend/domain.test.mjs`
- Create: `src/test/frontend/api.test.mjs`

**Interfaces:**
- Consumes: Spring classpath 资源读取；Node `node:test`。
- Produces: 对静态入口、无障碍契约、数据校验和四类 API 请求的可重复验证。

- [ ] **Step 1: 写入失败的资源与 JavaScript 单元测试**

  Java 测试读取 `static/index.html`、`static/css/styles.css` 与三个 `.mjs` 文件；Node 测试导入尚不存在的 `domain.mjs` 与 `api.mjs`，验证输入长度、统一 Result 解包、请求方法和请求体。

- [ ] **Step 2: 验证测试因资源尚不存在而失败**

  Run: `mvn -Dtest=FrontendResourceTest test`

  Expected: FAIL，原因是 `static/index.html` 不存在。

  Run: `node --test src/test/frontend/*.test.mjs`

  Expected: FAIL，原因是目标 ES Modules 不存在。

### Task 2: 实现纯数据与 API 层

**Files:**
- Create: `src/main/resources/static/js/domain.mjs`
- Create: `src/main/resources/static/js/api.mjs`

**Interfaces:**
- Consumes: `fetch` 和后端 `{code,message,data}` 响应。
- Produces: `validateInput(text)`、`formatScore(score)`、`formatDate(value)`、`modeLabel(mode)`、`createEmotionApi(fetchImpl)`。

- [ ] **Step 1: 实现使 Node 测试通过的最小代码**

  `validateInput` 返回 `{valid,text,message,count}`；`createEmotionApi` 暴露 `match`、`saveRecord`、`listRecords`、`deleteRecord`，非零业务码与非 2xx HTTP 都抛出可读错误。

- [ ] **Step 2: 运行 JavaScript 单元测试**

  Run: `node --test src/test/frontend/*.test.mjs`

  Expected: 全部 PASS。

### Task 3: 实现输入、结果与档案页面

**Files:**
- Create: `src/main/resources/static/index.html`
- Create: `src/main/resources/static/css/styles.css`
- Create: `src/main/resources/static/js/app.mjs`
- Create: `src/main/resources/static/assets/seeded-glass.png`

**Interfaces:**
- Consumes: Task 2 的数据与 API 接口；设计稿 `.impeccable/mocks/home-b-approved.png`。
- Produces: `/` 下可交互的单页应用，包含 compose、loading、results、archive、empty 和 error 状态。

- [ ] **Step 1: 编写语义 HTML 骨架**

  `body` 第一项保留方向契约注释；页面包含品牌导航、输入表单、`aria-live` 状态区、结果区、档案区和错误区域。

- [ ] **Step 2: 用玻璃墙网格完成桌面与移动视觉**

  CSS 使用既定六色 token、4–6px 金属分隔线、无圆角窗格、压花玻璃贴图、明确 `:focus-visible` 与 `prefers-reduced-motion`。

- [ ] **Step 3: 编排匹配与保存流程**

  `app.mjs` 绑定表单，调用 `match`，用 DOM API 渲染情感指纹和三个候选；保存请求使用当前原文与候选的 `conceptId`、`matchScore`、`explanation`。

- [ ] **Step 4: 编排档案读取与删除流程**

  点击“私人档案馆”时加载记录；删除按钮采用页内二次确认，并在成功后移除对应 DOM 与更新空状态。

- [ ] **Step 5: 验证资源契约转绿**

  Run: `mvn -Dtest=FrontendResourceTest test`

  Expected: PASS。

### Task 4: 更新使用文档并完成构建验证

**Files:**
- Modify: `README.md`
- Create: `DESIGN.md`

**Interfaces:**
- Consumes: 已完成页面和最终浏览器截图。
- Produces: 正确入口说明、视觉系统记录和可重复验证命令。

- [ ] **Step 1: 更新 README**

  删除“当前只包含后端 API”，把 `http://localhost:8080/` 设为首要入口，并补充前端功能与测试命令。

- [ ] **Step 2: 运行完整自动化验证**

  Run: `node --test src/test/frontend/*.test.mjs`

  Run: `mvn clean verify`

  Expected: 两组测试均成功，Spring Boot JAR 包含静态资源。

- [ ] **Step 3: 启动应用并验证实际浏览器**

  启动 Spring Boot，分别以 1440px 和 390px 宽度检查首页、匹配、保存、档案、删除和错误状态；确认浏览器控制台无错误。

- [ ] **Step 4: 运行机械设计检查并记录最终系统**

  Run: `node /Users/wxh/.codex/plugins/cache/openai-curated-remote/impeccable/4.1.1/skills/impeccable/scripts/detect.mjs --json src/main/resources/static/index.html src/main/resources/static/css/styles.css src/main/resources/static/js/app.mjs`

  根据实际成品写入 `DESIGN.md`，保存 `.impeccable/review/desktop.png` 与 `.impeccable/review/mobile.png`。
