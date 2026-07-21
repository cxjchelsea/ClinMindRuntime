package com.clinmind.runtime.evidence.phase12.retrieval.lexical;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class ClinicalQuestionLexicalNormalizer {

    public String normalize(String question) {
        if (question == null) {
            return "";
        }
        String normalized = Normalizer.normalize(question, Normalizer.Form.NFKC)
                .replace('\u3000', ' ')
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
        for (Replacement replacement : replacements()) {
            normalized = normalized.replace(replacement.from(), replacement.to());
        }
        return normalized.replaceAll("\\s+", " ").trim();
    }

    public String toPostgresWebSearchQuery(String question) {
        String normalized = normalize(question);
        return normalized.length() > 512 ? normalized.substring(0, 512) : normalized;
    }

    private List<Replacement> replacements() {
        return List.of(
                new Replacement("acs", "acute coronary syndrome"),
                new Replacement("mi", "myocardial infarction"),
                new Replacement("sob", "shortness of breath"),
                new Replacement("胸闷", "胸闷 chest discomfort"),
                new Replacement("胸痛", "胸痛 chest pain"),
                new Replacement("出汗", "出汗 sweating"),
                new Replacement("活动后", "活动后 exertional")
        );
    }

    private record Replacement(String from, String to) {}
}
