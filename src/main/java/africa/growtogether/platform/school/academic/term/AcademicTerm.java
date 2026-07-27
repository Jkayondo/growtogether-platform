package africa.growtogether.platform.school.academic.term;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import africa.growtogether.platform.school.academic.year.AcademicYear;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;


@Entity
@Table(name = "gts_academic_term")
public class AcademicTerm extends AuditedTenantEntity {


    @ManyToOne
    @JoinColumn(
            name = "academic_year_id",
            nullable = false
    )
    private AcademicYear academicYear;


    @Column(
            name = "term_code",
            nullable = false,
            length = 40
    )
    private String termCode;


    @Column(
            name = "term_name",
            nullable = false,
            length = 120
    )
    private String termName;


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
            name = "sequence_number",
            nullable = false
    )
    private Integer sequenceNumber;


    protected AcademicTerm() {
    }


    public AcademicTerm(
            AcademicYear academicYear,
            String termCode,
            String termName,
            LocalDate startDate,
            LocalDate endDate,
            Integer termSequence
    ) {

        this.academicYear = academicYear;
        this.termCode = termCode;
        this.termName = termName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sequenceNumber = termSequence;;
    }


    public AcademicYear getAcademicYear() {
        return academicYear;
    }


    public String getTermCode() {
        return termCode;
    }


    public String getTermName() {
        return termName;
    }


    public LocalDate getStartDate() {
        return startDate;
    }


    public LocalDate getEndDate() {
        return endDate;
    }


    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
}
