# Phase 1 人工 API 验收记录（Postman）

| 项目 | 内容 |
|---|---|
| 验收日期 | 2026-06-26 |
| 验收人 | 手动验收 |
| 验收结论 | **通过** — Phase 1 后端 MVP 人工 API 验收合格 |
| 代码基线 | commit `2abe52d` |
| 启动方式 | `set JAVA_HOME=D:\cxj\software\jdk21` → `mvn -DskipTests package` → `java -jar target/clinmind-runtime-0.1.0-SNAPSHOT.jar` |
| Base URL | `http://localhost:8080` |

## 用例汇总

| # | 场景 | 关键验证点 | 结论 |
|---|---|---|---|
| 1 | 患者端 · 普通胸痛 | 无诊断字段泄漏；`waiting_for_user` + 追问 | ✅ |
| 2 | 患者端 · 高风险胸痛 | `safety_gate_triggered`；无低风险安抚语 | ✅ |
| 3 | 医生端 · 同场景 | 有 DDx / 证据图；无 `patient_output` | ✅ |
| 4 | continue + trace | step 2 不含 `EntryAssessment`；状态升级正确 | ✅ |
| 5 | 养生模式 | `wellness_mode`；无临床输出 | ✅ |
| 6 | status / result / trace / 404 | 辅助接口与错误码正常 | ✅ |

## 详细请求与响应

### 用例 1 — 患者端 · 普通胸痛（信息收集）

POST `http://localhost:8080/api/v1/runtime/start`

```
{

  "session_id": "manual_001",

  "mode": "patient_facing",

  "input": { "text": "我最近胸口闷，活动后更明显" },

  "basic_info": { "age": 58, "sex": "male" }

}
```

结果：

```
{
    "success": true,
    "data": {
        "runtime_id": "rt_26dfad820209",
        "runtime_status": "waiting_for_user",
        "work_mode": "clinical_mode",
        "risk_level": "none",
        "entry_assessment": {
            "work_mode": "clinical_mode",
            "symptom_group": "chest_pain",
            "reason": "clinical symptoms detected",
            "confidence": 0.8
        },
        "case_frame": {
            "chief_complaint": "我最近胸口闷",
            "missing_slots": [
                "associated_symptoms",
                "symptom_duration"
            ],
            "symptoms": [
                {
                    "name": "chest_discomfort",
                    "duration": null,
                    "severity": "明显",
                    "location": "chest",
                    "trigger": "活动后",
                    "frequency": null,
                    "relief": null
                }
            ],
            "patient_profile": {
                "age": 58,
                "sex": "male",
                "risk_factors": []
            }
        },
        "knowledge_context": {
            "symptom_group": "chest_pain",
            "source_assets_count": 2
        },
        "safety_gate": {
            "triggered": false,
            "risk_level": "none",
            "matched_rules": [],
            "reason": null,
            "required_action": null,
            "patient_output_constraint": null,
            "fail_safe_required": false
        },
        "differential_board": null,
        "evidence_graph": null,
        "next_action": {
            "type": "ask_question",
            "content": "是否伴随出汗或呼吸困难？",
            "purpose": "collect missing evidence",
            "priority": "medium",
            "reason": "missing evidence should be collected before narrowing diagnosis"
        },
        "patient_output": {
            "allowed": true,
            "content": "为了更准确了解情况，我需要再确认一个问题：是否伴随出汗或呼吸困难？",
            "output_level": "O1_continue_questioning",
            "constraints_applied": [
                "no_definitive_diagnosis",
                "no_prescription"
            ]
        },
        "clinician_report": null
    },
    "error": null,
    "trace_id": "trace_17063271c57c"
}
```

### 用例 2 — 患者端 · 高风险胸痛

POST `http://localhost:8080/api/v1/runtime/start`

```
{

  "session_id": "manual_002",

  "mode": "patient_facing",

  "input": { "text": "胸口闷，活动后加重，出汗" }

}
```

结果：

