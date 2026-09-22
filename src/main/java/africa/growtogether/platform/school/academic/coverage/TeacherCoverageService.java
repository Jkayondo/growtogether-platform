package africa.growtogether.platform.school.academic.coverage;


import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
public class TeacherCoverageService {


    private final TeacherCoverageRepository repository;


    public TeacherCoverageService(
            TeacherCoverageRepository repository
    ) {

        this.repository = repository;

    }



    public List<TeacherCoverage> getTeacherCoverage(
            UUID tenantId,
            UUID teacherProfileId
    ) {

        return repository
                .findByTenantIdAndTeacherProfileId(
                        tenantId,
                        teacherProfileId
                );

    }



    public List<TeacherCoverage> getAssignmentCoverage(
            UUID tenantId,
            UUID teachingAssignmentId
    ) {

        return repository
                .findByTenantIdAndTeachingAssignmentId(
                        tenantId,
                        teachingAssignmentId
                );

    }



    public TeacherCoverage save(
            TeacherCoverage coverage
    ) {

        return repository.save(
                coverage
        );

    }



    public TeacherCoverage markCompleted(
            UUID tenantId,
            UUID coverageId,
            String remarks
    ) {


        TeacherCoverage coverage =
                repository.findByTenantIdAndId(
                        tenantId,
                        coverageId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teacher coverage not found."
                        )
                );


        coverage.markCompleted(
                LocalDate.now(),
                remarks
        );


        return repository.save(
                coverage
        );

    }



    public TeacherCoverage markInProgress(
            UUID tenantId,
            UUID coverageId
    ) {


        TeacherCoverage coverage =
                repository.findByTenantIdAndId(
                        tenantId,
                        coverageId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teacher coverage not found."
                        )
                );


        coverage.markInProgress();


        return repository.save(
                coverage
        );

    }



    public TeacherCoverage markRequiresRemediation(
            UUID tenantId,
            UUID coverageId
    ) {


        TeacherCoverage coverage =
                repository.findByTenantIdAndId(
                        tenantId,
                        coverageId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teacher coverage not found."
                        )
                );


        coverage.markRequiresRemediation();


        return repository.save(
                coverage
        );

    }



    public TeacherCoverage markAheadOfSchedule(
            UUID tenantId,
            UUID coverageId
    ) {


        TeacherCoverage coverage =
                repository.findByTenantIdAndId(
                        tenantId,
                        coverageId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Teacher coverage not found."
                        )
                );


        coverage.markAheadOfSchedule();


        return repository.save(
                coverage
        );

    }

}
