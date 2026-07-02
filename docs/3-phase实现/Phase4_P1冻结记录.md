# Phase 4-P1 冻结记录

> 本文档用于记录 ClinMindRuntime Phase 4-P1 的冻结状态、冻结依据、当前边界、已知限制和后续进入 Phase 4-P2 / Phase 5 前的检查项。  
> 冻结不代表系统产品化完成，也不代表可以直接上线临床场景；冻结表示 Phase 4-P1 的候选治理与安全加固 MVP 已完成，后续不再继续向 Phase 4-P1 中堆新能力。

---

# 一、冻结结论

```text
冻结阶段：Phase 4-P1
冻结状态：已冻结
当前项目阶段：Phase 4-P1 freeze complete / Phase 4-P2 or Phase 5 planning pending
冻结日期：2026-06-29
代码基线：commit 4c7d595
```

Phase 4-P1 已完成的主线：

```text
EvaluationRun / RuntimeCaseExecution
→ CandidateGenerationService
→ CandidateSanitizer（P1-A）
→ CandidateSourceRefFactory / Validator（P1-B）
→ ExperienceCandidate / TrainingExampleCandidate
→ CandidateStore（resourceType-aware not found，P1-C）
→ CandidateReviewService / CandidateReviewStore（P1-D/E）
→ CandidateReviewController（Review debug API，P1-F）
```

Phase 4-P1 的目标已经达到：

```text
Phase4-P0 生成的 Candidate 已提升为：可脱敏、可强校验、可记录 review 决策、仍不自动生效。
```

---

# 二、冻结依据

## 2.1 任务清单依据

冻结依据：

```text
docs/Phase4_P1开发任务清单.md
```

当前任务清单显示 Phase4-P1-A 到 Phase4-P1-F 均已完成：

```text
Phase4-P1-A：CandidateSanitizer 与脱敏策略
Phase4-P1-B：CandidateSourceRef Factory 与组合校验
Phase4-P1-C：CandidateNotFoundException resourceType
Phase4-P1-D：CandidateReview 数据结构
Phase4-P1-E：CandidateReviewStore 与 Service
Phase4-P1-F：Review Debug API 与端到端测试
```

## 2.2 人工验收依据

冻结依据：

```text
docs/Phase4_P1人工测试API结果.md
docs/Phase4_人工测试API结果.md（P0 基线，P1 回归仍有效）
```

人工 / E2E 验收覆盖：

```text
1. training candidate 脱敏与 sanitizer policy metadata。
2. basic_info 脱敏（age_bucket / sex）。
3. PATIENT_SAFE_REWRITE 保留截断 patient_output，移除 patient_output_level。
4. POST review APPROVE / REJECT。
5. GET review by review_id / candidate_id。
6. 非法 review 流转 → CANDIDATE_NOT_REVIEWABLE。
7. review 后不修改 AssetPackage / Runtime。
```

## 2.3 自动化测试依据

```text
CandidateSanitizerTest / CandidateSanitizationPolicyTest
TrainingExampleCandidateSanitizationIntegrationTest
CandidateSourceRefFactoryTest / CandidateSourceRefValidatorTest
CandidateGenerationSourceRefIntegrationTest
CandidateNotFoundExceptionTest / CandidateControllerErrorCodeTest
CandidateReviewRecordTest / CandidateReviewTransitionPolicyTest
CandidateReviewStoreTest / CandidateReviewServiceTest
CandidateReviewControllerTest
CandidateReviewEndToEndIntegrationTest
Phase 1 / 2 / 3 / 4-P0 回归测试
```

冻结时全量 `mvn test`：**292 项全绿**（Java 21）。

## 2.4 文档治理依据

冻结前已完成文档同步：

```text
docs/README.md
docs/AI_IMPLEMENTATION_SKILL.md
docs/架构文档缺口审查清单.md
docs/Phase4_P1冻结记录.md（本文档）
```

