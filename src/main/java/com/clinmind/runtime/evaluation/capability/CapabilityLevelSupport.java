package com.clinmind.runtime.evaluation.capability;

import java.util.List;

final class CapabilityLevelSupport {

    static final String L0 = "L0";
    static final String L1 = "L1";
    static final String L2 = "L2";
    static final String L3 = "L3";
    static final String L4 = "L4";
    static final String L5 = "L5";

    private CapabilityLevelSupport() {
    }

    static int rank(String level) {
        if (level == null || level.isBlank()) {
            return 0;
        }
        String normalized = level.trim();
        if (normalized.startsWith("L") && normalized.length() >= 2) {
            int end = 1;
            while (end < normalized.length() && Character.isDigit(normalized.charAt(end))) {
                end++;
            }
            if (end > 1) {
                return Integer.parseInt(normalized.substring(1, end));
            }
        }
        return 0;
    }

    static String levelForRank(int rank) {
        int clamped = Math.max(0, Math.min(5, rank));
        return "L" + clamped;
    }

    static List<String> patientOutputsForLevel(String level) {
        return switch (rank(level)) {
            case 0 -> List.of();
            case 1 -> List.of("O1_continue_questioning");
            case 2 -> List.of(
                    "O1_continue_questioning",
                    "O2_risk_hint");
            case 3, 4, 5 -> List.of(
                    "O1_continue_questioning",
                    "O2_risk_hint",
                    "O5_visit_or_urgent_care_recommendation");
            default -> List.of();
        };
    }

    static List<String> clinicianOutputsForLevel(String level) {
        return switch (rank(level)) {
            case 0, 1, 2, 3 -> List.of();
            case 4 -> List.of("O3_clinician_candidate_diagnosis");
            case 5 -> List.of(
                    "O3_clinician_candidate_diagnosis",
                    "O7_clinician_full_report");
            default -> List.of();
        };
    }

    static List<String> defaultConstraints() {
        return List.of(
                "no_definitive_diagnosis",
                "no_prescription",
                "requires_human_review");
    }
}
