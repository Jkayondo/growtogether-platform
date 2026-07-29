package africa.growtogether.platform.school.parent;


import africa.growtogether.platform.school.reportcard.ReportCard;
import africa.growtogether.platform.school.reportcard.ReportCardRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ParentAcademicViewService {


    private final ParentAcademicAccessRepository accessRepository;

    private final ReportCardRepository reportCardRepository;


    public ParentAcademicViewService(
            ParentAcademicAccessRepository accessRepository,
            ReportCardRepository reportCardRepository
    ) {

        this.accessRepository = accessRepository;
        this.reportCardRepository = reportCardRepository;
    }


    public ParentAcademicView getParentAcademicView(
            UUID parentId
    ) {


        List<ParentAcademicAccess> accesses =
                accessRepository
                        .findByParentIdAndAccessStatus(
                                parentId,
                                ParentAcademicAccessStatus.ACTIVE
                        );


        List<ParentAcademicView.LearnerAcademicSummary> learners =
                accesses.stream()
                        .map(access -> {

                            List<ReportCard> reports =
                                    reportCardRepository
                                            .findByLearnerIdOrderByCreatedAtDesc(
                                                    access.getLearnerId()
                                            );


                            return new ParentAcademicView.LearnerAcademicSummary(
                                    access.getLearnerId(),
                                    reports.stream()
                                            .map(ReportCard::getId)
                                            .toList()
                            );

                        })
                        .toList();


        return new ParentAcademicView(
                parentId,
                learners
        );
    }
}
