package com.clinmind.runtime.evidence.phase12.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MinimalMarkdownEvidenceParserTest {

    private final MinimalMarkdownEvidenceParser parser = new MinimalMarkdownEvidenceParser();

    @Test
    void splitsMarkdownByHeadingParagraphAndListItem() {
        ParsedEvidenceDocument document = parser.parse("""
                # Safety

                Chest discomfort with sweating should be treated as a safety signal.

                - Ask about duration.
                - Ask about radiation.
                """);

        assertThat(document.chunks()).hasSize(3);
        assertThat(document.chunks()).allSatisfy(chunk -> {
            assertThat(chunk.sectionPath()).isEqualTo("Safety");
            assertThat(chunk.locator()).startsWith("section:safety#line:");
            assertThat(chunk.text()).isNotBlank();
        });
    }

    @Test
    void rejectsBlankContent() {
        assertThatThrownBy(() -> parser.parse("   \n\n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content");
    }
}