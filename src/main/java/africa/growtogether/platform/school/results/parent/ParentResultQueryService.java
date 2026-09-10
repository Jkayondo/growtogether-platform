package africa.growtogether.platform.school.results.parent;


import africa.growtogether.platform.school.results.publication.ResultPublicationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
public class ParentResultQueryService {


    private final ResultPublicationService publicationService;

    private final africa.growtogether.platform.school.results.security.AcademicResultAccessGuard accessGuard;


    public ParentResultQueryService(
            ResultPublicationService publicationService,
            africa.growtogether.platform.school.results.security.AcademicResultAccessGuard accessGuard
    ){

        this.publicationService = publicationService;
        this.accessGuard = accessGuard;

    }



    public ParentResultView getResults(

            UUID tenantId,

            UUID learnerId,

            UUID academicYearId,

            UUID academicTermId,

            UUID classGradeId

    ){


        accessGuard.validateParentAccess(
                tenantId,
                learnerId
        );


        boolean published =
                publicationService.isPublished(
                        tenantId,
                        academicYearId,
                        academicTermId,
                        classGradeId,
                        "TERM_RESULT"
                );


        if(!published){

            throw new IllegalStateException(
                    "Results have not been published"
            );

        }


        /*
         * Result retrieval integration point.
         *
         * This will connect to:
         *
         * gts_student_term_result
         * gts_student_subject_result
         *
         * after learner relationship validation.
         *
         */


        return new ParentResultView(

                learnerId,

                academicTermId,

                null,

                null,

                null,

                List.of()

        );

    }

}
