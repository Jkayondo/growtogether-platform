package africa.growtogether.platform.school.assessment.marking;


import africa.growtogether.platform.school.academic.curriculum.ClassOffering;
import africa.growtogether.platform.school.academic.curriculum.ClassOfferingRepository;
import africa.growtogether.platform.school.academic.curriculum.StreamRepository;
import africa.growtogether.platform.school.academic.curriculum.SubjectOffering;
import africa.growtogether.platform.school.academic.curriculum.SubjectOfferingRepository;
import africa.growtogether.platform.school.academic.teaching.TeacherProfileRepository;
import africa.growtogether.platform.school.assessment.examination.paper.AssessmentPaper;
import africa.growtogether.platform.school.assessment.examination.paper.AssessmentPaperRepository;
import africa.growtogether.platform.school.assessment.examination.schedule.ExaminationSchedule;
import africa.growtogether.platform.school.assessment.examination.schedule.ExaminationScheduleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;


@Service
@Transactional
public class MarkSheetService {


    private final MarkSheetRepository repository;

    private final AssessmentComponentReferenceGateway
            assessmentComponentGateway;

    private final AssessmentPaperRepository
            assessmentPaperRepository;

    private final ExaminationScheduleRepository
            examinationScheduleRepository;

    private final SubjectOfferingRepository
            subjectOfferingRepository;

    private final ClassOfferingRepository
            classOfferingRepository;

    private final StreamRepository
            streamRepository;

    private final TeacherProfileRepository
            teacherProfileRepository;


    public MarkSheetService(
            MarkSheetRepository repository,
            AssessmentComponentReferenceGateway assessmentComponentGateway,
            AssessmentPaperRepository assessmentPaperRepository,
            ExaminationScheduleRepository examinationScheduleRepository,
            SubjectOfferingRepository subjectOfferingRepository,
            ClassOfferingRepository classOfferingRepository,
            StreamRepository streamRepository,
            TeacherProfileRepository teacherProfileRepository
    ) {

        this.repository =
                repository;

        this.assessmentComponentGateway =
                assessmentComponentGateway;

        this.assessmentPaperRepository =
                assessmentPaperRepository;

        this.examinationScheduleRepository =
                examinationScheduleRepository;

        this.subjectOfferingRepository =
                subjectOfferingRepository;

        this.classOfferingRepository =
                classOfferingRepository;

        this.streamRepository =
                streamRepository;

        this.teacherProfileRepository =
                teacherProfileRepository;
    }


    public MarkSheet create(
            UUID tenantId,
            CreateMarkSheetCommand command
    ) {

        requireTenant(
                tenantId
        );

        Objects.requireNonNull(
                command,
                "Mark sheet command is required"
        );


        String reference =
                normalizeReference(
                        command.markSheetReference()
                );


        if (
                repository
                        .existsByTenantIdAndMarkSheetReference(
                                tenantId,
                                reference
                        )
        ) {

            throw new IllegalArgumentException(
                    "Mark sheet reference already exists"
            );
        }


        AssessmentComponentReferenceGateway
                .AssessmentComponentReference component =
                assessmentComponentGateway
                        .find(
                                tenantId,
                                command.assessmentComponentId()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Assessment component not found for tenant"
                                        )
                        );


        if (
                !"ACTIVE".equals(
                        component.status()
                )
        ) {

            throw new IllegalArgumentException(
                    "Assessment component is not active"
            );
        }


