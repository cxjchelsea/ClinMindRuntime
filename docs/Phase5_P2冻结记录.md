# Phase 5-P2 冻结记录

> 本文档用于记录 ClinMindRuntime Phase 5-P2 的冻结状态、冻结依据、当前边界、已知限制与 Phase 5 整体归档引用。  
> 冻结不代表系统产品化完成，也不代表可以直接上线临床场景；冻结表示 Phase 5-P2 的最小前端 Console MVP 已完成，后续不再继续向 Phase 5-P2 中堆新能力。

---

# 一、冻结结论

```text
冻结阶段：Phase 5-P2
冻结状态：已冻结
当前项目阶段：Phase 5（P0/P1/P2）已全部完成并冻结
冻结日期：2026-06-25
代码基线：commit fe16d2b
```

Phase 5-P2 已完成的主线：

```text
console-web/（Vite + React + TypeScript）
→ DebugContextPanel + consoleClient
→ Runtime / Evaluation / Candidate / Review Queue / Audit Center 页面
→ ReviewForm（APPROVE / REJECT / DEPRECATE）
→ SensitiveFieldRenderGuard + GovernanceNotice
→ ConsoleAppSmokeTest / SensitiveFieldRedactionRenderTest 等 35 项 vitest
```

Phase 5-P2 的目标已经达到：

```text
Phase 5-P1 Safe Console API 已具备最小可视化入口；前端只消费 Safe DTO，不暴露敏感字段，Review 仍不自动上线或进入训练集。
```

---

# 二、冻结依据

## 2.1 任务清单依据

```text
docs/Phase5_P2开发任务清单.md
```

当前任务清单显示 Phase5-P2-A 到 Phase5-P2-F 均已完成。

## 2.2 自动化测试依据

```text
console-web：npm run test — 14 文件、35 项全绿
console-web：npm run build — tsc + vite 通过
后端：mvn test — exit 0（Phase 1–5 回归未被破坏）
```

覆盖：

```text
ConsoleAppSmoke.test.tsx
SensitiveFieldRedactionRender.test.tsx
RuntimePage / EvaluationPage / CandidatePage
ReviewQueueFlow / AuditCenterPage / AuditCenterFlow / PermissionErrorFlow
SensitiveFieldRenderGuard / reviewActions / consoleClient / errors
App.smoke.test.tsx
```

## 2.3 人工验收依据

```text
docs/Phase5_P2人工测试结果.md
```

## 2.4 文档治理依据

冻结前已完成文档同步：

```text
docs/Phase5_P2冻结记录.md（本文档）
docs/Phase5冻结记录.md
docs/Phase5_P2开发任务清单.md
docs/Phase5_P2人工测试结果.md
docs/ClinMindRuntime技术实现总方案.md
docs/架构文档缺口审查清单.md
docs/README.md
docs/AI_IMPLEMENTATION_SKILL.md
README.md
```

---

# 三、冻结范围

Phase 5-P2 冻结范围包括：

```text
1. console-web/ 工程与 vite 代理配置。
2. AppShell / Sidebar / DebugContextPanel。
3. consoleClient、types、errors、reviewActions。
4. RuntimePage / EvaluationPage / CandidatePage / ReviewQueuePage / AuditCenterPage。
5. ReviewForm、GovernanceNotice、SensitiveFieldRenderGuard、DetailPanel 等共享组件。
6. console-web/src/tests/ 下全部 P2 测试。
```

冻结后，以上能力只允许做：

```text
bug fix
测试补强
文档同步
依赖安全升级（不破坏 API 契约）
小型 UI 一致性修复
```

不再向 Phase 5-P2 中新增大能力。

---

# 四、冻结后的禁止事项

冻结后不允许在 Phase 5-P2 中继续加入：

```text
1. 完整产品化 Training Center / Model Registry UI。
2. 正式登录系统 / JWT / OAuth / 多租户。
3. 正式医生审核平台（多级审批 / 工作流）。
4. Docker Compose 一键编排（属后置任务）。
5. 绕过 Safe Console API 直接读取 raw snapshot。
6. 在前端缓存或展示患者原文、clinician_report、raw training input。
7. Review 成功文案暗示「已上线」或「已进入训练集」。
8. RAG / GraphRAG / 模型训练相关 UI。
```

这些能力须在新 Phase 或后置任务中单独立项。

---

# 五、已知限制

```text
1. Debug Token / Actor / Roles 通过侧边栏配置，不是生产级认证。
2. Token 不写入 localStorage（by design）。
3. 无 Docker Compose；后端与前端需分别启动。
4. 无完整分页 / 高级图表 / 设计系统。
5. Review 仍只更新 candidate review_status，不自动改 AssetPackage / CapabilityProfile。
6. postgres 专项后端测试仍依赖 Docker + RUN_POSTGRES_TESTS=true。
```

---

# 六、Phase 5 整体归档

Phase 5-P2 冻结后，Phase 5 全阶段归档见：

```text
docs/Phase5冻结记录.md
```

子阶段冻结记录：

```text
docs/Phase5_P0冻结记录.md
docs/Phase5_P1冻结记录.md
docs/Phase5_P2冻结记录.md（本文档）
```

---

# 七、最终结论

Phase 5-P2 已完成「可演示、可测试、可复盘」的最小前端治理 Console。  
后续工作应在新 Phase 或后置任务中立项，而不是继续向 P2 范围追加能力。
