package africa.growtogether.platform.school.visitor.domain;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "visitor_badges")
public class VisitorBadge extends AuditedTenantEntity {


    @Column(name = "visitor_check_in_id", nullable = false)
    private UUID visitorCheckInId;


    @Column(name = "badge_number", nullable = false, length = 100)
    private String badgeNumber;


    @Column(name = "badge_type", length = 50)
    private String badgeType;


    @Column(name = "badge_status", nullable = false, length = 50)
    private String badgeStatus;


    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;


    @Column(name = "issued_by", nullable = false, length = 150)
    private String issuedBy;


    @Column(name = "returned_at")
    private LocalDateTime returnedAt;


    @Column(name = "returned_by", length = 150)
    private String returnedBy;


    protected VisitorBadge() {
        // JPA constructor
    }


    public VisitorBadge(
            UUID visitorCheckInId,
            String badgeNumber,
            String badgeType,
            String issuedBy
    ) {

        this.visitorCheckInId = visitorCheckInId;
        this.badgeNumber = badgeNumber;
        this.badgeType = badgeType;
        this.badgeStatus = "ISSUED";
        this.issuedBy = issuedBy;
        this.issuedAt = LocalDateTime.now();

    }


    public void returnBadge(String returnedBy) {

        if ("RETURNED".equals(this.badgeStatus)) {
            throw new IllegalStateException(
                "Badge has already been returned."
            );
        }


        if ("DISABLED".equals(this.badgeStatus)) {
            throw new IllegalStateException(
                "Disabled badge cannot be returned."
            );
        }

        this.badgeStatus = "RETURNED";
        this.returnedBy = returnedBy;
        this.returnedAt = LocalDateTime.now();

    }


    public void disable() {

        if ("RETURNED".equals(this.badgeStatus)) {
            throw new IllegalStateException(
                "Returned badge cannot be disabled."
            );
        }


        if ("DISABLED".equals(this.badgeStatus)) {
            throw new IllegalStateException(
                "Badge is already disabled."
            );
        }


        this.badgeStatus = "DISABLED";

    }


    public UUID getVisitorCheckInId() {
        return visitorCheckInId;
    }


    public String getBadgeNumber() {
        return badgeNumber;
    }


    public String getBadgeType() {
        return badgeType;
    }


    public String getBadgeStatus() {
        return badgeStatus;
    }


    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }


    public String getIssuedBy() {
        return issuedBy;
    }


    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }


    public String getReturnedBy() {
        return returnedBy;
    }

}
