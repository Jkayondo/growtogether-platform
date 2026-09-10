package africa.growtogether.platform.school.integration.teacher;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eaif.*;
import africa.growtogether.platform.eaif.execution.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherAIWorkflowExecutionTest {
    private final UUID tenant = UUID.randomUUID(), user = UUID.randomUUID();
    private final UUID teacher = UUID.randomUUID(), assignment = UUID.randomUUID();
    private final UUID requestId = UUID.randomUUID();
    private final EnterpriseIdentityContext identity = mock(EnterpriseIdentityContext.class);
    private final TeacherAiAccessGuard access = mock(TeacherAiAccessGuard.class);
    private final AiFoundationService foundation = mock(AiFoundationService.class);
    private final AiTextExecutionService execution = mock(AiTextExecutionService.class);
    private final AiRequest request = mock(AiRequest.class);
    private final TeacherAIWorkflowService service =
            new TeacherAIWorkflowService(identity, access, foundation, execution);

    private void owned() {
        when(identity.requireUserId()).thenReturn(user);
        when(identity.hasPermission("ai.runtime.execute")).thenReturn(true);
        when(identity.hasPermission("ai.request.read")).thenReturn(true);
        when(foundation.get(tenant, requestId)).thenReturn(request);
        when(request.getId()).thenReturn(requestId);
        when(request.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(request.sourceService()).thenReturn("GT_SCHOOL_TEACHER");
        when(request.useCase()).thenReturn("TEACHER_ASSISTANCE");
        when(request.correlationId()).thenReturn(AiTextRequest.hash(
                tenant + ":" + user + ":" + teacher + ":" + assignment));
    }

    private TeacherAIWorkflowService.RequestStatus execute() {
        return service.execute(tenant, teacher, assignment, requestId, "input");
    }

    @Test void delegatesOnlyAfterOwnershipAndRequestChecks() {
        owned();
        when(execution.execute(tenant, requestId, "input")).thenReturn("eds:example");
        var result = execute();
        assertEquals(AiEnums.RequestStatus.SUCCEEDED, result.status());
        assertEquals("eds:example", result.outputReference());
        var order = inOrder(access, foundation, execution);
        order.verify(access).requireOwnedAssignment(tenant, teacher, assignment);
        order.verify(foundation).get(tenant, requestId);
        order.verify(execution).execute(tenant, requestId, "input");
        verifyNoMoreInteractions(execution);
    }

    @Test void missingPermissionPreventsExecution() {
        assertThrows(AccessDeniedException.class, this::execute);
        verifyNoInteractions(access, foundation, execution);
    }

    @Test void deniedTeacherOwnershipPreventsRequestLookup() {
        owned();
        doThrow(new AccessDeniedException("denied")).when(access)
                .requireOwnedAssignment(tenant, teacher, assignment);
        assertThrows(AccessDeniedException.class, this::execute);
        verifyNoInteractions(foundation, execution);
    }

    @Test void differentScopePreventsExecutionAndStatusRead() {
        owned();
        when(request.correlationId()).thenReturn("different-scope");
        assertThrows(AccessDeniedException.class, this::execute);
        assertThrows(AccessDeniedException.class,
                () -> service.status(tenant, teacher, assignment, requestId));
        verifyNoInteractions(execution);
    }

    @Test void otherProductRequestCannotExecute() {
        owned();
        when(request.sourceService()).thenReturn("OTHER_PRODUCT");
        assertThrows(AccessDeniedException.class, this::execute);
        verifyNoInteractions(execution);
    }

    @Test void otherUseCaseCannotExecute() {
        owned();
        when(request.useCase()).thenReturn("OTHER_USE_CASE");
        assertThrows(AccessDeniedException.class, this::execute);
        verifyNoInteractions(execution);
    }

    @Test void inactiveRequestCannotExecute() {
        owned();
        when(request.getStatus()).thenReturn(EntityStatus.INACTIVE);
        assertThrows(AccessDeniedException.class, this::execute);
        verifyNoInteractions(execution);
    }

    @Test void eaifRejectionPropagatesWithoutRetry() {
        owned();
        when(execution.execute(tenant, requestId, "input"))
                .thenThrow(new IllegalStateException("Not approved"));
        assertThrows(IllegalStateException.class, this::execute);
        verify(execution, times(1)).execute(tenant, requestId, "input");
    }

    @Test void statusReadDoesNotExecuteAndHidesUnfinishedOutput() {
        owned();
        when(request.requestStatus()).thenReturn(AiEnums.RequestStatus.RECEIVED);
        when(request.outputReference()).thenReturn("eds:unfinished");
        var result = service.status(tenant, teacher, assignment, requestId);
        assertEquals(AiEnums.RequestStatus.RECEIVED, result.status());
        assertNull(result.outputReference());
        verifyNoInteractions(execution);
    }

    @Test void completedStatusReturnsReference() {
        owned();
        when(request.requestStatus()).thenReturn(AiEnums.RequestStatus.SUCCEEDED);
        when(request.outputReference()).thenReturn("eds:example");
        assertEquals("eds:example",
                service.status(tenant, teacher, assignment, requestId).outputReference());
        verifyNoInteractions(execution);
    }

    @Test void statusRequiresReadPermission() {
        owned();
        when(identity.hasPermission("ai.request.read")).thenReturn(false);
        assertThrows(AccessDeniedException.class,
                () -> service.status(tenant, teacher, assignment, requestId));
        verifyNoInteractions(access, foundation, execution);
    }
}
