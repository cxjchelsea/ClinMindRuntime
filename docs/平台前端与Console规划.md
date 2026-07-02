# 平台前端与 Console 规划

> 本文档定义 ClinMindRuntime 后续平台前端的页面结构、角色边界、核心 Console、交互目标和接入阶段。  
> 前端不是 Phase 1–3 的主线，不应提前实现；本文档用于约束 Phase 5 平台化时如何展示 Runtime、Asset、Evaluation、Experience、Model 和 Audit 能力。

---

# 一、文档定位

前端平台的目标不是做普通问诊页面，而是做医疗 AI Runtime 的治理控制台。

它需要展示：

```text
一次 Runtime 当前处于什么状态。
使用了哪些知识和经验资产。
SafetyGate 和 DecisionBoundary 为什么触发。
Evaluation 结果如何影响能力授权。
经验候选和训练样本候选如何审核。
模型 Provider 如何注册、评估、上线和回滚。
```

---

# 二、技术选型

默认：

```text
React + TypeScript + Vite
```

原因：

```text
轻量、适合后台 Console、与当前项目重点匹配。
```

不默认使用 Next.js，除非后续需要 SSR、复杂路由或正式产品站点。

---

# 三、用户角色

| 角色 | 能力 |
|---|---|
| Admin | 全部管理、发布、回滚、权限、审计 |
| Clinician | 查看医生端报告、证据图、反馈病例 |
| Evaluator | 运行病例集、查看 EvaluationResult |
| Asset Reviewer | 审核知识资产、经验资产、模型版本 |
| Developer | 查看 debug trace、Provider 调试信息 |
| Patient | 不进入 Console，只使用 patient-facing API |

---

# 四、核心 Console

## 4.1 Runtime Console

页面：

```text
Runtime 列表
Runtime 详情
RuntimeTrace 时间线
CaseFrame 查看
DDx Board 查看
EvidenceGraph 查看
SafetyGate 记录
DecisionBoundary 记录
PatientOutput / ClinicianReport 对比
```

用途：

```text
医生协作、调试、复盘、演示 Runtime 主控能力。
```

## 4.2 Asset Console

页面：

```text
Asset Package 列表
Asset Package 详情
AssetMetadata 查看
Red Flag Rules 管理
Test Recommendation Rules 管理
Capability Profile 查看
资产版本对比
资产发布 / 回滚记录
```

用途：

```text
管理知识、规则、能力档案和资产版本。
```

## 4.3 Evaluation Center

页面：

```text
EvaluationCaseSet 列表
EvaluationRun 创建
EvaluationRun 详情
EvaluationItemResult 查看
ScoreBreakdown 图表
SafetyViolation 列表
RegressionFinding 列表
CapabilityProfileUpdateProposal 查看
```

用途：

```text
证明能力不是口头声明，而有病例集和指标依据。
```

## 4.4 Experience Memory Center

页面：

```text
ExperienceCandidate 列表
候选经验详情
来源 RuntimeTrace / EvaluationResult
审核状态
适用范围 / 不适用范围
发布 / 回滚
```

用途：

```text
让经验不是自动记忆，而是治理后的能力资产。
```

## 4.5 Model Registry / Training Center

页面：

```text
TrainingExampleCandidate 列表
TrainingDatasetVersion 列表
ModelProviderMetadata 列表
模型评估结果
模型上线申请
模型回滚记录
```

用途：

```text
管理模型训练、后训练、Provider 版本和评估依据。
```

## 4.6 Knowledge / RAG Console

页面：

```text
Evidence Source 列表
Evidence Chunk 查看
KG-lite node / edge 查看
RAG 检索调试
EvidenceRef 查看
知识版本与审核状态
```

用途：

```text
管理医学知识库、RAG Evidence、KG-lite 和 GraphRAG。
```

## 4.7 Audit & Governance Center

页面：

```text
AuditLog 列表
资产发布记录
CapabilityProfile 更新记录
模型上线记录
Trace 导出记录
权限变更记录
```

---

# 五、页面信息边界

```text
Patient-facing 页面：只展示安全表达。
Clinician 页面：可展示候选诊断和证据，但必须标注不确定性。
Debug 页面：可展示 trace 和 asset internals，仅内部角色可见。
Admin 页面：可发布、回滚、授权和查看审计。
```

---

# 六、Phase 接入计划

## Phase 1–3

```text
不做前端。
使用 API / Postman / JUnit 验证。
```

## Phase 4

```text
可选做极简 Evaluation / Trace 可视化页面，但不作为主线。
```

## Phase 5

```text
正式实现 React Console。
接入 Runtime / Asset / Evaluation / Experience / Model / Audit API。
```

---

# 七、禁止边界

```text
1. 不在 Phase 3-P0 提前做前端。
2. 不让前端直接调用底层模块。
3. 不让前端绕过 patient-facing / debug API 边界。
4. 不在患者端展示医生端 DDx / EvidenceGraph。
5. 不通过前端直接修改 CapabilityProfile，必须走 Proposal / Review / Publish。
```

---

# 八、最终结论

前端平台的价值是展示和治理 Runtime，而不是替代 Runtime。

Phase 5-P2 已交付 `console-web/` 最小治理 Console MVP（Runtime / Evaluation / Candidate / Review Queue / Audit Center）。本文档仍约束**完整产品化**前端（Training Center、Model Registry、正式登录、多租户等），这些能力仍后置。