## 2.5 相对 Phase4-P0 Hardening Backlog 的关闭项

Phase4-P0 冻结记录第六节列出的三项 hardening，已在 P1 实现：

```text
6.1 Training 候选 input 脱敏 → P1-A CandidateSanitizer
6.2 CandidateSourceRef 组合校验 → P1-B Factory / Validator
6.3 CandidateNotFoundException resourceType → P1-C
```

---

# 三、冻结范围

Phase 4-P1 冻结范围包括：

```text
1. candidate/sanitization/（Policy / Result / Sanitizer / InputSourceType）。
2. candidate/sourceref/（Factory / Validator / ValidationException）。
3. candidate/review/（Kind / Decision / Record / Request / TransitionPolicy / Service / Store）。
4. candidate/store/CandidateResourceType 与 resourceType-aware CandidateNotFoundException。
5. ApiExceptionHandler 基于 resourceType 的错误码映射。
6. TrainingExampleCandidateGenerator 脱敏接入与 sanitizer metadata。
7. ExperienceCandidateGenerator / TrainingExampleCandidateGenerator 的 SourceRef factory 接入。
8. CandidateReviewController（Review debug API）。
9. Phase 1 / Phase 2 / Phase 3 / Phase 4-P0 回归保护。
```

冻结后，以上能力只允许做：

```text
bug fix
测试补强
文档同步
错误码修正
小型一致性修复
P1 hardening（见第六节，需单独评估是否仍属 P1 范围）
```

不再向 Phase 4-P1 中新增大能力。

---

# 四、冻结前质量清理状态

| 清理项 | 状态 | 说明 |
|---|---|---|
| P1-A 至 P1-F 任务清单 | 已完成 | 全部标记 `[x]` |
| 人工 API 验收记录 | 已完成 | `Phase4_P1人工测试API结果.md` |
| AI 实现约束状态同步 | 已完成 | `AI_IMPLEMENTATION_SKILL.md` 标记 Phase4-P1 freeze |
| 文档导航状态同步 | 已完成 | `docs/README.md` 已更新 |
| 架构缺口审查状态同步 | 已完成 | `架构文档缺口审查清单.md` 已更新 |
| Training input 经 CandidateSanitizer | 已完成 | 集成测试与 E2E 已验证 |
| SourceRef factory 强校验 | 已完成 | 单元 / 集成测试已验证 |
| Review 只记录决策不自动生效 | 已完成 | Service 与 E2E 已验证 |
| P0 hardening 6.1–6.3 | 已关闭 | 见本文档 2.5 |

---

# 五、冻结后的禁止事项

冻结后不允许在 Phase 4-P1 中继续加入：

```text
1. 真实 RAG / GraphRAG。
2. Python AI Provider。
3. PostgreSQL / Redis / pgvector。
4. 前端 Training Center / Console。
5. 模型训练 / 后训练 / SFT / RLHF。
6. 正式医生审核平台（RBAC / 工作流 / 多级审批）。
7. ApprovedExperience 正式生效 / 自动上线。
8. TrainingDatasetVersion 正式发布。
9. AssetPackage / CapabilityProfile 自动修改。
10. Review 后自动触发 Runtime 行为变化。
11. ExperienceCandidate input 脱敏（未在 P1 范围，属 P2 hardening）。
12. MCP / LangGraph / Agent SDK 作为 Runtime 主控。
```

这些能力属于 Phase 4-P2 / Phase 5 或更后阶段。

---

# 六、已知限制与 P1 Hardening Backlog

以下项 **不阻塞 Phase 4-P1 冻结**，但应在接真实 RuntimeTrace 或进入 Phase 4-P2 前优先处理：

## 6.1 脱敏策略仅内存默认配置

现状：

```text
CandidateSanitizationPolicy.defaults() 为唯一生产策略（phase4-p1-default）。
无环境级 / 任务级策略切换 API，无策略版本管理界面。
```