        SubjectOffering subjectOffering =
                subjectOfferingRepository
                        .findByTenantIdAndId(
                                tenantId,
                                command.subjectOfferingId()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Subject offering not found for tenant"
                                        )
                        );


        if (
                !Objects.equals(
                        component.subjectOfferingId(),
                        subjectOffering.getId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Assessment component does not belong to subject offering"
            );
        }


        ClassOffering classOffering =
                classOfferingRepository
                        .findByTenantIdAndId(
                                tenantId,
                                command.classOfferingId()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Class offering not found for tenant"
                                        )
                        );


        if (
                !Objects.equals(
                        subjectOffering.getClassOfferingId(),
                        classOffering.getId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Subject offering does not belong to class offering"
            );
        }


        validateStream(
                tenantId,
                command.streamId(),
                subjectOffering
        );


        validateTeacher(
                tenantId,
                command.teacherProfileId()
        );


        validateScores(
                component,
                command.maximumScore(),
                command.passScore()
        );


        AssessmentPaper assessmentPaper =
                validateAssessmentPaper(
                        tenantId,
                        command,
                        component,
                        subjectOffering
                );


        validateSchedule(
                tenantId,
                command,
                assessmentPaper,
                classOffering
        );


        MarkSheet markSheet =
                new MarkSheet(
                        reference,
                        command.assessmentComponentId(),
                        command.assessmentPaperId(),
                        command.examinationScheduleId(),
                        command.subjectOfferingId(),
                        command.classOfferingId(),
                        command.streamId(),
                        command.teacherProfileId(),
                        command.maximumScore(),
                        command.passScore()
                );


        markSheet.setTenantId(
                tenantId
        );


        return repository.save(
                markSheet
        );
    }


    @Transactional(readOnly = true)
    public MarkSheet get(
            UUID tenantId,
            UUID id
    ) {

        requireTenant(
                tenantId
        );

        Objects.requireNonNull(
                id,
                "Mark sheet ID is required"
        );


        return repository
                .findByTenantIdAndId(
                        tenantId,
                        id
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Mark sheet not found for tenant"
                                )
                );
    }


    @Transactional(readOnly = true)
    public MarkSheet getByReference(
            UUID tenantId,
            String reference
    ) {

        requireTenant(
                tenantId
        );


        return repository
                .findByTenantIdAndMarkSheetReference(
                        tenantId,
                        normalizeReference(
                                reference
                        )
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Mark sheet not found for tenant"
                                )
                );
    }


    public MarkSheet open(
            UUID tenantId,
            UUID id,
            UUID actorId
    ) {

        MarkSheet sheet =
                get(
                        tenantId,
                        id
                );


        sheet.open(
                actorId
        );


        return sheet;
    }


    public MarkSheet submit(
            UUID tenantId,
            UUID id,
            UUID actorId
    ) {

        MarkSheet sheet =
                get(
                        tenantId,
                        id
                );


        sheet.submit(
                actorId
        );


        return sheet;
    }


    public MarkSheet startModeration(
            UUID tenantId,
            UUID id
    ) {

        MarkSheet sheet =
                get(
                        tenantId,
                        id
                );


        sheet.startModeration();


        return sheet;
    }


    public MarkSheet approveModeration(
            UUID tenantId,
            UUID id
    ) {

        MarkSheet sheet =
                get(
                        tenantId,
                        id
                );


        sheet.approve();


        return sheet;
    }


    public MarkSheet returnForCorrection(
            UUID tenantId,
            UUID id
    ) {

        MarkSheet sheet =
                get(
                        tenantId,
                        id
                );


        sheet.returnForCorrection();


        return sheet;
    }


    public MarkSheet lock(
            UUID tenantId,
            UUID id,
            UUID actorId,
            String reason
    ) {

        MarkSheet sheet =
                get(
                        tenantId,
                        id
                );


        sheet.lock(
                actorId,
                reason
        );


        return sheet;
    }


    private AssessmentPaper validateAssessmentPaper(
            UUID tenantId,
            CreateMarkSheetCommand command,
            AssessmentComponentReferenceGateway
                    .AssessmentComponentReference component,
            SubjectOffering subjectOffering
    ) {

        if (
                command.assessmentPaperId() == null
        ) {

            return null;
        }


        AssessmentPaper paper =
                assessmentPaperRepository
                        .findByTenantIdAndId(
                                tenantId,
                                command.assessmentPaperId()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Assessment paper not found for tenant"
                                        )
                        );


        if (
                !Objects.equals(
                        paper.getSubjectOfferingId(),
                        subjectOffering.getId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Assessment paper does not belong to subject offering"
            );
        }


        if (
                paper.getAssessmentComponentId() != null
                && !Objects.equals(
                        paper.getAssessmentComponentId(),
                        component.id()
                )
        ) {

            throw new IllegalArgumentException(
                    "Assessment paper does not belong to assessment component"
            );
        }


        if (
                paper.getMaximumScore() != null
                && paper.getMaximumScore().compareTo(
                        command.maximumScore()
                ) != 0
        ) {

            throw new IllegalArgumentException(
                    "Assessment paper maximum score does not match mark sheet"
            );
        }


        return paper;
    }


    private void validateSchedule(
            UUID tenantId,
            CreateMarkSheetCommand command,
            AssessmentPaper paper,
            ClassOffering classOffering
    ) {

        if (
                command.examinationScheduleId() == null
        ) {

            return;
        }


        if (
                paper == null
        ) {

            throw new IllegalArgumentException(
                    "Assessment paper is required when examination schedule is supplied"
            );
        }


        ExaminationSchedule schedule =
                examinationScheduleRepository
                        .findByTenantIdAndId(
                                tenantId,
                                command.examinationScheduleId()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Examination schedule not found for tenant"
                                        )
                        );


        if (
                !Objects.equals(
                        schedule.getAssessmentPaperId(),
                        paper.getId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Examination schedule does not belong to assessment paper"
            );
        }


        if (
                !Objects.equals(
                        schedule.getClassOfferingId(),
                        classOffering.getId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Examination schedule does not belong to class offering"
            );
        }


        if (
                paper.getExaminationSessionId() != null
                && !Objects.equals(
                        schedule.getExaminationSessionId(),
                        paper.getExaminationSessionId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Examination schedule does not belong to paper examination session"
            );
        }
    }


    private void validateStream(
            UUID tenantId,
            UUID streamId,
            SubjectOffering subjectOffering
    ) {

        UUID offeringStreamId =
                subjectOffering.getStreamId();


        if (
                streamId == null
        ) {

            if (
                    offeringStreamId != null
            ) {

                throw new IllegalArgumentException(
                        "Mark sheet stream must match subject offering stream"
                );
            }

            return;
        }


        streamRepository
                .findByTenantIdAndId(
                        tenantId,
                        streamId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Stream not found for tenant"
                                )
                );


        if (
                !Objects.equals(
                        streamId,
                        offeringStreamId
                )
        ) {

            throw new IllegalArgumentException(
                    "Mark sheet stream does not match subject offering stream"
            );
        }
    }


    private void validateTeacher(
            UUID tenantId,
            UUID teacherProfileId
    ) {

        if (
                teacherProfileId == null
        ) {

            return;
        }


        teacherProfileRepository
                .findByTenantIdAndId(
                        tenantId,
                        teacherProfileId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Teacher profile not found for tenant"
                                )
                );
    }


    private void validateScores(
            AssessmentComponentReferenceGateway
                    .AssessmentComponentReference component,
            BigDecimal maximumScore,
            BigDecimal passScore
    ) {

        if (
                maximumScore == null
                || maximumScore.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {

            throw new IllegalArgumentException(
                    "Maximum score must be greater than zero"
            );
        }


        if (
                component.maximumScore() != null
                && component.maximumScore().compareTo(
                        maximumScore
                ) != 0
        ) {

            throw new IllegalArgumentException(
                    "Assessment component maximum score does not match mark sheet"
            );
        }


        if (
                passScore != null
                && (
                    passScore.compareTo(
                            BigDecimal.ZERO
                    ) < 0
                    || passScore.compareTo(
                            maximumScore
                    ) > 0
                )
        ) {

            throw new IllegalArgumentException(
                    "Pass score must be between zero and maximum score"
            );
        }


        if (
                component.passScore() != null
                && passScore != null
                && component.passScore().compareTo(
                        passScore
                ) != 0
        ) {

            throw new IllegalArgumentException(
                    "Assessment component pass score does not match mark sheet"
            );
        }
    }


    private static void requireTenant(
            UUID tenantId
    ) {

        Objects.requireNonNull(
                tenantId,
                "Tenant is required"
        );
    }


    private static String normalizeReference(
            String reference
    ) {

        if (
                reference == null
                || reference.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Mark sheet reference is required"
            );
        }


        String normalized =
                reference.trim();


        if (
                normalized.length() > 100
        ) {

            throw new IllegalArgumentException(
                    "Mark sheet reference must not exceed 100 characters"
            );
        }


        return normalized;
    }
}
