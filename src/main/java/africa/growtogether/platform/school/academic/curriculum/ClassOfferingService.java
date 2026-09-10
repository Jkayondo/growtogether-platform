package africa.growtogether.platform.school.academic.curriculum;


import africa.growtogether.platform.school.academic.year.AcademicYearService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
public class ClassOfferingService {


    private final ClassOfferingRepository repository;
    private final AcademicYearService academicYears;
    private final CampusService campuses;
    private final ClassGradeService classGrades;


    public ClassOfferingService(
            ClassOfferingRepository repository,
            AcademicYearService academicYears,
            CampusService campuses,
            ClassGradeService classGrades
    ) {
        this.repository = repository;
        this.academicYears = academicYears;
        this.campuses = campuses;
        this.classGrades = classGrades;
    }


    @Transactional
    public ClassOffering create(
            UUID tenantId,
            String offeringCode,
            UUID academicYearId,
            UUID campusId,
            UUID academicProgrammeId,
            UUID studyTrackId,
            UUID curriculumVersionId,
            UUID classGradeId,
            Integer plannedCapacity,
            Integer minimumEnrollment,
            Integer maximumEnrollment,
            LocalDate enrollmentOpenDate,
            LocalDate enrollmentCloseDate
    ) {


        academicYears.get(
                tenantId,
                academicYearId
        );

        campuses.get(
                tenantId,
                campusId
        );

        classGrades.get(
                tenantId,
                classGradeId
        );


        ClassOffering offering =
                new ClassOffering(
                        offeringCode,
                        academicYearId,
                        campusId,
                        academicProgrammeId,
                        studyTrackId,
                        curriculumVersionId,
                        classGradeId,
                        plannedCapacity,
                        minimumEnrollment,
                        maximumEnrollment,
                        enrollmentOpenDate,
                        enrollmentCloseDate
                );


        offering.setTenantId(
                tenantId
        );


        return repository.save(
                offering
        );
    }


    @Transactional(readOnly = true)
    public ClassOffering get(
            UUID tenantId,
            UUID id
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        id
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Class offering not found for tenant"
                        )
                );
    }


    @Transactional(readOnly = true)
    public ClassOffering findByCode(
            UUID tenantId,
            String offeringCode
    ) {

        return repository
                .findByTenantIdAndOfferingCode(
                        tenantId,
                        offeringCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Class offering not found"
                        )
                );
    }


    @Transactional(readOnly = true)
    public List<ClassOffering> findByAcademicYear(
            UUID tenantId,
            UUID academicYearId
    ) {

        return repository
                .findByTenantIdAndAcademicYearId(
                        tenantId,
                        academicYearId
                );
    }


    @Transactional(readOnly = true)
    public List<ClassOffering> findByCampus(
            UUID tenantId,
            UUID campusId
    ) {

        return repository
                .findByTenantIdAndCampusId(
                        tenantId,
                        campusId
                );
    }


    @Transactional(readOnly = true)
    public List<ClassOffering> findByClassGrade(
            UUID tenantId,
            UUID classGradeId
    ) {

        return repository
                .findByTenantIdAndClassGradeId(
                        tenantId,
                        classGradeId
                );
    }


    @Transactional
    public ClassOffering activate(
            ClassOffering offering
    ) {

        offering.activate();

        return repository.save(
                offering
        );
    }


    @Transactional
    public ClassOffering deactivate(
            ClassOffering offering
    ) {

        offering.deactivate();

        return repository.save(
                offering
        );
    }

}
