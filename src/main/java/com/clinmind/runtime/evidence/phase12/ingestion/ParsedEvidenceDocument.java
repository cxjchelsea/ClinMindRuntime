package com.clinmind.runtime.evidence.phase12.ingestion;

import com.clinmind.runtime.evidence.phase12.SpanType;
import java.util.List;

record ParsedEvidenceDocument(List<ParsedChunk> chunks) {
    ParsedEvidenceDocument {
        chunks = chunks == null ? List.of() : List.copyOf(chunks);
    }
}

record ParsedChunk(
        String sectionPath,
        int ordinal,
        String text,
        SpanType spanType,
        String locator
) {}