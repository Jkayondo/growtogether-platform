package africa.growtogether.platform.school.assessment.marking;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class CandidateScoreTest {


    private static final UUID SHEET =
            UUID.randomUUID();

    private static final UUID STUDENT =
            UUID.randomUUID();

    private static final UUID ENROLLMENT =
            UUID.randomUUID();

    private static final UUID ACTOR =
            UUID.randomUUID();


    @Test
    void startsAsDraft() {

        CandidateScore score =
                new CandidateScore(
                        SHEET,
                        STUDENT,
                        ENROLLMENT
                );

        assertEquals(
                "DRAFT",
                score.getScoreStatus()
        );

        assertFalse(
                score.isAbsent()
        );

        assertEquals(
                "MANUAL",
                score.getSourceType()
        );
    }


    @Test
    void entersScoreAndRecordsEvidence() {

        CandidateScore score =
                new CandidateScore(
                        SHEET,
                        STUDENT,
                        ENROLLMENT
                );

        score.enterScore(
                new BigDecimal("72.50"),
                ACTOR
        );

        assertEquals(
                new BigDecimal("72.50"),
                score.getRawScore()
        );

        assertEquals(
                new BigDecimal("72.50"),
                score.getFinalScore()
        );

        assertEquals(
                "ENTERED",
                score.getScoreStatus()
        );

        assertEquals(
                ACTOR,
                score.getEnteredBy()
        );

        assertNotNull(
                score.getEnteredAt()
        );
    }


    @Test
    void markAbsentClearsExistingScoresAndUsesEnteredLifecycle() {

        CandidateScore score =
                new CandidateScore(
                        SHEET,
                        STUDENT,
                        ENROLLMENT
                );

        score.enterScore(
                new BigDecimal("65.00"),
                ACTOR
        );

        score.markAbsent(
                ACTOR
        );

        assertTrue(
                score.isAbsent()
        );

        assertNull(
                score.getRawScore()
        );

        assertNull(
                score.getAdjustedScore()
        );

        assertNull(
                score.getFinalScore()
        );

        assertEquals(
                "ENTERED",
                score.getScoreStatus()
        );

        assertEquals(
                ACTOR,
                score.getEnteredBy()
        );
    }


    @Test
    void scoreCanReplacePriorAbsentEntry() {

        CandidateScore score =
                new CandidateScore(
                        SHEET,
                        STUDENT,
                        ENROLLMENT
                );

        score.markAbsent(
                ACTOR
        );

        score.enterScore(
                new BigDecimal("80.00"),
                ACTOR
        );

        assertFalse(
                score.isAbsent()
        );

        assertEquals(
                new BigDecimal("80.00"),
                score.getFinalScore()
        );

        assertEquals(
                "ENTERED",
                score.getScoreStatus()
        );
    }


    @Test
    void rejectsNegativeScore() {

        CandidateScore score =
                new CandidateScore(
                        SHEET,
                        STUDENT,
                        ENROLLMENT
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> score.enterScore(
                        new BigDecimal("-1"),
                        ACTOR
                )
        );
    }


    @Test
    void validatesAndApprovesInOrder() {

        CandidateScore score =
                new CandidateScore(
                        SHEET,
                        STUDENT,
                        ENROLLMENT
                );

        score.enterScore(
                new BigDecimal("50"),
                ACTOR
        );

        score.validate(
                ACTOR
        );

        assertEquals(
                "VALIDATED",
                score.getScoreStatus()
        );

        assertEquals(
                ACTOR,
                score.getValidatedBy()
        );

        score.approve(
                ACTOR
        );

        assertEquals(
                "APPROVED",
                score.getScoreStatus()
        );

        assertEquals(
                ACTOR,
                score.getApprovedBy()
        );
    }
}
