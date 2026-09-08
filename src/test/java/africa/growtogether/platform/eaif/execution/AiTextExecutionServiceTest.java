package africa.growtogether.platform.eaif.execution;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.eaif.*;
import africa.growtogether.platform.eaif.audit.EaifAuditService;
import africa.growtogether.platform.eaif.approval.*;
import africa.growtogether.platform.eaif.governance.policy.AiGovernancePolicyService;
import africa.growtogether.platform.eaif.integration.EaifConfigurationGateway;
import africa.growtogether.platform.eip.AiProviderExecutionGateway;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AiTextExecutionServiceTest {
    final UUID tenant = UUID.randomUUID(), id = UUID.randomUUID();
    final String input = "Assessment evidence";
    final EnterpriseIdentityContext identity = mock(EnterpriseIdentityContext.class);
    final EaifConfigurationGateway config = mock(EaifConfigurationGateway.class);
    final AiFoundationService foundation = mock(AiFoundationService.class);
    final AiExecutionCatalogue catalogue = mock(AiExecutionCatalogue.class);
    final AiGovernancePolicyService governance = mock(AiGovernancePolicyService.class);
    final EaifAuditService audit = mock(EaifAuditService.class);
    final EaifApprovalService approvals = mock(EaifApprovalService.class);
    final AiProviderExecutionGateway gateway = mock(AiProviderExecutionGateway.class);
    final AiExecutionOutputStore outputs = mock(AiExecutionOutputStore.class);
    final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    AiRequest request;
    AiTextExecutionService service;
    @BeforeEach void setup() {
        when(identity.hasPermission("ai.runtime.execute")).thenReturn(true);
        when(config.providerExecutionEnabled()).thenReturn(true);
        when(config.maximumInputCharacters()).thenReturn(100000);
        when(transactions.getTransaction(any())).thenAnswer(i -> new SimpleTransactionStatus());
        request = new AiRequest(tenant, "GT", "TEST", "MODEL", AiTextRequest.hash(input), AiEnums.RiskLevel.LOW, null);
        request.approve();
        when(foundation.get(tenant, id)).thenReturn(request);
        when(governance.allows(tenant, "DEFAULT_AI_POLICY", AiEnums.RiskLevel.LOW)).thenReturn(true);
        when(catalogue.resolve(tenant, "MODEL")).thenReturn(
                new AiExecutionCatalogue.Selection("PROVIDER", AiEnums.ProviderType.OPENAI_COMPATIBLE, "model", 100));
        service = new AiTextExecutionService(identity, config, foundation, catalogue, governance,
                audit, gateway, outputs, transactions, approvals);
    }
    @Test void commitsClaimBeforeDispatchAndPersistsOutputReference() {
        var result = new AiTextResult("resp_test", "Explanation");
        when(gateway.execute(eq(tenant), eq("PROVIDER"), any(), any())).thenReturn(result);
        when(outputs.save(tenant, id, result)).thenReturn("eds:test");
        assertEquals("eds:test", service.execute(tenant, id, input));
        var order = inOrder(foundation, audit, transactions, gateway, outputs);
        order.verify(foundation).get(tenant, id);
        order.verify(foundation).begin(tenant, id);
        order.verify(audit).start(tenant, id);
        order.verify(transactions).commit(any());
        order.verify(gateway).execute(eq(tenant), eq("PROVIDER"), any(), any());
        order.verify(outputs).save(tenant, id, result);
        order.verify(foundation).succeed(tenant, id, "eds:test");
        order.verify(audit).complete(tenant, id, "eds:test");
    }
    @Test void disabledExecutionNeverDispatches() {
        when(config.providerExecutionEnabled()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> service.execute(tenant, id, input));
        verifyNoInteractions(gateway, outputs, foundation);
    }
    @Test void crossTenantNeverLoadsRequest() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Denied"))
                .when(identity).requireTenant(tenant);
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> service.execute(tenant, id, input));
        verifyNoInteractions(foundation, gateway);
    }
    @Test void permissionDeniedNeverLoadsRequest() {
        when(identity.hasPermission("ai.runtime.execute")).thenReturn(false);
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> service.execute(tenant, id, input));
        verifyNoInteractions(foundation, gateway);
    }
    @Test void changedInputCannotUseExistingApproval() {
        assertThrows(IllegalArgumentException.class, () -> service.execute(tenant, id, input + " modified"));
        verifyNoInteractions(gateway, outputs);
        verify(foundation, never()).begin(any(), any());
    }
    @Test void unapprovedOrAlreadyProcessingRequestCannotDispatch() {
        request.begin();
        assertThrows(IllegalStateException.class, () -> service.execute(tenant, id, input));
        verifyNoInteractions(gateway);
    }
    @Test void missingHumanApprovalCannotDispatch() {
        when(governance.requiresApproval(tenant, "DEFAULT_AI_POLICY")).thenReturn(true);
        when(approvals.get(tenant, id)).thenThrow(new IllegalArgumentException("Missing"));
        assertThrows(IllegalArgumentException.class, () -> service.execute(tenant, id, input));
        verifyNoInteractions(gateway);
    }
    @Test void providerFailureUpdatesBothLifecyclesWithoutRawError() {
        when(gateway.execute(any(), any(), any(), any())).thenThrow(new IllegalStateException("private provider details"));
        var error = assertThrows(IllegalStateException.class, () -> service.execute(tenant, id, input));
        verify(foundation).fail(tenant, id, "AI_EXECUTION_OR_OUTPUT_STORAGE_FAILED");
        verify(audit).fail(tenant, id);
        verifyNoInteractions(outputs);
        assertNull(error.getCause());
    }
    @Test void failedClaimCommitNeverDispatches() {
        doThrow(new org.springframework.dao.OptimisticLockingFailureException("Concurrent claim"))
                .when(transactions).commit(any());
        assertThrows(RuntimeException.class, () -> service.execute(tenant, id, input));
        verifyNoInteractions(gateway, outputs);
    }
}
