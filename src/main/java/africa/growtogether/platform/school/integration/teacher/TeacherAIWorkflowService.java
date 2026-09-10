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

    public TeacherAIWorkflowService(EnterpriseIdentityContext identity,
            TeacherAiAccessGuard access, AiFoundationService foundation) {
        this.identity = identity;
        this.access = access;
        this.foundation = foundation;
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

    public record Submission(UUID requestId, AiEnums.RequestStatus status) {}
}
