package com.clinmind.runtime.evaluation;

import java.util.List;

public interface EvaluationCaseRepository {

    String getDefaultCaseSetId();

    EvaluationCaseSet loadCaseSet(String caseSetId);

    List<EvaluationCase> loadCases(String caseSetId);

    List<EvaluationCase> loadCasesBySymptomGroup(String caseSetId, String symptomGroup);

    List<EvaluationCase> loadCasesByTag(String caseSetId, String tag);
}
