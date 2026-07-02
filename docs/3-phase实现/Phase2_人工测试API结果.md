# Phase 2 人工 API 验收记录（Postman）

| 项目 | 内容 |
|------|------|
| 验收日期 | 2026-06-26 |
| 验收人 | 手动验收 |
| 验收结论 | **通过** — Phase 2 共享能力资产原型人工 API 验收合格 |
| 代码基线 | commit `9d6d4e1`（人工 Postman 响应采集于 `82805db`；`assets-used` 路径在 `9d6d4e1` 加固为 debug API） |
| 启动方式 | `set JAVA_HOME=D:\cxj\software\jdk21` → `mvn -DskipTests package` → `java -jar target\clinmind-runtime-0.1.0-SNAPSHOT.jar` |
| Base URL | `http://localhost:8080` |

## 用例汇总

| # | 场景 | 关键验证点 | 结论 |
|---|------|------------|------|
| 1 | 资产 API · 包列表 | `phase2-default`，`default_package=true` | ✅ |
| 2 | 资产 API · 包详情 | `version=0.2.0`，含 chest_pain/fever | ✅ |
| 3 | 资产 API · 症状群摘要 | `must_not_miss_count=2`，`asset_ref` 含 `@` | ✅ |
| 4 | 资产 API · 未知包 | HTTP 404，`PACKAGE_NOT_FOUND` | ✅ |
| 5 | 患者 · 普通胸痛 | 无诊断泄漏；`source_assets_count=4`；`waiting_for_user` | ✅ |
| 6 | 患者 · 高风险 | `safety_gate_triggered`；输出含「风险信号」 | ✅ |
| 7 | 医生端 · 默认包 | DDx 4 项；`source_assets` 为 `asset_id@version` | ✅ |
| 8 | continue + trace | 安全门触发；step≥2 无 `EntryAssessment` | ✅ |
| 9 | Trace 资产记录 | `asset_package_id`；`knowledge_used` 含 `@` | ✅ |
| 10 | assets-used（debug） | `GET /api/v1/debug/runtime/{id}/assets-used`；`package_id=phase2-default` | ✅ |
| 11 | ExperienceContext | trace 含 `ExperienceContext` 与 `experience_used` | ✅ |
| 12 | 养生模式回归 | `wellness_mode`；无临床管线 | ✅ |
| 13 | status / result / 404 | 辅助接口正常 | ✅ |
| 14 | 替代包 phase2-alt | DDx 2 项；`package_id=phase2-alt` | ✅（JUnit） |
| 15 | broken-package | `error_safe_halted` + fail_safe | ✅（JUnit） |

## Phase 2 专项结论

| 验收项 | 结果 |
|--------|------|
| 默认资产包 Runtime 跑通 | ✅ |
| 资产 API 可查询包与症状群摘要 | ✅ |
| Trace 记录 `asset_package_id` / `asset_id@version` | ✅ |
| `/api/v1/debug/runtime/{id}/assets-used` 可查询本轮使用资产 | ✅ |
| ExperienceContext 命中并记录经验资产 | ✅ |
| 患者端不泄露 DDx / must_not_miss | ✅ |
| 医生端可见 DDx / EvidenceGraph | ✅ |
| Phase 1 行为未退化（回归抽测） | ✅ |
| 替代包 / broken 包（jar 外） | ✅ JUnit 137 项全绿 |

### 关键 runtime_id 对照

| 用例 | runtime_id | 备注 |
|------|------------|------|
| 5 患者普通 | `rt_5c8cf5d33ca6` | trace: `trace_23af3c63a07c` |
| 6 患者高风险 | `rt_e2bbd692437d` | `emergency_hint` + 安全门 |
| 7 医生端 | `rt_753e15b6a202` | 用于用例 8/9/10 |
| 11 经验 | `rt_b7b581df6ae4` | trace: `trace_cb859f7de36d` |

### 与 Phase 1 的差异（预期内）

- 患者端 `knowledge_context` 现为 **`source_assets_count`**（Phase 1 同为计数，Phase 2 底层为 4 条资产引用）。
- 医生端 / trace 中 `source_assets` / `knowledge_used` 为 **`asset_id@0.2.0`** 格式，不再是 `assets/symptom-groups/...` 路径。
- trace `output_summary` 新增 **`asset_package_id`**、**`asset_package_version`**。
- `assets-used`（debug API）在多次 continue 后会 **累积多轮记录**（同一 asset 可出现多条，属当前实现行为，不影响验收）。

