package com.clinmind.runtime.view.common;

import com.clinmind.runtime.view.clinician.dto.CaseFrameViewDto;
import com.clinmind.runtime.view.clinician.dto.ClinicianCaseSummaryDto;
import com.clinmind.runtime.view.clinician.dto.ClinicianCaseViewDto;
import com.clinmind.runtime.view.clinician.dto.ClinicianReportDraftViewDto;
import com.clinmind.runtime.view.clinician.dto.ClinicianSuggestionDto;
import com.clinmind.runtime.view.clinician.dto.DdxCandidateViewDto;
import com.clinmind.runtime.view.clinician.dto.EvidenceItemViewDto;
import com.clinmind.runtime.view.clinician.dto.InquiryTurnViewDto;
import com.clinmind.runtime.view.clinician.dto.PatientSummaryDto;
import com.clinmind.runtime.view.clinician.dto.RiskSignalViewDto;
import com.clinmind.runtime.view.clinician.dto.RuntimeBoundarySummaryDto;
import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import com.clinmind.runtime.view.patient.dto.CareNavigationDto;
import com.clinmind.runtime.view.patient.dto.PatientFactSummaryDto;
import com.clinmind.runtime.view.patient.dto.PatientQuestionDto;
import com.clinmind.runtime.view.patient.dto.PatientRuntimeViewDto;
import com.clinmind.runtime.view.patient.dto.PatientSafeSummaryDto;
import com.clinmind.runtime.view.patient.dto.PatientSessionSummaryDto;
import com.clinmind.runtime.view.patient.dto.SafetyNoticeDto;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DemoRuntimeSeedProvider {

    public static final String DEMO_RUNTIME_ID = "runtime-demo-001";

    public List<PatientSessionSummaryDto> patientSessions() {
        return List.of(patientSessionSummary());
    }

    public Optional<PatientRuntimeViewDto> patientRuntimeView(String sessionId) {
        if (!DEMO_RUNTIME_ID.equals(sessionId)) {
            return Optional.empty();
        }
        return Optional.of(new PatientRuntimeViewDto(
                DEMO_RUNTIME_ID,
                DEMO_RUNTIME_ID,
                "in_guided_inquiry",
                "系统已记录胸口不适、轻度气短和近期活动信息。当前页面只提供整理后的安全摘要，不能替代医生判断。",
                List.of(
                        new PatientFactSummaryDto("主要不适", "胸口不适，活动后更明显。", "来自患者自述，后续需要医生核实。"),
                        new PatientFactSummaryDto("伴随情况", "轻度气短，无已记录的晕厥描述。", "仍需要补充持续时间和诱发因素。"),
                        new PatientFactSummaryDto("当前状态", "问询进行中，尚未形成医疗结论。", "患者端仅展示安全投影。")),
                List.of(
                        new PatientQuestionDto("q1", "不适开始到现在大约持续了多久？", "帮助医生了解时间变化。"),
                        new PatientQuestionDto("q2", "休息后是否明显缓解？", "帮助区分活动相关和持续性不适。")),
                patientSafetyNotices(),
                patientCareNavigation(),
                List.of("continue_guided_inquiry", "view_safe_summary"),
                "本页面不是诊断、处方或治疗建议。",
                ProjectionStatus.COMPLETE,
                List.of()));
    }

    public Optional<PatientSafeSummaryDto> patientSafeSummary(String sessionId) {
        return patientRuntimeView(sessionId).map(view -> new PatientSafeSummaryDto(
                view.sessionId(),
                view.runtimeId(),
                view.safeSummary(),
                view.safetyNotices(),
                view.careNavigation(),
                view.disclaimer(),
                view.projectionStatus()));
    }

    public List<ClinicianCaseSummaryDto> clinicianCases() {
        return List.of(new ClinicianCaseSummaryDto(
                DEMO_RUNTIME_ID,
                DEMO_RUNTIME_ID,
                "needs_clinician_review",
                "WATCH",
                "胸口不适伴轻度气短，问询资料待医生复核。",
                "2026-07-08T10:20:00Z",
                "demo-clinician",
                ProjectionStatus.COMPLETE));
    }

    public Optional<ClinicianCaseViewDto> clinicianCaseView(String caseId) {
        if (!DEMO_RUNTIME_ID.equals(caseId)) {
            return Optional.empty();
        }
        ClinicianReportDraftViewDto reportDraft = clinicianReportDraft(caseId).orElseThrow();
        return Optional.of(new ClinicianCaseViewDto(
                DEMO_RUNTIME_ID,
                DEMO_RUNTIME_ID,
                "needs_clinician_review",
                new PatientSummaryDto(
                        "adult",
                        "not specified",
                        "胸口不适伴轻度气短。",
                        List.of("演示数据，不包含真实身份信息。", "问询尚未完成，需医生复核。")),
                new CaseFrameViewDto(
                        "活动相关胸口不适，需要进一步询问持续时间、诱因、缓解因素和伴随症状。",
                        List.of("患者自述活动后更明显。", "已提示紧急症状升级时立即寻求急救。"),
                        List.of("持续时间", "既往心血管风险因素", "疼痛性质和放射部位", "生命体征")),
                List.of(
                        new InquiryTurnViewDto("patient", "描述胸口不适，活动后更明显。", "10:11"),
                        new InquiryTurnViewDto("assistant", "询问是否伴随呼吸困难、出汗、晕厥等危险信号。", "10:13"),
                        new InquiryTurnViewDto("patient", "报告轻度气短，未描述晕厥。", "10:18")),
                List.of(
                        new DdxCandidateViewDto("心源性胸痛可能", "medium", "活动相关胸口不适需要谨慎排查。", "缺少生命体征、疼痛性质、风险因素和检查结果。"),
                        new DdxCandidateViewDto("肌肉骨骼相关不适", "low", "仍需询问体位、触压和运动相关性。", "不能作为最终判断。")),
                List.of(new EvidenceItemViewDto(
                        "胸痛安全分流原则",
                        "Clinical safety summary",
                        "持续胸痛、明显呼吸困难、晕厥等应优先急救评估。",
                        "用于医生复核安全提示是否充分。")),
                List.of(new RiskSignalViewDto("Red-flag watch", "watch", "当前资料不足，需补齐危险信号和生命体征。")),
                List.of(
                        new ClinicianSuggestionDto("补问持续时间", "建议医生确认开始时间、持续时长和休息后变化。"),
                        new ClinicianSuggestionDto("补问伴随表现", "建议确认出汗、恶心、晕厥、放射痛等情况。")),
                reportDraft,
                new RuntimeBoundarySummaryDto(
                        "SAFETY_NOTICE_PRESENT",
                        "ROLE_SPECIFIC_PROJECTION",
                        List.of("医生端内容为辅助建议，最终判断权属于医生。")),
                ProjectionStatus.COMPLETE,
                List.of()));
    }

    public Optional<ClinicianReportDraftViewDto> clinicianReportDraft(String caseId) {
        if (!DEMO_RUNTIME_ID.equals(caseId)) {
            return Optional.empty();
        }
        return Optional.of(new ClinicianReportDraftViewDto(
                DEMO_RUNTIME_ID,
                DEMO_RUNTIME_ID,
                "资料提示胸口不适需医生进一步评估；当前内容仅为辅助草稿。",
                List.of("不适是否向肩背或手臂放射？", "是否有既往心血管疾病或相关风险因素？"),
                "医生保留最终判断权。P1 草稿只读，真实保存后置。",
                true,
                false,
                ProjectionStatus.COMPLETE));
    }

    private PatientSessionSummaryDto patientSessionSummary() {
        return new PatientSessionSummaryDto(
                DEMO_RUNTIME_ID,
                DEMO_RUNTIME_ID,
                "in_guided_inquiry",
                "胸口不适伴轻度气短，已进入安全问询流程。",
                "MEDIUM",
                "继续回答安全问询，并在症状加重时立即联系急救服务。",
                "2026-07-08T10:20:00Z",
                ProjectionStatus.COMPLETE);
    }

    private List<SafetyNoticeDto> patientSafetyNotices() {
        return List.of(
                new SafetyNoticeDto("urgent", "如出现持续胸痛、明显呼吸困难、出冷汗、晕厥或症状快速加重，请立即拨打当地急救电话。"),
                new SafetyNoticeDto("info", "AI 只能帮助整理信息，不能替代医生诊疗。"));
    }

    private List<CareNavigationDto> patientCareNavigation() {
        return List.of(
                new CareNavigationDto("准备就医信息", "记录不适出现时间、诱因、缓解方式和既往病史，便于医生沟通。"),
                new CareNavigationDto("保持安全观察", "避免独自等待；若症状升级，优先寻求急救。"));
    }
}
