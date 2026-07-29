package africa.growtogether.platform.school.reportcard;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(
        name = "academic_periods",
        indexes = {
                @Index(
                        name = "ix_academic_period_tenant",
                        columnList = "tenant_id"
                )
        }
)
public class AcademicPeriod extends AuditedTenantEntity {


    @Column(
            name = "period_name",
            nullable = false,
            length = 100
    )
    private String periodName;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "period_type",
            nullable = false,
            length = 30
    )
    private AcademicPeriodType periodType;


    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDate startDate;


    @Column(
            name = "end_date",
            nullable = false
    )
    private LocalDate endDate;


    protected AcademicPeriod() {
    }


    public AcademicPeriod(
            UUID tenantId,
            String periodName,
            AcademicPeriodType periodType,
            LocalDate startDate,
            LocalDate endDate
    ) {

        setTenantId(tenantId);

        this.periodName = periodName;
        this.periodType = periodType;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public String getPeriodName() {
        return periodName;
    }


    public AcademicPeriodType getPeriodType() {
        return periodType;
    }


    public LocalDate getStartDate() {
        return startDate;
    }


    public LocalDate getEndDate() {
        return endDate;
    }
}
