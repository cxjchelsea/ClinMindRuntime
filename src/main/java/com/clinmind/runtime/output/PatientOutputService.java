package com.clinmind.runtime.output;

import com.clinmind.runtime.state.DecisionBoundaryResult;
import com.clinmind.runtime.state.NextAction;
import com.clinmind.runtime.state.NextActionType;
import com.clinmind.runtime.state.OutputLevel;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.QuestionTestPolicyResult;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PatientOutputService {

    public PatientOutput buildPatientOutput(RuntimeState state) {
        DecisionBoundaryResult boundary = state.getDecisionBoundary();
        if (boundary == null) {
            return conservativeOutput("decision boundary unavailable");
        }

        QuestionTestPolicyResult policy = state.getQuestionTestPolicy();
        SafetyGateResult safetyGate = state.getSafetyGate();
        List<String> constraintsApplied = new ArrayList<>(boundary.constraints());

        String content = buildContent(policy, safetyGate, boundary);
        if (content == null || content.isBlank()) {
            return conservativeOutput("unable to build patient-safe output");
        }

        return new PatientOutput(true, content, boundary.allowedOutputLevel(), List.copyOf(constraintsApplied));
    }

    private String buildContent(
            QuestionTestPolicyResult policy,
            SafetyGateResult safetyGate,
            DecisionBoundaryResult boundary) {
        if (safetyGate != null && safetyGate.triggered()) {
            return "当前描述中存在需要重视的风险信号，系统不能给出低风险判断或确定诊断。"
                    + "请尽快前往线下医疗机构评估，必要时寻求紧急帮助。";
        }
        if (policy == null || policy.nextAction() == null) {
            return "为了更准确了解情况，请继续补充症状细节。";
        }

        NextAction action = policy.nextAction();
        return switch (action.type()) {
            case ASK_QUESTION -> "为了更准确了解情况，我需要再确认一个问题：" + action.content();
            case RECOMMEND_TEST -> "目前还不能排除一些需要重视的情况，建议先进行相关检查（"
                    + action.content() + "），并结合医生评估。";
            case RECOMMEND_VISIT -> "当前情况建议尽快线下就医评估，不要自行判断为普通或低风险问题。";
            case WAIT_FOR_USER -> "请继续补充您的症状信息，以便系统继续评估。";
            default -> "系统正在继续收集信息，请补充更多症状细节。";
        };
    }

    private PatientOutput conservativeOutput(String reason) {
        return new PatientOutput(
                false,
                "当前系统进入保守模式，暂时无法提供进一步判断。请尽快联系线下医疗机构。原因：" + reason,
                OutputLevel.O1_CONTINUE_QUESTIONING,
                List.of("fail_safe", "no_definitive_diagnosis", "no_prescription"));
    }
}
