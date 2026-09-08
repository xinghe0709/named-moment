# Evaluation and Prompt Refinement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为「此刻有名」增加可重复的 20 条本地模型评测，并依据基线结果优化四套提示词与必要的最小匹配逻辑。

**Architecture:** 使用一个默认禁用的 Spring Boot 集成测试直接调用 `EmotionMatchService`，从 JSON 读取输入与多个可接受概念，并把详细结果写入 `target`。提示词采用统一的角色、任务、维度、流程和约束结构；只有当报告证明问题出在召回或代码校验时才修改 Java 逻辑。

**Tech Stack:** Java 21、Spring Boot 4.1.1、Spring AI 2.0.1、JUnit 5、Jackson 3、PostgreSQL/pgvector、Maven

**Spec:** `docs/superpowers/specs/2026-09-08-evaluation-prompt-design.md`

## Global Constraints

- 不新增数据库表、业务接口或前端。
- Java 21 运行，Java 代码保持 Java 8 风格。
- 使用 Lombok 和字段注入；不引入新依赖。
- 默认测试不连接本地 AI；真实评测仅由 `RUN_LOCAL_AI_EVAL=true` 启用。
- 不读取、输出或提交 `.env` 中的真实密钥。

---

### Task 1: 建立 20 条本地 AI 评测基准

**Files:**
- Create: `src/test/resources/evaluation/emotion-match-cases.json`
- Create: `src/test/java/com/example/namedmoment/service/EmotionMatchEvaluationLocalTest.java`
- Modify: `docs/evaluation-cases.md`

**Interfaces:**
- Consumes: `EmotionMatchService.match(String)` 和 `EmotionMatchResponse`
- Produces: 显式启用的 `EmotionMatchEvaluationLocalTest`，报告路径 `target/emotion-evaluation-results.json`

- [x] **Step 1: 写入包含 20 条输入和多个可接受概念的 JSON 固定集**

每项使用以下结构，`acceptableConcepts` 使用数据库中的精确名称：

```json
{
  "id": 1,
  "input": "毕业离开住了多年的城市后，一瓶熟悉的饮料突然让我觉得从前的生活像另一个世界。",
  "acceptableConcepts": ["Natsukashii (懐かしい)", "Hiraeth", "Tizita (ትዝታ)"]
}
```

- [x] **Step 2: 写出默认禁用的真实集成测试**

测试通过 `@EnabledIfEnvironmentVariable(named = "RUN_LOCAL_AI_EVAL", matches = "true")` 启用。它遍历全部用例，检查三个唯一结果、降序分数、HTTPS 来源、非空解释和 Top 3 可接受概念命中，并将每条指纹、模式、候选和耗时写入 `target/emotion-evaluation-results.json`。所有用例运行完成后一次性断言总体命中不少于 16 条，使一次执行能够暴露全部失败样本。

- [x] **Step 3: 运行未优化提示词的评测并确认失败或记录真实基线**

Run:

```bash
set -a; source .env; set +a
RUN_LOCAL_AI_EVAL=true JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
  mvn -Dtest=EmotionMatchEvaluationLocalTest test
```

Expected: 20 条全部被执行；若命中少于 16 条则测试以列出失败编号的断言失败，否则报告仍作为优化前基线保存。

- [x] **Step 4: 在评测文档记录基线 Top 3、匹配模式和人工复核项**

从生成报告读取事实，不把未运行样本标为通过。

### Task 2: 按统一模板优化四套提示词

**Files:**
- Modify: `src/main/resources/prompts/emotion-fingerprint.st`
- Modify: `src/main/resources/prompts/emotion-rerank.st`
- Modify: `src/main/resources/prompts/emotion-fallback.st`
- Modify: `src/main/resources/prompts/emotion-tool-rerank.st`
- Modify: `src/test/java/com/example/namedmoment/service/EmotionFingerprintServiceTest.java`
- Modify: `src/test/java/com/example/namedmoment/service/EmotionRankingServiceTest.java`
- Modify: `src/test/java/com/example/namedmoment/service/EmotionFallbackServiceTest.java`

**Interfaces:**
- Consumes: `EmotionFingerprint`、`ConceptRanking` 的原有结构化输出 Schema
- Produces: 不改变 Java DTO 的四套分阶段提示词

- [x] **Step 1: 运行现有提示词边界测试**

Run:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
  mvn -Dtest=EmotionFingerprintServiceTest,EmotionRankingServiceTest,EmotionFallbackServiceTest test
```

Expected: 原有注入防护、不得诊断、不得创造概念、只使用候选 ID 等服务边界全部通过。提示词属于模型配置，质量变化由 Task 1 的真实输出评测验证，不新增只检查标题文字的脆弱测试。

- [x] **Step 2: 最小化改写提示词并保持输出契约不变**

指纹输出仍只有 `meaning`、`description`；两个重排输出仍只有 `conceptId`、`matchScore`、`explanation`；工具阶段仍必须且只调用一次 `searchEmotionConcepts`。

- [x] **Step 3: 重跑相关单元测试**

Run 同 Step 1。Expected: 全部通过。

- [x] **Step 4: 重跑真实评测并与基线逐条比较**

Run 同 Task 1 Step 3。Expected: 结构事实 20/20，通过 Top 3 命中门槛，且没有新增结构化输出或工具调用错误。

### Task 3: 只修复评测证明存在的代码逻辑问题

**Files:**
- Modify if evidenced: `src/main/java/com/example/namedmoment/service/EmotionMatchService.java`
- Modify if evidenced: `src/main/java/com/example/namedmoment/utils/EmbeddingTextUtils.java`
- Modify if evidenced: `src/main/java/com/example/namedmoment/constant/AppConstants.java`
- Modify if evidenced: `src/main/resources/mapper/EmotionConceptMapper.xml`
- Test corresponding production file in `src/test/java`
- Modify: `docs/evaluation-cases.md`
- Modify: `README.md`

**Interfaces:**
- Consumes: 优化后评测报告中的指纹、召回、重排和兜底证据
- Produces: 最小代码修复与可复现的最终评测说明

- [x] **Step 1: 对每个失败样本定位首次偏离阶段**

对照指纹、向量候选、重排结果和工具返回候选；形成单一根因假设。若问题只在提示词，不修改 Java 或 SQL。

- [x] **Step 2: 为已确认的代码根因写最小失败测试并运行 RED**

例如只有当候选在召回边界被错误过滤时，才在 `EmotionMatchServiceTest` 中添加精确分数边界用例；预期先失败。

- [x] **Step 3: 做单一最小修复并运行 GREEN**

运行新增测试和对应服务测试，确认修复只改变目标行为。

- [x] **Step 4: 运行完整自动化与真实 AI 评测**

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn clean verify
set -a; source .env; set +a
RUN_LOCAL_AI_IT=true RUN_LOCAL_AI_EVAL=true \
  JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn test
```

Expected: 默认套件零失败；本地工具调用测试通过；20 条评测结构事实全部通过且 Top 3 命中不少于 16 条。

- [x] **Step 5: 更新 README 和最终评测记录**

记录运行命令、门槛、最终结果及仍需人工判断的解释贴合与前三项区分，不记录密钥。
