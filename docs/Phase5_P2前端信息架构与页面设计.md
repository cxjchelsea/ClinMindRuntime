# Phase 5-P2 前端信息架构与页面设计

> 本文档定义 Phase 5-P2 最小前端 Console 的页面结构、信息架构、交互范围和展示边界。  
> P2 前端只服务于治理对象展示与最小 review 操作，不做正式医疗产品页面。

---

# 一、信息架构总览

推荐导航结构：

```text
ClinMind Console
├── Overview（可选）
├── Runtime Sessions
├── Evaluation Runs
├── Candidates
├── Review Queue
├── Audit Center
└── Debug Context
```

P2 可以不单独做 Overview，优先完成 5 个核心页面。

---

# 二、全局布局

## 2.1 AppShell

包含：

```text
顶部栏：项目名、当前 actor、roles、API base URL 状态
侧边栏：Runtime / Evaluation / Candidates / Review Queue / Audit Center
主区域：列表 + 详情面板
底部/角落：错误提示、请求状态
```

## 2.2 Debug Context Panel

用于输入：

```text
API Base URL
X-Debug-Token
X-Debug-Actor
X-Debug-Roles
```

建议 roles 以多选或逗号文本方式输入：

```text
SYSTEM_ADMIN
EVALUATION_REVIEWER
CANDIDATE_REVIEWER
AUDIT_REVIEWER
READ_ONLY_OBSERVER
```

P2 不做真实登录。

---

# 三、Runtime Sessions 页面

## 3.1 列表过滤

```text
status
session_id
limit
```

## 3.2 列表列

```text
runtime_id
session_id
runtime_status
mode
asset_package_id
asset_package_version
trace_count
updated_at
```

## 3.3 详情面板

展示：

```text
runtime_id
session_id
status
work_mode
mode
asset refs
version
trace_count
safety_triggered
created_at / updated_at
```

禁止展示：

```text
患者原文
input_history
patient_output
clinician_report
内部 DDx 全量内容
```

---

# 四、Evaluation Runs 页面

## 4.1 列表过滤

```text
status
case_set_id
limit
```

## 4.2 列表列

```text
run_id
case_set_id
case_set_version
asset_package_id
asset_package_version
status
started_at
completed_at
```

## 4.3 详情面板

展示：

```text
total_cases
passed_cases
failed_cases
pass_rate
item summary table
```

Item summary：

```text
case_id
runtime_id
passed
score
```

---

# 五、Candidates 页面

## 5.1 列表过滤

```text
kind
review_status
risk_level
limit
```

## 5.2 列表列

```text
candidate_id
candidate_kind
candidate_type / task_type
risk_level
review_status
sanitization_status
source_type
case_id
asset_package_id / version
created_at
```

## 5.3 详情面板

展示：

```text
candidate_id
kind
type / task_type
review_status
risk_level
sanitization_status
title / label
summary / reason
source_ref summary
policy metadata
```

禁止展示：

```text
raw training input
完整 input_texts
patient_output
clinician_report
```

---

# 六、Review Queue 页面

## 6.1 过滤条件

```text
kind
risk_level
task_type
limit
```

默认只看：

```text
review_status = REVIEW_REQUIRED
```

## 6.2 操作

P2 允许：

```text
Approve
Reject
Deprecate（如果后端当前状态允许）
```

Review 表单字段：

```text
decision
reason
reviewer
metadata（P2 可不做）
```

## 6.3 关键提示文案

页面必须提示：

```text
Review 只更新 candidate review_status，不会自动修改 AssetPackage、CapabilityProfile、Runtime 或 TrainingDataset。
```

---

# 七、Audit Center 页面

## 7.1 Summary 区

展示：

```text
total_count
count_by_action_type
count_by_resource_type
count_by_result_status
recent_failures
recent_review_actions
```

## 7.2 Audit Log 列表过滤

```text
actor
action_type
resource_type
resource_id
result_status
from
to
limit
```

## 7.3 列表列

```text
audit_id
actor
action_type
resource_type
resource_id
result_status
created_at
```

## 7.4 详情面板

展示 metadata summary，但不得展示敏感 metadata。

---

# 八、错误态设计

必须覆盖：

```text
401：缺少或错误 Debug Token
403：角色不足
404：资源不存在
400：查询参数错误
500：后端异常
network error：后端未启动 / API base URL 错误
```

错误展示建议：

```text
顶部 ErrorBanner + 页面局部错误信息
```

---

# 九、空态设计

每个列表页面都需要空态：

```text
当前没有可展示数据
请先运行 Runtime / Evaluation / Candidate generation
```

---

# 十、最终结论

Phase 5-P2 前端信息架构应保持克制：

```text
列表优先
详情辅助
操作最小
安全边界显式
```

它的目标是展示治理链路，而不是做完整业务后台。
