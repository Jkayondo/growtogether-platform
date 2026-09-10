package africa.growtogether.platform.school.integration.teacher;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eaif.AiEnums;
import africa.growtogether.platform.eaif.AiFoundationService;
import africa.growtogether.platform.eaif.execution.AiTextRequest;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherAIWorkflowService {
    private final EnterpriseIdentityContext identity;
    private final TeacherAiAccessGuard access;
    private final AiFoundationService foundation;
    private final africa.growtogether.platform.eaif.execution.AiTextExecutionService execution;

    public TeacherAIWorkflowService(EnterpriseIdentityContext identity,
            TeacherAiAccessGuard access, AiFoundationService foundation,
            africa.growtogether.platform.eaif.execution.AiTextExecutionService execution) {
        this.identity = identity;
        this.access = access;
        this.foundation = foundation;
        this.execution = execution;
    }

    // Preserved legacy summary; this is not an execution-readiness indicator.
    public TeacherWorkflowSummary integrate(UUID teacherId) {
        return new TeacherWorkflowSummary(
                teacherId, 0, TeacherWorkflowStatus.ACTIVE.name());
    }

    /**
     * Submission only. EAIF owns approval decisions; no provider call occurs.
     * Suspend an outer transaction so EAIF submission commits before a later
     * execution operation attempts to claim the request.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Submission submit(UUID tenantId, UUID teacherProfileId,
            UUID assignmentId, String modelCode, String input) {
        identity.requireTenant(tenantId);
        if (!identity.hasPermission("ai.request.create"))
            throw new AccessDeniedException("AI request creation permission required.");

        UUID userId = identity.requireUserId();
        access.requireOwnedAssignment(tenantId, teacherProfileId, assignmentId);

        if (modelCode == null || modelCode.isBlank()
                || modelCode.length() > 100)
            throw new IllegalArgumentException("Invalid model code.");
        if (input == null || input.isBlank() || input.length() > 100000)
            throw new IllegalArgumentException("Invalid AI input size.");

        String scope = AiTextRequest.hash(
                tenantId + ":" + userId + ":" + teacherProfileId + ":" + assignmentId);

        var request = foundation.submit(
                tenantId,
                "GT_SCHOOL_TEACHER",
                "TEACHER_ASSISTANCE",
                modelCode,
                AiTextRequest.hash(input),
                AiEnums.RiskLevel.HIGH,
                scope);

        return new Submission(request.getId(), request.requestStatus());
    }


    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public RequestStatus execute(UUID tenantId, UUID teacherProfileId,
            UUID assignmentId, UUID requestId, String input) {
        ownedRequest(tenantId, teacherProfileId, assignmentId, requestId,
                "ai.runtime.execute");
        String reference = execution.execute(tenantId, requestId, input);
        return new RequestStatus(requestId, AiEnums.RequestStatus.SUCCEEDED, reference);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public RequestStatus status(UUID tenantId, UUID teacherProfileId,
            UUID assignmentId, UUID requestId) {
        var request = ownedRequest(tenantId, teacherProfileId, assignmentId,
                requestId, "ai.request.read");
        return new RequestStatus(request.getId(), request.requestStatus(),
                request.requestStatus() == AiEnums.RequestStatus.SUCCEEDED
                        ? request.outputReference() : null);
    }

    private africa.growtogether.platform.eaif.AiRequest ownedRequest(
            UUID tenantId, UUID teacherProfileId, UUID assignmentId,
            UUID requestId, String permission) {
        identity.requireTenant(tenantId);
        if (!identity.hasPermission(permission))
            throw new AccessDeniedException("AI request permission required.");
        UUID userId = identity.requireUserId();
        access.requireOwnedAssignment(tenantId, teacherProfileId, assignmentId);
        if (requestId == null)
            throw new AccessDeniedException("AI request access denied.");

        var request = foundation.get(tenantId, requestId);
        String scope = AiTextRequest.hash(
                tenantId + ":" + userId + ":" + teacherProfileId + ":" + assignmentId);
        if (request.getStatus()
                    != africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE
                || !"GT_SCHOOL_TEACHER".equals(request.sourceService())
                || !"TEACHER_ASSISTANCE".equals(request.useCase())
                || !scope.equals(request.correlationId()))
            throw new AccessDeniedException("AI request access denied.");
        return request;
    }

    // A document reference does not grant permission to download its contents.
    public record RequestStatus(UUID requestId, AiEnums.RequestStatus status,
            String outputReference) {}

    public record Submission(UUID requestId, AiEnums.RequestStatus status) {}
}
