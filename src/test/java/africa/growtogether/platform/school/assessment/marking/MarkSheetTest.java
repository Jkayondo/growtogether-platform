package africa.growtogether.platform.school.assessment.marking;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class MarkSheetTest {


    private static MarkSheet newSheet() {

        return new MarkSheet(
                "MS-2026-001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00")
        );
    }


    @Test
    void newMarkSheetStartsDraft() {

        MarkSheet sheet =
                newSheet();

        assertEquals(
                "DRAFT",
                sheet.getMarkSheetStatus()
        );

        assertEquals(
                0,
                sheet.getExpectedCandidateCount()
        );

        assertEquals(
                0,
                sheet.getRecordedCandidateCount()
        );
    }


    @Test
    void openRecordsActorAndTimestamp() {

        MarkSheet sheet =
                newSheet();

        UUID actor =
                UUID.randomUUID();

        sheet.open(
                actor
        );

        assertEquals(
                "OPEN",
                sheet.getMarkSheetStatus()
        );

        assertEquals(
                actor,
                sheet.getEntryOpenedBy()
        );

        assertNotNull(
                sheet.getEntryOpenedAt()
        );
    }


    @Test
    void cannotSubmitDirectlyFromDraft() {

        MarkSheet sheet =
                newSheet();

        assertThrows(
                IllegalStateException.class,
                () ->
                        sheet.submit(
                                UUID.randomUUID()
                        )
        );

        assertEquals(
                "DRAFT",
                sheet.getMarkSheetStatus()
        );
    }


    @Test
    void submitRecordsRequiredDatabaseEvidence() {

        MarkSheet sheet =
                newSheet();

        UUID opener =
                UUID.randomUUID();

        UUID submitter =
                UUID.randomUUID();

        sheet.open(
                opener
        );

        sheet.submit(
                submitter
        );

        assertEquals(
                "SUBMITTED",
                sheet.getMarkSheetStatus()
        );

        assertEquals(
                submitter,
                sheet.getSubmittedBy()
        );

        assertNotNull(
                sheet.getSubmittedAt()
        );
    }


    @Test
    void moderationApprovalAndLockFollowControlledLifecycle() {

        MarkSheet sheet =
                newSheet();

        UUID actor =
                UUID.randomUUID();

        sheet.open(
                actor
        );

        sheet.submit(
                actor
        );

        sheet.startModeration();

        assertEquals(
                "MODERATION",
                sheet.getMarkSheetStatus()
        );

        sheet.approve();

        assertEquals(
                "APPROVED",
                sheet.getMarkSheetStatus()
        );

        sheet.lock(
                actor,
                "Final approved marks"
        );

        assertEquals(
                "LOCKED",
                sheet.getMarkSheetStatus()
        );

        assertEquals(
                actor,
                sheet.getLockedBy()
        );

        assertNotNull(
                sheet.getLockedAt()
        );

        assertEquals(
                "Final approved marks",
                sheet.getLockReason()
        );
    }


    @Test
    void cannotLockBeforeApproval() {

        MarkSheet sheet =
                newSheet();

        sheet.open(
                UUID.randomUUID()
        );

        sheet.submit(
                UUID.randomUUID()
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        sheet.lock(
                                UUID.randomUUID(),
                                "Too early"
                        )
        );
    }


    @Test
    void moderationCanReturnSheetForCorrectionAndReopen() {

        MarkSheet sheet =
                newSheet();

        UUID actor =
                UUID.randomUUID();

        sheet.open(
                actor
        );

        sheet.submit(
                actor
        );

        sheet.startModeration();

        sheet.returnForCorrection();

        assertEquals(
                "RETURNED",
                sheet.getMarkSheetStatus()
        );

        sheet.open(
                actor
        );

        assertEquals(
                "OPEN",
                sheet.getMarkSheetStatus()
        );
    }


    @Test
    void invalidScoreConfigurationIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new MarkSheet(
                                "MS-INVALID",
                                UUID.randomUUID(),
                                null,
                                null,
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                null,
                                null,
                                BigDecimal.ZERO,
                                null
                        )
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new MarkSheet(
                                "MS-INVALID-PASS",
                                UUID.randomUUID(),
                                null,
                                null,
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                null,
                                null,
                                new BigDecimal("100.00"),
                                new BigDecimal("101.00")
                        )
        );
    }
}
