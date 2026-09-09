---
name: "此刻有名"
description: "让难以言说的瞬间在一面背光玻璃馆藏墙中逐步被照亮。"
colors:
  mullion: "#171918"
  seeded-glass: "#e9ece7"
  mist-glass: "#c9d6d3"
  memory-cobalt: "#1646b8"
  source-amber: "#f2a93b"
  archive-oxblood: "#7a1f2b"
  chalk: "#f7f8f5"
  muted-ink: "#59615e"
typography:
  display:
    fontFamily: '"Avenir Next", Avenir, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
    fontSize: "clamp(38px, 5.4vw, 86px)"
    fontWeight: 600
    lineHeight: 1.02
    letterSpacing: "-0.035em"
  headline:
    fontFamily: '"Avenir Next", Avenir, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
    fontSize: "clamp(38px, 4.8vw, 74px)"
    fontWeight: 560
    lineHeight: 1.12
    letterSpacing: "-0.035em"
  title:
    fontFamily: '"Avenir Next", Avenir, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
    fontSize: "clamp(28px, 3vw, 48px)"
    fontWeight: 580
    lineHeight: 1.16
    letterSpacing: "-0.03em"
  input:
    fontFamily: '"Avenir Next", Avenir, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
    fontSize: "clamp(19px, 1.7vw, 26px)"
    fontWeight: 450
    lineHeight: 1.75
  body:
    fontFamily: '"Avenir Next", Avenir, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
    fontSize: "16px"
    fontWeight: 400
    lineHeight: 1.6
  label:
    fontFamily: '"Avenir Next", Avenir, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
    fontSize: "13px"
    fontWeight: 650
    lineHeight: 1.6
    letterSpacing: "0.06em"
  action:
    fontFamily: '"Avenir Next", Avenir, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif'
    fontSize: "clamp(19px, 1.8vw, 27px)"
    fontWeight: 600
    lineHeight: 1.6
    letterSpacing: "0.04em"
rounded:
  none: "0"
spacing:
  mullion-desktop: "6px"
  mullion-tablet: "5px"
  mullion-mobile: "4px"
  page-gutter: "clamp(16px, 2vw, 32px)"
  page-gutter-mobile: "20px"
components:
  glass-pane:
    backgroundColor: "{colors.seeded-glass}"
    textColor: "{colors.mullion}"
    rounded: "{rounded.none}"
  match-action:
    backgroundColor: "{colors.mullion}"
    textColor: "{colors.chalk}"
    typography: "{typography.action}"
    rounded: "{rounded.none}"
    width: "100%"
    height: "clamp(76px, 11vh, 94px)"
  framed-action:
    backgroundColor: "transparent"
    textColor: "{colors.mullion}"
    typography: "{typography.label}"
    rounded: "{rounded.none}"
    padding: "10px 16px"
    height: "48px"
  framed-action-inverse:
    backgroundColor: "{colors.chalk}"
    textColor: "{colors.mullion}"
    typography: "{typography.label}"
    rounded: "{rounded.none}"
    padding: "10px 16px"
    height: "48px"
  status-toast:
    backgroundColor: "{colors.mullion}"
    textColor: "{colors.chalk}"
    rounded: "{rounded.none}"
    padding: "18px 22px"
---

# Design System: 此刻有名

## Overview

**Creative North Star: “背光玻璃馆藏墙”**

“此刻有名”是一面可操作的建筑玻璃墙：墨黑金属框架把冷白压花玻璃分成连续窗格，钴蓝、琥珀与酒红只在可信来源、候选差异和个人选择需要被看见时从背后整面亮起。界面通过空间、尺度和材料建立情绪，不借用日记纸张的怀旧感，也不使用 AI 产品常见的霓虹控制台语汇。

交互故事始终是“写下—辨认—选择—收藏”。输入是最大的安静窗格，第一候选获得最大的结果窗格，收藏记录继续生长为同一面墙。方向种子为 `67ec61a6`；它标记这套视觉世界的来源，不是面向用户的装饰。

**Key Characteristics:**

- 一面连续、无悬浮层的建筑网格。
- 冷白压花玻璃作为常态，整块背光色作为少量高意义状态。
- 依靠字号、字重和占格面积建立层级，不使用图标或装饰字体。
- 内容优先、材料克制；纹理只提供触感，不降低可读性。
- 状态变化温和且可跳过，核心内容不依赖动画才出现。

## Colors

色板由冷白玻璃与墨黑窗框主导，三种饱和背光色分别承担记忆聚焦、来源可信度和档案节奏。

### Primary

- **记忆钴蓝**（`memory-cobalt`）：第一候选、真实概念窗格、文本选区与主要聚焦语义。彩色底上使用 `chalk` 文字。

