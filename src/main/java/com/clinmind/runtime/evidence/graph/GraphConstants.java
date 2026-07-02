package com.clinmind.runtime.evidence.graph;

import java.util.Set;

public final class GraphConstants {

    public static final String KG_LITE_GRAPH_EVIDENCE_PROVIDER_ID = "kg_lite_graph_evidence_provider";
    public static final String KG_LITE_GRAPH_EVIDENCE_PROVIDER_VERSION = "0.7.1-p1";
    public static final String DEFAULT_GRAPH_PACKAGE_ID = "phase7-default";
    public static final String DEFAULT_GRAPH_VERSION = "phase7-kg-lite-0.1.0";
    public static final int DEFAULT_MAX_PATH_DEPTH = 2;
    public static final int MAX_MAX_PATH_DEPTH = 3;
    public static final int DEFAULT_MAX_PATH_COUNT = 5;
    public static final int MAX_MAX_PATH_COUNT = 10;
    public static final int MAX_ACCEPTED_GRAPH_CANDIDATES = 5;

    public static final Set<String> SUPPORTED_SYMPTOM_GROUPS = Set.of(
            "chest_pain", "fever", "abdominal_pain");

    private GraphConstants() {
    }
}
