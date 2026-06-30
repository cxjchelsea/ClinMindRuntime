# Phase 5-P0 人工 API 验收记录

| 项目 | 内容 |
|------|------|
| 验收日期 | 2026-06-30 |
| 代码基线 | commit `2ad656d` |
| 验收结论 | **通过** — in-memory 369 项全绿（含 postgres 专项 22 项） |
| 启动方式（in-memory） | `mvn -DskipTests package` → `java -jar target/clinmind-runtime-0.1.0-SNAPSHOT.jar` |
| 启动方式（postgres） | `java -jar ... --spring.profiles.active=postgres`（需本地 PostgreSQL） |
| Base URL | `http://localhost:8080` |
| 自动化 | 369 项全量回归（347 in-memory + 22 postgres 专项，`RUN_POSTGRES_TESTS=true`） |

## 用例汇总

| # | 场景 | 关键验证点 | 结论 |
|---|------|------------|------|
| 1 | GET persistence health（in-memory） | `mode=in-memory`，store 均为 in-memory | ✅ |
| 2 | POST evaluation run | 产生 `CREATE_EVALUATION_RUN` AuditLog | ✅ |
| 3 | POST candidate generation | 产生 `GENERATE_CANDIDATES` AuditLog | ✅ |
| 4 | POST candidate review | 产生 `REVIEW_*` AuditLog | ✅ |
| 5 | GET audit-logs | 可按 resource 查询 | ✅ |
| 6 | Debug token 保护 | `require-debug-token=true` 时无 token 返回 401 | ✅ |
| 7 | postgres 模式 health | `mode=postgres`，jdbc stores | ✅（Testcontainers 22 项全绿） |

## 启用 postgres 专项测试

前置条件：Docker Desktop 已启动（Docker Engine 29+ 需 Testcontainers 1.21.4+）。

```powershell
$env:RUN_POSTGRES_TESTS='true'
$env:JAVA_HOME='D:\cxj\software\jdk21'
mvn test -Dtest=FlywayMigrationTest,DatabaseSchemaSmokeTest,Jdbc*Test,Postgres*Test,Phase5PostgresEndToEndIntegrationTest
```

项目已包含 `src/test/resources/docker-java.properties`（`api.version=1.44`）以兼容 Docker Engine 29。

## 边界说明

```text
1. 默认 in-memory 启动不依赖数据库。
2. AuditLog metadata 不保存 patient_output / input_texts 等敏感字段。
3. APPROVED 候选仍不代表 Runtime 可用。
4. 未实现 RBAC / 前端 Console / 正式审核平台。
5. Debug token 仅保护 /api/v1/debug/** 路径。
```
