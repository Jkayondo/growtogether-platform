package africa.growtogether.platform.school.visitor.domain;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;


@Entity
@Table(name = "visitor_check_ins")
public class VisitorCheckIn extends AuditedTenantEntity {


    @ManyToOne
    @JoinColumn(name = "visitor_request_id", nullable = false)
    private VisitorRequest visitorRequest;


    @ManyToOne
    @JoinColumn(name = "visitor_id", nullable = false)
    private SchoolVisitor visitor;


    @Column(name = "gate_location", length = 100)
    private String gateLocation;


    @Column(name = "badge_number", length = 100)
    private String badgeNumber;


    @Column(name = "checked_in_at", nullable = false)
    private LocalDateTime checkedInAt;


    @Column(name = "checked_out_at")
    private LocalDateTime checkedOutAt;


    @Column(name = "checked_in_by", nullable = false, length = 150)
    private String checkedInBy;


    @Column(name = "checked_out_by", length = 150)
    private String checkedOutBy;


    @Column(name = "check_in_status", nullable = false, length = 50)
    private String checkInStatus;

    protected VisitorCheckIn() {
        // JPA constructor
    }


    public VisitorCheckIn(
            VisitorRequest visitorRequest,
            SchoolVisitor visitor,
            String gateLocation,
            String badgeNumber,
            String checkedInBy
    ) {

        this.visitorRequest = visitorRequest;
        this.visitor = visitor;
        this.gateLocation = gateLocation;
        this.badgeNumber = badgeNumber;
        this.checkedInBy = checkedInBy;
        this.checkedInAt = LocalDateTime.now();
        this.checkInStatus = "CHECKED_IN";
    }


    public void checkOut(
            String checkedOutBy
    ) {

        if (!"CHECKED_IN".equals(this.checkInStatus)) {
            throw new IllegalStateException(
                    "Visitor is not currently checked in"
            );
        }


        this.checkedOutAt = LocalDateTime.now();
        this.checkedOutBy = checkedOutBy;
        this.checkInStatus = "CHECKED_OUT";
    }


    public VisitorRequest getVisitorRequest() {
        return visitorRequest;
    }


    public SchoolVisitor getVisitor() {
        return visitor;
    }


    public String getGateLocation() {
        return gateLocation;
    }


    public String getBadgeNumber() {
        return badgeNumber;
    }


    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }


    public LocalDateTime getCheckedOutAt() {
        return checkedOutAt;
    }


    public String getCheckedInBy() {
        return checkedInBy;
    }


    public String getCheckedOutBy() {
        return checkedOutBy;
    }


    public String getCheckInStatus() {
        return checkInStatus;
    }
}