```
{
    "success": true,
    "data": {
        "runtime_id": "rt_a127c4d543da",
        "runtime_status": "safety_gate_triggered",
        "work_mode": "emergency_hint",
        "risk_level": "high",
        "entry_assessment": {
            "work_mode": "emergency_hint",
            "symptom_group": "chest_pain",
            "reason": "possible emergency features detected",
            "confidence": 0.85
        },
        "case_frame": {
            "chief_complaint": "胸口闷",
            "missing_slots": [
                "age",
                "sex",
                "symptom_duration"
            ],
            "symptoms": [
                {
                    "name": "chest_discomfort",
                    "duration": null,
                    "severity": "加重",
                    "location": "chest",
                    "trigger": "活动后",
                    "frequency": null,
                    "relief": null
                },
                {
                    "name": "sweating",
                    "duration": null,
                    "severity": "加重",
                    "location": null,
                    "trigger": "活动后",
                    "frequency": null,
                    "relief": null
                }
            ],
            "patient_profile": {
                "age": null,
                "sex": null,
                "risk_factors": []
            }
        },
        "knowledge_context": {
            "symptom_group": "chest_pain",
            "source_assets_count": 2
        },
        "safety_gate": {
            "triggered": true,
            "risk_level": "high",
            "matched_rules": [
                "rf_001"
            ],
            "reason": "matched red flag rules: rf_001",
            "required_action": "urgent_evaluation",
            "patient_output_constraint": "no_low_risk_reassurance",
            "fail_safe_required": false
        },
        "differential_board": null,
        "evidence_graph": null,
        "next_action": {
            "type": "recommend_visit",
            "content": "urgent_evaluation",
            "purpose": "safety gate triggered",
            "priority": "high",
            "reason": "danger signal matched; urgent evaluation required"
        },
        "patient_output": {
            "allowed": true,
            "content": "当前描述中存在需要重视的风险信号，系统不能给出低风险判断或确定诊断。请尽快前往线下医疗机构评估，必要时寻求紧急帮助。",
            "output_level": "O5_visit_or_urgent_care_recommendation",
            "constraints_applied": [
                "no_definitive_diagnosis",
                "no_prescription",
                "no_low_risk_reassurance",
                "no_low_risk_reassurance"
            ]
        },
        "clinician_report": null
    },
    "error": null,
    "trace_id": "trace_8a79c7a2740e"
}
```

### 用例 3 — 医生端 · 同场景可见 DDx

POST `http://localhost:8080/api/v1/runtime/start`

```
{

  "session_id": "manual_003",

  "mode": "clinician_copilot",

  "input": { "text": "胸口闷，活动后更明显" }

}
```

结果：

