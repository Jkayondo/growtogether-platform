package africa.growtogether.platform.school.results.transcript;


import africa.growtogether.platform.school.results.AcademicResultRecord;

import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class AcademicTranscriptAdapterService {



    public TranscriptEntry map(

            AcademicResultRecord record

    ) {


        return new TranscriptEntry(

                record.getStudentId(),

                record.getAcademicYearId(),

                record.getTermId(),

                record.getClassId(),

                record.getAssessmentId(),

                record.getSubjectId(),

                record.getSubjectName(),

                record.getAssessmentScore(),

                record.getGradeCode(),

                record.getGradeName(),

                record.getGradePoint(),

                record.getGradingSchemeId(),

                record.getAggregationRuleId(),

                record.getDivisionRuleId()

        );

    }




    public List<TranscriptEntry> mapAll(

            List<AcademicResultRecord> records

    ) {


        return records.stream()

                .map(this::map)

                .toList();

    }


}