---

## 详细请求与响应

### 用例 1 — 资产包列表

GET `http://localhost:8080/api/v1/assets/packages`

```
{
    "success": true,
    "data": {
        "packages": [
            {
                "package_id": "phase2-default",
                "version": "0.2.0",
                "status": "active",
                "display_name": "Phase 2 Default Asset Package",
                "supported_symptom_groups": ["chest_pain", "fever"],
                "default_package": true
            }
        ]
    },
    "error": null,
    "trace_id": null
}
```

---

### 用例 2 — 默认包详情

GET `http://localhost:8080/api/v1/assets/packages/phase2-default`

```
{
    "success": true,
    "data": {
        "package_id": "phase2-default",
        "version": "0.2.0",
        "status": "active",
        "display_name": "Phase 2 Default Asset Package",
        "supported_symptom_groups": ["chest_pain", "fever"],
        "default_package": true,
        "description": "Minimal shared assets for Phase 2 prototype",
        "source": "internal_phase2_yaml",
        "owner": "ClinMindRuntime"
    },
    "error": null,
    "trace_id": null
}
```

---

### 用例 3 — 症状群资产摘要

GET `http://localhost:8080/api/v1/assets/packages/phase2-default/symptom-groups/chest_pain`

```
{
    "success": true,
    "data": {
        "package_id": "phase2-default",
        "asset_id": "asset_symptom_chest_pain_v1",
        "asset_ref": "asset_symptom_chest_pain_v1@0.2.0",
        "asset_type": "symptom_group",
        "version": "0.2.0",
        "symptom_group": "chest_pain",
        "common_diagnosis_count": 2,
        "must_not_miss_count": 2,
        "required_question_count": 2,
        "recommended_test_count": 2,
        "source_asset_ids": ["asset_symptom_chest_pain_v1@0.2.0"]
    },
    "error": null,
    "trace_id": null
}
```

---

### 用例 4 — 未知包 404

GET `http://localhost:8080/api/v1/assets/packages/not-exist`

```
{
    "success": false,
    "data": null,
    "error": {
        "code": "PACKAGE_NOT_FOUND",
        "message": "Asset package not found: not-exist"
    },
    "trace_id": null
}
```

---

### 用例 5 — 患者端 · 普通胸痛

POST `http://localhost:8080/api/v1/runtime/start`

```json
{
  "session_id": "p2_manual_001",
  "mode": "patient_facing",
  "input": { "text": "我最近胸口闷，活动后更明显" },
  "basic_info": { "age": 58, "sex": "male" }
}
```

```
runtime_id: rt_5c8cf5d33ca6
runtime_status: waiting_for_user
knowledge_context: { symptom_group: chest_pain, source_assets_count: 4 }
patient_output.allowed: true
differential_board / evidence_graph / clinician_report: null（患者端隔离）
```

---

### 用例 6 — 患者端 · 高风险

POST `http://localhost:8080/api/v1/runtime/start`

```json
{
  "session_id": "p2_manual_002",
  "mode": "patient_facing",
  "input": { "text": "胸口闷，活动后加重，出汗" }
}
```

```
runtime_id: rt_e2bbd692437d
runtime_status: safety_gate_triggered
work_mode: emergency_hint
safety_gate.triggered: true, matched_rules: [rf_001]
patient_output.content: 含「风险信号」与就医建议
```

---

### 用例 7 — 医生端 · 默认包

POST `http://localhost:8080/api/v1/runtime/start`

```json
{
  "session_id": "p2_manual_003",
  "mode": "clinician_copilot",
  "input": { "text": "胸口闷，活动后更明显" }
}
```

```
runtime_id: rt_753e15b6a202
clinician_report.ddx_summary.length: 4
differential_board.candidates.length: 4
evidence_graph.items.length: 4
knowledge_context.source_assets:
  - asset_symptom_chest_pain_v1@0.2.0
  - asset_red_flag_rf_001@0.2.0
  - asset_red_flag_rf_002@0.2.0
  - asset_test_rec_test_001@0.2.0
patient_output: null
```

---

### 用例 8 — continue + trace

