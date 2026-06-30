package com.clinmind.runtime.persistence;

import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.CandidateSourceRef;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import org.springframework.stereotype.Component;

@Component
public class CandidateSnapshotMapper {

    private final JsonSnapshotMapper jsonSnapshotMapper;

    public CandidateSnapshotMapper(JsonSnapshotMapper jsonSnapshotMapper) {
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    public String toJson(CandidateGenerationResult result) {
        return jsonSnapshotMapper.toJson(result);
    }

    public CandidateGenerationResult generationFromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, CandidateGenerationResult.class);
    }

    public String toJson(ExperienceCandidate candidate) {
        return jsonSnapshotMapper.toJson(candidate);
    }

    public ExperienceCandidate experienceFromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, ExperienceCandidate.class);
    }

    public String toJson(TrainingExampleCandidate candidate) {
        return jsonSnapshotMapper.toJson(candidate);
    }

    public TrainingExampleCandidate trainingFromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, TrainingExampleCandidate.class);
    }

    public String toJson(CandidateSourceRef sourceRef) {
        return jsonSnapshotMapper.toJson(sourceRef);
    }

    public CandidateSourceRef sourceRefFromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, CandidateSourceRef.class);
    }
}
