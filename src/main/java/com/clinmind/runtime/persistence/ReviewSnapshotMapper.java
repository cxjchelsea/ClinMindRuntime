package com.clinmind.runtime.persistence;

import com.clinmind.runtime.candidate.review.CandidateReviewRecord;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import org.springframework.stereotype.Component;

@Component
public class ReviewSnapshotMapper {

    private final JsonSnapshotMapper jsonSnapshotMapper;

    public ReviewSnapshotMapper(JsonSnapshotMapper jsonSnapshotMapper) {
        this.jsonSnapshotMapper = jsonSnapshotMapper;
    }

    public String toJson(CandidateReviewRecord record) {
        return jsonSnapshotMapper.toJson(record);
    }

    public CandidateReviewRecord fromJson(String json) {
        return jsonSnapshotMapper.fromJson(json, CandidateReviewRecord.class);
    }
}
