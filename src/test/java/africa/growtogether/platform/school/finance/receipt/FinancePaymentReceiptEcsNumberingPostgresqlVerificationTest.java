package africa.growtogether.platform.school.finance.receipt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import africa.growtogether.platform.ecs.ConfigurationCryptoService;
import africa.growtogether.platform.ecs.ConfigurationDataType;
import africa.growtogether.platform.ecs.ConfigurationDefinition;
import africa.growtogether.platform.ecs.ConfigurationDefinitionRepository;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolveRequest;
import africa.growtogether.platform.ecs.ConfigurationDtos.ResolvedValue;
import africa.growtogether.platform.ecs.ConfigurationScope;
import africa.growtogether.platform.ecs.ConfigurationService;
import africa.growtogether.platform.ecs.ConfigurationValue;
import africa.growtogether.platform.ecs.ConfigurationValueRepository;
import africa.growtogether.platform.ecs.ConfigurationVersionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FinancePaymentReceiptEcsNumberingPostgresqlVerificationTest {

    private static final String CONFIG_CODE =
            "GT_SCHOOL_FINANCE_RECEIPT_NUMBER_FORMAT";

    private static final UUID TENANT_A =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final UUID TENANT_B =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            );

    private Connection connection;
    private JdbcTemplate jdbc;

    @BeforeAll
    void migrateFreshDatabase() throws Exception {

        postgres.start();

        Flyway flyway =
                Flyway.configure()
                        .dataSource(
                                postgres.getJdbcUrl(),
                                postgres.getUsername(),
                                postgres.getPassword()
                        )
                        .locations(
                                "filesystem:src/main/resources/db/migration"
                        )
                        .cleanDisabled(true)
                        .load();

        var result =
                flyway.migrate();

        assertTrue(
                result.success
        );

        connection =
                java.sql.DriverManager.getConnection(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                );

        /*
         * Number sequence carries a tenant FK.
         *
         * This verification uses fixed synthetic tenant UUIDs and
         * deliberately does not create application tenant fixtures.
         * The isolated connection therefore disables FK triggers only
         * while exercising the sequence component.
         */
        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                "SET session_replication_role = replica"
                        )
        ) {
            ps.execute();
        }

        jdbc =
                new JdbcTemplate(
                        new SingleConnectionDataSource(
                                connection,
                                true
                        )
                );
    }

    @AfterAll
    void shutdown() throws Exception {

        if (connection != null) {

            try (
                    PreparedStatement ps =
                            connection.prepareStatement(
                                    "SET session_replication_role = origin"
                            )
            ) {
                ps.execute();
            }

            connection.close();
        }

        postgres.stop();
    }

    @Test
    void v280RegistersReceiptNumberFormatDefinition() throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                SELECT
                                    code,
                                    data_type,
                                    default_value,
                                    allowed_scopes,
                                    required,
                                    secret_value,
                                    active
                                FROM ecs_configuration_definitions
                                WHERE code = ?
                                """
                        )
        ) {

            ps.setString(
                    1,
                    CONFIG_CODE
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next()
                );

                assertEquals(
                        CONFIG_CODE,
                        rs.getString(
                                "code"
                        )
                );

                assertEquals(
                        "STRING",
                        rs.getString(
                                "data_type"
                        )
                );

                assertEquals(
                        "RCT-{sequence:10}",
                        rs.getString(
                                "default_value"
                        )
                );

                assertArrayEquals(
                        new String[]{
                                "PLATFORM",
                                "TENANT"
                        },
                        (String[]) rs.getArray(
                                "allowed_scopes"
                        ).getArray()
                );

                assertTrue(
                        rs.getBoolean(
                                "required"
                        )
                );

                assertFalse(
                        rs.getBoolean(
                                "secret_value"
                        )
                );

                assertTrue(
                        rs.getBoolean(
                                "active"
                        )
                );

                assertFalse(
                        rs.next()
                );
            }
        }
    }

    @Test
    void ecsServiceResolvesDefinitionDefaultWhenNoScopedValueExists() {

        ConfigurationDefinition definition =
                definition();

        ConfigurationDefinitionRepository definitions =
                mock(
                        ConfigurationDefinitionRepository.class
                );

        ConfigurationValueRepository values =
                mock(
                        ConfigurationValueRepository.class
                );

        ConfigurationVersionRepository versions =
                mock(
                        ConfigurationVersionRepository.class
                );

        ConfigurationCryptoService crypto =
                mock(
                        ConfigurationCryptoService.class
                );

        when(
                definitions.findByCodeIgnoreCase(
                        CONFIG_CODE
                )
        ).thenReturn(
                Optional.of(
                        definition
                )
        );

        when(
                values.candidates(
                        any(),
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(
                List.of()
        );

        ConfigurationService service =
                new ConfigurationService(
                        definitions,
                        values,
                        versions,
                        crypto
                );

        ResolvedValue resolved =
                service.resolve(
                        new ResolveRequest(
                                CONFIG_CODE,
                                null,
                                null,
                                TENANT_A
                        )
                );

        assertEquals(
                CONFIG_CODE,
                resolved.code()
        );

        assertEquals(
                ConfigurationDataType.STRING,
                resolved.dataType()
        );

        assertEquals(
                "RCT-{sequence:10}",
                resolved.value()
        );

        assertEquals(
                null,
                resolved.sourceScope()
        );

        assertFalse(
                resolved.secret()
        );
    }

    @Test
    void ecsServiceTenantOverrideWinsOverPlatformValue() {

        ConfigurationDefinition definition =
                definition();

        ConfigurationValue platform =
                new ConfigurationValue(
                        definition,
                        ConfigurationScope.PLATFORM,
                        null,
                        null,
                        null
                );

        platform.writePlain(
                "RCT-{sequence:10}",
                "platform default",
                "platform-hash"
        );

        ConfigurationValue tenant =
                new ConfigurationValue(
                        definition,
                        ConfigurationScope.TENANT,
                        null,
                        null,
                        TENANT_A
                );

        tenant.writePlain(
                "PPIS-{sequence:6}",
                "tenant override",
                "tenant-hash"
        );

        ConfigurationDefinitionRepository definitions =
                mock(
                        ConfigurationDefinitionRepository.class
                );

        ConfigurationValueRepository values =
                mock(
                        ConfigurationValueRepository.class
                );

        ConfigurationVersionRepository versions =
                mock(
                        ConfigurationVersionRepository.class
                );

        ConfigurationCryptoService crypto =
                mock(
                        ConfigurationCryptoService.class
                );

        when(
                definitions.findByCodeIgnoreCase(
                        CONFIG_CODE
                )
        ).thenReturn(
                Optional.of(
                        definition
                )
        );

        when(
                values.candidates(
                        any(),
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(
                List.of(
                        platform,
                        tenant
                )
        );

        ConfigurationService service =
                new ConfigurationService(
                        definitions,
                        values,
                        versions,
                        crypto
                );

        ResolvedValue resolved =
                service.resolve(
                        new ResolveRequest(
                                CONFIG_CODE,
                                null,
                                null,
                                TENANT_A
                        )
                );

        assertEquals(
                "PPIS-{sequence:6}",
                resolved.value()
        );

        assertEquals(
                ConfigurationScope.TENANT,
                resolved.sourceScope()
        );
    }

    @Test
    void tenantOverrideCanBePersistedInEcsValueTable() throws Exception {

        UUID definitionId =
                definitionId();

        UUID valueId =
                UUID.randomUUID();

        String hash =
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                INSERT INTO ecs_configuration_values (
                                    id,
                                    definition_id,
                                    scope,
                                    tenant_id,
                                    stored_value,
                                    encrypted,
                                    value_hash,
                                    change_reason,
                                    active,
                                    version,
                                    created_at,
                                    updated_at
                                )
                                VALUES (
                                    ?,
                                    ?,
                                    'TENANT',
                                    ?,
                                    ?,
                                    FALSE,
                                    ?,
                                    ?,
                                    TRUE,
                                    0,
                                    CURRENT_TIMESTAMP,
                                    CURRENT_TIMESTAMP
                                )
                                """
                        )
        ) {

            ps.setObject(
                    1,
                    valueId
            );

            ps.setObject(
                    2,
                    definitionId
            );

            ps.setObject(
                    3,
                    TENANT_B
            );

            ps.setString(
                    4,
                    "WITS-{sequence:8}"
            );

            ps.setString(
                    5,
                    hash
            );

            ps.setString(
                    6,
                    "S4 verification tenant override"
            );

            assertEquals(
                    1,
                    ps.executeUpdate()
            );
        }

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                SELECT
                                    scope,
                                    tenant_id,
                                    stored_value,
                                    active
                                FROM ecs_configuration_values
                                WHERE id = ?
                                """
                        )
        ) {

            ps.setObject(
                    1,
                    valueId
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next()
                );

                assertEquals(
                        "TENANT",
                        rs.getString(
                                "scope"
                        )
                );

                assertEquals(
                        TENANT_B,
                        rs.getObject(
                                "tenant_id",
                                UUID.class
                        )
                );

                assertEquals(
                        "WITS-{sequence:8}",
                        rs.getString(
                                "stored_value"
                        )
                );

                assertTrue(
                        rs.getBoolean(
                                "active"
                        )
                );
            }
        }
    }

    @Test
    void receiptNumberServiceUsesResolvedFormatsAndTenantScopedSequence() {

        ConfigurationService configuration =
                Mockito.mock(
                        ConfigurationService.class
                );

        when(
                configuration.resolve(
                        any(ResolveRequest.class)
                )
        ).thenAnswer(
                invocation -> {

                    ResolveRequest request =
                            invocation.getArgument(
                                    0
                            );

                    if (
                            TENANT_B.equals(
                                    request.tenantId()
                            )
                    ) {

                        return new ResolvedValue(
                                CONFIG_CODE,
                                ConfigurationDataType.STRING,
                                "WITS-{sequence:8}",
                                ConfigurationScope.TENANT,
                                0L,
                                false
                        );
                    }

                    return new ResolvedValue(
                            CONFIG_CODE,
                            ConfigurationDataType.STRING,
                            "RCT-{sequence:10}",
                            null,
                            0L,
                            false
                    );
                }
        );

        FinancePaymentReceiptNumberService numbers =
                new FinancePaymentReceiptNumberService(
                        jdbc,
                        configuration
                );

        assertEquals(
                "RCT-0000000001",
                numbers.next(
                        TENANT_A
                )
        );

        assertEquals(
                "RCT-0000000002",
                numbers.next(
                        TENANT_A
                )
        );

        assertEquals(
                "WITS-00000001",
                numbers.next(
                        TENANT_B
                )
        );

        assertEquals(
                "WITS-00000002",
                numbers.next(
                        TENANT_B
                )
        );

        Long tenantASequence =
                jdbc.queryForObject(
                        """
                        SELECT last_issued_number
                        FROM gts_payment_receipt_number_sequence
                        WHERE tenant_id = ?
                        """,
                        Long.class,
                        TENANT_A
                );

        Long tenantBSequence =
                jdbc.queryForObject(
                        """
                        SELECT last_issued_number
                        FROM gts_payment_receipt_number_sequence
                        WHERE tenant_id = ?
                        """,
                        Long.class,
                        TENANT_B
                );

        assertEquals(
                2L,
                tenantASequence
        );

        assertEquals(
                2L,
                tenantBSequence
        );
    }

    private UUID definitionId() throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                SELECT id
                                FROM ecs_configuration_definitions
                                WHERE code = ?
                                """
                        )
        ) {

            ps.setString(
                    1,
                    CONFIG_CODE
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next()
                );

                UUID id =
                        rs.getObject(
                                1,
                                UUID.class
                        );

                assertNotNull(
                        id
                );

                return id;
            }
        }
    }

    private static ConfigurationDefinition definition() {

        return new ConfigurationDefinition(
                CONFIG_CODE,
                "GT School Finance Receipt Number Format",
                "SCHOOL_FINANCE",
                "Receipt number format",
                ConfigurationDataType.STRING,
                "RCT-{sequence:10}",
                "{}",
                Set.of(
                        ConfigurationScope.PLATFORM,
                        ConfigurationScope.TENANT
                ),
                true,
                false
        );
    }
}
