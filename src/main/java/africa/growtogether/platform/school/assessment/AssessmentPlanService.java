package africa.growtogether.platform.school.assessment;

import africa.growtogether.platform.school.academic.curriculum.CampusRepository;
import africa.growtogether.platform.school.academic.curriculum.ClassGradeRepository;
import africa.growtogether.platform.school.academic.curriculum.CurriculumVersionRepository;
import africa.growtogether.platform.school.academic.curriculum.Stream;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.term.AcademicTerm;
import africa.growtogether.platform.school.academic.term.AcademicTermRepository;
import africa.growtogether.platform.school.academic.year.AcademicYear;
import africa.growtogether.platform.school.academic.year.AcademicYearRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
public class AssessmentPlanService {


    private final AssessmentPlanRepository repository;
    private final AcademicYearRepository academicYears;
    private final AcademicTermRepository academicTerms;
    private final CampusRepository campuses;
    private final ClassGradeRepository classGrades;
    private final StreamRepository streams;
    private final CurriculumVersionRepository curriculumVersions;
    private final AssessmentPlanReferenceGateway references;


    public AssessmentPlanService(
            AssessmentPlanRepository repository,
            AcademicYearRepository academicYears,
            AcademicTermRepository academicTerms,
            CampusRepository campuses,
            ClassGradeRepository classGrades,
            StreamRepository streams,
            CurriculumVersionRepository curriculumVersions,
            AssessmentPlanReferenceGateway references
    ) {
        this.repository = repository;
        this.academicYears = academicYears;
        this.academicTerms = academicTerms;
        this.campuses = campuses;
        this.classGrades = classGrades;
        this.streams = streams;
        this.curriculumVersions = curriculumVersions;
        this.references = references;
    }


    @Transactional
    public AssessmentPlan create(
            UUID tenantId,
            CreateAssessmentPlanCommand command
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (command == null) {
            throw new IllegalArgumentException(
                    "command must not be null"
            );
        }


        AcademicYear academicYear =
                academicYears
                        .findByTenantIdAndId(
                                tenantId,
                                command.academicYearId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Academic year not found for tenant"
                                )
                        );

        validateAcademicYearEffectiveDates(
                academicYear,
                command.effectiveFrom(),
                command.effectiveTo()
        );


        if (command.academicTermId() != null) {

            AcademicTerm term =
                    academicTerms
                            .findByTenantIdAndId(
                                    tenantId,
                                    command.academicTermId()
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Academic term not found for tenant"
                                    )
                            );

            if (
                    term.getAcademicYear() == null
                    || !command.academicYearId().equals(
                            term.getAcademicYear().getId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Academic term does not belong to academic year"
                );
            }

