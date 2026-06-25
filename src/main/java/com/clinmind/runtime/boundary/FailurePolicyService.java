package com.clinmind.runtime.boundary;

import com.clinmind.runtime.state.OutputLevel;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FailurePolicyService {

    private final DecisionBoundaryService decisionBoundaryService;

    public FailurePolicyService(DecisionBoundaryService decisionBoundaryService) {
        this.decisionBoundaryService = decisionBoundaryService;
    }

    public RuntimeState handleFailure(String moduleName, Exception error, RuntimeState state) {
        state.setRuntimeStatus(RuntimeStatus.ERROR_SAFE_HALTED);
        state.setDecisionBoundary(decisionBoundaryService.failSafeBoundary(
                moduleName + " failed: " + error.getMessage()));

        state.setPatientOutput(new PatientOutput(
                false,
                "系统进入安全保护模式，暂时无法继续提供判断。请尽快联系线下医疗机构。",
                OutputLevel.O1_CONTINUE_QUESTIONING,
                List.of("fail_safe", "module_failure:" + moduleName)));

        state.setClinicianReport(null);
        return state;
    }
}
