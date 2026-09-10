package africa.growtogether.platform.school.results;


import africa.growtogether.platform.school.grading.FinalResultClassification;
import africa.growtogether.platform.school.grading.GradeCalculationResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class AcademicResultPersistenceService {


    private final AcademicResultRecordService recordService;



    public AcademicResultPersistenceService(

            AcademicResultRecordService recordService

    ) {

        this.recordService = recordService;

    }



    public List<AcademicResultRecord> persistSubjectResults(

            UUID tenantId,

            UUID studentId,

            UUID academicYearId,

            UUID termId,

            UUID gradingProfileId,

            UUID gradingSchemeId,

            FinalResultClassification result

    ) {


        return result.getSubjectResults()

                .stream()

                .map(subjectResult ->

                        saveSubjectResult(

                                studentId,

                                academicYearId,

                                termId,

                                gradingProfileId,

                                gradingSchemeId,

                                result,

                                subjectResult

                        )

                )

                .toList();

    }



    private AcademicResultRecord saveSubjectResult(

            UUID studentId,

            UUID academicYearId,

            UUID termId,

            UUID gradingProfileId,

            UUID gradingSchemeId,

            FinalResultClassification result,

            GradeCalculationResult subjectResult

    ) {


        AcademicResultRecord record =

                new AcademicResultRecord(

                        studentId,

                        academicYearId,

                        termId,

                        gradingProfileId,

                        gradingSchemeId,

                        null,

                        "UNKNOWN",

                        subjectResult.getScore(),

                        subjectResult.getGradeCode(),

                        subjectResult.getGradeName(),

                        subjectResult.getGradePoint(),

                        result.getAggregationResult()
                                .getTotalPoints(),

                        result.getDivisionResult()
                                .getDivisionCode(),

                        result.getDivisionResult()
                                .getDivisionName()

                );


        return recordService.save(

                null,

                record

        );

    }

}
