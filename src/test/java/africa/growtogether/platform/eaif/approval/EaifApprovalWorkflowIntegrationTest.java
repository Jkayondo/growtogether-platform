package africa.growtogether.platform.eaif.approval;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;


class EaifApprovalWorkflowIntegrationTest {


    @Test
    void approvePendingRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID approver = UUID.randomUUID();


        EaifApprovalRecord record =
                new EaifApprovalRecord(
                        tenantId,
                        requestId
                );


        record.approve(
                approver,
                "Approved for controlled execution"
        );


        assertEquals(
                ApprovalStatus.APPROVED,
                record.approvalStatus()
        );


        assertEquals(
                approver,
                record.approvedBy()
        );


        assertEquals(
                "Approved for controlled execution",
                record.decisionReason()
        );
    }



    @Test
    void rejectPendingRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID reviewer = UUID.randomUUID();


        EaifApprovalRecord record =
                new EaifApprovalRecord(
                        tenantId,
                        requestId
                );


        record.reject(
                reviewer,
                "Rejected by governance policy"
        );


        assertEquals(
                ApprovalStatus.REJECTED,
                record.approvalStatus()
        );


        assertEquals(
                reviewer,
                record.approvedBy()
        );


        assertEquals(
                "Rejected by governance policy",
                record.decisionReason()
        );
    }



    @Test
    void preventDuplicateApproval() {

        EaifApprovalRecord record =
                new EaifApprovalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );


        record.approve(
                UUID.randomUUID(),
                "First approval"
        );


        assertThrows(
                IllegalStateException.class,
                () ->
                        record.approve(
                                UUID.randomUUID(),
                                "Second approval"
                        )
        );
    }



    @Test
    void preventDuplicateRejection() {

        EaifApprovalRecord record =
                new EaifApprovalRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );


        record.reject(
                UUID.randomUUID(),
                "First rejection"
        );


        assertThrows(
                IllegalStateException.class,
                () ->
                        record.reject(
                                UUID.randomUUID(),
                                "Second rejection"
                        )
        );
    }



    @Test
    void tenantIsolationIsMaintained() {

        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();


        EaifApprovalRecord record =
                new EaifApprovalRecord(
                        tenantA,
                        UUID.randomUUID()
                );


        assertNotEquals(
                tenantB,
                record.getTenantId()
        );
    }
}
