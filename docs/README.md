# ClinMindRuntime 文档导航

> 本文件是 `docs/` 目录的入口，用于说明当前文档体系如何阅读、哪些文档是当前实现约束、哪些是历史基线、哪些是后续规划。  
> 当前阶段不进行大规模物理搬迁，先通过本文档完成“逻辑归档”。后续如果需要再移动目录，必须同步更新所有文档引用和 `AI_IMPLEMENTATION_SKILL.md`。

---

# 一、当前项目阶段

当前仓库状态：

```text
Phase 1-P0：Runtime MVP 已完成。
Phase 2-P0：共享能力资产原型已完成。
Phase 3-P0：训练与评估闭环 MVP 已完成并冻结。
Phase 4-P0：经验候选与训练数据候选沉淀机制已完成并冻结。
Phase 4-P1：候选治理与安全加固已完成并冻结。
当前阶段：Phase 4-P1 freeze complete，下一步是 Phase4-P2 或 Phase 5 规划。
```

当前最优先任务：

```text
1. 读取 docs/Phase4_P1冻结记录.md，遵守 P1 冻结边界。
2. 新大能力进入 Phase4-P2 / Phase 5 规划，不向 P1 继续堆功能。
3. 保持 Phase1/2/3/4-P0/4-P1 回归测试通过。
4. 继续禁止提前接入 RAG、Python Provider、数据库、前端和模型训练（除非进入对应 Phase 规划）。
```

---

# 二、推荐阅读顺序

## 2.1 面试官 / 第一次看项目

```text
1. docs/项目展示导读.md
2. docs/ClinMindRuntime完整系统设计.md
3. docs/ClinMindRuntime技术实现总方案.md
4. docs/Phase3_P0冻结记录.md
5. docs/Phase4_P0冻结记录.md
6. docs/Phase4_P1冻结记录.md
7. docs/Phase4_P1人工测试API结果.md
8. docs/Phase4_人工测试API结果.md
9. docs/Phase4_P1候选治理与安全加固_实现规格.md
10. docs/架构模式与设计模式说明.md
```

阅读目标：快速理解项目定位、当前实现、为什么不是普通 RAG / Agent Demo、Phase 3/4-P0/4-P1 已经实现并冻结了什么。

## 2.2 后续代码实现 / AI 编码工具

```text
1. docs/AI_IMPLEMENTATION_SKILL.md
2. docs/Phase4_P1冻结记录.md
3. docs/Phase4_P1开发任务清单.md
4. docs/Phase4_P1候选治理与安全加固_实现规格.md
5. docs/Phase4_P1候选脱敏与来源校验设计.md
6. docs/Phase4_P1候选Review记录设计.md
7. docs/Phase4_P0冻结记录.md
8. docs/Phase4_开发任务清单.md
9. docs/Phase4_经验候选与训练数据候选沉淀_实现规格.md
10. docs/数据安全与合规边界规划.md
11. docs/测试与CI总方案.md
```

阅读目标：明确 Phase 4-P1 已冻结边界；新能力应进入 P2/P5 规划，不得跳到 RAG、数据库、前端、模型训练或正式审核平台。

## 2.3 架构学习 / 自我复盘

```text
1. docs/架构模式与设计模式说明.md
2. docs/ClinMindRuntime完整系统设计.md
3. docs/ClinMindRuntime阶段拆分路线图.md
4. docs/全局技术栈与架构选型.md
5. docs/AI前沿技术选型与接入规划.md
6. docs/模型训练与后训练规划.md
```

阅读目标：理解 Runtime、Provider、Evaluation、Candidate、Safety、Trace、模型训练、AI 前沿技术之间的关系。

---

# 三、文档分类

## 3.1 项目入口文档

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/README.md` | 活跃 | 文档导航入口 |
| `docs/项目展示导读.md` | 活跃 | 面向面试官 / 自我复盘的项目导读 |

## 3.2 当前实现约束文档

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/AI_IMPLEMENTATION_SKILL.md` | 活跃 | 约束 AI / Cursor / Claude Code / Codex 后续实现 |
| `docs/Phase4_P1冻结记录.md` | 已冻结 | Phase4-P1 冻结依据、边界与 hardening backlog |
| `docs/Phase4_P1开发任务清单.md` | 已完成 | Phase4-P1-A 到 P1-F 均已完成 |
| `docs/Phase4_P1候选治理与安全加固_实现规格.md` | 已实现基线 | Phase4-P1 总体实现规格 |
| `docs/Phase4_P1候选脱敏与来源校验设计.md` | 已实现基线 | CandidateSanitizer 与 SourceRef 强校验设计 |
| `docs/Phase4_P1候选Review记录设计.md` | 已实现基线 | 最小 review 记录设计 |
| `docs/Phase4_P1人工测试API结果.md` | 验收归档 | Phase 4-P1 人工 / E2E API 验收记录 |
| `docs/Phase4_P0冻结记录.md` | 已冻结 | Phase4-P0 冻结依据、边界与 hardening backlog |
| `docs/测试与CI总方案.md` | 活跃 | 约束测试分层、回归基线和 CI 演进 |

