package africa.growtogether.platform.school.reportcard;


import africa.growtogether.platform.school.reporting.AcademicGradeRecord;
import africa.growtogether.platform.school.reporting.AcademicGradeRecordRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ReportCardGenerationService {


    private final AcademicGradeRecordRepository gradeRepository;


    private final ReportCardRepository reportCardRepository;


    public ReportCardGenerationService(
            AcademicGradeRecordRepository gradeRepository,
            ReportCardRepository reportCardRepository
    ) {

        this.gradeRepository = gradeRepository;
        this.reportCardRepository = reportCardRepository;
    }


    public ReportCardSummary generate(
            UUID learnerId,
            UUID academicPeriodId
    ) {


        List<AcademicGradeRecord> records =
                gradeRepository
                        .findByLearnerIdOrderByCreatedAtDesc(
                                learnerId
                        );


        List<ReportCardSummary.SubjectPerformance> subjects =
                records.stream()
                        .map(record ->
                                new ReportCardSummary.SubjectPerformance(
                                        record.getSubjectConfigurationId(),
                                        record.getScore(),
                                        record.getGradeValue()
                                )
                        )
                        .toList();


        return new ReportCardSummary(
                learnerId,
                academicPeriodId,
                subjects,
                null
        );
    }
}
