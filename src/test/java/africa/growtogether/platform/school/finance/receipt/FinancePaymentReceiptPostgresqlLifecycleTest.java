package africa.growtogether.platform.school.finance.receipt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FinancePaymentReceiptPostgresqlLifecycleTest {

    private final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            );

    private Connection connection;

    private final UUID tenantA =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private final UUID tenantB =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private final UUID studentA =
            UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            );

    private final UUID paymentA =
            UUID.fromString(
                    "44444444-4444-4444-4444-444444444444"
            );

    private final UUID receiptA =
            UUID.fromString(
                    "55555555-5555-5555-5555-555555555555"
            );

    private final UUID actor =
            UUID.fromString(
                    "66666666-6666-6666-6666-666666666666"
            );

    @BeforeAll
    void migrateFreshPostgresql17() throws Exception {

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

        assertEquals(
                17,
                databaseMajorVersion()
        );

        assertTrue(
                tableExists(
                        "gts_payment_receipt"
                )
        );

        assertTrue(
                tableExists(
                        "gts_payment_receipt_number_sequence"
                )
        );

        assertTrue(
                flywayVersionSucceeded(
                        "280"
                )
        );
    }

    @AfterAll
    void stopPostgresql() throws Exception {

        if (connection != null) {
            connection.close();
        }

        postgres.stop();
    }

    @BeforeEach
    void resetReceiptFixtures() throws Exception {

        execute(
                """
                TRUNCATE TABLE
                    gts_finance_history,
                    gts_payment_receipt,
                    gts_payment_receipt_number_sequence,
                    gts_student_payment
                CASCADE
                """
        );

        /*
         * Test fixture insertion deliberately disables FK trigger
         * enforcement only while creating minimal upstream payment
         * rows. Unique and CHECK constraints remain active.
         *
         * The receipt/payment FK metadata is separately asserted.
         */
        execute(
                "SET session_replication_role = replica"
        );

        insertPaymentFixture(
                tenantA,
                studentA,
                paymentA,
                "PAY-001",
                new BigDecimal("150000.00")
        );

        execute(
                "SET session_replication_role = origin"
        );
    }

    @Test
    void candidateV280MigratesOnFreshPostgresql17() throws Exception {

        assertEquals(
                17,
                databaseMajorVersion()
        );

        assertTrue(
                tableExists(
                        "gts_payment_receipt_number_sequence"
                )
        );

        assertTrue(
                flywayVersionSucceeded(
                        "280"
                )
        );
    }

    @Test
    void receiptNumberSequenceIsAtomicAndTenantScoped() throws Exception {

        execute(
                "SET session_replication_role = replica"
        );

        long a1 =
                nextSequence(
                        tenantA
                );

        long a2 =
                nextSequence(
                        tenantA
                );

        long b1 =
                nextSequence(
                        tenantB
                );

        execute(
                "SET session_replication_role = origin"
        );

        assertEquals(
                1L,
                a1
        );

        assertEquals(
                2L,
                a2
        );

        assertEquals(
                1L,
                b1
        );
    }

    @Test
    void receiptPersistsAuthoritativePaymentSnapshot() throws Exception {

        execute(
                "SET session_replication_role = replica"
        );

        insertReceipt(
                tenantA,
                studentA,
                paymentA,
                receiptA,
                "RCT-0000000001",
                new BigDecimal("150000.00")
        );

        execute(
                "SET session_replication_role = origin"
        );

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                SELECT
                                    r.receipt_number,
                                    r.receipt_date,
                                    r.currency_code,
                                    r.receipt_amount,
                                    r.student_payment_id,
                                    p.payment_reference,
                                    p.currency_code,
                                    p.payment_amount
                                FROM gts_payment_receipt r
                                JOIN gts_student_payment p
                                  ON p.id = r.student_payment_id
                                WHERE r.tenant_id = ?
                                  AND r.id = ?
                                """
                        )
        ) {

            ps.setObject(
                    1,
                    tenantA
            );

            ps.setObject(
                    2,
                    receiptA
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next()
                );

                assertEquals(
                        "RCT-0000000001",
                        rs.getString(
                                "receipt_number"
                        )
                );

                assertEquals(
                        "PAY-001",
                        rs.getString(
                                "payment_reference"
                        )
                );

                assertEquals(
                        "UGX",
                        rs.getString(
                                "currency_code"
                        )
                );

                assertEquals(
                        0,
                        rs.getBigDecimal(
                                "receipt_amount"
                        ).compareTo(
                                rs.getBigDecimal(
                                        "payment_amount"
                                )
                        )
                );

                assertEquals(
                        paymentA,
                        rs.getObject(
                                "student_payment_id",
                                UUID.class
                        )
                );
            }
        }
    }

    @Test
    void samePaymentCannotReceiveSecondReceipt() throws Exception {

        execute(
                "SET session_replication_role = replica"
        );

        insertReceipt(
                tenantA,
                studentA,
                paymentA,
                receiptA,
                "RCT-0000000001",
                new BigDecimal("150000.00")
        );

        assertThrows(
                SQLException.class,
                () ->
                        insertReceipt(
                                tenantA,
                                studentA,
                                paymentA,
                                UUID.randomUUID(),
                                "RCT-0000000002",
                                new BigDecimal("150000.00")
                        )
        );

        rollbackIfNeeded();

        execute(
                "SET session_replication_role = origin"
        );
    }

    @Test
    void receiptNumberIsUniqueWithinTenant() throws Exception {

        execute(
                "SET session_replication_role = replica"
        );

        insertReceipt(
                tenantA,
                studentA,
                paymentA,
                receiptA,
                "RCT-0000000001",
                new BigDecimal("150000.00")
        );

        UUID secondPayment =
                UUID.randomUUID();

        insertPaymentFixture(
                tenantA,
                studentA,
                secondPayment,
                "PAY-002",
                new BigDecimal("200000.00")
        );

        assertThrows(
                SQLException.class,
                () ->
                        insertReceipt(
                                tenantA,
                                studentA,
                                secondPayment,
                                UUID.randomUUID(),
                                "RCT-0000000001",
                                new BigDecimal("200000.00")
                        )
        );

        rollbackIfNeeded();

        execute(
                "SET session_replication_role = origin"
        );
    }

    @Test
    void sameReceiptNumberMayExistInDifferentTenant() throws Exception {

        execute(
                "SET session_replication_role = replica"
        );

        insertReceipt(
                tenantA,
                studentA,
                paymentA,
                receiptA,
                "RCT-0000000001",
                new BigDecimal("150000.00")
        );

        UUID paymentB =
                UUID.randomUUID();

        UUID studentB =
                UUID.randomUUID();

        insertPaymentFixture(
                tenantB,
                studentB,
                paymentB,
                "PAY-B-001",
                new BigDecimal("90000.00")
        );

        insertReceipt(
                tenantB,
                studentB,
                paymentB,
                UUID.randomUUID(),
                "RCT-0000000001",
                new BigDecimal("90000.00")
        );

        execute(
                "SET session_replication_role = origin"
        );

        assertEquals(
                2L,
                count(
                        """
                        SELECT COUNT(*)
                        FROM gts_payment_receipt
                        WHERE receipt_number = 'RCT-0000000001'
                        """
                )
        );
    }

    @Test
    void receiptAmountMustRemainPositive() throws Exception {

        execute(
                "SET session_replication_role = replica"
        );

        assertThrows(
                SQLException.class,
                () ->
                        insertReceipt(
                                tenantA,
                                studentA,
                                paymentA,
                                receiptA,
                                "RCT-0000000001",
                                BigDecimal.ZERO
                        )
        );

        rollbackIfNeeded();

        execute(
                "SET session_replication_role = origin"
        );
    }

    @Test
    void invalidReceiptLifecycleStatusIsRejected() throws Exception {

        execute(
                "SET session_replication_role = replica"
        );

        assertThrows(
                SQLException.class,
                () ->
                        insertReceiptWithStatus(
                                tenantA,
                                studentA,
                                paymentA,
                                receiptA,
                                "RCT-0000000001",
                                new BigDecimal("150000.00"),
                                "INVALID"
                        )
        );

        rollbackIfNeeded();

        execute(
                "SET session_replication_role = origin"
        );
    }

    @Test
    void edsDocumentLinkCanBeAttachedOnlyWhenPreviouslyNull()
            throws Exception {

        execute(
                "SET session_replication_role = replica"
        );

        insertReceipt(
                tenantA,
                studentA,
                paymentA,
                receiptA,
                "RCT-0000000001",
                new BigDecimal("150000.00")
        );

        execute(
                "SET session_replication_role = origin"
        );

        UUID document1 =
                UUID.randomUUID();

        UUID document2 =
                UUID.randomUUID();

        int first =
                update(
                        """
                        UPDATE gts_payment_receipt
                           SET eds_receipt_document_id = ?,
                               updated_at = ?,
                               updated_by = ?
                         WHERE tenant_id = ?
                           AND id = ?
                           AND status = 'ACTIVE'
                           AND eds_receipt_document_id IS NULL
                        """,
                        document1,
                        Timestamp.from(
                                Instant.now()
                        ),
                        actor.toString(),
                        tenantA,
                        receiptA
                );

        int second =
                update(
                        """
                        UPDATE gts_payment_receipt
                           SET eds_receipt_document_id = ?,
                               updated_at = ?,
                               updated_by = ?
                         WHERE tenant_id = ?
                           AND id = ?
                           AND status = 'ACTIVE'
                           AND eds_receipt_document_id IS NULL
                        """,
                        document2,
                        Timestamp.from(
                                Instant.now()
                        ),
                        actor.toString(),
                        tenantA,
                        receiptA
                );

        assertEquals(
                1,
                first
        );

        assertEquals(
                0,
                second
        );

        assertEquals(
                document1,
                scalarUuid(
                        """
                        SELECT eds_receipt_document_id
                        FROM gts_payment_receipt
                        WHERE id = ?
                        """,
                        receiptA
                )
        );
    }

    @Test
    void receiptIssuedHistoryPersistsSeparately() throws Exception {

        execute(
                "SET session_replication_role = replica"
        );

        insertReceipt(
                tenantA,
                studentA,
                paymentA,
                receiptA,
                "RCT-0000000001",
                new BigDecimal("150000.00")
        );

        insertHistory(
                tenantA,
                receiptA
        );

        execute(
                "SET session_replication_role = origin"
        );

        assertEquals(
                1L,
                count(
                        """
                        SELECT COUNT(*)
                        FROM gts_finance_history
                        WHERE tenant_id = '11111111-1111-1111-1111-111111111111'
                          AND entity_id = '55555555-5555-5555-5555-555555555555'
                          AND entity_type = 'RECEIPT'
                          AND event_type = 'RECEIPT_ISSUED'
                        """
                )
        );
    }

    @Test
    void receiptForeignKeysRemainDeclared() throws Exception {

        assertTrue(
                foreignKeyExists(
                        "gts_payment_receipt",
                        "student_payment_id",
                        "gts_student_payment"
                )
        );

        assertTrue(
                foreignKeyExists(
                        "gts_payment_receipt",
                        "student_id",
                        "gts_student"
                )
        );
    }

    private int databaseMajorVersion() throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                "SHOW server_version_num"
                        );
                ResultSet rs =
                        ps.executeQuery()
        ) {

            assertTrue(
                    rs.next()
            );

            int value =
                    Integer.parseInt(
                            rs.getString(1)
                    );

            return value / 10000;
        }
    }

    private boolean flywayVersionSucceeded(
            String version
    ) throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                SELECT success
                                FROM flyway_schema_history
                                WHERE version = ?
                                ORDER BY installed_rank DESC
                                LIMIT 1
                                """
                        )
        ) {

            ps.setString(
                    1,
                    version
            );

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        && rs.getBoolean(1);
            }
        }
    }

    private boolean tableExists(
            String table
    ) throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                SELECT EXISTS (
                                    SELECT 1
                                    FROM information_schema.tables
                                    WHERE table_schema = 'public'
                                      AND table_name = ?
                                )
                                """
                        )
        ) {

            ps.setString(
                    1,
                    table
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next()
                );

                return rs.getBoolean(1);
            }
        }
    }

    private boolean foreignKeyExists(
            String table,
            String column,
            String referencedTable
    ) throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                SELECT EXISTS (
                                    SELECT 1
                                    FROM pg_constraint c
                                    JOIN pg_class t
                                      ON t.oid = c.conrelid
                                    JOIN pg_class rt
                                      ON rt.oid = c.confrelid
                                    JOIN unnest(c.conkey)
                                      WITH ORDINALITY AS ck(attnum, ord)
                                      ON TRUE
                                    JOIN pg_attribute a
                                      ON a.attrelid = t.oid
                                     AND a.attnum = ck.attnum
                                    WHERE c.contype = 'f'
                                      AND t.relname = ?
                                      AND a.attname = ?
                                      AND rt.relname = ?
                                )
                                """
                        )
        ) {

            ps.setString(
                    1,
                    table
            );

            ps.setString(
                    2,
                    column
            );

            ps.setString(
                    3,
                    referencedTable
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next()
                );

                return rs.getBoolean(1);
            }
        }
    }

    private long nextSequence(
            UUID tenant
    ) throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                INSERT INTO gts_payment_receipt_number_sequence (
                                    tenant_id,
                                    last_issued_number,
                                    created_at,
                                    updated_at
                                )
                                VALUES (?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                                ON CONFLICT (tenant_id)
                                DO UPDATE SET
                                    last_issued_number =
                                        gts_payment_receipt_number_sequence.last_issued_number + 1,
                                    updated_at = CURRENT_TIMESTAMP
                                RETURNING last_issued_number
                                """
                        )
        ) {

            ps.setObject(
                    1,
                    tenant
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next()
                );

                return rs.getLong(1);
            }
        }
    }

    private void insertPaymentFixture(
            UUID tenant,
            UUID student,
            UUID payment,
            String reference,
            BigDecimal amount
    ) throws Exception {

        Map<String, Object> values =
                new LinkedHashMap<>();

        values.put("id", payment);
        values.put("tenant_id", tenant);
        values.put(
                "student_financial_account_id",
                UUID.randomUUID()
        );
        values.put("student_id", student);
        values.put(
                "payment_reference",
                reference
        );
        values.put(
                "payment_date",
                Date.valueOf(
                        LocalDate.of(
                                2026,
                                9,
                                20
                        )
                )
        );
        values.put(
                "currency_code",
                "UGX"
        );
        values.put(
                "payment_amount",
                amount
        );
        values.put(
                "payment_method",
                "CASH"
        );
        values.put(
                "payment_status",
                "RECEIVED"
        );
        values.put(
                "received_by",
                actorValue(
                        "gts_student_payment",
                        "received_by"
                )
        );
        values.put(
                "status",
                "ACTIVE"
        );
        values.put(
                "version",
                0L
        );
        values.put(
                "created_at",
                Timestamp.from(
                        Instant.now()
                )
        );
        values.put(
                "created_by",
                actor.toString()
        );
        values.put(
                "updated_at",
                Timestamp.from(
                        Instant.now()
                )
        );
        values.put(
                "updated_by",
                actor.toString()
        );

        insertKnownColumns(
                "gts_student_payment",
                values
        );
    }

    private void insertReceipt(
            UUID tenant,
            UUID student,
            UUID payment,
            UUID receipt,
            String number,
            BigDecimal amount
    ) throws Exception {

        insertReceiptWithStatus(
                tenant,
                student,
                payment,
                receipt,
                number,
                amount,
                "ISSUED"
        );
    }

    private void insertReceiptWithStatus(
            UUID tenant,
            UUID student,
            UUID payment,
            UUID receipt,
            String number,
            BigDecimal amount,
            String receiptStatus
    ) throws Exception {

        Map<String, Object> values =
                new LinkedHashMap<>();

        values.put("id", receipt);
        values.put("tenant_id", tenant);
        values.put(
                "receipt_number",
                number
        );
        values.put(
                "student_payment_id",
                payment
        );
        values.put(
                "student_id",
                student
        );
        values.put(
                "receipt_date",
                Date.valueOf(
                        LocalDate.of(
                                2026,
                                9,
                                20
                        )
                )
        );
        values.put(
                "currency_code",
                "UGX"
        );
        values.put(
                "receipt_amount",
                amount
        );
        values.put(
                "issued_at",
                Timestamp.from(
                        Instant.now()
                )
        );
        values.put(
                "issued_by",
                actorValue(
                        "gts_payment_receipt",
                        "issued_by"
                )
        );
        values.put(
                "receipt_status",
                receiptStatus
        );
        values.put(
                "status",
                "ACTIVE"
        );
        values.put(
                "version",
                0L
        );
        values.put(
                "created_at",
                Timestamp.from(
                        Instant.now()
                )
        );
        values.put(
                "created_by",
                actor.toString()
        );
        values.put(
                "updated_at",
                Timestamp.from(
                        Instant.now()
                )
        );
        values.put(
                "updated_by",
                actor.toString()
        );

        insertKnownColumns(
                "gts_payment_receipt",
                values
        );
    }

    private void insertHistory(
            UUID tenant,
            UUID receipt
    ) throws Exception {

        Map<String, Object> values =
                new LinkedHashMap<>();

        values.put(
                "id",
                UUID.randomUUID()
        );
        values.put(
                "tenant_id",
                tenant
        );
        values.put(
                "entity_type",
                "RECEIPT"
        );
        values.put(
                "entity_id",
                receipt
        );
        values.put(
                "event_type",
                "RECEIPT_ISSUED"
        );
        values.put(
                "event_description",
                "Receipt issued"
        );
        values.put(
                "event_by",
                actorValue(
                        "gts_finance_history",
                        "event_by"
                )
        );
        values.put(
                "effective_at",
                Timestamp.from(
                        Instant.now()
                )
        );
        values.put(
                "created_at",
                Timestamp.from(
                        Instant.now()
                )
        );
        values.put(
                "created_by",
                actor.toString()
        );

        insertKnownColumns(
                "gts_finance_history",
                values
        );
    }

    private void insertKnownColumns(
            String table,
            Map<String, Object> candidateValues
    ) throws Exception {

        Map<String, Object> values =
                new LinkedHashMap<>();

        for (
                Map.Entry<String, Object> entry
                : candidateValues.entrySet()
        ) {

            if (
                    columnExists(
                            table,
                            entry.getKey()
                    )
            ) {
                values.put(
                        entry.getKey(),
                        entry.getValue()
                );
            }
        }

        /*
         * Every NOT NULL/no-default column must be explicitly
         * supplied. This makes the fixture fail transparently if
         * the committed schema adds an unexpected mandatory field.
         */
        for (
                String required
                : requiredColumnsWithoutDefaults(
                        table
                )
        ) {

            if (!values.containsKey(required)) {

                throw new IllegalStateException(
                        "No fixture value for mandatory column "
                        + table
                        + "."
                        + required
                );
            }
        }

        String columns =
                String.join(
                        ", ",
                        values.keySet()
                );

        String placeholders =
                String.join(
                        ", ",
                        java.util.Collections.nCopies(
                                values.size(),
                                "?"
                        )
                );

        String sql =
                "INSERT INTO "
                + table
                + " ("
                + columns
                + ") VALUES ("
                + placeholders
                + ")";

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            int index = 1;

            for (
                    Object value
                    : values.values()
            ) {
                ps.setObject(
                        index++,
                        value
                );
            }

            ps.executeUpdate();
        }
    }

    /**
     * Evidence-fixture helper only.
     *
     * Finance schemas use UUID for some domain actor columns
     * (for example received_by / issued_by), while generic
     * created_by / updated_by audit fields are textual.
     *
     * The fixture therefore binds the actor using the actual
     * PostgreSQL column type instead of guessing.
     */
    private Object actorValue(
            String table,
            String column
    ) throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                SELECT udt_name
                                FROM information_schema.columns
                                WHERE table_schema = 'public'
                                  AND table_name = ?
                                  AND column_name = ?
                                """
                        )
        ) {

            ps.setString(
                    1,
                    table
            );

            ps.setString(
                    2,
                    column
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalStateException(
                            "Actor column not found: "
                            + table
                            + "."
                            + column
                    );
                }

                String type =
                        rs.getString(
                                1
                        );

                System.out.println(
                        "FIXTURE_ACTOR_COLUMN="
                        + table
                        + "."
                        + column
                        + "|UDT="
                        + type
                );

                if ("uuid".equalsIgnoreCase(type)) {
                    return actor;
                }

                return actor.toString();
            }
        }
    }


    private java.util.List<String>
    requiredColumnsWithoutDefaults(
            String table
    ) throws Exception {

        java.util.List<String> result =
                new java.util.ArrayList<>();

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                SELECT column_name
                                FROM information_schema.columns
                                WHERE table_schema = 'public'
                                  AND table_name = ?
                                  AND is_nullable = 'NO'
                                  AND column_default IS NULL
                                ORDER BY ordinal_position
                                """
                        )
        ) {

            ps.setString(
                    1,
                    table
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    result.add(
                            rs.getString(1)
                    );
                }
            }
        }

        return result;
    }

    private boolean columnExists(
            String table,
            String column
    ) throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                """
                                SELECT EXISTS (
                                    SELECT 1
                                    FROM information_schema.columns
                                    WHERE table_schema = 'public'
                                      AND table_name = ?
                                      AND column_name = ?
                                )
                                """
                        )
        ) {

            ps.setString(
                    1,
                    table
            );

            ps.setString(
                    2,
                    column
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next()
                );

                return rs.getBoolean(1);
            }
        }
    }

    private int update(
            String sql,
            Object... values
    ) throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            for (
                    int i = 0;
                    i < values.length;
                    i++
            ) {
                ps.setObject(
                        i + 1,
                        values[i]
                );
            }

            return ps.executeUpdate();
        }
    }

    private UUID scalarUuid(
            String sql,
            Object value
    ) throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            ps.setObject(
                    1,
                    value
            );

            try (ResultSet rs = ps.executeQuery()) {

                assertTrue(
                        rs.next()
                );

                return rs.getObject(
                        1,
                        UUID.class
                );
            }
        }
    }

    private long count(
            String sql
    ) throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                sql
                        );
                ResultSet rs =
                        ps.executeQuery()
        ) {

            assertTrue(
                    rs.next()
            );

            return rs.getLong(1);
        }
    }

    private void execute(
            String sql
    ) throws Exception {

        try (
                PreparedStatement ps =
                        connection.prepareStatement(
                                sql
                        )
        ) {
            ps.execute();
        }
    }

    private void rollbackIfNeeded()
            throws Exception {

        if (!connection.getAutoCommit()) {
            connection.rollback();
        }
    }
}
