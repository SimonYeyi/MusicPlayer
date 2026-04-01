# Superpowers 工作流

> **Superpowers** 是一套 Agent 驱动的开发方法论，通过结构化的设计规格（Spec）和实施计划（Plan）文档，引导 AI Agent 逐步完成复杂功能的开发。

> **重要原则**：Superpowers 是**强制性工作流程**，流程顺序固定不可调整。Agent 在任何任务前都会自动检查并调用相关技能，不会遗漏。

---

## 核心理念

把一个功能的开发分为两个阶段：

1. **想清楚** - brainstorming，先问清楚需求，再写 Spec
2. **做出来** - writing-plans，按 Plan 一步步执行

每个阶段都有对应的文档和技能支撑，文档是 AI Agent 理解需求和执行任务的依据。

### Superpowers 技能体系

| 技能 | 作用 | 时机 |
|------|------|------|
| **brainstorming** | 问清楚需求 | Step 1 |
| **using-git-worktrees** | 创建隔离分支工作空间 | brainstorming Phase 4 |
| **writing-plans** | 拆解 Task + 生成 Plan | Step 2 |
| **executing-plans / subagent-driven-development** | 两种执行方式 | Step 3 |
| **test-driven-development** | RED-GREEN-REFACTOR 循环 | Step 3 |
| **requesting-code-review** | 任务间强制审查 | 任务之间 |
| **finishing-a-development-branch** | 验证测试 + 合并决策 | Step 4 |

---

## 技能说明

### brainstorming
在开始写代码之前，通过提问澄清需求的细节：

**Phase 1-3:**
- 确认功能范围和边界条件
- 明确用户场景和交互流程
- 识别潜在的技术风险或依赖
- 生成设计文档（Spec）

**Phase 4:**
- 设计批准后，调用 `using-git-worktrees` 创建隔离分支工作空间

### using-git-worktrees
创建隔离的 git worktree 工作空间：
- 创建新分支
- 隔离主分支不受污染
- 完成后与 `finishing-a-development-branch` 配合清理

### writing-plans
将需求拆解为可执行的小任务：
- 每个任务 2-5 分钟完成
- 任务之间无依赖，可独立测试
- 按依赖顺序排列（数据层 → 业务层 → UI 层）
- 生成 Plan 文档

### executing-plans / subagent-driven-development
两种执行方式（二选一）：
- **方式 A (subagent-driven-development)**: 并行派发子代理处理无依赖 Task，每 Task 完成后 requesting-code-review
- **方式 B (executing-plans)**: 顺序逐项执行，所有 Task 完成后统一验证

### test-driven-development
强制 RED-GREEN-REFACTOR 循环：
- **Red**: 先写测试（明确期望）
- **Green**: 写最简单代码让测试通过
- **Refactor**: 重构优化代码

### requesting-code-review
任务间强制代码审查：
- 每个 Task 完成后必须 Review
- 确认代码质量和设计一致性
- 发现问题及时修正

### finishing-a-development-branch
任务完成后验证和决策：
- 验证测试
- 提供合并/PR/清理选项

---

## 开发流程