后续 hardening：

```text
支持按环境或 case set 配置 policy；review 时可查看 policy diff。
```

## 6.2 REAL_RUNTIME 来源未做 live 抽测

现状：

```text
CandidateInputSourceType.REAL_RUNTIME 的 drop / mask 行为已有单元测试，
但冻结时未做 live HTTP 抽测（JAR 文件锁导致未能重启服务）。
E2E 覆盖以 SYNTHETIC_EVALUATION 为主。
```

后续 hardening：

```text
接真实 RuntimeCaseExecution 后补 live 验收与集成测试。
```

## 6.3 Review 与 Store 均为内存实现

现状：

```text
InMemoryCandidateStore / InMemoryCandidateReviewStore，进程重启后数据丢失。
Review API 为 debug 接口，无 RBAC / AuditLog。
```

后续 hardening：

```text
PostgreSQL 持久化；正式审核平台前补 RBAC 与审计链。
```

## 6.4 ExperienceCandidate 未接入 Sanitizer

现状：

```text
P1 仅对 TrainingExampleCandidate.input 做 CandidateSanitizer 处理。
ExperienceCandidate 仍直接使用生成器构建的 summary / context。
```

P1 可接受原因：

```text
1. 当前 experience 候选主要来自 synthetic evaluation / metric message。
2. 默认 REVIEW_REQUIRED，不自动生效。
```

后续 hardening：

```text
Phase4-P2 评估 ExperienceCandidate 是否需要独立脱敏策略。
```

## 6.5 APPROVED 候选仍不代表 Runtime 可用

现状：

```text
review_status=APPROVED 仅表示人工决策已记录，不触发 Asset / Capability / Training 变更。
```

这是 **设计边界**，不是 bug；后续 Phase 需单独设计 ApprovedExperience 生效链路。

---

# 七、进入 Phase 4-P2 / Phase 5 前的条件

进入下一阶段前需要先完成：

```text
1. 明确 Phase 4-P2 范围（ApprovedExperience、TrainingDatasetVersion、DoctorFeedback 等仍不在 P1）。
2. 评估第六节 hardening 项优先级，尤其 REAL_RUNTIME live 验收与持久化。
3. 新增或升级 Phase4-P2 详细设计与任务清单（如需要）。
4. 保持 Phase 1 / Phase 2 / Phase 3 / Phase 4-P0 / Phase 4-P1 回归测试通过。
5. 仍禁止自动上线经验、自动训练模型、自动改资产包。
```

Phase 4-P2 / Phase 5 推荐主题（backlog，未启动）：

```text
DoctorFeedback source
FollowUpOutcome source
ApprovedExperience 正式生效
ExperienceAssetVersion
TrainingDatasetVersion
Candidate export
PostgreSQL persistence
Frontend Training Center
AuditLog / RBAC
```

---

# 八、测试说明

冻结记录创建时，已在 Java 21 环境下运行全量 `mvn test`，**292 项全绿**。

已有测试与验收依据：

```text
docs/Phase4_P1开发任务清单.md
docs/Phase4_P1人工测试API结果.md
docs/Phase4_P1候选治理与安全加固_实现规格.md
docs/Phase4_P1候选脱敏与来源校验设计.md
docs/Phase4_P1候选Review记录设计.md
```

后续如果再次修改 Phase 4-P1 范围内代码，应重新运行测试并视情况更新验收记录。

---

# 九、最终结论

Phase 4-P1 已经可以冻结。

当前项目下一步不是继续向 Phase 4-P1 加功能，而是：

```text
1. 保持 Phase 4-P1 冻结边界。
2. 规划 Phase 4-P2 或 Phase 5 专项（ApprovedExperience、持久化、正式审核等）。
3. 接真实 RuntimeTrace 前优先处理 REAL_RUNTIME live 验收与持久化 hardening。
```
