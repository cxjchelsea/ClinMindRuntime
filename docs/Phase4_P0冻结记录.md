# Phase 4-P0 冻结记录

> 本文档用于记录 ClinMindRuntime Phase 4-P0 的冻结状态、冻结依据、当前边界、已知限制和后续进入 Phase 4-P1 前的检查项。  
> 冻结不代表系统产品化完成，也不代表可以直接上线临床场景；冻结表示 Phase 4-P0 的候选沉淀机制 MVP 已完成，后续不再继续向 Phase 4-P0 中堆新能力。

---

# 一、冻结结论

```text
冻结阶段：Phase 4-P0
冻结状态：已冻结
当前项目阶段：Phase 4-P0 freeze complete / Phase 4-P1 planning pending
冻结日期：2026-06-29
代码基线：commit bbfeabd
```

Phase 4-P0 已完成的主线：

```text
EvaluationRun / EvaluationResult / EvaluationItemResult / MetricResult
→ CandidateGenerationPolicy
→ CandidateMappingPolicy
→ ExperienceCandidateGenerator
→ TrainingExampleCandidateGenerator
→ CandidateGenerationService
→ CandidateStore
→ CandidateController（debug API）
```

Phase 4-P0 的目标已经达到：

```text
评估暴露的问题 → 可追踪、可审核、不可自动生效的 ExperienceCandidate / TrainingExampleCandidate
```

---

# 二、冻结依据

## 2.1 任务清单依据

冻结依据：

```text
docs/Phase4_开发任务清单.md
```

当前任务清单显示 Phase4-P0-A 到 Phase4-P0-G 均已完成：

```text
Phase4-P0-A：Candidate 数据结构
Phase4-P0-B：CandidateStore
Phase4-P0-C：CandidateMappingPolicy
Phase4-P0-D：ExperienceCandidateGenerator
Phase4-P0-E：TrainingExampleCandidateGenerator
Phase4-P0-F：CandidateGenerationService
Phase4-P0-G：Candidate debug API 与验收测试
```

## 2.2 人工验收依据

冻结依据：

```text
docs/Phase4_人工测试API结果.md
```

人工 API 验收覆盖：

```text
1. 从 failure EvaluationRun 生成候选。
2. 查询 generation result。
3. 查询 experience / training candidates 列表与单个候选。
4. 从 passed run 生成空候选。
5. unknown run_id / generation_id 错误路径。
6. 非法 policy 参数错误路径。
7. 候选均为 REVIEW_REQUIRED。
```

关键验收 ID（2026-06-29 手动抽测）：

```text
失败 run：eval_95fc766c8c92
generation：cand_gen_023dbd069a33
passed run：eval_08fe96f0a487
```

## 2.3 自动化测试依据

```text
CandidateControllerTest
CandidateEndToEndIntegrationTest
CandidateGenerationServiceTest / IntegrationTest
ExperienceCandidateGeneratorTest
TrainingExampleCandidateGeneratorTest
InMemoryCandidateStoreTest
Phase 1 / 2 / 3 回归测试
```

冻结时全量 `mvn test`：**245 项全绿**（Java 21）。

## 2.4 文档治理依据

冻结前已完成文档同步：

```text
docs/README.md
docs/AI_IMPLEMENTATION_SKILL.md
docs/架构文档缺口审查清单.md
docs/Phase4_P0冻结记录.md（本文档）
```

---

# 三、冻结范围

Phase 4-P0 冻结范围包括：

```text
1. Candidate 数据结构（ExperienceCandidate / TrainingExampleCandidate / CandidateSourceRef 等）。
2. InMemoryCandidateStore。
3. CandidateMappingPolicy。
4. ExperienceCandidateGenerator / TrainingExampleCandidateGenerator。
5. CandidateGenerationService（只读 EvaluationRunStore）。
6. Candidate debug API（/api/v1/debug/candidates/**）。
7. Phase 1 / Phase 2 / Phase 3 回归保护。
```

冻结后，以上能力只允许做：

```text
bug fix
测试补强
文档同步
错误码修正
小型一致性修复
P0 hardening（见第六节，需单独评估是否仍属 P0 范围）
```

不再向 Phase 4-P0 中新增大能力。

---

# 四、冻结前质量清理状态

