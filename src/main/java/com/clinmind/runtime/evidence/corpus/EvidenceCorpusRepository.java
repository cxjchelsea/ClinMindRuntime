package com.clinmind.runtime.evidence.corpus;

import java.util.List;
import java.util.Optional;

public interface EvidenceCorpusRepository {

    EvidenceCorpus loadDefaultCorpus();

    Optional<EvidenceCorpus> findByPackageId(String packageId);

    List<EvidenceChunk> findBySymptomGroup(String symptomGroup);

    boolean isAvailable();
}
