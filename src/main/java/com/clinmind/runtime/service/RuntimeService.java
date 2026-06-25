package com.clinmind.runtime.service;

import com.clinmind.runtime.api.ContinueRuntimeRequest;
import com.clinmind.runtime.api.StartRuntimeRequest;
import com.clinmind.runtime.api.UserInputRequest;
import com.clinmind.runtime.caseframe.CaseFrameService;
import com.clinmind.runtime.entry.EntryAssessmentService;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.RuntimeTrace;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import com.clinmind.runtime.storage.RuntimeStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RuntimeService {

    private final RuntimeStore runtimeStore;
    private final EntryAssessmentService entryAssessmentService;
    private final CaseFrameService caseFrameService;

    public RuntimeService(
            RuntimeStore runtimeStore,
            EntryAssessmentService entryAssessmentService,
            CaseFrameService caseFrameService) {
        this.runtimeStore = runtimeStore;
        this.entryAssessmentService = entryAssessmentService;
        this.caseFrameService = caseFrameService;
    }

    public RuntimeExecutionResult startRuntime(StartRuntimeRequest request) {
        RuntimeState state = RuntimeState.createDefault(request.sessionId());
        state.setUserId(request.userId());
        state.setMode(request.mode());
        state.setRuntimeStatus(RuntimeStatus.ENTRY_ASSESSING);

        UserInput userInput = toUserInput(request.input());
        state.getInputHistory().add(userInput);

        EntryAssessmentResult entry = entryAssessmentService.assessEntry(userInput, request.basicInfo());
        state.setEntryAssessment(entry);
        state.setWorkMode(entry.workMode());
        state.setRuntimeStatus(statusAfterEntry(entry.workMode()));

        if (entry.workMode() != WorkMode.UNSUPPORTED) {
            state.setCaseFrame(caseFrameService.buildOrUpdateCaseFrame(
                    userInput, state.getCaseFrame(), request.basicInfo()));
        }

        RuntimeTrace trace = buildTrace(state, 1, userInput, request.basicInfo());
        runtimeStore.create(state);
        runtimeStore.addTrace(trace);
        state.getRuntimeTraceIds().add(trace.getTraceId());
        state.bumpVersion();
        runtimeStore.update(state);
        return new RuntimeExecutionResult(state, trace);
    }

    public RuntimeExecutionResult continueRuntime(ContinueRuntimeRequest request) {
        RuntimeState state = runtimeStore.get(request.runtimeId());
        UserInput userInput = toUserInput(request.input());
        state.getInputHistory().add(userInput);

        state.setCaseFrame(caseFrameService.buildOrUpdateCaseFrame(
                userInput, state.getCaseFrame(), null));

        if (state.getRuntimeStatus() != RuntimeStatus.ERROR_SAFE_HALTED
                && state.getRuntimeStatus() != RuntimeStatus.COMPLETED) {
            state.setRuntimeStatus(RuntimeStatus.WAITING_FOR_USER);
        }

        int step = runtimeStore.getTraces(request.runtimeId()).size() + 1;
        RuntimeTrace trace = buildTrace(state, step, userInput, null);
        runtimeStore.addTrace(trace);
        state.getRuntimeTraceIds().add(trace.getTraceId());
        state.bumpVersion();
        runtimeStore.update(state);
        return new RuntimeExecutionResult(state, trace);
    }

    public RuntimeState getStatus(String runtimeId) {
        return runtimeStore.get(runtimeId);
    }

    public RuntimeState getResult(String runtimeId) {
        return runtimeStore.get(runtimeId);
    }

    public List<RuntimeTrace> getTraces(String runtimeId) {
        return runtimeStore.getTraces(runtimeId);
    }

    private RuntimeStatus statusAfterEntry(WorkMode workMode) {
        return switch (workMode) {
            case UNSUPPORTED -> RuntimeStatus.ERROR_SAFE_HALTED;
            case WELLNESS_MODE -> RuntimeStatus.WELLNESS_MODE;
            default -> RuntimeStatus.COLLECTING_CASE_INFO;
        };
    }

    private RuntimeTrace buildTrace(
            RuntimeState state,
            int step,
            UserInput userInput,
            Map<String, Object> basicInfo) {
        RuntimeTrace trace = RuntimeTrace.create(state.getRuntimeId(), step, userInput.text());
        trace.recordModule("EntryAssessment");

        Map<String, Object> outputSummary = new LinkedHashMap<>();
        if (state.getEntryAssessment() != null) {
            outputSummary.put("work_mode", state.getEntryAssessment().workMode().getValue());
            outputSummary.put("symptom_group", state.getEntryAssessment().symptomGroup());
        }
        if (state.getWorkMode() != WorkMode.UNSUPPORTED) {
            trace.recordModule("CaseFrameBuilder");
            outputSummary.put("chief_complaint", state.getCaseFrame().chiefComplaint());
            outputSummary.put("missing_slots", state.getCaseFrame().missingSlots());
        }
        if (basicInfo != null) {
            outputSummary.put("basic_info_applied", true);
        }
        trace.setOutputSummary(outputSummary);
        return trace;
    }

    private UserInput toUserInput(UserInputRequest request) {
        return new UserInput(request.text(), request.attachments());
    }
}
