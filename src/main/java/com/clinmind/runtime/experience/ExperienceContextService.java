package com.clinmind.runtime.experience;

import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.ExperienceContext;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.trace.TraceStep;
import org.springframework.stereotype.Service;

@Service
public class ExperienceContextService {

    @TraceStep("ExperienceContext")
    public ExperienceContext buildExperienceContext(CaseFrame caseFrame, KnowledgeContext knowledgeContext) {
        return new ExperienceContext();
    }
}
