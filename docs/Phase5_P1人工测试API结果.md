# Phase 5-P1 人工 API 验收记录

| 项目 | 内容 |
|------|------|
| 验收日期 | 2026-07-01 |
| 代码基线 | commit `abc74e2` |
| 验收结论 | **通过** — in-memory 405 项全绿；postgres 专项 23 项（含 `Phase5P1ConsolePostgresEndToEndIntegrationTest`，需 Docker + `RUN_POSTGRES_TESTS=true`） |
| 启动方式（in-memory） | `mvn -DskipTests package` → `java -jar target/clinmind-runtime-0.1.0-SNAPSHOT.jar` |
| 启动方式（postgres） | `java -jar ... --spring.profiles.active=postgres`（需本地 PostgreSQL） |
| Base URL | `http://localhost:8080` |
| 自动化 | 405 项 in-memory 全量回归 + 23 项 postgres 专项（`RUN_POSTGRES_TESTS=true`） |

## 用例汇总

| # | 场景 | 关键验证点 | 结论 |
|---|------|------------|------|
| 1 | READ_ONLY_OBSERVER 查询有限 summary | `GET /api/v1/debug/console/runtime-sessions` 等列表可访问 | ✅（`ConsoleAccessDeniedControllerTest` + 集成测试） |
| 2 | READ_ONLY_OBSERVER 不能 review candidate | review POST 返回 403 | ✅（`CandidateReviewAccessPolicyIntegrationTest`） |
| 3 | CANDIDATE_REVIEWER 查询 review queue 并 review | review queue + review POST 200 | ✅（Console + review 集成测试） |
| 4 | AUDIT_REVIEWER 查询 audit center | summary / audit-logs 可访问 | ✅（`AuditCenterAccessPolicyTest`） |
| 5 | 无 token 请求 debug console 返回 401 | `X-Debug-Token` 缺失 | ✅（`DebugTokenFilterTest` + Console 控制器测试） |
| 6 | token 有效但 role 不足返回 403 | `ACCESS_DENIED` | ✅（`ConsoleAccessDeniedControllerTest`） |
| 7 | Console candidate detail 不返回 raw training input | 无 `input` / `policy_metadata.input` | ✅（`ConsoleSensitiveFieldRedactionIntegrationTest`） |
| 8 | Console runtime detail 不返回患者原文 | 无 `patient_output` / `input_history` | ✅（`ConsoleSensitiveFieldRedactionIntegrationTest`） |
| 9 | Console 查询和 review 产生 AuditLog | `QUERY_CONSOLE_*` action 写入 | ✅（`ConsoleAuditTrailIntegrationTest`） |
| 10 | postgres 模式 Console E2E | 持久化对象经 Console API 可查、role 生效、无敏感泄露 | ✅（`Phase5P1ConsolePostgresEndToEndIntegrationTest`，Docker 环境） |

## 启用 postgres 专项测试

前置条件：Docker Desktop 已启动（Docker Engine 29+ 需 Testcontainers 1.21.4+）。

```powershell
$env:RUN_POSTGRES_TESTS='true'
$env:JAVA_HOME='D:\cxj\software\jdk21'
mvn test -Dtest=FlywayMigrationTest,DatabaseSchemaSmokeTest,Jdbc*Test,Postgres*Test,Phase5PostgresEndToEndIntegrationTest,Phase5P1ConsolePostgresEndToEndIntegrationTest
```

项目已包含 `src/test/resources/docker-java.properties`（`api.version=1.44`）以兼容 Docker Engine 29。

## 边界说明

```text
1. Console API 前缀为 /api/v1/debug/console/**，仍受 DebugTokenFilter 保护。
2. RBAC-lite 通过 X-Debug-Actor / X-Debug-Roles header 传递，不是正式登录系统。
3. Safe DTO 过滤患者原文、clinician_report、未脱敏 candidate input。
4. APPROVED 候选仍不代表 Runtime 可用，不自动修改 AssetPackage / CapabilityProfile。
5. 未实现完整前端 Console / 正式审核平台 / RAG / 模型训练。
```
