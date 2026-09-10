package africa.growtogether.platform.school.grading;


import java.math.BigDecimal;


public class DivisionClassificationResult {


    private final BigDecimal aggregateValue;

    private final String divisionCode;

    private final String divisionName;

    private final String description;



    public DivisionClassificationResult(

            BigDecimal aggregateValue,

            String divisionCode,

            String divisionName,

            String description

    ) {

        this.aggregateValue = aggregateValue;
        this.divisionCode = divisionCode;
        this.divisionName = divisionName;
        this.description = description;

    }



    public BigDecimal getAggregateValue() {

        return aggregateValue;

    }



    public String getDivisionCode() {

        return divisionCode;

    }



    public String getDivisionName() {

        return divisionName;

    }



    public String getDescription() {

        return description;

    }

}