### Secondary

- **来源琥珀**（`source-amber`）：可靠来源窗格、焦点轮廓、分析指示、操作悬停和选中描边。它是确认与可核验性的信号，不是通用装饰色。

### Tertiary

- **档案酒红**（`archive-oxblood`）：第三候选、档案馆节奏、输入错误与删除二次确认。只在需要重量或警示的局部出现。

### Neutral

- **窗框墨黑**（`mullion`）：网格缝、正文、主操作和页面底层结构。
- **冷白压花玻璃**（`seeded-glass`）：大多数可阅读窗格的默认表面。
- **雾面玻璃**（`mist-glass`）：承诺窗、情感指纹和安静的次级区域。
- **粉笔白**（`chalk`）：深色或彩色背光上的高对比文字。
- **柔墨灰**（`muted-ink`）：元信息、计数、语言和次级说明。

**The Whole-Pane Backlight Rule.** 饱和色应占据完整窗格并表达真实状态；不要把它拆成渐变、散落图标或无意义的小色点。

**The Trust Signal Rule.** 琥珀只用于来源、焦点、进度、悬停和确认等可行动或可信度信号，稀少性是它的辨识度。

## Typography

**Display / Body Font:** Avenir Next；中文依次回退到 PingFang SC、Hiragino Sans GB、Microsoft YaHei，最后使用系统无衬线字体。

**Character:** 同一组人文无衬线字体贯穿中西文。紧凑字距的大标题形成沉静、确定的建筑尺度；正文保持松弛行距，让私人叙述和跨语言释义都易读。

### Hierarchy

- **Display**：用于第一候选概念名；桌面上最大可达 86px，行高 1.02，允许任意位置换行以容纳不同语言。
- **Headline**：用于“告诉我一个你无法解释的瞬间”；默认 38–74px，在矮屏和移动端采用实现中的独立响应式尺寸。
- **Title**：用于结果、档案和空态标题；28–48px，字重 580。
- **Input**：用于主叙述输入；19–26px、行高 1.75，矮屏收紧到 1.55，移动端固定 19px。
- **Body**：基础正文为 16px、行高 1.6；概念含义按窗格层级提升到 17–28px。
- **Label**：元信息通常为 13–14px、字重 650–700；候选序号使用 0.06em 字距，匹配度与计数使用等宽数字特性。

**The Scale-Before-Ornament Rule.** 概念地位由字号、字重与窗格面积表达；不要引入衬线、等宽标签字体、字形特效或图标来制造“文化感”。

## Layout

页面没有居中容器或卡片外边距；窗格直接贴合视口，并以 `mullion` 底色透出的实体缝连接。缝宽在桌面、平板和手机分别为 6px、5px、4px，页面水平内边距默认随视口在 16–32px 间变化，手机固定为 20px。页面最小宽度为 320px，并禁止横向溢出。

桌面顶部导航为粘性三列网格，比例约为 2.6:7:2.2，高 86px。输入首屏沿用三列：左侧窄承诺窗、中央主书写窗、右侧三块信任窗；中央表单由至少 420px 的输入窗与 76–94px 的金属操作横梁组成，并占满导航以下的视口高度。

结果页使用约 1:2:1 的三列、两行网格：情感指纹与第一候选分别跨两行，第二、第三候选在右侧上下排列。档案馆使用 12 列网格，普通记录跨 5 列，每组三条中的第一条跨 7 列，形成持续但不机械的墙面节奏。

在 980px 及以下，顶部导航变为两列，输入区变为约 1:2 的两列结构；结果页变为左侧情感指纹、右侧依次堆叠三个候选。在 680px 及以下，输入、结果与档案全部按阅读顺序改为单列：主书写窗优先，随后才是承诺与信任窗；主书写区域至少 510px，主操作高 76px，候选主窗至少 610px，所有关键操作铺满宽度且触控高度至少 52px。高度不超过 820px 时，输入区缩短间距和字号，但不改变任务顺序。

**The Continuous Wall Rule.** 新页面必须延续同一面网格，通过跨列、跨行和响应式重排形成层级；不要在墙内再创建带外边距的卡片列表。

## Elevation & Depth

系统默认无浮层、无圆角卡片阴影，也不使用背景模糊。深度来自黑色窗框、色块背光和低透明度玻璃纹理：纹理以 960px 平铺、16% 不透明度和 multiply 混合覆盖在常态窗格；钴蓝与酒红窗格改用 19% 不透明度和 screen 混合，保持文字清晰。

阴影只服务状态：输入聚焦使用 4px 琥珀内描边；选中候选使用 8px 琥珀内描边；主操作右端的 4×38px 琥珀灯带带有 14px 微光；全局反馈是唯一真正抬离墙面的元素，使用向下扩散的 12px/40px 阴影并以 4px 琥珀顶边锚定。

