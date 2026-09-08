package africa.growtogether.platform.eaif.execution;

import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eaif.*;
import africa.growtogether.platform.eaif.audit.*;
import africa.growtogether.platform.eaif.governance.policy.AiGovernancePolicyService;
import africa.growtogether.platform.eaif.integration.*;
import africa.growtogether.platform.eds.*;
import africa.growtogether.platform.eip.AiProviderExecutionGateway;
import africa.growtogether.platform.file.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Real PostgreSQL + GT file storage + EDS + request/audit persistence.
 * Provider transport, model selection, policy resolution and outbound events are test doubles.
 * No live credentials, provider calls or malware-scanner claims.
 */
@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AiOutputStoragePostgresIntegrationTest {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("gt_ai_storage_test").withUsername("gt_test").withPassword("gt_test");
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.data.redis.repositories.enabled", () -> "false");
    }
    @Autowired AiTextExecutionService execution;
    @Autowired AiRequestRepository requests;
    @Autowired EaifAuditService audits;
    @Autowired DocumentRepository documents;
    @Autowired DocumentLifecycleService lifecycle;
    @Autowired DocumentSecurityService security;
    @Autowired LocalFileStorageProvider storage;
    @Autowired FilePolicyProperties filePolicy;
    @MockitoBean AiProviderExecutionGateway provider;
    @MockitoBean AiExecutionCatalogue catalogue;
    @MockitoBean EaifConfigurationGateway configuration;
    @MockitoBean AiGovernancePolicyService governance;
    @MockitoBean EaifPlatformIntegrationGateway outboundEvents;
    @TempDir Path temporaryStorage;
    Path originalRoot;
    List<String> originalTypes;
    long originalMax;
    UUID tenant, actor, requestId;
    final String input = "Synthetic assessment evidence only";
    final String output = "Synthetic explanation awaiting teacher review";

    @BeforeEach void setup() {
        tenant = UUID.randomUUID(); actor = UUID.randomUUID();
        authenticate(tenant, Set.of("ai.runtime.execute", "document.restricted.read"));
        originalRoot = (Path) ReflectionTestUtils.getField(storage, "root");
        // Confine the real LocalFileStorageProvider to JUnit-owned storage without changing production code.
        ReflectionTestUtils.setField(storage, "root", temporaryStorage);
        originalTypes = new ArrayList<>(filePolicy.getAllowedTypes());
        originalMax = filePolicy.getMaxSizeBytes();
        when(configuration.providerExecutionEnabled()).thenReturn(true);
        when(configuration.maximumInputCharacters()).thenReturn(100000);
        when(governance.allows(tenant, "DEFAULT_AI_POLICY", AiEnums.RiskLevel.LOW)).thenReturn(true);
        when(catalogue.resolve(tenant, "STORAGE_TEST")).thenReturn(new AiExecutionCatalogue.Selection(
                "TEST_ONLY", AiEnums.ProviderType.OPENAI_COMPATIBLE, "test-model", 100));
        when(provider.execute(eq(tenant), eq("TEST_ONLY"), any(), any()))
                .thenReturn(new AiTextResult("resp_synthetic", output));
        var request = new AiRequest(tenant, "GT_TEST", "OUTPUT_STORAGE_TEST", "STORAGE_TEST",
                AiTextRequest.hash(input), AiEnums.RiskLevel.LOW, "storage-test");
        request.approve();
        requestId = requests.saveAndFlush(request).getId();
        audits.create(tenant, requestId, "GT_TEST", "STORAGE_TEST", null, AiEnums.RiskLevel.LOW,
                actor, "DEFAULT_AI_POLICY", "ALLOWED", "Synthetic storage integration fixture");
    }
    @AfterEach void cleanup() {
        if (originalRoot != null) ReflectionTestUtils.setField(storage, "root", originalRoot);
        if (originalTypes != null) filePolicy.setAllowedTypes(originalTypes);
        filePolicy.setMaxSizeBytes(originalMax);
        SecurityContextHolder.clearContext();
        RequestContextHolder.clear();
    }
    private void authenticate(UUID activeTenant, Set<String> permissions) {
        var principal = new GtPrincipal(actor, "storage-test", activeTenant, Set.of(), permissions, UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        RequestContextHolder.set(new RequestContext("storage-test", activeTenant.toString()));
    }
    @Test void persistsBytesRestrictedDocumentAndMatchingLifecycleReferences() throws Exception {
        // Intentionally use actual application.yml binding: missing text/plain must fail this test.
        assertTrue(filePolicy.getAllowedTypes().contains("text/plain"), "Configure text/plain before this gate");

        String reference = execution.execute(tenant, requestId, input);
        assertTrue(reference.startsWith("eds:"));
        UUID documentId = UUID.fromString(reference.substring(4));
        var document = documents.findByIdAndTenantId(documentId, tenant).orElseThrow();
        assertEquals(DocumentClassification.RESTRICTED, document.classification());
        assertEquals(1, document.currentVersion());
        var versions = lifecycle.versions(documentId);
        assertEquals(1, versions.size());
        var version = versions.get(0);
        assertEquals("text/plain", version.mimeType());
        Path stored = Path.of(version.storageKey()).toAbsolutePath().normalize();
        assertTrue(stored.startsWith(temporaryStorage.toAbsolutePath()));
        byte[] bytes = Files.readAllBytes(stored);
        String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(content.contains(output));
        assertTrue(content.contains(requestId.toString()));
        assertEquals(bytes.length, version.sizeBytes());
        assertEquals(AiTextRequest.hash(content), version.checksum());
        var saved = requests.findByIdAndTenantId(requestId, tenant).orElseThrow();
        assertEquals(AiEnums.RequestStatus.SUCCEEDED, saved.requestStatus());
        assertEquals(reference, saved.outputReference());
        var audit = audits.get(tenant, requestId);
        assertEquals(ExecutionStatus.COMPLETED, audit.executionStatus());
        assertEquals(reference, audit.outputReference());
        authenticate(tenant, Set.of("ai.runtime.execute"));
        assertThrows(SecurityException.class, () -> security.preview(documentId));
        authenticate(UUID.randomUUID(), Set.of("document.restricted.read"));
        assertThrows(NoSuchElementException.class, () -> security.preview(documentId));
    }
    @Test void disallowedMimeMarksFailureWithoutDocumentOrFile() throws Exception {
        filePolicy.setAllowedTypes(List.of("application/pdf"));
        assertThrows(IllegalStateException.class, () -> execution.execute(tenant, requestId, input));
        assertEquals(AiEnums.RequestStatus.FAILED,
                requests.findByIdAndTenantId(requestId, tenant).orElseThrow().requestStatus());
        assertEquals(ExecutionStatus.FAILED, audits.get(tenant, requestId).executionStatus());
        assertNull(requests.findByIdAndTenantId(requestId, tenant).orElseThrow().outputReference());
        assertEquals(0, documents.findAll().stream().filter(d -> tenant.equals(d.getTenantId())).count());
        try (var files = Files.walk(temporaryStorage)) { assertEquals(0, files.filter(Files::isRegularFile).count()); }
    }
    @Test void repeatedExecutionDoesNotCallProviderOrStoreAgain() throws Exception {
        execution.execute(tenant, requestId, input);
        assertThrows(IllegalStateException.class, () -> execution.execute(tenant, requestId, input));
        verify(provider, times(1)).execute(eq(tenant), eq("TEST_ONLY"), any(), any());
        assertEquals(1, documents.findAll().stream().filter(d -> tenant.equals(d.getTenantId())).count());
        try (var files = Files.walk(temporaryStorage)) { assertEquals(1, files.filter(Files::isRegularFile).count()); }
    }
}
