package africa.growtogether.platform.school.grading;


import java.util.List;


public class FinalResultClassification {


    private final List<GradeCalculationResult> subjectResults;


    private final GradeAggregationResult aggregationResult;


    private final DivisionClassificationResult divisionResult;



    public FinalResultClassification(

            List<GradeCalculationResult> subjectResults,

            GradeAggregationResult aggregationResult,

            DivisionClassificationResult divisionResult

    ) {

        this.subjectResults = subjectResults;
        this.aggregationResult = aggregationResult;
        this.divisionResult = divisionResult;

    }



    public List<GradeCalculationResult> getSubjectResults() {

        return subjectResults;

    }



    public GradeAggregationResult getAggregationResult() {

        return aggregationResult;

    }



    public DivisionClassificationResult getDivisionResult() {

        return divisionResult;

    }

}
