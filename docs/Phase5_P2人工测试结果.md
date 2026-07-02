# Phase 5-P2 前端 Console 验收记录

| 项目 | 内容 |
|------|------|
| 验收日期 | 2026-06-25 |
| 代码基线 | commit `270b015` |
| 验收结论 | **通过** — 前端 35 项 vitest 全绿；`npm run build` 通过；后端 `mvn test` 回归通过（exit 0，Phase 1–5 未被破坏） |
| 后端启动 | `mvn -DskipTests package` → `java -jar target/clinmind-runtime-0.1.0-SNAPSHOT.jar` |
| 前端启动 | `cd console-web && npm install && npm run dev` → `http://localhost:5173` |
| Base URL | 后端 `http://localhost:8080`；前端 `/api` 代理至 8080 |

## 自动化验收

| # | 场景 | 关键验证点 | 结论 |
|---|------|------------|------|
| 1 | 前端工程 build | `npm run build`（tsc + vite） | ✅ |
| 2 | 前端单元 / 集成测试 | `npm run test` — 14 文件、35 项 | ✅ |
| 3 | ConsoleAppSmokeTest | 五页导航、index 重定向 | ✅ |
| 4 | SensitiveFieldRedactionRenderTest | Runtime / Candidate / Evaluation / Audit 详情不渲染敏感字段 | ✅ |
| 5 | Runtime / Evaluation 页面 | 列表 + Safe DTO 详情 | ✅ |
| 6 | Candidate / Review Queue | 列表、详情、review 表单、治理提示 | ✅ |
| 7 | Audit Center | summary、log 列表、过滤、详情、403 展示 | ✅ |
| 8 | 后端全量回归 | `mvn test`（in-memory 默认套件） | ✅ |

## 本地联调演示清单（人工）

前置：后端 jar 已启动；前端 `npm run dev`；Debug Context 配置有效 `X-Debug-Token` 与合适 roles。

| # | 页面 | 操作 | 预期 | 结论 |
|---|------|------|------|------|
| 1 | Runtime Sessions | 查询列表并点选一行 | 显示 Safe DTO 摘要，无 `patient_output` / `input_history` | ✅（自动化 + 规格对齐） |
| 2 | Evaluation Runs | 查询 run 并查看 item | 显示聚合摘要，无敏感输出字段 | ✅ |
| 3 | Candidates | 查询候选并查看详情 | 无 raw training input / `policy_metadata.input` | ✅ |
| 4 | Review Queue | CANDIDATE_REVIEWER 提交 APPROVE | 成功提示含「未自动上线或进入训练集」，queue 刷新 | ✅（ReviewQueueFlowTest） |
| 5 | Review Queue | READ_ONLY_OBSERVER | 隐藏 review 表单，后端 403 有 UI 反馈 | ✅ |
| 6 | Audit Center | AUDIT_REVIEWER 查 summary / logs | 显示 count 统计与 log 列表 | ✅ |
| 7 | Audit Center | READ_ONLY_OBSERVER | 403 + 角色提示 | ✅（PermissionErrorFlowTest） |
| 8 | Debug Context | 测试连接失败 | ErrorBanner / panel 显示网络错误 | ✅（App.smoke.test.tsx） |

## 前端测试文件一览

```text
console-web/src/tests/
  App.smoke.test.tsx
  ConsoleAppSmoke.test.tsx
  SensitiveFieldRedactionRender.test.tsx
  RuntimePage.test.tsx
  EvaluationPage.test.tsx
  CandidatePage.test.tsx
  ReviewQueueFlow.test.tsx
  AuditCenterPage.test.tsx
  AuditCenterFlow.test.tsx
  PermissionErrorFlow.test.tsx
  SensitiveFieldRenderGuard.test.ts
  consoleClient.test.ts
  reviewActions.test.ts
  errors.test.ts
```

## 边界说明

```text
1. Phase 5-P2 是最小治理 Console MVP，不是完整生产前端。
2. 认证仍为 Debug Token + X-Debug-Actor / X-Debug-Roles header，不是正式登录。
3. Review 只更新 candidate review_status，不自动上线经验或进入训练集。
4. 前端 SensitiveFieldRenderGuard 与后端 Safe DTO 双重约束，不展示患者原文与未脱敏输入。
5. 未实现 Docker Compose 一键编排、正式医生审核平台、RAG、模型训练。
```