```
{
    "success": true,
    "data": {
        "runtime_id": "rt_065b41da3ca7",
        "runtime_status": "waiting_for_user",
        "work_mode": "clinical_mode",
        "risk_level": "none",
        "entry_assessment": {
            "work_mode": "clinical_mode",
            "symptom_group": "chest_pain",
            "reason": "clinical symptoms detected",
            "confidence": 0.8
        },
        "case_frame": {
            "chief_complaint": "胸口闷",
            "missing_slots": [
                "age",
                "associated_symptoms",
                "sex",
                "symptom_duration"
            ],
            "symptoms": [
                {
                    "name": "chest_discomfort",
                    "duration": null,
                    "severity": "明显",
                    "location": "chest",
                    "trigger": "活动后",
                    "frequency": null,
                    "relief": null
                }
            ],
            "patient_profile": {
                "age": null,
                "sex": null,
                "risk_factors": []
            }
        },
        "knowledge_context": {
            "symptom_group": "chest_pain",
            "common_diagnoses": [
                {
                    "name": "musculoskeletal_chest_pain",
                    "risk_level": "low"
                },
                {
                    "name": "gastroesophageal_reflux",
                    "risk_level": "low"
                }
            ],
            "must_not_miss": [
                {
                    "name": "acute_coronary_syndrome",
                    "risk_level": "high"
                },
                {
                    "name": "aortic_dissection",
                    "risk_level": "high"
                }
            ],
            "red_flags": [
                {
                    "rule_id": "rf_001",
                    "symptom_group": "chest_pain",
                    "features": [
                        "activity_related",
                        "sweating"
                    ],
                    "risk_level": "high",
                    "action": "urgent_evaluation",
                    "patient_constraint": "no_low_risk_reassurance"
                },
                {
                    "rule_id": "rf_002",
                    "symptom_group": "chest_pain",
                    "features": [
                        "severe_pain"
                    ],
                    "risk_level": "high",
                    "action": "urgent_evaluation",
                    "patient_constraint": "no_low_risk_reassurance"
                }
            ],
            "required_questions": [
                "是否活动后加重？",
                "是否伴随出汗或呼吸困难？"
            ],
            "recommended_tests": [
                "心电图",
                "心肌酶"
            ],
            "source_assets": [
                "assets/symptom-groups/chest-pain.yml",
                "assets/red-flag-rules.yml"
            ]
        },
        "safety_gate": {
            "triggered": false,
            "risk_level": "none",
            "matched_rules": [],
            "reason": null,
            "required_action": null,
            "patient_output_constraint": null,
            "fail_safe_required": false
        },
        "differential_board": {
            "candidates": [
                {
                    "name": "acute_coronary_syndrome",
                    "status": "must_not_miss",
                    "risk_level": "high",
                    "reason": "must not miss diagnosis",
                    "patient_visible": false
                },
                {
                    "name": "aortic_dissection",
                    "status": "must_not_miss",
                    "risk_level": "high",
                    "reason": "must not miss diagnosis",
                    "patient_visible": false
                },
                {
                    "name": "musculoskeletal_chest_pain",
                    "status": "possible",
                    "risk_level": "low",
                    "reason": "common diagnosis candidate",
                    "patient_visible": false
                },
                {
                    "name": "gastroesophageal_reflux",
                    "status": "possible",
                    "risk_level": "low",
                    "reason": "common diagnosis candidate",
                    "patient_visible": false
                }
            ],
            "updated_reason": "initial differential board from static rules"
        },
        "evidence_graph": {
            "items": [
                {
                    "diagnosis": "acute_coronary_syndrome",
                    "supporting_evidence": [
                        "chest_discomfort triggered by 活动后 severity=明显",
                        "chief complaint: 胸口闷"
                    ],
                    "opposing_evidence": [],
                    "missing_evidence": [
                        "missing slot: age",
                        "missing slot: associated_symptoms",
                        "missing slot: sex",
                        "missing slot: symptom_duration",
                        "high risk diagnosis not ruled out: acute_coronary_syndrome",
                        "unanswered question: 是否伴随出汗或呼吸困难？"
                    ],
                    "conflicting_evidence": [],
                    "status": "must_not_miss",
                    "next_questions": [
                        "是否伴随出汗或呼吸困难？"
                    ],
                    "recommended_tests": []
                },
                {
                    "diagnosis": "aortic_dissection",
                    "supporting_evidence": [
                        "chest_discomfort triggered by 活动后 severity=明显",
                        "chief complaint: 胸口闷"
                    ],
                    "opposing_evidence": [],
                    "missing_evidence": [
                        "missing slot: age",
                        "missing slot: associated_symptoms",
                        "missing slot: sex",
                        "missing slot: symptom_duration",
                        "high risk diagnosis not ruled out: aortic_dissection",
                        "unanswered question: 是否伴随出汗或呼吸困难？"
                    ],
                    "conflicting_evidence": [],
                    "status": "must_not_miss",
                    "next_questions": [
                        "是否伴随出汗或呼吸困难？"
                    ],
                    "recommended_tests": []
                },
                {
                    "diagnosis": "musculoskeletal_chest_pain",
                    "supporting_evidence": [
                        "chest_discomfort triggered by 活动后 severity=明显",
                        "chief complaint: 胸口闷",
                        "retained as possible"
                    ],
                    "opposing_evidence": [],
                    "missing_evidence": [
                        "missing slot: age",
                        "missing slot: associated_symptoms",
                        "missing slot: sex",
                        "missing slot: symptom_duration",
                        "unanswered question: 是否伴随出汗或呼吸困难？"
                    ],
                    "conflicting_evidence": [],
                    "status": "possible",
                    "next_questions": [
                        "是否伴随出汗或呼吸困难？"
                    ],
                    "recommended_tests": []
                },
                {
                    "diagnosis": "gastroesophageal_reflux",
                    "supporting_evidence": [
                        "chest_discomfort triggered by 活动后 severity=明显",
                        "chief complaint: 胸口闷",
                        "retained as possible"
                    ],
                    "opposing_evidence": [],
                    "missing_evidence": [
                        "missing slot: age",
                        "missing slot: associated_symptoms",
                        "missing slot: sex",
                        "missing slot: symptom_duration",
                        "unanswered question: 是否伴随出汗或呼吸困难？"
                    ],
                    "conflicting_evidence": [],
                    "status": "possible",
                    "next_questions": [
                        "是否伴随出汗或呼吸困难？"
                    ],
                    "recommended_tests": []
                }
            ]
        },
        "next_action": {
            "type": "ask_question",
            "content": "是否伴随出汗或呼吸困难？",
            "purpose": "collect missing evidence",
            "target_diagnosis": "acute_coronary_syndrome",
            "priority": "medium",
            "reason": "missing evidence should be collected before narrowing diagnosis"
        },
        "patient_output": null,
        "clinician_report": {
            "allowed": true,
            "case_summary": "主诉：胸口闷。 已识别症状数：1。 缺失信息：age, associated_symptoms, sex, symptom_duration。",
            "safety_summary": "未触发危险信号规则。",
            "ddx_summary": [
                {
                    "name": "acute_coronary_syndrome",
                    "status": "must_not_miss",
                    "risk_level": "high",
                    "reason": "must not miss diagnosis",
                    "patient_visible": false
                },
                {
                    "name": "aortic_dissection",
                    "status": "must_not_miss",
                    "risk_level": "high",
                    "reason": "must not miss diagnosis",
                    "patient_visible": false
                },
                {
                    "name": "musculoskeletal_chest_pain",
                    "status": "possible",
                    "risk_level": "low",
                    "reason": "common diagnosis candidate",
                    "patient_visible": false
                },
                {
                    "name": "gastroesophageal_reflux",
                    "status": "possible",
                    "risk_level": "low",
                    "reason": "common diagnosis candidate",
                    "patient_visible": false
                }
            ],
            "evidence_summary": {
                "items": [
                    {
                        "diagnosis": "acute_coronary_syndrome",
                        "supporting_evidence": [
                            "chest_discomfort triggered by 活动后 severity=明显",
                            "chief complaint: 胸口闷"
                        ],
                        "opposing_evidence": [],
                        "missing_evidence": [
                            "missing slot: age",
                            "missing slot: associated_symptoms",
                            "missing slot: sex",
                            "missing slot: symptom_duration",
                            "high risk diagnosis not ruled out: acute_coronary_syndrome",
                            "unanswered question: 是否伴随出汗或呼吸困难？"
                        ],
                        "conflicting_evidence": [],
                        "status": "must_not_miss",
                        "next_questions": [
                            "是否伴随出汗或呼吸困难？"
                        ],
                        "recommended_tests": []
                    },
                    {
                        "diagnosis": "aortic_dissection",
                        "supporting_evidence": [
                            "chest_discomfort triggered by 活动后 severity=明显",
                            "chief complaint: 胸口闷"
                        ],
                        "opposing_evidence": [],
                        "missing_evidence": [
                            "missing slot: age",
                            "missing slot: associated_symptoms",
                            "missing slot: sex",
                            "missing slot: symptom_duration",
                            "high risk diagnosis not ruled out: aortic_dissection",
                            "unanswered question: 是否伴随出汗或呼吸困难？"
                        ],
                        "conflicting_evidence": [],
                        "status": "must_not_miss",
                        "next_questions": [
                            "是否伴随出汗或呼吸困难？"
                        ],
                        "recommended_tests": []
                    },
                    {
                        "diagnosis": "musculoskeletal_chest_pain",
                        "supporting_evidence": [
                            "chest_discomfort triggered by 活动后 severity=明显",
                            "chief complaint: 胸口闷",
                            "retained as possible"
                        ],
                        "opposing_evidence": [],
                        "missing_evidence": [
                            "missing slot: age",
                            "missing slot: associated_symptoms",
                            "missing slot: sex",
                            "missing slot: symptom_duration",
                            "unanswered question: 是否伴随出汗或呼吸困难？"
                        ],
                        "conflicting_evidence": [],
                        "status": "possible",
                        "next_questions": [
                            "是否伴随出汗或呼吸困难？"
                        ],
                        "recommended_tests": []
                    },
                    {
                        "diagnosis": "gastroesophageal_reflux",
                        "supporting_evidence": [
                            "chest_discomfort triggered by 活动后 severity=明显",
                            "chief complaint: 胸口闷",
                            "retained as possible"
                        ],
                        "opposing_evidence": [],
                        "missing_evidence": [
                            "missing slot: age",
                            "missing slot: associated_symptoms",
                            "missing slot: sex",
                            "missing slot: symptom_duration",
                            "unanswered question: 是否伴随出汗或呼吸困难？"
                        ],
                        "conflicting_evidence": [],
                        "status": "possible",
                        "next_questions": [
                            "是否伴随出汗或呼吸困难？"
                        ],
                        "recommended_tests": []
                    }
                ]
            },
            "recommended_questions": [
                "是否伴随出汗或呼吸困难？",
                "是否活动后加重？"
            ],
            "recommended_tests": [
                "心电图",
                "心肌酶"
            ]
        }
    },
    "error": null,
    "trace_id": "trace_6231fc0ec475"
}
```

