package africa.growtogether.platform.eaif;

import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eaif.approval.*;
import africa.growtogether.platform.eaif.audit.*;
import africa.growtogether.platform.eaif.execution.AiTextRequest;
import africa.growtogether.platform.eaif.governance.policy.*;
import africa.growtogether.platform.eaif.integration.EaifPlatformIntegrationGateway;
import africa.growtogether.platform.eip.AiProviderExecutionGateway;
import africa.growtogether.platform.school.integration.teacher.TeacherAIWorkflowService;
import africa.growtogether.platform.school.integration.teacher.TeacherAiAccessGuard;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Real teacher submission proxy, EAIF lifecycle and PostgreSQL persistence.
 * Ownership, policy decisions and outbound events are test doubles.
 * No provider execution or live credentials.
 */
@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TeacherAiSubmissionPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("gt_teacher_submission_test")
                    .withUsername("gt_test")
                    .withPassword("gt_test");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.data.redis.repositories.enabled", () -> "false");
    }

    @Autowired TeacherAIWorkflowService workflow;
    @Autowired AiModelRepository models;
    @Autowired AiRequestRepository requests;
    @Autowired EaifApprovalService approvals;
    @Autowired EaifAuditService audits;
    @Autowired PlatformTransactionManager transactionManager;

    @MockitoBean TeacherAiAccessGuard ownership;
    @MockitoBean AiGovernancePolicyService governance;
    @MockitoBean EaifPlatformIntegrationGateway outboundEvents;
    @MockitoBean AiProviderExecutionGateway provider;

    UUID tenant;
    UUID actor;
    UUID teacher;
    UUID assignment;
    static final String MODEL = "TEACHER_SUBMISSION_TEST";
    static final String INPUT = "Synthetic teaching evidence for transaction testing";

    @BeforeEach
    void setup() {
        tenant = UUID.randomUUID();
        actor = UUID.randomUUID();
        teacher = UUID.randomUUID();
        assignment = UUID.randomUUID();

        var principal = new GtPrincipal(
                actor, "submission-test", tenant, Set.of(),
                Set.of("ai.request.create"), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        RequestContextHolder.set(
                new RequestContext("submission-test", tenant.toString()));

        models.saveAndFlush(new AiModel(
                tenant, MODEL, "TEST_ONLY", "test-model", AiEnums.Capability.CHAT));

        when(governance.evaluate(
                tenant, "DEFAULT_AI_POLICY", AiEnums.RiskLevel.HIGH))
                .thenReturn(new AiGovernanceDecision(
                        "DEFAULT_AI_POLICY", AiEnums.RiskLevel.HIGH,
                        true, true, "Synthetic approval-required policy"));
        when(governance.requiresApproval(tenant, "DEFAULT_AI_POLICY"))
                .thenReturn(true);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.clear();
    }

    @Test
    void submissionSurvivesCallingTransactionRollback() {
        var submittedId = new AtomicReference<UUID>();
        var outer = new TransactionTemplate(transactionManager);

        outer.executeWithoutResult(status -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());

            var submitted = workflow.submit(
                    tenant, teacher, assignment, MODEL, INPUT);
            submittedId.set(submitted.requestId());
            assertEquals(AiEnums.RequestStatus.RECEIVED, submitted.status());

            // A separate transaction must see committed submission records
            // before the caller's transaction ends.
            var independent = new TransactionTemplate(transactionManager);
            independent.setPropagationBehavior(
                    TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            independent.executeWithoutResult(
                    ignored -> assertPersistedSubmission(submitted.requestId()));

            status.setRollbackOnly();
        });

        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        assertNotNull(submittedId.get());
        assertPersistedSubmission(submittedId.get());
        verify(ownership).requireOwnedAssignment(tenant, teacher, assignment);
        verifyNoInteractions(provider);
    }

    @Test
    void lateSubmissionFailureRollsBackRequestApprovalAndAudit() {
        var attemptedId = new AtomicReference<UUID>();
        var injectedFailure = new IllegalStateException(
                "Synthetic outbound-event failure");

        doAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            attemptedId.set(id);

            // Flush all real persistence work before injecting the failure.
            requests.flush();
            assertPersistedSubmission(id);
            throw injectedFailure;
        }).when(outboundEvents).publishAnalytics(
                any(UUID.class), anyString(), anyString(), anyString());

        var actual = assertThrows(IllegalStateException.class,
                () -> workflow.submit(tenant, teacher, assignment, MODEL, INPUT));
        assertSame(injectedFailure, actual);

        UUID id = attemptedId.get();
        assertNotNull(id, "Submission must reach the late failure point");
        assertTrue(requests.findByIdAndTenantId(id, tenant).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> approvals.get(tenant, id));
        assertThrows(IllegalArgumentException.class, () -> audits.get(tenant, id));
        verifyNoInteractions(provider);
    }

    private void assertPersistedSubmission(UUID id) {
        var request = requests.findByIdAndTenantId(id, tenant).orElseThrow();
        assertEquals(AiEnums.RequestStatus.RECEIVED, request.requestStatus());
        assertEquals("GT_SCHOOL_TEACHER", request.sourceService());
        assertEquals("TEACHER_ASSISTANCE", request.useCase());
        assertEquals(AiEnums.RiskLevel.HIGH, request.riskLevel());
        assertEquals(AiTextRequest.hash(INPUT), request.inputHash());
        assertEquals(AiTextRequest.hash(
                tenant + ":" + actor + ":" + teacher + ":" + assignment),
                request.correlationId());

        var approval = approvals.get(tenant, id);
        assertEquals(id, approval.aiRequestId());
        assertEquals(ApprovalStatus.PENDING, approval.approvalStatus());

        var audit = audits.get(tenant, id);
        assertEquals(id, audit.aiRequestId());
        assertEquals(ExecutionStatus.RECEIVED, audit.executionStatus());
        assertEquals("DEFAULT_AI_POLICY", audit.governancePolicyCode());
        assertEquals("APPROVAL_REQUIRED", audit.governanceDecision());
        assertEquals(MODEL, audit.modelCode());
        assertNull(audit.outputReference());
    }
}