| 清理项 | 状态 | 说明 |
|---|---|---|
| P0-A 至 P0-G 任务清单 | 已完成 | 全部标记 `[x]` |
| 人工 API 验收记录 | 已完成 | `Phase4_人工测试API结果.md` |
| AI 实现约束状态同步 | 已完成 | `AI_IMPLEMENTATION_SKILL.md` 标记 Phase4-P0 freeze |
| 文档导航状态同步 | 已完成 | `docs/README.md` 已更新 |
| 架构缺口审查状态同步 | 已完成 | `架构文档缺口审查清单.md` 已更新 |
| 候选默认 REVIEW_REQUIRED | 已完成 | 生成器与 API 验收已验证 |
| 不修改 AssetPackage / CapabilityProfile | 已完成 | Service 只读 EvaluationRunStore |
| 不调用 RuntimeService / EvaluationRunner 重新执行 | 已完成 | 集成测试与架构约束已验证 |

---

# 五、冻结后的禁止事项

冻结后不允许在 Phase 4-P0 中继续加入：

```text
1. 真实 RAG / GraphRAG。
2. Python AI Provider。
3. PostgreSQL / Redis / pgvector。
4. 前端 Training Center / Console。
5. 模型训练 / 后训练 / SFT / RLHF。
6. 医生审核流 / 候选自动上线。
7. TrainingDataset 正式发布。
8. AssetPackage / CapabilityProfile 自动修改。
9. 经验自动进化。
10. MCP / LangGraph / Agent SDK 作为 Runtime 主控。
```

这些能力属于 Phase 4-P1 / Phase 5 或更后阶段。

---

# 六、已知限制与 P0 Hardening Backlog

以下项 **不阻塞 Phase 4-P0 冻结**，但应在接真实 RuntimeTrace 或进入 Phase 4-P1 前优先处理：

## 6.1 Training 候选 input 脱敏

现状：

```text
TrainingExampleCandidateGenerator 会将 evaluation case 的 input_texts、basic_info、
case frame summary、patient output 等写入 training candidate input。
```

P0 可接受原因：

```text
1. 当前使用 synthetic evaluation cases。
2. 候选默认 REVIEW_REQUIRED。
3. sanitization_status 默认 NEEDS_REVIEW。
4. debug API 设计已要求不返回未脱敏患者原文。
```

后续 hardening：

```text
接入真实 RuntimeTrace 前，必须增加 CandidateSanitizer / 生成前脱敏层，
按 sourceType 决定哪些字段可进入 training candidate input。
参考：docs/数据安全与合规边界规划.md
```

## 6.2 CandidateSourceRef 组合校验

现状：

```text
CandidateSourceRef 目前只强校验 sourceType，
未按 sourceType 强校验 evaluationRunId、caseId、assetPackageId、assetPackageVersion 等组合字段。
```

后续 hardening：

```text
在 record 或 factory 层增加按 sourceType 的必填字段校验，
例如 EVALUATION_METRIC 必须带 evaluationRunId + caseId + metricId + assetPackageId/Version。
```

## 6.3 CandidateNotFoundException 错误码判定

现状：

```text
ApiExceptionHandler 对 experience / training candidate not found 的部分判断
依赖异常 message 是否包含 "Training example"。
```

后续 hardening：

```text
为 CandidateNotFoundException 增加显式 resourceType（GENERATION / EXPERIENCE / TRAINING_EXAMPLE），
store 抛出时指定类型，handler 不再依赖 message 字符串。
```

---

# 七、进入 Phase 4-P1 前的条件

进入 Phase 4-P1 前需要先完成：

```text
1. 明确 Phase 4-P1 范围（审核流、ApprovedExperience、TrainingDataset 等仍不在 P0）。
2. 评估第六节 hardening 项优先级，尤其脱敏层。
3. 新增或升级 Phase4-P1 详细设计与任务清单。
4. 保持 Phase 1 / Phase 2 / Phase 3 / Phase 4-P0 回归测试通过。
5. 仍禁止自动上线经验、自动训练模型、自动改资产包。
```

Phase 4-P1 推荐主题（backlog，未启动）：

```text
DoctorFeedback source
FollowUpOutcome source
Candidate review workflow
ApprovedExperience
ExperienceAssetVersion
TrainingDatasetVersion
Candidate export
```

---

# 八、测试说明

冻结记录创建时，已在 Java 21 环境下运行全量 `mvn test`，**245 项全绿**。

已有测试与验收依据：

```text
docs/Phase4_开发任务清单.md
docs/Phase4_人工测试API结果.md
docs/Phase4_API与测试设计.md
```

后续如果再次修改 Phase 4-P0 范围内代码，应重新运行测试并视情况更新验收记录。

---

# 九、最终结论

Phase 4-P0 已经可以冻结。

当前项目下一步不是继续向 Phase 4-P0 加功能，而是：

```text
1. 保持 Phase 4-P0 冻结边界。
2. 规划 Phase 4-P1（候选审核、正式经验、训练数据集等）。
3. 在接真实 trace 前优先处理脱敏与 CandidateSourceRef hardening。
```