```
需求
  │
  ▼
┌─────────────────────────────┐
│  Step 1: brainstorming      │
│  ├─ Phase 1-3: 需求澄清     │
│  │    + 写 Spec 设计文档    │
│  └─ Phase 4: using-git-    │
│       worktrees (创建隔离分支)│
└─────────────────────────────┘
         │
         ▼
┌─────────────────────────────┐
│  Step 2: writing-plans      │
│  ├─ 拆解 Task（2-5 分钟）   │
│  ├─ 排定顺序                │
│  └─ 写 Plan 实施计划        │
└─────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────┐
│  Step 3: 执行方式（二选一）                          │
│                                                     │
│  ┌───────────────────────┐  ┌─────────────────────┐ │
│  │ 方式 A:               │  │ 方式 B:             │ │
│  │ subagent-driven-     │  │ executing-plans     │ │
│  │ development           │  │                     │ │
│  │                       │  │                     │ │
│  │ - 并行派发子代理       │  │ - 顺序逐项执行       │ │
│  │ - 每 Task 完成后      │  │ - 所有 Task 完成后   │ │
│  │   requesting-code-    │  │   统一验证           │ │
│  └───────────────────────┘  └─────────────────────┘ │
│                                                     │
│  两种方式共用的 TDD 循环：                          │
│  ┌─────────────────────────────────────────────────┐│
│  │  每个 Task: RED-GREEN-REFACTOR                 ││
│  │  ├─ RED: 先写测试                              ││
│  │  ├─ GREEN: 写最简单代码让测试通过               ││
│  │  └─ REFACTOR: 重构优化代码                     ││
│  └─────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────┐
│  Step 4: finishing-a-        │
│         development-branch   │
│  ├─ 验证测试                │
│  └─ 合并/PR/清理选项        │
└─────────────────────────────┘
```

---

## 文档体系

```
docs/superpowers/
├── SUPERPOWERS.md              # 工作流说明（本文档）
├── specs/                      # 设计规格文档
│   └── YYYY-MM-DD-feature-design.md
└── plans/                      # 实施计划文档
    └── YYYY-MM-DD-feature.md
```

| 文档 | 内容 | 作用 |
|------|------|------|
| **Spec** | 功能概述、核心逻辑、流程图、UI 设计、接口变更 | 告诉 AI "做什么"和"为什么" |
| **Plan** | 任务拆分、步骤指令、代码模板 | 告诉 AI "怎么做"和"按什么顺序" |

---

## Spec vs Plan

| | **Spec** | **Plan** |
|---|---|---|
| **目的** | 理解需求和设计 | 指导具体执行 |
| **问题** | 这个功能要做什么？为什么要这样设计？ | 具体从哪开始？怎么拆分任务？ |
| **内容** | 核心逻辑、流程图、UI 草图、数据模型 | Task 列表、代码模板、commit 命令 |
| **格式** | 自由格式，重点是清晰 | 模板化，每个 Task 可独立执行 |

---

## Task 排列原则

1. **核心逻辑优先** - 先实现底层工具类或业务逻辑
2. **数据层优先于 UI 层** - 先改数据库/Repository，再改 ViewModel，最后改 UI
3. **被调用的先于调用的** - 先改被依赖的组件，再改依赖方
4. **独立组件优先于页面** - 先改可复用组件，再改具体页面
5. **最后一个 Task 是构建测试**

---

## Commit 规范

每个 Task 完成后单独 commit，采用 Angular 规范：

| 类型 | 使用场景 |
|------|----------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `refactor` | 重构（不改变功能） |
| `docs` | 文档更新 |
| `chore` | 构建/工具变更 |
| `test` | 测试相关 |

---

## 文档命名

- Spec 文档：`YYYY-MM-DD-<功能名>-design.md`
- Plan 文档：`YYYY-MM-DD-<功能名>.md`

日期前缀确保每次变更产生新文档，旧文档作为历史保留。

---

## 为什么需要这两层文档？

**Spec** 让 AI 理解设计意图，而不只是执行步骤。有了 Spec，AI 可以：
- 理解为什么这么做，而不是机械执行
- 在实现过程中发现设计问题
- 与人类讨论设计的合理性

**Plan** 把设计转化为可执行的步骤。有了 Plan：
- 每个 Task 独立可测试
- 进度可追踪
- 出错时可定位到具体步骤

---

## 与其他方法论的区别

| | Superpowers | 传统敏捷 | 一步到位 |
|---|---|---|---|
| **文档** | Spec + Plan | Sprint Planning | 口头描述需求 |
| **迭代** | 任务级迭代 | Sprint 迭代 | 无迭代 |
| **回滚** | 单 Task 回滚 | Sprint 回滚 | 全量回滚 |
| **适用** | 复杂功能 | 持续交付 | 简单改动 |
