# ClinMindRuntime 文档导航

> 本文件是 `docs/` 目录入口。  
> 当前总设计版本：v3.0。  
> 当前项目状态：Phase 1–11 P1 已冻结；Phase 12-P0 Clinical Evidence Engine 处于设计评审期。

---

# 一、当前阶段

```text
Phase 1–11 P1：FROZEN
Phase 12-P0：DESIGN REVIEW
Phase 12-P1：PLANNED
Phase 12-P2：PLANNED
Phase 13–22：按路线图后置
```

Phase 12-P0 设计完成评审前，只允许：

```text
设计审阅
语料与许可证调研
Provider 候选验证
pgvector / PostgreSQL Full-text 技术 Spike
Offline Evaluation CaseSet 草案
```

暂不允许正式 Phase 12-P0 产品代码进入 `main`。

---

# 二、权威阅读顺序

## 2.1 了解项目全貌

```text
1. docs/0-项目入口/项目展示导读.md
2. docs/0-项目入口/00_项目设计地图.md
3. docs/1-总设计/ClinMindRuntime完整系统设计.md
4. docs/1-总设计/ClinMindRuntime阶段拆分路线图.md
5. docs/1-总设计/ClinMindRuntime技术实现总方案.md
6. docs/1-总设计/Phase11后架构缺口与路线收敛决策.md
```

## 2.2 进入 Phase 12 设计

```text
1. docs/3-phase实现/Phase12真实临床能力纵切_总体设计.md
2. docs/3-phase实现/Phase12_P0ClinicalEvidenceEngine_实现规格.md
3. docs/3-phase实现/Phase12_P0EvidenceEngine_API与测试设计.md
4. docs/3-phase实现/Phase12_P0开发任务清单.md
5. docs/4-实现约束/AI_IMPLEMENTATION_SKILL.md
```

## 2.3 开始编码前

必须确认：

```text
总体设计已审阅
实现规格已审阅
API 与测试设计已审阅
任务清单已审阅
Source Manifest 范围确定
许可证记录方式确定
Provider 与 DenseIndexPort 方案确定
Evaluation CaseSet 结构确定
```

---

# 三、目录结构

```text
docs/
├── README.md
│
├── 0-项目入口/
│   ├── 00_项目设计地图.md
│   ├── 项目展示导读.md
│   └── 架构文档缺口审查清单.md
│
├── 1-总设计/
│   ├── ClinMindRuntime完整系统设计.md
│   ├── ClinMindRuntime阶段拆分路线图.md
│   ├── ClinMindRuntime技术实现总方案.md
│   ├── Phase11后架构缺口与路线收敛决策.md
│   ├── 全局技术栈与架构选型.md
│   └── 架构模式与设计模式说明.md
│
├── 2-专项设计/
│   ├── AI前沿技术选型与接入规划.md
│   ├── 医学知识库与RAG构建规划.md
│   ├── 模型训练与后训练规划.md
│   ├── Python_AIProvider接入规划.md
│   ├── 数据安全与合规边界规划.md
│   ├── 数据库持久化设计.md
│   ├── 平台前端与Console规划.md
│   ├── 部署与运维规划.md
│   └── 测试与CI总方案.md
│
├── 3-phase实现/
│   ├── Phase1_* ... Phase11_*
│   ├── Phase12真实临床能力纵切_总体设计.md
│   ├── Phase12_P0ClinicalEvidenceEngine_实现规格.md
│   ├── Phase12_P0EvidenceEngine_API与测试设计.md
│   └── Phase12_P0开发任务清单.md
│
└── 4-实现约束/
    └── AI_IMPLEMENTATION_SKILL.md
```

---

# 四、核心文档职责

| 文档 | 职责 |
|---|---|
| `ClinMindRuntime完整系统设计.md` | 定义系统最终是什么、能力域、逻辑平面和永久边界 |
| `ClinMindRuntime阶段拆分路线图.md` | 定义各能力属于哪个 Phase、进入条件和状态 |
| `ClinMindRuntime技术实现总方案.md` | 定义包、接口、API、存储、测试、部署和运维落点 |
| `Phase11后架构缺口与路线收敛决策.md` | 记录为何优先做真实 Evidence / LLM / FHIR 纵切 |
| `Phase12真实临床能力纵切_总体设计.md` | 定义 P0/P1/P2 的范围、依赖和共同边界 |
| `Phase12_P0ClinicalEvidenceEngine_实现规格.md` | 定义 P0 领域模型、检索链路、责任边界和完成标准 |
| `Phase12_P0EvidenceEngine_API与测试设计.md` | 定义 API、Provider Contract、数据库测试、Evaluation 与冻结阈值 |
| `Phase12_P0开发任务清单.md` | 定义正式实现顺序、依赖、交付物和验收标准 |
| `AI_IMPLEMENTATION_SKILL.md` | 约束 AI 编码工具当前允许和禁止的工作 |

---

# 五、Phase 12-P0 设计摘要

```text
Source Registry
→ Versioned Evidence Asset
→ Chunk / Span / Curated Claim
→ PostgreSQL Lexical Recall
→ Real Dense Recall
→ Reciprocal Rank Fusion
→ Cross-encoder Rerank
→ Authority / Freshness / Applicability
→ Citation Entailment
→ Conflict Detection
→ EvidenceValidation
→ RuntimeEvidenceGraph Patch
→ Runtime Commit / DecisionBoundary
```

技术责任：

```text
Java Runtime
负责编排、过滤、验证、采用、提交、Trace、Audit 和 Evaluation。

Python Provider
只负责 Embedding、Rerank、Citation Entailment 等无状态模型推理。

PostgreSQL
负责证据资产、版本、Claim、Citation、检索 Trace 与词法索引。
```

---

# 六、冻结文档规则

每个 Phase 进入 FROZEN 至少需要：

```text
实现规格
API 与测试设计
开发任务清单
代码与 migration
自动化测试
人工测试结果
Evaluation 结果
冻结记录
README / 项目地图 / 技术方案 / AI 约束同步
```

冻结记录不能通过改写历史事实来补齐，必须与仓库实际状态一致。

---

# 七、当前禁止事项

```text
不让模型、Agent、Evidence 或 Tool 接管 Runtime。
不让 Evidence Engine 直接输出患者诊断。
不让未验证 Citation 进入 accepted EvidenceGraph。
不导入真实患者或 PHI 数据。
不发布许可证未知的证据资产。
不提前建设 ClinicalFactLedger、完整 Governance Kernel、写操作或 Multi-Agent。
不一次性建立 Phase 13–22 空目录和空表。
不把 Mock / hash embedding / token overlap 描述为真实生产能力。
```

---

# 八、已冻结基线

Phase 11-P1 冻结依据：

```text
docs/3-phase实现/Phase11_P1人工测试结果.md
docs/3-phase实现/Phase11_P1冻结记录.md
```

Phase 11-P1 冻结后只允许修复明确回归，不再扩展 Role-specific View 范围。