package africa.growtogether.platform.school.academic.year;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "gts_academic_year")
public class AcademicYear extends AuditedTenantEntity {


    @Column(
            name = "academic_year_code",
            nullable = false,
            length = 40
    )
    private String academicYearCode;


    @Column(
            name = "academic_year_name",
            nullable = false,
            length = 120
    )
    private String academicYearName;


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


    @Column(
            name = "current_year",
            nullable = false
           
    )
    private boolean currentYear;


    protected AcademicYear() {
    }


    public AcademicYear(
            UUID tenantId,
            String yearCode,
            String yearName,
            LocalDate startDate,
            LocalDate endDate
    ) {

        setTenantId(tenantId);

        this.academicYearCode = yearCode;
	this.academicYearName = yearName;
	this.startDate = startDate;
	this.endDate = endDate;
	this.currentYear = false;
    }


    public String getAcademicYearCode() {
        return academicYearCode;
    }


    public boolean isCurrentYear() {
        return currentYear;
    }


    public LocalDate getStartDate() {
        return startDate;
    }


    public LocalDate getEndDate() {
        return endDate;
    }
}
