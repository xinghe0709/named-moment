# API 请求示例

默认地址：`http://localhost:8080`

所有接口使用统一响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

## 1. 匹配三个概念

```bash
curl -X POST 'http://localhost:8080/api/emotions/match' \
  -H 'Content-Type: application/json' \
  -d '{
    "text": "毕业离开住了多年的城市后，一瓶熟悉的饮料突然让我觉得从前的生活像另一个世界。"
  }'
```

响应示意：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "fingerprint": {
      "meaning": "离开旧生活后被熟悉物件突然唤起的怀念与疏离",
      "description": "熟悉饮料把人带回过去，同时提醒那段生活和当时的自己已经难以返回。"
    },
    "matchMode": "RAG",
    "candidates": [
      {
        "conceptId": 5,
        "name": "Natsukashii (懐かしい)",
        "language": "日语",
        "meaning": "旧事重现时温暖而微酸的怀念",
        "description": "某个味道、声音或物件突然唤回珍贵往昔，让人高兴它曾存在，也知道它已经过去。",
        "sourceUrl": "https://hifisamurai.github.io/lexicography/",
        "matchScore": 95,
        "explanation": "熟悉饮料触发了对旧生活的温暖回忆，同时也带来时间已经过去的轻微酸楚。"
      }
    ]
  }
}
```

说明：

- `candidates` 正常固定返回 3 条，并按 `matchScore` 从高到低排列。
- `matchMode=RAG` 表示向量召回与模型重排成功。
- `matchMode=TOOL_FALLBACK` 表示 RAG 阶段异常或有效候选不足，已改用数据库工具查询兜底。
- 示例中的 ID、分数和解释仅用于展示格式，以实际模型输出为准。

## 2. 保存用户选中的结果

客户端应把用户最终选择的候选项原样带入请求，不要自动保存全部三个结果。

```bash
curl -X POST 'http://localhost:8080/api/emotions/records' \
  -H 'Content-Type: application/json' \
  -d '{
    "inputText": "毕业离开住了多年的城市后，一瓶熟悉的饮料突然让我觉得从前的生活像另一个世界。",
    "conceptId": 5,
    "matchScore": 95,
    "explanation": "熟悉饮料触发了对旧生活的温暖回忆，同时也带来时间已经过去的轻微酸楚。"
  }'
```

响应中的概念名称、含义、描述和来源由服务端根据 `conceptId` 从数据库读取。

## 3. 查看已保存记录

```bash
curl 'http://localhost:8080/api/emotions/records'
```

记录按创建时间倒序返回。

## 4. 删除一条记录

```bash
curl -X DELETE 'http://localhost:8080/api/emotions/records/1'
```

## 5. 常见错误

输入为空或少于 5 个字符：

```json
{
  "code": 40001,
  "message": "参数错误",
  "data": null
}
```

保存时提交了不存在的 `conceptId`：

```json
{
  "code": 40001,
  "message": "参数错误",
  "data": null
}
```

删除的情感记录不存在：

```json
{
  "code": 40401,
  "message": "情感记录不存在",
  "data": null
}
```

情感指纹模型调用失败：`50001`。RAG 与工具兜底均失败：`50002`。数据库访问失败：`50003`。
