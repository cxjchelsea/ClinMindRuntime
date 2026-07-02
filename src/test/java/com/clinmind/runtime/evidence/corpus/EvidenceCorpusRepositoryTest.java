package com.clinmind.runtime.evidence.corpus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EvidenceCorpusRepositoryTest {

    @Autowired
    private EvidenceCorpusRepository corpusRepository;

    @Test
    void loadsDefaultCorpusWithChunks() {
        EvidenceCorpus corpus = corpusRepository.loadDefaultCorpus();
        assertThat(corpusRepository.isAvailable()).isTrue();
        assertThat(corpus.chunkCount()).isGreaterThanOrEqualTo(9);
        assertThat(corpusRepository.findBySymptomGroup("chest_pain")).hasSizeGreaterThanOrEqualTo(3);
        assertThat(corpusRepository.findBySymptomGroup("fever")).hasSizeGreaterThanOrEqualTo(3);
        assertThat(corpusRepository.findBySymptomGroup("abdominal_pain")).hasSizeGreaterThanOrEqualTo(3);
    }
}
