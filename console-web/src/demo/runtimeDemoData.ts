import type { ClinicianCaseSummary, ClinicianCaseView } from '../shared/types/clinicianViews';
import type { GovernanceRuntimeProjection } from '../shared/types/governanceViews';
import type { PatientRuntimeView, PatientSessionSummary } from '../shared/types/patientViews';

export const DEMO_RUNTIME_ID = 'runtime-demo-001';

export const patientSessionSummaries: PatientSessionSummary[] = [
  {
    session_id: DEMO_RUNTIME_ID,
    runtime_id: DEMO_RUNTIME_ID,
    status: 'in_guided_inquiry',
    chief_complaint_summary: '胸口不适伴轻度气短，已进入安全问询流程。',
    updated_at: '2026-07-08 10:20',
    safe_next_step: '继续回答安全问询，并在症状加重时立即联系急救服务。',
  },
];

export const patientRuntimeView: PatientRuntimeView = {
  session_id: DEMO_RUNTIME_ID,
  runtime_id: DEMO_RUNTIME_ID,
  status: 'in_guided_inquiry',
  safe_summary:
    '系统已记录胸口不适、轻度气短和近期活动信息。当前页面只提供整理后的安全摘要，不能替代医生判断。',
  collected_facts: [
    {
      label: '主要不适',
      value: '胸口不适，活动后更明显。',
      confidence_note: '来自患者自述，后续需要医生核实。',
    },
    {
      label: '伴随情况',
      value: '轻度气短，无已记录的晕厥描述。',
      confidence_note: '仍需要补充持续时间和诱发因素。',
    },
    {
      label: '当前状态',
      value: '问询进行中，尚未形成医疗结论。',
      confidence_note: '患者端仅展示安全投影。',
    },
  ],
  next_questions: [
    {
      id: 'q1',
      prompt: '不适开始到现在大约持续了多久？',
      reason_for_asking: '帮助医生了解时间变化。',
    },
    {
      id: 'q2',
      prompt: '休息后是否明显缓解？',
      reason_for_asking: '帮助区分活动相关和持续性不适。',
    },
  ],
  safety_notices: [
    {
      level: 'urgent',
      message: '如出现持续胸痛、明显呼吸困难、出冷汗、晕厥或症状快速加重，请立即拨打当地急救电话。',
    },
    {
      level: 'info',
      message: 'AI 只能帮助整理信息，不能替代医生诊疗。',
    },
  ],
  care_navigation: [
    {
      label: '准备就医信息',
      description: '记录不适出现时间、诱因、缓解方式和既往病史，便于医生沟通。',
    },
    {
      label: '保持安全观察',
      description: '避免独自等待；若症状升级，优先寻求急救。',
    },
  ],
  allowed_actions: ['continue_guided_inquiry', 'view_safe_summary'],
  disclaimer: '本页面不是诊断、处方或治疗建议。',
};

export const clinicianCaseSummaries: ClinicianCaseSummary[] = [
  {
    case_id: DEMO_RUNTIME_ID,
    runtime_id: DEMO_RUNTIME_ID,
    status: 'needs_clinician_review',
    risk_level: 'watch',
    chief_complaint_summary: '胸口不适伴轻度气短，问询资料待医生复核。',
    updated_at: '2026-07-08 10:20',
    assigned_clinician: 'demo-clinician',
  },
];

export const clinicianCaseView: ClinicianCaseView = {
  case_id: DEMO_RUNTIME_ID,
  runtime_id: DEMO_RUNTIME_ID,
  patient_summary: {
    age_band: 'adult',
    sex: 'not specified',
    chief_complaint_summary: '胸口不适伴轻度气短。',
    context_notes: ['演示数据，不包含真实身份信息。', '问询尚未完成，需医生复核。'],
  },
  case_frame: {
    current_problem: '活动相关胸口不适，需要进一步询问持续时间、诱因、缓解因素和伴随症状。',
    known_context: ['患者自述活动后更明显。', '已提示紧急症状升级时立即寻求急救。'],
    missing_information: ['持续时间', '既往心血管风险因素', '疼痛性质和放射部位', '生命体征'],
  },
  inquiry_timeline: [
    {
      speaker: 'patient',
      summary: '描述胸口不适，活动后更明显。',
      timestamp: '10:11',
    },
    {
      speaker: 'assistant',
      summary: '询问是否伴随呼吸困难、出汗、晕厥等危险信号。',
      timestamp: '10:13',
    },
    {
      speaker: 'patient',
      summary: '报告轻度气短，未描述晕厥。',
      timestamp: '10:18',
    },
  ],
  ddx_board: [
    {
      name: '心源性胸痛可能',
      likelihood: 'medium',
      supporting_summary: '活动相关胸口不适需要谨慎排查。',
      uncertainty_note: '缺少生命体征、疼痛性质、风险因素和检查结果。',
    },
    {
      name: '肌肉骨骼相关不适',
      likelihood: 'low',
      supporting_summary: '仍需询问体位、触压和运动相关性。',
      uncertainty_note: '不能作为最终判断。',
    },
  ],
  evidence_panel: [
    {
      title: '胸痛安全分流原则',
      source: 'Clinical safety summary',
      summary: '持续胸痛、明显呼吸困难、晕厥等应优先急救评估。',
      relevance: '用于医生复核安全提示是否充分。',
    },
  ],
  risk_panel: [
    {
      label: 'Red-flag watch',
      level: 'watch',
      note: '当前资料不足，需补齐危险信号和生命体征。',
    },
  ],
  ai_suggestions: [
    {
      label: '补问持续时间',
      description: '建议医生确认开始时间、持续时长和休息后变化。',
    },
    {
      label: '补问伴随表现',
      description: '建议确认出汗、恶心、晕厥、放射痛等情况。',
    },
  ],
  report_draft: {
    impression: '资料提示胸口不适需医生进一步评估；当前内容仅为辅助草稿。',
    suggested_questions: ['不适是否向肩背或手臂放射？', '是否有既往心血管疾病或相关风险因素？'],
    clinician_note: '医生保留最终判断权。P0 草稿不提交真实系统。',
  },
};

export const governanceRuntimeProjection: GovernanceRuntimeProjection = {
  runtime_id: DEMO_RUNTIME_ID,
  safe_console_route: '/governance/overview',
  timeline_route: `/governance/runtimes/${DEMO_RUNTIME_ID}`,
  audit_route: '/governance/audits',
  candidate_route: '/governance/candidates',
  note: '治理端继续使用 Phase10 Safe DTO，只读查看运行链路、候选项和审计摘要。',
};
