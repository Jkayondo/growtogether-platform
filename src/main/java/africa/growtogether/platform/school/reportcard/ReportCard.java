package africa.growtogether.platform.school.reportcard;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(
        name = "report_cards",
        indexes = {
                @Index(
                        name = "ix_report_card_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class ReportCard extends AuditedTenantEntity {


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    @Column(
            name = "academic_period_id",
            nullable = false
    )
    private UUID academicPeriodId;


    @Column(
            name = "overall_comment",
            length = 500
    )
    private String overallComment;


    protected ReportCard() {
    }


    public ReportCard(
            UUID tenantId,
            UUID learnerId,
            UUID academicPeriodId
    ) {

        setTenantId(tenantId);

        this.learnerId = learnerId;
        this.academicPeriodId = academicPeriodId;
    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public UUID getAcademicPeriodId() {
        return academicPeriodId;
    }


    public String getOverallComment() {
        return overallComment;
    }


    public void addComment(
            String comment
    ) {

        this.overallComment = comment;
    }
}