### 用例 4 — continue + trace 真实性

用 用例 1 的 `runtime_id`。

POST `http://localhost:8080/api/v1/runtime/continue`

```
{

  "runtime_id": "rt_这里换成用例1的id",

  "input": { "text": "有点出汗，走路快的时候更明显，休息会缓解" }

}
```

结果：

```
{
    "success": true,
    "data": {
        "runtime_id": "rt_26dfad820209",
        "runtime_status": "safety_gate_triggered",
        "work_mode": "clinical_mode",
        "risk_level": "high",
        "entry_assessment": {
            "work_mode": "clinical_mode",
            "symptom_group": "chest_pain",
            "reason": "clinical symptoms detected",
            "confidence": 0.8
        },
        "case_frame": {
            "chief_complaint": "我最近胸口闷",
            "missing_slots": [
                "symptom_duration"
            ],
            "symptoms": [
                {
                    "name": "chest_discomfort",
                    "duration": null,
                    "severity": "明显",
                    "location": "chest",
                    "trigger": "活动后",
                    "frequency": null,
                    "relief": null
                },
                {
                    "name": "sweating",
                    "duration": null,
                    "severity": "明显",
                    "location": null,
                    "trigger": "走路",
                    "frequency": null,
                    "relief": "休息会缓解"
                }
            ],
            "patient_profile": {
                "age": 58,
                "sex": "male",
                "risk_factors": []
            }
        },
        "knowledge_context": {
            "symptom_group": "chest_pain",
            "source_assets_count": 2
        },
        "safety_gate": {
            "triggered": true,
            "risk_level": "high",
            "matched_rules": [
                "rf_001"
            ],
            "reason": "matched red flag rules: rf_001",
            "required_action": "urgent_evaluation",
            "patient_output_constraint": "no_low_risk_reassurance",
            "fail_safe_required": false
        },
        "differential_board": null,
        "evidence_graph": null,
        "next_action": {
            "type": "recommend_visit",
            "content": "urgent_evaluation",
            "purpose": "safety gate triggered",
            "priority": "high",
            "reason": "danger signal matched; urgent evaluation required"
        },
        "patient_output": {
            "allowed": true,
            "content": "当前描述中存在需要重视的风险信号，系统不能给出低风险判断或确定诊断。请尽快前往线下医疗机构评估，必要时寻求紧急帮助。",
            "output_level": "O5_visit_or_urgent_care_recommendation",
            "constraints_applied": [
                "no_definitive_diagnosis",
                "no_prescription",
                "no_low_risk_reassurance",
                "no_low_risk_reassurance"
            ]
        },
        "clinician_report": null
    },
    "error": null,
    "trace_id": "trace_1b4655c1c916"
}
```

