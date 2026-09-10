package africa.growtogether.platform.school.grading;


import java.math.BigDecimal;


public class GradeAggregationResult {


    private final BigDecimal aggregateScore;

    private final BigDecimal totalPoints;

    private final Integer subjectCount;

    private final String aggregationRuleCode;



    public GradeAggregationResult(

            BigDecimal aggregateScore,

            BigDecimal totalPoints,

            Integer subjectCount,

            String aggregationRuleCode

    ) {

        this.aggregateScore = aggregateScore;
        this.totalPoints = totalPoints;
        this.subjectCount = subjectCount;
        this.aggregationRuleCode = aggregationRuleCode;

    }



    public BigDecimal getAggregateScore() {

        return aggregateScore;

    }



    public BigDecimal getTotalPoints() {

        return totalPoints;

    }



    public Integer getSubjectCount() {

        return subjectCount;

    }



    public String getAggregationRuleCode() {

        return aggregationRuleCode;

    }

}
