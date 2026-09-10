package africa.growtogether.platform.school.grading;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(
        name = "gts_grade_division_rule"
)
public class GradeDivisionRule
        extends AuditedTenantEntity {


    @Column(
            name = "grading_scheme_id",
            nullable = false
    )
    private UUID gradingSchemeId;


    @Column(
            name = "division_code",
            nullable = false,
            length = 100
    )
    private String divisionCode;


    @Column(
            name = "division_name",
            nullable = false,
            length = 250
    )
    private String divisionName;


    @Column(
            name = "minimum_aggregate",
            nullable = false
    )
    private Integer minimumAggregate;


    @Column(
            name = "maximum_aggregate",
            nullable = false
    )
    private Integer maximumAggregate;


    @Column(
            name = "grade_point"
    )
    private BigDecimal gradePoint;


    @Column(
            name = "description",
            length = 1500
    )
    private String description;


    @Column(
            name = "sequence_number",
            nullable = false
    )
    private Integer sequenceNumber;


    protected GradeDivisionRule() {
    }


    public UUID getGradingSchemeId() {
        return gradingSchemeId;
    }


    public String getDivisionCode() {
        return divisionCode;
    }


    public String getDivisionName() {
        return divisionName;
    }


    public Integer getMinimumAggregate() {
        return minimumAggregate;
    }


    public Integer getMaximumAggregate() {
        return maximumAggregate;
    }


    public BigDecimal getGradePoint() {
        return gradePoint;
    }


    public String getDescription() {
        return description;
    }


    public Integer getSequenceNumber() {
        return sequenceNumber;
    }


    public boolean matches(
            Integer aggregate
    ) {

        return aggregate != null
                && aggregate >= minimumAggregate
                && aggregate <= maximumAggregate;

    }

}