GET `http://localhost:8080/api/v1/runtime/{runtime_id}/trace`

```
{
    "success": true,
    "data": {
        "runtime_id": "rt_26dfad820209",
        "traces": [
            {
                "trace_id": "trace_17063271c57c",
                "runtime_id": "rt_26dfad820209",
                "step": 1,
                "input": "我最近胸口闷，活动后更明显",
                "modules_executed": [
                    "EntryAssessment",
                    "CaseFrameBuilder",
                    "KnowledgeContext",
                    "ExperienceContext",
                    "SafetyGate",
                    "DifferentialDiagnosisBoard",
                    "EvidenceGraph",
                    "QuestionTestPolicy",
                    "DecisionBoundary",
                    "PatientOutput"
                ],
                "knowledge_used": [
                    "assets/symptom-groups/chest-pain.yml",
                    "assets/red-flag-rules.yml"
                ],
                "experience_used": [],
                "output_summary": {
                    "work_mode": "clinical_mode",
                    "symptom_group": "chest_pain",
                    "chief_complaint": "我最近胸口闷",
                    "missing_slots": [
                        "associated_symptoms",
                        "symptom_duration"
                    ],
                    "must_not_miss_count": 2,
                    "safety_gate_triggered": false,
                    "next_action_type": "ask_question",
                    "next_action_content": "是否伴随出汗或呼吸困难？",
                    "allowed_output_level": "O1_continue_questioning",
                    "patient_output_level": "O1_continue_questioning",
                    "basic_info_applied": true,
                    "runtime_status": "waiting_for_user",
                    "trace_step_count": 10
                },
                "created_at": "2026-06-26T01:37:50.130216400Z"
            },
            {
                "trace_id": "trace_1b4655c1c916",
                "runtime_id": "rt_26dfad820209",
                "step": 2,
                "input": "有点出汗，走路快的时候更明显，休息会缓解",
                "modules_executed": [
                    "CaseFrameBuilder",
                    "KnowledgeContext",
                    "ExperienceContext",
                    "SafetyGate",
                    "DifferentialDiagnosisBoard",
                    "EvidenceGraph",
                    "QuestionTestPolicy",
                    "DecisionBoundary",
                    "PatientOutput"
                ],
                "knowledge_used": [
                    "assets/symptom-groups/chest-pain.yml",
                    "assets/red-flag-rules.yml"
                ],
                "experience_used": [],
                "output_summary": {
                    "work_mode": "clinical_mode",
                    "symptom_group": "chest_pain",
                    "chief_complaint": "我最近胸口闷",
                    "missing_slots": [
                        "symptom_duration"
                    ],
                    "must_not_miss_count": 2,
                    "safety_gate_triggered": true,
                    "next_action_type": "recommend_visit",
                    "next_action_content": "urgent_evaluation",
                    "allowed_output_level": "O5_visit_or_urgent_care_recommendation",
                    "patient_output_level": "O5_visit_or_urgent_care_recommendation",
                    "runtime_status": "safety_gate_triggered",
                    "trace_step_count": 9
                },
                "created_at": "2026-06-26T01:49:10.538995900Z"
            }
        ]
    },
    "error": null,
    "trace_id": null
}
```

