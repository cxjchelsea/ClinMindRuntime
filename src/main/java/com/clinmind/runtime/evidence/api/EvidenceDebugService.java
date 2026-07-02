package com.clinmind.runtime.evidence.api;

import com.clinmind.runtime.api.ApiException;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceConstants;
import com.clinmind.runtime.evidence.EvidenceRef;
import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.EvidenceRetrievalResult;
import com.clinmind.runtime.evidence.EvidenceRetrievalTrace;
import com.clinmind.runtime.evidence.EvidenceValidationResult;
import com.clinmind.runtime.evidence.api.dto.EvidenceCandidateSafeDto;
import com.clinmind.runtime.evidence.api.dto.EvidenceCaseFrameSummaryRequest;
import com.clinmind.runtime.evidence.api.dto.EvidenceCorpusChunkDto;
import com.clinmind.runtime.evidence.api.dto.EvidenceCorpusResponse;
import com.clinmind.runtime.evidence.api.dto.EvidenceQueryTraceDto;
import com.clinmind.runtime.evidence.api.dto.EvidenceRefSafeDto;
import com.clinmind.runtime.evidence.api.dto.EvidenceRetrieveRequest;
import com.clinmind.runtime.evidence.api.dto.EvidenceRetrieveResponse;
import com.clinmind.runtime.evidence.api.dto.EvidenceValidationResultDto;
import com.clinmind.runtime.evidence.corpus.EvidenceChunk;
import com.clinmind.runtime.evidence.corpus.EvidenceCorpus;
import com.clinmind.runtime.evidence.corpus.EvidenceCorpusRepository;
import com.clinmind.runtime.evidence.runtime.EvidenceRetrievalRuntime;
import com.clinmind.runtime.state.IdGenerator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EvidenceDebugService {

    private final EvidenceRetrievalRuntime evidenceRetrievalRuntime;
    private final EvidenceCorpusRepository corpusRepository;
    private final AuditLogService auditLogService;

    public EvidenceDebugService(
            EvidenceRetrievalRuntime evidenceRetrievalRuntime,
            EvidenceCorpusRepository corpusRepository,
            AuditLogService auditLogService) {
        this.evidenceRetrievalRuntime = evidenceRetrievalRuntime;
        this.corpusRepository = corpusRepository;
        this.auditLogService = auditLogService;
    }

    public EvidenceRetrieveResponse retrieve(EvidenceRetrieveRequest request) {
        validateRetrieveRequest(request);
        EvidenceRetrievalRequest retrievalRequest = toRetrievalRequest(request);
        EvidenceRetrievalResult result = evidenceRetrievalRuntime.retrieve(retrievalRequest);
        auditLogService.record(
                AuditActionType.RUN_EVIDENCE_RETRIEVAL,
                AuditResourceType.EVIDENCE_RETRIEVAL,
                result.retrievalId(),
                AuditResultStatus.SUCCESS,
                Map.of(
                        "runtime_id", result.runtimeId(),
                        "provider_id", result.providerId(),
                        "status", result.status().name()));
        return toRetrieveResponse(result);
    }

    public EvidenceRetrieveResponse getRetrieval(String retrievalId) {
        EvidenceRetrievalResult result = evidenceRetrievalRuntime
                .findById(retrievalId)
                .orElseThrow(() -> new EvidenceRetrievalNotFoundException(retrievalId));
        auditLogService.record(
                AuditActionType.QUERY_EVIDENCE_RETRIEVAL,
                AuditResourceType.EVIDENCE_RETRIEVAL,
                retrievalId,
                AuditResultStatus.SUCCESS,
                Map.of("status", result.status().name()));
        return toRetrieveResponse(result);
    }

    public EvidenceCorpusResponse getCorpus() {
        EvidenceCorpus corpus = corpusRepository.loadDefaultCorpus();
        List<EvidenceCorpusChunkDto> chunks = corpus.chunks().stream()
                .map(this::toCorpusChunkDto)
                .toList();
        auditLogService.record(
                AuditActionType.QUERY_EVIDENCE_CORPUS,
                AuditResourceType.EVIDENCE_RETRIEVAL,
                corpus.packageId(),
                AuditResultStatus.SUCCESS,
                Map.of("chunk_count", chunks.size()));
        return new EvidenceCorpusResponse(corpus.packageId(), corpus.version(), chunks.size(), chunks);
    }

    private void validateRetrieveRequest(EvidenceRetrieveRequest request) {
        if (request.runtimeId() == null || request.runtimeId().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "runtime_id is required");
        }
        if (request.symptomGroup() == null || request.symptomGroup().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "symptom_group is required");
        }
        if (request.caseFrameSummary() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "case_frame_summary is required");
        }
        if (request.roleContext() != null && "patient_direct_answer".equalsIgnoreCase(request.roleContext())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "forbidden role_context");
        }
    }

    private EvidenceRetrievalRequest toRetrievalRequest(EvidenceRetrieveRequest request) {
        EvidenceCaseFrameSummaryRequest summary = request.caseFrameSummary();
        Map<String, Object> caseFrameSummary = new LinkedHashMap<>();
        if (summary.chiefComplaint() != null) {
            caseFrameSummary.put("chief_complaint", summary.chiefComplaint());
        }
        List<String> knownFacts = summary.knownFacts() == null ? List.of() : summary.knownFacts();
        List<String> missingFacts = summary.missingFacts() == null ? List.of() : summary.missingFacts();
        caseFrameSummary.put("known_facts", knownFacts);
        caseFrameSummary.put("missing_facts", missingFacts);

        int limit = request.retrievalLimit() == null
                ? EvidenceConstants.DEFAULT_RETRIEVAL_LIMIT
                : request.retrievalLimit();

        return new EvidenceRetrievalRequest(
                IdGenerator.evidenceRetrievalId(),
                request.runtimeId(),
                request.symptomGroup(),
                caseFrameSummary,
                knownFacts,
                missingFacts,
                request.candidateDdxSummary() == null ? List.of() : request.candidateDdxSummary(),
                request.redFlagSummary() == null ? List.of() : request.redFlagSummary(),
                request.assetPackageId(),
                request.assetPackageVersion(),
                limit,
                request.roleContext() == null ? "clinician_debug" : request.roleContext());
    }

    private EvidenceRetrieveResponse toRetrieveResponse(EvidenceRetrievalResult result) {
        EvidenceRetrievalTrace trace = result.queryTrace();
        return new EvidenceRetrieveResponse(
                result.retrievalId(),
                result.runtimeId(),
                result.providerId(),
                result.providerVersion(),
                result.evidenceCorpusVersion(),
                result.status().name(),
                result.evidenceCandidates().stream().map(this::toCandidateDto).toList(),
                toValidationDto(result.validationResult()),
                new EvidenceQueryTraceDto(
                        trace == null ? List.of() : trace.queryTerms(),
                        trace == null ? 0 : trace.matchedChunkCount(),
                        trace != null && trace.traceId() != null),
                result.warnings());
    }

    private EvidenceValidationResultDto toValidationDto(EvidenceValidationResult validationResult) {
        if (validationResult == null) {
            return new EvidenceValidationResultDto("UNKNOWN", List.of(), List.of(), List.of());
        }
        return new EvidenceValidationResultDto(
                validationResult.status().name(),
                validationResult.acceptedCandidateIds(),
                validationResult.rejectedCandidateIds(),
                validationResult.reasons());
    }

    private EvidenceCandidateSafeDto toCandidateDto(EvidenceCandidate candidate) {
        EvidenceRef ref = candidate.evidenceRef();
        return new EvidenceCandidateSafeDto(
                candidate.candidateId(),
                ref == null ? null : toRefDto(ref),
                candidate.matchedCaseFrameFields(),
                candidate.relatedDdxItem(),
                candidate.useCase() == null ? null : candidate.useCase().getValue(),
                candidate.confidence(),
                candidate.reasonSummary());
    }

    private EvidenceRefSafeDto toRefDto(EvidenceRef ref) {
        return new EvidenceRefSafeDto(
                ref.evidenceId(),
                ref.sourceId(),
                ref.chunkId(),
                ref.sourceType(),
                ref.title(),
                ref.sectionPath(),
                ref.symptomGroup(),
                ref.diagnosisTags(),
                ref.evidenceStrength(),
                ref.supportsOrRefutes(),
                ref.riskLevel() == null ? null : ref.riskLevel().name(),
                ref.assetPackageVersion(),
                ref.evidenceCorpusVersion(),
                ref.retrievedBy(),
                ref.retrievalScore());
    }

    private EvidenceCorpusChunkDto toCorpusChunkDto(EvidenceChunk chunk) {
        return new EvidenceCorpusChunkDto(
                chunk.chunkId(),
                chunk.sourceId(),
                chunk.title(),
                chunk.symptomGroup(),
                chunk.riskLevel().name(),
                chunk.useCases().stream().map(useCase -> useCase.getValue()).toList());
    }
}
