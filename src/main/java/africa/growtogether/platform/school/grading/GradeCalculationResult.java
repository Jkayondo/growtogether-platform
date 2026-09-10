package africa.growtogether.platform.school.grading;


import java.math.BigDecimal;


public class GradeCalculationResult {


    private final BigDecimal score;

    private final String gradeCode;

    private final String gradeName;

    private final BigDecimal gradePoint;

    private final boolean passGrade;

    private final boolean distinctionGrade;



    public GradeCalculationResult(
            BigDecimal score,
            String gradeCode,
            String gradeName,
            BigDecimal gradePoint,
            boolean passGrade,
            boolean distinctionGrade
    ) {

        this.score = score;
        this.gradeCode = gradeCode;
        this.gradeName = gradeName;
        this.gradePoint = gradePoint;
        this.passGrade = passGrade;
        this.distinctionGrade = distinctionGrade;

    }



    public BigDecimal getScore() {
        return score;
    }


    public String getGradeCode() {
        return gradeCode;
    }


    public String getGradeName() {
        return gradeName;
    }


    public BigDecimal getGradePoint() {
        return gradePoint;
    }


    public boolean isPassGrade() {
        return passGrade;
    }


    public boolean isDistinctionGrade() {
        return distinctionGrade;
    }

}