### 用例 5 — 养生模式不进临床管线

POST `http://localhost:8080/api/v1/runtime/start`

```
{

  "session_id": "manual_005",

  "mode": "patient_facing",

  "input": { "text": "我想了解养生建议，怎么锻炼比较好" }

}
```

结果：

```
{
    "success": true,
    "data": {
        "runtime_id": "rt_f18e5795e6be",
        "runtime_status": "wellness_mode",
        "work_mode": "wellness_mode",
        "risk_level": null,
        "entry_assessment": {
            "work_mode": "wellness_mode",
            "symptom_group": null,
            "reason": "wellness consultation",
            "confidence": 0.8
        },
        "case_frame": {
            "chief_complaint": "我想了解养生建议",
            "missing_slots": [
                "age",
                "associated_symptoms",
                "sex",
                "symptoms"
            ],
            "symptoms": [],
            "patient_profile": {
                "age": null,
                "sex": null,
                "risk_factors": []
            }
        },
        "knowledge_context": null,
        "safety_gate": null,
        "differential_board": null,
        "evidence_graph": null,
        "next_action": null,
        "patient_output": null,
        "clinician_report": null
    },
    "error": null,
    "trace_id": "trace_43f4f9c50d15"
}
```

### 用例 6 — status / result / trace 可读

任选用例 1 或 3 的 `runtime_id`。

GET `http://localhost:8080/api/v1/runtime/{runtime_id}/status`

结果：

```
{
    "success": true,
    "data": {
        "runtime_id": "rt_26dfad820209",
        "runtime_status": "safety_gate_triggered",
        "work_mode": "clinical_mode",
        "risk_level": "high",
        "updated_at": "2026-06-26T01:49:10.539996600Z"
    },
    "error": null,
    "trace_id": null
}
```

GET `http://localhost:8080/api/v1/runtime/{runtime_id}/result`

结果：