POST `http://localhost:8080/api/v1/runtime/continue`（`runtime_id=rt_753e15b6a202`）

```json
{
  "runtime_id": "rt_753e15b6a202",
  "input": { "text": "有点出汗，走路快的时候更明显，休息会缓解" }
}
```

```
runtime_status: safety_gate_triggered
safety_gate.triggered: true
case_frame.symptoms: 含 sweating（trigger: 走路）
```

GET `http://localhost:8080/api/v1/runtime/rt_753e15b6a202/trace`

```
traces.length: 3
step 2 modules_executed: 不含 EntryAssessment
step 3 modules_executed: 不含 EntryAssessment
safety_gate_triggered: true（step 2/3 output_summary）
```

---

### 用例 9 — Trace 资产记录

GET `http://localhost:8080/api/v1/runtime/rt_753e15b6a202/trace`（step 1 摘要）

```
output_summary.asset_package_id: phase2-default
output_summary.asset_package_version: 0.2.0
knowledge_used: [asset_symptom_chest_pain_v1@0.2.0, asset_red_flag_rf_001@0.2.0, ...]
experience_used: [asset_experience_exp_chest_activity_001@0.2.0]
modules_executed: 含 KnowledgeContext, ExperienceContext, SafetyGate, DecisionBoundary
```

---

### 用例 10 — assets-used（debug API）

GET `http://localhost:8080/api/v1/debug/runtime/rt_753e15b6a202/assets-used`

> 当前正式路径为 debug 内部接口；`82805db` 验收时曾使用 `/api/v1/runtime/{id}/assets-used`，已在 `9d6d4e1` 迁移。

```
package_id: phase2-default
package_version: 0.2.0
assets: 含 KnowledgeContext / ExperienceContext / SafetyGate / EvidenceGraph / DecisionBoundary 等模块记录
示例 asset_ref: asset_symptom_chest_pain_v1@0.2.0
```

---

### 用例 11 — ExperienceContext

POST start（`session_id=p2_manual_exp`，医生端，「活动后更明显」）→ `runtime_id=rt_b7b581df6ae4`

结合用例 9 trace：

```
modules_executed 含 ExperienceContext
experience_used 含 asset_experience_exp_chest_activity_001@0.2.0
```

---

### 用例 12 — 养生模式回归

POST `http://localhost:8080/api/v1/runtime/start`

```json
{
  "session_id": "p2_manual_wellness",
  "mode": "patient_facing",
  "input": { "text": "我想了解养生建议，怎么锻炼比较好" }
}
```

**预期（与 Phase 1 一致）：** `work_mode=wellness_mode`，`runtime_status=wellness_mode`，无临床管线输出。

---

### 用例 13 — status / result / 404

| 请求 | 预期 |
|------|------|
| `GET .../runtime/{id}/status` | 200，状态与最近一次操作一致 |
| `GET .../runtime/{id}/result` | 200；患者场景下无完整 DDx 泄漏 |
| `GET .../runtime/rt_not_exist/status` | 404，`RUNTIME_NOT_FOUND` |

---

### 用例 14–15 — 扩展包（JUnit 验收）

`phase2-alt` / `broken-package` 不在 `java -jar` 的 main classpath 内，Postman 标准包未执行；以下 JUnit 已通过：

- `RuntimeWithAlternateAssetPackageTest` — alt 包 DDx 2 项，`assets-used.package_id=phase2-alt`
- `RuntimeWithBrokenAssetPackageTest` — `error_safe_halted`，`constraints_applied` 含 `fail_safe`
- 全量：`mvn test` — **137 passed**（含 debug 路径与版本校验加固）

---

## 备注

```text
1. 用例 8 使用 rt_753e15b6a202（用例 7 医生端会话）做 continue，trace 共 3 步，符合「continue 轮次不含 EntryAssessment」。
2. assets-used 在多轮 continue 后存在重复 asset 记录，已记录为观察项，不阻塞 Phase 2 验收。
3. 扩展包 14–15 以自动化测试为准；若需 Postman 复现，需将 test/resources 下资产包复制到 main/resources 后重新打包。
4. 用例 10 响应内容采于 82805db；路径已随 9d6d4e1 更新为 GET /api/v1/debug/runtime/{runtime_id}/assets-used。
```
