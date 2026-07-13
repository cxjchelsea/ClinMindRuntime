# Phase 11 后架构缺口与路线收敛决策

> 文档性质：架构决策记录  
> 对应路线图：`ClinMindRuntime阶段拆分路线图.md` v3.0  
> 当前系统基线：Phase 1–11 P0 已冻结；Phase 11-P1 收口中  
> 决策目标：确定实现优先级，同时保证完整系统设计、技术实现总方案和新增设计全部进入长期路线。

---

# 一、背景

ClinMindRuntime 已覆盖 Runtime、Agent、RAG、KG-lite、Python Provider、模型治理、Tool / MCP / Skills 治理、Evaluation、Candidate、Persistence、Audit、Governance Console 和三角色前端。

这证明以下架构原则可以成立：

```text
Runtime 主控
能力受控
结果可验证
输出可治理
过程可评估、可审计、可回滚
```

但当前系统存在明显失衡：

```text
控制平面
Runtime / Policy / Validation / Audit / Evaluation / Governance
相对完整

真实能力平面
LLM Agent / Evidence / Embedding / Rerank / FHIR / Clinical Workflow
仍以规则、Mock、YAML 或局部投影为主
```

如果继续无顺序地增加 Multi-Agent、MCP、Skill Store、Console 页面和生产平台对象，会进一步扩大治理外壳，却无法证明系统具有真实临床能力。

---

# 二、决策澄清：收敛实现顺序，不收缩完整设计

“路线收敛”不等于删除总设计中的能力。

本次决策采用：

```text
完整设计范围全部保留
+
近期实现主线严格收敛
+
所有后续能力按依赖和触发条件进入正式 Phase
```

因此：

- 完整系统设计中的八个能力域全部保留；
- 技术实现总方案中的全部包、API、存储和平台能力全部进入路线图；
- Agent 扩展、GraphRAG、训练治理、MCP、Skills、Experience Memory、完整 Console、生产治理、Voice / Realtime 等都具有后续 Phase；
- Phase 11 后新增的 Evidence Domain、Clinical Fact Plane、Policy IR、RuntimeRiskState、Recovery、Capability Lease、Shadow Execution 和分层 Evaluation 全部纳入；
- 暂不优先实现只表示未满足依赖，不表示从产品愿景删除。

---

# 三、近期优先级

Phase 11 后的前三个优先方向：

```text
第一优先：真实临床证据与单场景纵切
第二优先：真实能力接入所需的最小 Runtime 统一治理
第三优先：患者事实与纵向状态
```

只有第一优先立即启动；第二优先随真实能力同步建设；第三优先在单次纵切稳定后进入。

这解决当前最重要的问题：

```text
被治理的能力仍然太薄。
```

---

# 四、必须保留的系统边界

1. Runtime 是唯一主控；
2. Agent 只能生成 Proposal / Draft / Candidate / Finding；
3. RAG 只能产生 EvidenceCandidate / EvidenceRef；
4. Model 只能作为 Provider；
5. Tool / MCP / Skill 结果必须经过授权和校验；
6. SafetyGate 和 DecisionBoundary 不由模型接管；
7. Patient / Clinician View 必须在后端完成安全投影；
8. Candidate、Experience、Dataset、Model、Prompt、Asset 不自动发布；
9. Trace、Audit、Evaluation 覆盖关键调用；
10. 外部数据只能作为数据或证据，不能成为 Runtime 控制指令；
11. 高风险外部写操作必须经过事务治理与人工批准；
12. 任何框架都不得替代 Runtime 主控。

---

# 五、当前主要架构缺口

## 5.1 Evidence 缺少真实临床资产和 Claim 级证据链

需要补齐：

```text
SourceRegistry
EvidenceAssetVersion
EvidenceClaim
ClaimEvidenceLink
EvidenceGrade
EvidenceApplicability
Freshness
Conflict
Citation Verification
```

EvidenceProvider 应从简单检索插件升级为核心临床能力。

## 5.2 Runtime Policy 语义仍然分散

近期先建立：

```text
CapabilityDecision
RuntimeDatum / Provenance / Trust
Post-Capability Safety
RecoveryAction
```

长期再收束为：

```text
Policy IR
CapabilityDecisionEngine
RuntimeRiskState
Capability Lease
Causal Trace
```

## 5.3 缺少独立患者事实与纵向状态域

未来需要明确区分：