```
{
    "success": true,
    "data": {
        "runtime_id": "rt_26dfad820209",
        "runtime_status": "safety_gate_triggered",
        "patient_output": {
            "allowed": true,
            "content": "当前描述中存在需要重视的风险信号，系统不能给出低风险判断或确定诊断。请尽快前往线下医疗机构评估，必要时寻求紧急帮助。",
            "output_level": "O5_visit_or_urgent_care_recommendation",
            "constraints_applied": [
                "no_definitive_diagnosis",
                "no_prescription",
                "no_low_risk_reassurance",
                "no_low_risk_reassurance"
            ]
        },
        "clinician_report": {
            "allowed": false,
            "case_summary": null,
            "safety_summary": null,
            "ddx_summary": [],
            "evidence_summary": null,
            "recommended_questions": [],
            "recommended_tests": []
        },
        "decision_boundary": {
            "allowed_output_level": "O5_visit_or_urgent_care_recommendation",
            "patient_diagnosis_label_allowed": false,
            "clinician_ddx_allowed": false,
            "reason": "high risk active; patient output tightened",
            "constraints": [
                "no_definitive_diagnosis",
                "no_prescription",
                "no_low_risk_reassurance",
                "no_low_risk_reassurance"
            ]
        }
    },
    "error": null,
    "trace_id": null
}
```

GET `http://localhost:8080/api/v1/runtime/rt_not_exist/status`

结果：

```
{
    "success": false,
    "data": null,
    "error": {
        "code": "RUNTIME_NOT_FOUND",
        "message": "Runtime 不存在"
    },
    "trace_id": null
}
```

GET `http://localhost:8080/api/v1/runtime/{runtime_id}/trace`

结果：

```
{
    "success": true,
    "data": {
        "runtime_id": "rt_26dfad820209",
        "traces": [
            {
                "trace_id": "trace_17063271c57c",
                "runtime_id": "rt_26dfad820209",
                "step": 1,
                "input": "我最近胸口闷，活动后更明显",
                "modules_executed": [
                    "EntryAssessment",
                    "CaseFrameBuilder",
                    "KnowledgeContext",
                    "ExperienceContext",
                    "SafetyGate",
                    "DifferentialDiagnosisBoard",
                    "EvidenceGraph",
                    "QuestionTestPolicy",
                    "DecisionBoundary",
                    "PatientOutput"
                ],
                "knowledge_used": [
                    "assets/symptom-groups/chest-pain.yml",
                    "assets/red-flag-rules.yml"
                ],
                "experience_used": [],
                "output_summary": {
                    "work_mode": "clinical_mode",
                    "symptom_group": "chest_pain",
                    "chief_complaint": "我最近胸口闷",
                    "missing_slots": [
                        "associated_symptoms",
                        "symptom_duration"
                    ],
                    "must_not_miss_count": 2,
                    "safety_gate_triggered": false,
                    "next_action_type": "ask_question",
                    "next_action_content": "是否伴随出汗或呼吸困难？",
                    "allowed_output_level": "O1_continue_questioning",
                    "patient_output_level": "O1_continue_questioning",
                    "basic_info_applied": true,
                    "runtime_status": "waiting_for_user",
                    "trace_step_count": 10
                },
                "created_at": "2026-06-26T01:37:50.130216400Z"
            },
            {
                "trace_id": "trace_1b4655c1c916",
                "runtime_id": "rt_26dfad820209",
                "step": 2,
                "input": "有点出汗，走路快的时候更明显，休息会缓解",
                "modules_executed": [
                    "CaseFrameBuilder",
                    "KnowledgeContext",
                    "ExperienceContext",
                    "SafetyGate",
                    "DifferentialDiagnosisBoard",
                    "EvidenceGraph",
                    "QuestionTestPolicy",
                    "DecisionBoundary",
                    "PatientOutput"
                ],
                "knowledge_used": [
                    "assets/symptom-groups/chest-pain.yml",
                    "assets/red-flag-rules.yml"
                ],
                "experience_used": [],
                "output_summary": {
                    "work_mode": "clinical_mode",
                    "symptom_group": "chest_pain",
                    "chief_complaint": "我最近胸口闷",
                    "missing_slots": [
                        "symptom_duration"
                    ],
                    "must_not_miss_count": 2,
                    "safety_gate_triggered": true,
                    "next_action_type": "recommend_visit",
                    "next_action_content": "urgent_evaluation",
                    "allowed_output_level": "O5_visit_or_urgent_care_recommendation",
                    "patient_output_level": "O5_visit_or_urgent_care_recommendation",
                    "runtime_status": "safety_gate_triggered",
                    "trace_step_count": 9
                },
                "created_at": "2026-06-26T01:49:10.538995900Z"
            }
        ]
    },
    "error": null,
    "trace_id": null
}
```

