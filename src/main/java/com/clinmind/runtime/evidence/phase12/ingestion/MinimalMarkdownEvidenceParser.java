package com.clinmind.runtime.evidence.phase12.ingestion;

import com.clinmind.runtime.evidence.phase12.SpanType;
import java.util.ArrayList;
import java.util.List;

class MinimalMarkdownEvidenceParser {

    static final String PARSER_VERSION = "phase12-p0-markdown-parser-1";

    ParsedEvidenceDocument parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        List<ParsedChunk> chunks = new ArrayList<>();
        String currentSection = "document";
        StringBuilder paragraph = new StringBuilder();
        int ordinal = 0;
        int lineNo = 0;
        for (String rawLine : text.replace("\r\n", "\n").replace('\r', '\n').split("\n")) {
            lineNo++;
            String line = rawLine.strip();
            if (line.startsWith("#")) {
                ordinal = flush(chunks, paragraph, currentSection, ordinal, lineNo - 1);
                currentSection = normalizeHeading(line);
                continue;
            }
            if (line.isBlank()) {
                ordinal = flush(chunks, paragraph, currentSection, ordinal, lineNo - 1);
                continue;
            }
            if (line.startsWith("- ") || line.startsWith("* ") || line.matches("^[0-9]+\\.\\s+.*")) {
                ordinal = flush(chunks, paragraph, currentSection, ordinal, lineNo - 1);
                chunks.add(new ParsedChunk(currentSection, ordinal++, normalizeListItem(line), inferSpanType(line), locator(currentSection, lineNo)));
                continue;
            }
            if (!paragraph.isEmpty()) {
                paragraph.append(' ');
            }
            paragraph.append(line);
        }
        flush(chunks, paragraph, currentSection, ordinal, lineNo);
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("no parseable chunk found");
        }
        return new ParsedEvidenceDocument(chunks);
    }

    private int flush(List<ParsedChunk> chunks, StringBuilder paragraph, String section, int ordinal, int lineNo) {
        String text = paragraph.toString().strip();
        if (!text.isBlank()) {
            chunks.add(new ParsedChunk(section, ordinal++, text, inferSpanType(text), locator(section, Math.max(lineNo, 1))));
            paragraph.setLength(0);
        }
        return ordinal;
    }

    private String normalizeHeading(String line) {
        String heading = line.replaceFirst("^#+", "").strip();
        return heading.isBlank() ? "document" : heading;
    }

    private String normalizeListItem(String line) {
        return line.replaceFirst("^[-*]\\s+", "").replaceFirst("^[0-9]+\\.\\s+", "").strip();
    }

    private SpanType inferSpanType(String text) {
        String lower = text.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("risk") || lower.contains("urgent") || lower.contains("safety")) {
            return SpanType.SAFETY_NOTICE;
        }
        if (lower.contains("applicability") || lower.contains("audience") || lower.contains("jurisdiction")) {
            return SpanType.APPLICABILITY_NOTE;
        }
        if (lower.contains("license") || lower.contains("governance") || lower.contains("review")) {
            return SpanType.GOVERNANCE_NOTE;
        }
        if (lower.contains("should") || lower.contains("must")) {
            return SpanType.RECOMMENDATION;
        }
        return SpanType.BACKGROUND;
    }

    private String locator(String section, int lineNo) {
        return "section:" + section.replaceAll("\\s+", "_").toLowerCase(java.util.Locale.ROOT) + "#line:" + lineNo;
    }
}