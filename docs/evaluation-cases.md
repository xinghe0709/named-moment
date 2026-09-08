# 「此刻有名」MVP 固定评估集

## 评价规则

每条输入都通过 `POST /api/emotions/match` 执行并记录前三名。人工检查四项：

- `事实正确`：三项均来自数据库，且概念与来源一致。
- `核心命中`：至少一个结果准确捕捉主要感受。
- `解释贴合`：解释联系输入中的真实细节，不编造经历。
- `结果有区分`：前三项不是同义重复。

通过线：20 条全部事实正确；至少 16 条同时满足核心命中与解释贴合。

## 固定输入

1. 毕业离开住了多年的城市后，一瓶熟悉的饮料突然让我觉得从前的生活像另一个世界。
2. 我想念的不是某个地方，而是那个地方曾经的自己。
3. 回到故乡后街道几乎没变，但我已经找不到当年属于这里的感觉。
4. 明知道这段相聚很快结束，所以连普通的晚饭都显得格外珍贵。
5. 亲人离开很久了，我遇到好消息时还是会下意识想告诉他。
6. 一段关系没有争吵也没有告别，只是慢慢不再说话。
7. 我在人群里很热闹，却觉得没有一个人真正看见我。
8. 和老朋友很久没见，坐下来却像昨天才分别。
9. 在陌生城市有人递给我一碗热汤，我突然产生了家的感觉。
10. 我同时属于两种文化，也常常觉得自己不完全属于任何一边。
11. 大雨停后空气很凉，我什么都不想追，只想安静地走回家。
12. 看见广阔星空时，我感到自己很渺小，却一点也不害怕。
13. 忙了很久的事情结束后，没有兴奋，只有身体终于松下来的感觉。
14. 阳光照在晾晒的床单上，那一刻普通生活让我非常满足。
15. 听到朋友取得成功，我真心觉得像自己也得到了一份礼物。
16. 新生活终于开始了，我既自由又害怕，像站在没有栏杆的门口。
17. 做出选择后我并不后悔，却会想念那些从此不会发生的人生。
18. 我们彼此都明白对方在关心，却谁也没有把那句话说出来。
19. 独自旅行时没有孤单，反而第一次听清了自己的想法。
20. 整理旧房间时发现童年的小东西，我为它还在而高兴，也为时间过去而难过。

## 运行记录

2026-09-08 使用本地 Qwen 聊天模型、Embedding 模型和 PostgreSQL 中的 100 条真实概念执行。最终联合回归中 19 条走 RAG，第 2 条因重排解释引用了其他候选名称，被语义校验拒绝后通过数据库工具兜底完成。

结果摘要：

- 数据库事实、三个唯一 ID、分数范围和 HTTPS 来源：20/20。
- 初始可接受概念 Top 3 命中：19/20，高于 16/20 的 MVP 门槛。
- 平均耗时 5267ms；RAG 样本约 4.5～5.8 秒，发生两次重排校验和工具兜底的第 2 条为 12995ms。
- 60 条解释长度为 42～73 个字符，平均 58 个字符。
- 重排解释若明确引用了其他候选名称，会被判为无效并触发现有重试；最终响应会把少量绝对化措辞归一为更克制的表达。
- 第 10 条是当前概念库覆盖边界：返回内容围绕“游离、缺失和无对象忧郁”，但没有命中初始标注的 `Ibasho / Cynefin / Heimat`。
- 模型输出存在随机波动；单轮结果用于回归观察，稳定性应通过后续多轮评测衡量。

| # | Top 3 | 事实正确 | 核心命中 | 解释贴合 | 结果有区分 | 备注 |
|---|---|---|---|---|---|---|
| 1 | Natsukashii / Hiraeth / Tizita | 是 | 是 | 是 | 是 | RAG |
| 2 | Hiraeth / Natsukashii / Manqué | 是 | 是 | 是 | 是 | TOOL_FALLBACK；解释错位校验触发 |
| 3 | Hiraeth / Cynefin / Hiányérzet | 是 | 是 | 是 | 是 | RAG |
| 4 | Mono no aware / Utakata / Memento mori | 是 | 是 | 是 | 是 | RAG |
| 5 | Charmolypi / Tizita / Natsukashii | 是 | 是 | 是 | 是 | RAG |
| 6 | Vemod / Anitya / Saudade | 是 | 是 | 是 | 是 | RAG |
| 7 | Hiányérzet / Toska / Duḥkha | 是 | 是 | 是 | 是 | RAG |
| 8 | Retrouvailles / Ah-un / Natsukashii | 是 | 是 | 是 | 是 | RAG |
| 9 | Kama muta / Heimat / Gezellig | 是 | 是 | 是 | 是 | RAG |
| 10 | Elvágyódás / Hiányérzet / Toska | 是 | 否 | 是 | 是 | RAG；未命中初始可接受概念集合 |
| 11 | Gelassenheit / Niksen / Mysa | 是 | 是 | 是 | 是 | RAG |
| 12 | Gelassenheit / Yūgen / Ataraxia | 是 | 是 | 是 | 是 | RAG |
| 13 | Fjaka / Gelassenheit / Fàng xīn | 是 | 是 | 是 | 是 | RAG |
| 14 | Komorebi / Hygge / Pohoda | 是 | 是 | 是 | 是 | RAG |
| 15 | Firgun / Muditā / Kama muta | 是 | 是 | 是 | 是 | RAG |
| 16 | Datsuzoku / Anitya / Elvágyódás | 是 | 是 | 是 | 是 | RAG |
| 17 | Manqué / Hrepenenje / Sehnsucht | 是 | 是 | 是 | 是 | RAG |
| 18 | Ah-un / Zweisamkeit / Eshra | 是 | 是 | 是 | 是 | RAG |
| 19 | Datsuzoku / Shinrin-yoku / Mysa | 是 | 是 | 是 | 是 | RAG |
| 20 | Natsukashii / Tizita / Wabi-sabi | 是 | 是 | 是 | 是 | RAG |

评测原始报告由测试写入 `target/emotion-evaluation-results.json`，每次运行会覆盖。固定输入和可接受概念位于 `src/test/resources/evaluation/emotion-match-cases.json`。