**The Structural Depth Rule.** 静态层级先用窗框、纹理和整块背光表达；只有聚焦、选择或短暂反馈可以使用阴影。

## Shapes

所有核心表面与控件均为直角矩形（0 圆角）。窗格靠实体缝分隔，按钮和来源链接使用 2px 当前文字色边框；说明分区使用 1–2px 横线。档案计数也是方形色块，不使用药丸徽章。系统不使用装饰图标，操作依靠完整文字说明。

**The Mullion Rule.** 金属缝必须保持可见且有真实厚度；不要用细发丝线、圆角间距或投影替代它。

## Components

### Navigation

品牌窗与档案入口本身就是玻璃窗格。桌面保留中央雾面留白窗，手机隐藏该窗并让品牌与档案入口同排。品牌与入口悬停时只增加一层半透明白色光照；页面切换后将焦点移到目标标题，品牌按钮返回书写态。

### Writing Pane

主输入框无边框、透明、不可拖拽改变尺寸，插入光标为钴蓝。焦点由整个书写窗的 4px 琥珀内描边表达。输入少于 5 个字符时主操作禁用并显示具体说明；计数持续显示当前长度与 2000 字上限。

### Primary and Framed Actions

主操作嵌入中央窗下方的墨黑横梁，使用粉笔白文字与右侧琥珀灯带；禁用态透明度为 56%，可用悬停态仅把墨黑提亮。来源、选择、删除和页内重试使用 2px 方形边框，桌面最小高度 48px，手机为 52px；悬停统一变为琥珀底。彩色候选中的选择按钮反转为粉笔白底、墨黑字。

### Candidate Panes

三个候选不做等权卡片：第一候选占据钴蓝主窗，第二候选保持冷白，第三候选使用酒红。每个窗格依次包含候选序号、语言、匹配度、名称、含义、描述、匹配解释、来源和选择动作。成功收藏后，选中窗格在 720ms 内增亮并出现 8px 琥珀内描边；所有选择按钮随即禁用，选中项与未选中项分别显示明确结果。保存失败只恢复当前操作，并在窗格内以 alert 文本报告。

### Archive Panes

档案记录按“冷白、钴蓝、琥珀、酒红”循环背光，每组三条中的第一条加宽。删除使用页内二次确认：第一次点击变为酒红确认态并说明影响，4.2 秒后自动恢复；第二次点击才执行删除。空档案和读取错误继续占据完整玻璃墙，分别提供开始书写和重试动作。

### Status and Motion

分析状态按 2.2 秒轮换三条真实过程文案，同时设置表单 `aria-busy`。琥珀状态灯以 1.8 秒呼吸，三块信任窗以 2.8 秒明暗变化并按 0.45 秒错开；不显示虚假百分比。全局成功反馈停留 4.4 秒。所有动画只是增强，`prefers-reduced-motion: reduce` 下滚动改为立即、动画和过渡缩至 0.01ms 且只运行一次。

### Accessibility Contract

页面使用中文语言标记、语义化导航/表单/分区/文章、可见的跳转到主要内容链接，以及原生按钮和链接。键盘焦点统一使用 4px 琥珀轮廓；状态采用 `aria-live="polite"`，错误切换为 `role="alert"`。来源链接只接受清理后的 HTTP(S) 地址，在新窗口打开时带 `noopener noreferrer`。用户输入和模型内容必须通过文本节点渲染，不得作为 HTML 注入。

## Do's and Don'ts

### Do:

- **Do** 让所有内容住在同一面连续窗墙中，并以 6/5/4px 的响应式金属缝保持结构。
- **Do** 用整个窗格的钴蓝、琥珀或酒红表达候选、来源、选择与档案节奏。
- **Do** 保持第一候选明显大于其余候选，并让档案记录通过跨列宽度形成节奏。
- **Do** 为键盘焦点、等待、错误、空态、保存和删除提供不依赖颜色的文字反馈。
- **Do** 保持至少 48px 的桌面操作高度与 52px 的移动触控高度，并尊重减少动态效果偏好。

### Don't:

- **Don't** 引入纸张日记、悬浮圆角卡片、胶囊标签或层叠式 glassmorphism。
- **Don't** 使用渐变、背景模糊、普遍阴影、霓虹描边或装饰性小图标替代结构。
- **Don't** 把三个候选压成等宽等高的通用卡片，也不要用徽章制造虚假层级。
- **Don't** 暴露 RAG、Embedding、工具调用等技术术语，或把匹配度写成诊断概率。
- **Don't** 让纹理、动画或高饱和颜色承担正文可读性所必需的信息。
