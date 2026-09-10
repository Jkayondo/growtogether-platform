package africa.growtogether.platform.school.progression;


import africa.growtogether.platform.school.academic.progression.LearnerProgressionHistory;
import africa.growtogether.platform.school.academic.progression.LearnerProgressionHistoryService;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;


import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class LearnerProgressionHistoryIntegrationTest {


    @Autowired
    private LearnerProgressionHistoryService service;


    @Autowired
    private JdbcTemplate jdbc;


    private UUID organizationId;

    private UUID tenantId;


    @BeforeEach
    void createFixture() {


        organizationId = UUID.randomUUID();

        tenantId = UUID.randomUUID();


        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (?, ?, ?, ?)
                """,
                organizationId,
                "PROG-HIST-" + shortId(organizationId),
                "Progression History Test Organization",
                Timestamp.from(Instant.now())
        );


        jdbc.update(
                """
                INSERT INTO eiam_tenant (
                    id,
                    organization_id,
                    code,
                    name,
                    status,
                    created_at,
                    version
                )
                VALUES (?, ?, ?, ?, 'ACTIVE', ?, 0)
                """,
                tenantId,
                organizationId,
                "PROG-HIST-" + shortId(tenantId),
                "Progression History Test Tenant",
                Timestamp.from(Instant.now())
        );

    }


    @Test
    void learnerProgressionHistoryCanBeRecordedAndRetrieved() {


        UUID learnerId =
                UUID.randomUUID();


        UUID promotionDecisionId =
                UUID.randomUUID();


        LearnerProgressionHistory history =
                service.recordProgression(
                        tenantId,
                        learnerId,
                        promotionDecisionId,
                        "PROMOTED",
                        LocalDate.now()
                );


        assertNotNull(history);


        List<LearnerProgressionHistory> results =
                service.findLearnerHistory(
                        tenantId,
                        learnerId
                );


        assertFalse(
                results.isEmpty()
        );


        assertEquals(
                "PROMOTED",
                results.get(0)
                        .getProgressionType()
        );


        assertEquals(
                promotionDecisionId,
                results.get(0)
                        .getPromotionDecisionId()
        );

    }


    private String shortId(UUID id) {

        return id.toString()
                .substring(0, 8);

    }

}
