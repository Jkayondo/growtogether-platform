package africa.growtogether.platform.eaif.approval;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
    name = "eaif_approval_records",
    indexes = {
        @Index(
            name = "ix_eaif_approval_request",
            columnList = "tenant_id,ai_request_id"
        ),
        @Index(
            name = "ix_eaif_approval_status",
            columnList = "tenant_id,approval_status"
        )
    }
)
public class EaifApprovalRecord extends AuditedTenantEntity {


    @Column(
            name = "ai_request_id",
            nullable = false
    )
    private UUID aiRequestId;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "approval_status",
            nullable = false,
            length = 30
    )
    private ApprovalStatus approvalStatus;


    @Column(
            name = "requested_at",
            nullable = false
    )
    private Instant requestedAt;


    @Column(
            name = "approved_by"
    )
    private UUID approvedBy;


    @Column(
            name = "approved_at"
    )
    private Instant approvedAt;


    @Column(
            name = "decision_reason",
            length = 500
    )
    private String decisionReason;


    protected EaifApprovalRecord() {
    }


    public EaifApprovalRecord(
            UUID tenantId,
            UUID aiRequestId
    ) {

        setTenantId(tenantId);

        this.aiRequestId = aiRequestId;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.requestedAt = Instant.now();
    }


    public void approve(
            UUID userId,
            String reason
    ) {

        if (approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException(
                    "Approval is not pending"
            );
        }

        approvalStatus = ApprovalStatus.APPROVED;
        approvedBy = userId;
        approvedAt = Instant.now();
        decisionReason = reason;
    }


    public void reject(
            UUID userId,
            String reason
    ) {

        if (approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException(
                    "Approval is not pending"
            );
        }

        approvalStatus = ApprovalStatus.REJECTED;
        approvedBy = userId;
        approvedAt = Instant.now();
        decisionReason = reason;
    }


    public UUID aiRequestId() {
        return aiRequestId;
    }


    public ApprovalStatus approvalStatus() {
        return approvalStatus;
    }


    public UUID approvedBy() {
        return approvedBy;
    }


    public Instant approvedAt() {
        return approvedAt;
    }


    public String decisionReason() {
        return decisionReason;
    }
}