## 3.3 已冻结 Phase 文档：Phase 4-P0

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/Phase4_经验候选与训练数据候选沉淀_实现规格.md` | 已实现基线 | Phase4-P0 总体实现规格 |
| `docs/Phase4_数据结构设计.md` | 已实现基线 | Candidate 数据结构依据 |
| `docs/Phase4_候选生成策略设计.md` | 已实现基线 | CandidateGenerationPolicy 与映射策略依据 |
| `docs/Phase4_Runtime与Evaluation接入设计.md` | 已实现基线 | Phase4 只读 Evaluation / Runtime 结果的接入边界 |
| `docs/Phase4_API与测试设计.md` | 已实现基线 | Candidate debug API 与测试依据 |
| `docs/Phase4_开发任务清单.md` | 已完成 | Phase4-P0-A 到 P0-G 均已完成 |
| `docs/Phase4_人工测试API结果.md` | 验收归档 | Phase 4-P0 人工 API 验收记录 |
| `docs/Phase4_P0冻结记录.md` | 冻结归档 | Phase 4-P0 冻结状态、边界与 hardening backlog |

## 3.4 已冻结 Phase 文档：Phase 3

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/Phase3_训练与评估闭环_实现规格.md` | 已实现基线 | Phase 3 总体实现规格 |
| `docs/Phase3_评估数据结构设计.md` | 已实现基线 | Evaluation 数据结构依据 |
| `docs/Phase3_病例集与考试流程设计.md` | 已实现基线 | YAML case set 与 EvaluationRunner 依据 |
| `docs/Phase3_Runtime评估接入设计.md` | 已实现基线 | Evaluation 必须通过 RuntimeService 的约束 |
| `docs/Phase3_CapabilityProfile更新机制设计.md` | 已实现基线 | CapabilityProfileUpdateProposal 依据 |
| `docs/Phase3_API与测试设计.md` | 已实现基线 | Evaluation debug API 和测试依据 |
| `docs/Phase3_开发任务清单.md` | 已完成 | Phase3-P0-A 到 P0-G 均已完成 |
| `docs/Phase3_人工测试API结果.md` | 验收归档 | Phase 3 人工 API 验收记录 |
| `docs/Phase3_P0冻结记录.md` | 冻结归档 | Phase 3-P0 冻结状态、冻结依据和 Phase4 前置条件 |

## 3.5 总体架构文档

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/ClinMindRuntime完整系统设计.md` | 稳定基线 | 定义系统定位和长期愿景 |
| `docs/ClinMindRuntime阶段拆分路线图.md` | 稳定基线 | 定义 Phase 1–5 的演进顺序 |
| `docs/ClinMindRuntime技术实现总方案.md` | 活跃 | 定义代码级落地方案 |
| `docs/全局技术栈与架构选型.md` | 规划基线 | 定义 Java / Python / PostgreSQL / pgvector / React / Docker 等选型 |
| `docs/架构模式与设计模式说明.md` | 学习说明 | 用于理解架构模式、设计模式、代码模式 |

## 3.6 历史 Phase 文档

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/Phase1_*.md` | 历史基线 | Runtime MVP 设计、回归依据、演进说明 |
| `docs/Phase2_*.md` | 历史基线 | Asset Provider 原型设计、回归依据、演进说明 |

## 3.7 后续专项规划文档

| 文档 | 状态 | 用途 |
|---|---|---|
| `docs/AI前沿技术选型与接入规划.md` | 规划级覆盖 | MCP / Agent SDK / LangGraph / GraphRAG / Skills 等后续边界 |
| `docs/模型训练与后训练规划.md` | 规划级覆盖 | 模型训练、后训练、Model Provider 后续路线 |
| `docs/医学知识库与RAG构建规划.md` | 规划级覆盖 | KnowledgeContext、RAG、KG-lite、GraphRAG 与 EvidenceGraph 边界 |
| `docs/数据安全与合规边界规划.md` | 规划级覆盖 | 敏感数据、脱敏、debug API、训练数据和审计边界 |
| `docs/数据库持久化设计.md` | 规划级覆盖 | PostgreSQL、Redis、pgvector、Trace、Evaluation、Model Registry 后续持久化 |
| `docs/平台前端与Console规划.md` | 规划级覆盖 | Runtime Console、Asset Console、Evaluation Center、Model Registry、Audit Center 后续范围 |
| `docs/部署与运维规划.md` | 规划级覆盖 | Docker Compose、多服务部署、环境变量、健康检查、日志和监控后续路线 |

注意：这些文档是后续规划，不是当前实现清单。

---

# 四、当前不应做什么

```text
1. 不应继续无限新增总体规划文档。
2. 不应向 Phase 4-P0 / Phase 4-P1 继续堆新能力（均已冻结）。
3. 不应提前实现 RAG / GraphRAG / Python AI Provider。
5. 不应提前接 PostgreSQL / Redis / pgvector。
6. 不应提前做前端 Console。
7. 不应提前实现模型训练 / 后训练。
8. 不应大规模移动 docs 文件，除非同步更新全部引用。
```

---

# 五、当前建议做什么

```text
1. 以 docs/Phase4_P1冻结记录.md 作为 P1 边界依据。
2. 规划 Phase4-P2 或 Phase 5 专项前，先升级设计与任务清单。
3. 保持 Phase1/2/3/4-P0/4-P1 回归测试通过。
```

---

# 六、文档维护规则

```text
1. 新增重要文档后，必须更新本文件。
2. 新增或修改实现约束后，必须更新 AI_IMPLEMENTATION_SKILL.md。
3. 发现新的架构缺口后，必须更新 架构文档缺口审查清单.md。
4. 进入新 Phase 前，必须先把规划文档升级为实现规格。
5. 不再凭感觉判断文档是否完整，以 docs/架构文档缺口审查清单.md 为准。
```

---

# 七、最终结论

当前文档体系已支撑 Phase4-P1 冻结归档。Phase3-P0、Phase4-P0、Phase4-P1 均已冻结。下一步应规划 Phase4-P2 或 Phase 5 专项，参考 `docs/Phase4_P1冻结记录.md` 第六节 hardening backlog。