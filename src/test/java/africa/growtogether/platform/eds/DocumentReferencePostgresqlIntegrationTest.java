package africa.growtogether.platform.eds;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DocumentReferencePostgresqlIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            );

    private static JdbcTemplate jdbc;

    @BeforeAll
    static void migrateCommittedSchema() {

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                );

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();

        jdbc =
                new JdbcTemplate(
                        dataSource
                );
    }

    @Test
    void v207MigrationIsAppliedAndReferenceTableExists() {

        Integer applied =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                          FROM flyway_schema_history
                         WHERE version = '207'
                           AND success = TRUE
                        """,
                        Integer.class
                );

        assertThat(applied)
                .isEqualTo(1);

        String table =
                jdbc.queryForObject(
                        """
                        SELECT to_regclass(
                            'public.eds_document_references'
                        )::text
                        """,
                        String.class
                );

        assertThat(table)
                .isEqualTo(
                        "eds_document_references"
                );
    }

    @Test
    void referenceIdentityColumnsArePresentAndNotNullable() {

        List<String> columns =
                jdbc.queryForList(
                        """
                        SELECT column_name
                          FROM information_schema.columns
                         WHERE table_schema = 'public'
                           AND table_name =
                               'eds_document_references'
                           AND is_nullable = 'NO'
                        """,
                        String.class
                );

        assertThat(columns)
                .contains(
                        "id",
                        "tenant_id",
                        "document_id",
                        "reference_type",
                        "reference_id"
                );
    }

    @Test
    void referenceUniquenessIsEnforcedByCommittedSchema() {

        String definition =
                jdbc.queryForObject(
                        """
                        SELECT pg_get_constraintdef(c.oid)
                          FROM pg_constraint c
                          JOIN pg_class t
                            ON t.oid = c.conrelid
                          JOIN pg_namespace n
                            ON n.oid = t.relnamespace
                         WHERE n.nspname = 'public'
                           AND t.relname =
                               'eds_document_references'
                           AND c.conname =
                               'uk_eds_document_reference'
                        """,
                        String.class
                );

        assertThat(definition)
                .isNotNull();

        String normalized =
                definition
                        .toLowerCase()
                        .replace("\"", "");

        assertThat(normalized)
                .contains("unique")
                .contains("tenant_id")
                .contains("document_id")
                .contains("reference_type")
                .contains("reference_id");
    }

    @Test
    void referenceDocumentForeignKeyTargetsEnterpriseDocument() {

        List<String> definitions =
                jdbc.queryForList(
                        """
                        SELECT pg_get_constraintdef(c.oid)
                          FROM pg_constraint c
                          JOIN pg_class t
                            ON t.oid = c.conrelid
                          JOIN pg_namespace n
                            ON n.oid = t.relnamespace
                         WHERE n.nspname = 'public'
                           AND t.relname =
                               'eds_document_references'
                           AND c.contype = 'f'
                        """,
                        String.class
                );

        assertThat(definitions)
                .anySatisfy(
                        definition -> {

                            String normalized =
                                    definition
                                            .toLowerCase()
                                            .replace("\"", "");

                            assertThat(normalized)
                                    .contains("document_id")
                                    .contains(
                                            "references eds_documents"
                                    );
                        }
                );
    }

    @Test
    void referenceLookupHasTenantScopedDatabaseIndex() {

        List<String> indexDefinitions =
                jdbc.queryForList(
                        """
                        SELECT indexdef
                          FROM pg_indexes
                         WHERE schemaname = 'public'
                           AND tablename =
                               'eds_document_references'
                        """,
                        String.class
                );

        assertThat(indexDefinitions)
                .anySatisfy(
                        definition -> {

                            String normalized =
                                    definition
                                            .toLowerCase()
                                            .replace("\"", "")
                                            .replaceAll(
                                                    "\\s+",
                                                    " "
                                            );

                            assertThat(normalized)
                                    .contains("tenant_id")
                                    .contains("reference_type")
                                    .contains("reference_id");
                        }
                );
    }
}