```text
患者事实
医学知识
本轮 Runtime Evidence
系统经验
Agent 推断
```

并建立 Raw Event、ClinicalFactLedger、Current State Projection、双时间和冲突重建。

## 5.4 缺少有副作用动作的事务语义

真实写操作出现后，必须引入：

```text
RuntimeActionProposal
ShadowRuntimeState
Preconditions
Staged Execution
Postconditions
Commit / Rollback
Human Approval
Kill Switch
```

## 5.5 缺少真实能力失败后的恢复

系统不能以“全部阻断”为安全目标，还需要：

```text
Rule fallback
Restricted retry
Ask clarification
Human review
Rollback
Safe halt
```

并评测 false block rate 和 recovery success。

---

# 六、全量阶段决策

```text
Phase 11-P1
收口真实 Runtime 角色投影

Phase 12
真实 Evidence + 真实 LLM Agent + 只读 FHIR + 单场景纵切

Phase 13
Clinical Data / Fact / Longitudinal State

Phase 14
Policy IR、RuntimeRiskState、Capability Lease 和因果 Trace

Phase 15
真实写操作的 Shadow / Staged Commit / Rollback

Phase 16
受控 Agent、可恢复 Workflow、Multi-Agent / Handoff

Phase 17
Deep Evidence、KG-lite / GraphRAG 和知识资产治理

Phase 18
真实 ModelProvider、训练、后训练、模型发布和回滚

Phase 19
真实 Tool / MCP / Skills 与 FHIR/EHR/HIS/LIS/PACS 集成

Phase 20
医生反馈、Clinical Experience Memory 和持续改进

Phase 21
生产认证、租户、完整治理 Console、发布、运维和合规

Phase 22
Voice / Realtime、Browser / Computer Use 和隔离前沿实验
```

上述阶段全部属于正式长期路线，但进入顺序由依赖关系控制。

---

# 七、Phase 12 首个纵切

固定首个领域：

```text
胸痛 / 胸闷风险分层与追问
```

必须打通：

```text
患者输入
→ LLM 追问 Proposal
→ Runtime 校验
→ FHIR 只读查询
→ Evidence Engine
→ Claim / Citation / Applicability
→ Post-Capability Safety
→ PatientOutput / ClinicianReport
→ Governance Trace
→ Evaluation
```

这是后续 Clinical Fact、统一 Policy、Agent Workflow、模型训练、Tool 集成和生产平台的真实基础。

---

# 八、后置不等于取消

Phase 12 完成前暂不优先建设：

- Multi-Agent / Handoff；
- 大规模远程 MCP 和 Skill Store；
- 完整 GraphRAG 平台；
- 完整 Clinical Fact Ledger；
- 写入型医疗工具；
- LoRA、DPO、RFT、蒸馏；
- 正式认证、多租户和完整审核平台；
- Voice、Realtime、Browser / Computer Use；
- 自动发布能力。

这些项目已经在路线图的 Phase 13–22 中拥有明确归属，不能再被理解为“从设计中删除”。

---

# 九、路线图完整性验收

新版路线图必须能逐项回答：

1. 八个能力域分别在哪个 Phase 实现；
2. 五层架构分别在哪个 Phase 完善；
3. 技术实现总方案中的 Java 包分别归属哪个 Phase；
4. Agent、RAG、Model、Tool、MCP、Skill 各自如何演进；
5. API、存储、Evaluation、Console、发布和运维在哪里实现；
6. 模型训练、后训练、GraphRAG、Multi-Agent、Experience Memory 是否有正式阶段；
7. 新增 Evidence、Clinical Fact、统一 Policy、Risk、Recovery、Action Transaction 是否有正式阶段；
8. Voice / Realtime、Browser 等实验技术是否被明确标为条件能力；
9. 永久禁止的能力是否与“后置能力”清楚区分。

只有全部能够映射，路线图才算覆盖完整系统设计和技术实现总方案。

---

# 十、决策结论

ClinMindRuntime 后续采用：

```text
完整设计范围不缩减
+
当前实现主线聚焦 Phase 12
+
后续能力按 Phase 13–22 逐步进入
```

核心目标不是让路线图变得更短，而是让它同时做到：

```text
近期可执行
长期无遗漏
依赖关系清楚
禁止边界明确
每项设计可追踪
```

因此，新版路线图既阻止无边界横向开发，也保证完整系统设计、技术实现总方案和 Phase 11 后新增架构中的全部正式能力都具有明确落点。