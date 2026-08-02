package africa.growtogether.platform.school.visitor.domain;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;


@Entity
@Table(name = "visitor_requests")
public class VisitorRequest extends AuditedTenantEntity {


    @ManyToOne
    @JoinColumn(name = "visitor_id", nullable = false)
    private SchoolVisitor visitor;


    @Column(name = "host_user_id")
    private java.util.UUID hostUserId;


    @Column(name = "purpose", nullable = false, length = 255)
    private String purpose;


    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;


    @Column(name = "expected_arrival_time")
    private LocalTime expectedArrivalTime;


    @Column(name = "expected_departure_time")
    private LocalTime expectedDepartureTime;


    @Column(name = "request_status", nullable = false, length = 50)
    private String requestStatus;


    @Column(name = "approval_comment", length = 500)
    private String approvalComment;


    @Column(name = "approved_by", length = 150)
    private String approvedBy;


    protected VisitorRequest() {
        // JPA constructor
    }


    public VisitorRequest(
            SchoolVisitor visitor,
            java.util.UUID hostUserId,
            String purpose,
            LocalDate visitDate,
            LocalTime expectedArrivalTime,
            LocalTime expectedDepartureTime
    ) {

        this.visitor = visitor;
        this.hostUserId = hostUserId;
        this.purpose = purpose;
        this.visitDate = visitDate;
        this.expectedArrivalTime = expectedArrivalTime;
        this.expectedDepartureTime = expectedDepartureTime;
        this.requestStatus = "PENDING";
    }


    public SchoolVisitor getVisitor() {
        return visitor;
    }


    public java.util.UUID getHostUserId() {
        return hostUserId;
    }


    public String getPurpose() {
        return purpose;
    }


    public LocalDate getVisitDate() {
        return visitDate;
    }


    public LocalTime getExpectedArrivalTime() {
        return expectedArrivalTime;
    }


    public LocalTime getExpectedDepartureTime() {
        return expectedDepartureTime;
    }


    public String getRequestStatus() {
        return requestStatus;
    }


    public String getApprovalComment() {
        return approvalComment;
    }


    public String getApprovedBy() {
        return approvedBy;
    }
    public void approve(
        String approvedBy,
        String comment
) {

    if (!"PENDING".equals(this.requestStatus)) {
        throw new IllegalStateException(
                "Only pending requests can be approved"
        );
    }

    this.requestStatus = "APPROVED";
    this.approvedBy = approvedBy;
    this.approvalComment = comment;
}


public void reject(
        String rejectedBy,
        String comment
) {

    if (!"PENDING".equals(this.requestStatus)) {
        throw new IllegalStateException(
                "Only pending requests can be rejected"
        );
    }

    this.requestStatus = "REJECTED";
    this.approvedBy = rejectedBy;
    this.approvalComment = comment;
}


public void complete() {

    if (!"APPROVED".equals(this.requestStatus)) {
        throw new IllegalStateException(
                "Only approved requests can be completed"
        );
    }

    this.requestStatus = "COMPLETED";
  }

}
