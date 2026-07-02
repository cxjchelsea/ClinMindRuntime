package com.clinmind.runtime.evidence.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.EvidencePolicyContext;
import com.clinmind.runtime.evidence.corpus.EvidenceCorpusRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EvidenceProviderPolicyTest {

    @Autowired
    private EvidenceProviderPolicy policy;

    @Autowired
    private EvidenceCorpusRepository corpusRepository;

    @Test
    void allowsSupportedSymptomGroups() {
        EvidencePolicyContext context = new EvidencePolicyContext(
                "rt_test", "sess", "chest_pain", List.of(), false, corpusRepository.isAvailable(), "phase2-default", "0.2.0");
        assertThat(policy.evaluate(context).allowed()).isTrue();
    }

    @Test
    void rejectsUnknownSymptomGroup() {
        EvidencePolicyContext context = new EvidencePolicyContext(
                "rt_test", "sess", "unknown_group", List.of(), false, corpusRepository.isAvailable(), "phase2-default", "0.2.0");
        assertThat(policy.evaluate(context).allowed()).isFalse();
    }
}
