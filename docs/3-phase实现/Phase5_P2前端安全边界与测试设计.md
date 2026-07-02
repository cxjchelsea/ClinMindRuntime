# Phase 5-P2 前端安全边界与测试设计

> 本文档定义 Phase 5-P2 前端 Console MVP 的安全边界、禁止事项、测试分层和人工验收要求。  
> P2 前端是 internal governance console，不是患者端页面，也不是正式医生审核平台。

---

# 一、安全边界原则

Phase 5-P2 前端必须遵守：

```text
1. 只调用 Phase 5-P1 Console API 和既有 review API。
2. 不调用 raw debug API 获取未脱敏对象。
3. 不展示患者原文、clinician_report、raw training input。
4. 不把 CANDIDATE APPROVED 表述为“已上线”。
5. 不把 Review Queue 表述为正式医生审核平台。
6. 不在前端实现绕过后端 RBAC-lite 的逻辑。
```

---

# 二、敏感字段禁止清单

前端渲染层不得出现以下字段名或内容：

```text
patient_output
clinician_report
input_history
input_texts
raw_input
patient_input
raw training input
full ddx board
```

测试中应通过字符串扫描确保 response render 后不出现上述字段。

---

# 三、权限边界

P2 前端只负责传递：

```text
X-Debug-Token
X-Debug-Actor
X-Debug-Roles
```

权限判断由后端完成。

前端可以根据 role 做 UI 显示控制，但不能只依赖前端隐藏按钮来当作安全机制。

示例：

```text
READ_ONLY_OBSERVER：前端隐藏 Review 按钮，但后端仍必须返回 403。
CANDIDATE_REVIEWER：显示 Review 按钮。
AUDIT_REVIEWER：显示 Audit Center。
SYSTEM_ADMIN：显示全部页面。
```

---

# 四、Review 操作安全文案

Review Queue 页面必须显示提示：

```text
本操作只更新 Candidate 的 review_status，不会自动修改 AssetPackage、CapabilityProfile、Runtime 或 TrainingDataset。
```

Approve 成功后的文案应是：

```text
候选已标记为 APPROVED。
```

不能写成：

```text
经验已上线
训练数据已发布
模型已学习
系统已自动优化
```

---

# 五、测试分层

## 5.1 Unit Test

```text
consoleClient header test
error mapping test
DebugContext persistence test
SensitiveFieldRenderGuard test
review endpoint selection test
```

## 5.2 Component Test

```text
DebugContextPanelTest
RuntimePageTest
EvaluationPageTest
CandidatePageTest
ReviewQueuePageTest
AuditCenterPageTest
ErrorBannerTest
```

## 5.3 Integration / Mock API Test

```text
ConsoleAppSmokeTest
ReviewQueueFlowTest
AuditCenterFlowTest
PermissionErrorFlowTest
SensitiveFieldRedactionRenderTest
```

## 5.4 Manual Test

新增：

```text
docs/Phase5_P2人工测试结果.md
```

验收场景：

```text
1. 填写 token / actor / roles 后可访问 Console。
2. READ_ONLY_OBSERVER 可看 summary，不可 review。
3. CANDIDATE_REVIEWER 可 review candidate。
4. AUDIT_REVIEWER 可看 Audit Center。
5. Runtime detail 不显示患者原文。
6. Candidate detail 不显示 raw training input。
7. Review 成功后刷新 review queue。
8. 后端未启动时显示 network error。
9. token 错误时显示 401。
10. role 不足时显示 403。
```

---

# 六、构建与启动验收

P2 完成后应支持：

```powershell
cd console-web
npm install
npm run dev
npm run test
npm run build
```

P2 不要求：

```text
Dockerized frontend
Spring Boot 静态资源打包
Nginx 部署
CI 自动部署
```

---

# 七、与后端回归的关系

前端开发不得破坏后端：

```text
mvn test
RUN_POSTGRES_TESTS=true mvn test（可选 / Docker 环境）
```

如果只改前端，可以不强制跑全部 postgres 专项，但归档前必须记录后端回归情况。

---

# 八、最终结论

Phase 5-P2 的安全要求是：

```text
前端只展示 Safe DTO，
权限由后端判定，
Review 不自动生效，
任何页面不泄露敏感字段。
```
