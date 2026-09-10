package africa.growtogether.platform.school.results.publication;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
public class ResultPublicationService {


    private final ResultPublicationRepository repository;


    public ResultPublicationService(
            ResultPublicationRepository repository
    ){

        this.repository = repository;

    }



    public ResultPublication publish(
            UUID tenantId,
            UUID publicationId,
            UUID publishedBy
    ){

        ResultPublication publication =
                repository.findById(publicationId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Result publication not found"
                                )
                        );


        publication.publish(
                publishedBy
        );


        return publication;

    }



    public ResultPublication unpublish(
            UUID publicationId,
            UUID userId
    ){

        ResultPublication publication =
                repository.findById(publicationId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Result publication not found"
                                )
                        );


        publication.unpublish(
                userId
        );


        return publication;

    }



    @Transactional(readOnly = true)
    public boolean isPublished(
            UUID tenantId,
            UUID academicYearId,
            UUID academicTermId,
            UUID classGradeId,
            String publicationType
    ){

        return repository
                .existsByTenantIdAndAcademicYearIdAndAcademicTermIdAndClassGradeIdAndPublicationTypeAndPublicationStatus(
                        tenantId,
                        academicYearId,
                        academicTermId,
                        classGradeId,
                        publicationType,
                        "PUBLISHED"
                );

    }


}
