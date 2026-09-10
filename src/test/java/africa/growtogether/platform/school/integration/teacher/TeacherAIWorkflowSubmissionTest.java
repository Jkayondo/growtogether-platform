package africa.growtogether.platform.school.integration.teacher;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eaif.*;
import africa.growtogether.platform.eaif.execution.AiTextRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherAIWorkflowSubmissionTest {
    private final UUID tenant = UUID.randomUUID();
    private final UUID user = UUID.randomUUID();
    private final UUID teacher = UUID.randomUUID();
    private final UUID assignment = UUID.randomUUID();
    private final EnterpriseIdentityContext identity = mock(EnterpriseIdentityContext.class);
    private final TeacherAiAccessGuard access = mock(TeacherAiAccessGuard.class);
    private final AiFoundationService foundation = mock(AiFoundationService.class);
    private final TeacherAIWorkflowService service =
            new TeacherAIWorkflowService(identity, access, foundation);

    private void permitted() {
        when(identity.hasPermission("ai.request.create")).thenReturn(true);
        when(identity.requireUserId()).thenReturn(user);
    }

    @Test void preservesPendingApprovalAndHashesExactInput() {
        permitted();
        String input = " Draft a teaching explanation. ";
        String scope = AiTextRequest.hash(
                tenant + ":" + user + ":" + teacher + ":" + assignment);
        AiRequest request = mock(AiRequest.class);
        UUID requestId = UUID.randomUUID();
        when(request.getId()).thenReturn(requestId);
        when(request.requestStatus()).thenReturn(AiEnums.RequestStatus.RECEIVED);
        when(foundation.submit(tenant, "GT_SCHOOL_TEACHER", "TEACHER_ASSISTANCE",
                "MODEL", AiTextRequest.hash(input), AiEnums.RiskLevel.HIGH, scope))
                .thenReturn(request);

        var result = service.submit(tenant, teacher, assignment, "MODEL", input);

        assertEquals(requestId, result.requestId());
        assertEquals(AiEnums.RequestStatus.RECEIVED, result.status());
        var order = inOrder(access, foundation);
        order.verify(access).requireOwnedAssignment(tenant, teacher, assignment);
        order.verify(foundation).submit(tenant, "GT_SCHOOL_TEACHER",
                "TEACHER_ASSISTANCE", "MODEL", AiTextRequest.hash(input),
                AiEnums.RiskLevel.HIGH, scope);
        verifyNoMoreInteractions(foundation);
    }

    @Test void missingPermissionCannotSubmit() {
        assertThrows(AccessDeniedException.class,
                () -> service.submit(tenant, teacher, assignment, "MODEL", "input"));
        verifyNoInteractions(access, foundation);
    }

    @Test void deniedOwnershipCannotSubmit() {
        permitted();
        doThrow(new AccessDeniedException("denied")).when(access)
                .requireOwnedAssignment(tenant, teacher, assignment);
        assertThrows(AccessDeniedException.class,
                () -> service.submit(tenant, teacher, assignment, "MODEL", "input"));
        verifyNoInteractions(foundation);
    }

    @Test void crossTenantCannotSubmit() {
        doThrow(new AccessDeniedException("denied")).when(identity).requireTenant(tenant);
        assertThrows(AccessDeniedException.class,
                () -> service.submit(tenant, teacher, assignment, "MODEL", "input"));
        verifyNoInteractions(access, foundation);
    }

    @Test void invalidInputCannotSubmit() {
        permitted();
        assertThrows(IllegalArgumentException.class,
                () -> service.submit(tenant, teacher, assignment, "MODEL", " "));
        assertThrows(IllegalArgumentException.class,
                () -> service.submit(tenant, teacher, assignment, "MODEL",
                        "x".repeat(100001)));
        verifyNoInteractions(foundation);
    }

    @Test void invalidModelCannotSubmit() {
        permitted();
        assertThrows(IllegalArgumentException.class,
                () -> service.submit(tenant, teacher, assignment, "", "input"));
        verifyNoInteractions(foundation);
    }
}