            validateTermEffectiveDates(
                    term,
                    command.effectiveFrom(),
                    command.effectiveTo()
            );
        }


        campuses
                .findByTenantIdAndId(
                        tenantId,
                        command.campusId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Campus not found for tenant"
                        )
                );


        classGrades
                .findByTenantIdAndId(
                        tenantId,
                        command.classGradeId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Class grade not found for tenant"
                        )
                );


        if (command.academicProgrammeId() != null
                && !references.academicProgrammeExists(
                        tenantId,
                        command.academicProgrammeId()
                )) {

            throw new IllegalArgumentException(
                    "Academic programme not found for tenant"
            );
        }


        if (command.studyTrackId() != null) {

            if (!references.studyTrackExists(
                    tenantId,
                    command.studyTrackId()
            )) {
                throw new IllegalArgumentException(
                        "Study track not found for tenant"
                );
            }

            if (
                    command.academicProgrammeId() != null
                    && !references.studyTrackBelongsToProgramme(
                            tenantId,
                            command.studyTrackId(),
                            command.academicProgrammeId()
                    )
            ) {
                throw new IllegalArgumentException(
                        "Study track does not belong to academic programme"
                );
            }
        }


        if (command.curriculumVersionId() != null) {

            curriculumVersions
                    .findByTenantIdAndId(
                            tenantId,
                            command.curriculumVersionId()
                    )
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Curriculum version not found for tenant"
                            )
                    );
        }


        if (command.streamId() != null) {

            Stream stream =
                    streams
                            .findByTenantIdAndId(
                                    tenantId,
                                    command.streamId()
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Stream not found for tenant"
                                    )
                            );

            if (!command.campusId().equals(
                    stream.getCampusId()
            )) {
                throw new IllegalArgumentException(
                        "Stream does not belong to campus"
                );
            }

            if (!command.classGradeId().equals(
                    stream.getClassGradeId()
            )) {
                throw new IllegalArgumentException(
                        "Stream does not belong to class grade"
                );
            }
        }


        if (
                command.gradingSchemeId() != null
                && !references.gradingSchemeExists(
                        tenantId,
                        command.gradingSchemeId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Grading scheme not found for tenant"
            );
        }


        if (
                repository.existsByTenantIdAndPlanCode(
                        tenantId,
                        command.planCode()
                )
        ) {
            throw new IllegalArgumentException(
                    "Assessment plan code already exists for tenant"
            );
        }


        if (
                repository.countByTenantAndAcademicScope(
                        tenantId,
                        command.academicYearId(),
                        command.academicTermId(),
                        command.campusId(),
                        command.academicProgrammeId(),
                        command.studyTrackId(),
                        command.classGradeId(),
                        command.streamId()
                ) > 0
        ) {
            throw new IllegalArgumentException(
                    "Assessment plan already exists for academic scope"
            );
        }

        AssessmentPlan plan =
                new AssessmentPlan(
                        command.planCode(),
                        command.planName(),
                        command.description(),
                        command.academicYearId(),
                        command.academicTermId(),
                        command.campusId(),
                        command.academicProgrammeId(),
                        command.studyTrackId(),
                        command.curriculumVersionId(),
                        command.classGradeId(),
                        command.streamId(),
                        command.gradingSchemeId(),
                        command.effectiveFrom(),
                        command.effectiveTo(),
                        command.workflowInstanceId()
                );

        plan.setTenantId(
                tenantId
        );

        return repository.save(
                plan
        );
    }


    @Transactional(readOnly = true)
    public AssessmentPlan get(
            UUID tenantId,
            UUID assessmentPlanId
    ) {

        return repository
                .findByTenantIdAndId(
                        tenantId,
                        assessmentPlanId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Assessment plan not found"
                        )
                );
    }


    @Transactional(readOnly = true)
    public List<AssessmentPlan> findByAcademicYear(
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
    public List<AssessmentPlan> findByCampus(
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
    public List<AssessmentPlan> findByClassGrade(
            UUID tenantId,
            UUID classGradeId
    ) {

        return repository
                .findByTenantIdAndClassGradeId(
                        tenantId,
                        classGradeId
                );
    }


    private void validateAcademicYearEffectiveDates(
            AcademicYear academicYear,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (
                academicYear.getStartDate() != null
                && effectiveFrom != null
                && effectiveFrom.isBefore(
                        academicYear.getStartDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Assessment plan effectiveFrom is before academic year"
            );
        }

        if (
                academicYear.getEndDate() != null
                && effectiveFrom != null
                && effectiveFrom.isAfter(
                        academicYear.getEndDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Assessment plan effectiveFrom is after academic year"
            );
        }

        if (
                academicYear.getEndDate() != null
                && effectiveTo != null
                && effectiveTo.isAfter(
                        academicYear.getEndDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Assessment plan effectiveTo is after academic year"
            );
        }
    }


    private void validateTermEffectiveDates(
            AcademicTerm term,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (
                effectiveFrom != null
                && effectiveFrom.isBefore(
                        term.getStartDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Assessment plan effectiveFrom is before academic term"
            );
        }

        if (
                effectiveFrom != null
                && effectiveFrom.isAfter(
                        term.getEndDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Assessment plan effectiveFrom is after academic term"
            );
        }

        if (
                effectiveTo != null
                && effectiveTo.isAfter(
                        term.getEndDate()
                )
        ) {
            throw new IllegalArgumentException(
                    "Assessment plan effectiveTo is after academic term"
            );
        }
    }
}
