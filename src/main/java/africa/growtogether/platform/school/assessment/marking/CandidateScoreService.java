package africa.growtogether.platform.school.assessment.marking;


import africa.growtogether.platform.school.academic.curriculum.ClassOffering;
import africa.growtogether.platform.school.academic.curriculum.ClassOfferingRepository;
import africa.growtogether.platform.school.assessment.examination.candidate.ExaminationCandidate;
import africa.growtogether.platform.school.assessment.examination.candidate.ExaminationCandidateRepository;
import africa.growtogether.platform.school.assessment.examination.registration.CandidatePaperRegistration;
import africa.growtogether.platform.school.assessment.examination.registration.CandidatePaperRegistrationRepository;
import africa.growtogether.platform.school.enrollment.StudentEnrollment;
import africa.growtogether.platform.school.enrollment.StudentEnrollmentRepository;
import africa.growtogether.platform.school.student.StudentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;


@Service
@Transactional
public class CandidateScoreService {


    private final CandidateScoreRepository repository;

    private final MarkSheetRepository markSheets;

    private final ClassOfferingRepository classOfferings;

    private final StudentRepository students;

    private final StudentEnrollmentRepository enrollments;

    private final ExaminationCandidateRepository examinationCandidates;

    private final CandidatePaperRegistrationRepository
            candidatePaperRegistrations;


    public CandidateScoreService(
            CandidateScoreRepository repository,
            MarkSheetRepository markSheets,
            ClassOfferingRepository classOfferings,
            StudentRepository students,
            StudentEnrollmentRepository enrollments,
            ExaminationCandidateRepository examinationCandidates,
            CandidatePaperRegistrationRepository
                    candidatePaperRegistrations
    ) {

        this.repository = repository;
        this.markSheets = markSheets;
        this.classOfferings = classOfferings;
        this.students = students;
        this.enrollments = enrollments;
        this.examinationCandidates = examinationCandidates;
        this.candidatePaperRegistrations =
                candidatePaperRegistrations;
    }


    public CandidateScore create(
            UUID tenantId,
            CreateCandidateScoreCommand command
    ) {

        requireTenant(tenantId);

        Objects.requireNonNull(
                command,
                "Candidate score command is required"
        );

        UUID markSheetId =
                Objects.requireNonNull(
                        command.markSheetId(),
                        "Mark sheet is required"
                );

        UUID studentId =
                Objects.requireNonNull(
                        command.studentId(),
                        "Student is required"
                );

        UUID enrollmentId =
                Objects.requireNonNull(
                        command.studentEnrollmentId(),
                        "Student enrollment is required"
                );


        MarkSheet markSheet =
                requireMarkSheet(
                        tenantId,
                        markSheetId
                );

        requireEntryOpen(
                markSheet
        );


        students.findByTenantIdAndId(
                        tenantId,
                        studentId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Student not found"
                        )
                );


        StudentEnrollment enrollment =
                enrollments.findByTenantIdAndId(
                                tenantId,
                                enrollmentId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Student enrollment not found"
                                )
                        );


        if (
                !studentId.equals(
                        enrollment.getStudentId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Student enrollment does not belong to student"
            );
        }


        if (
                !"ACTIVE".equals(
                        enrollment.getEnrollmentStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "Student enrollment must be ACTIVE"
            );
        }


        ClassOffering classOffering =
                classOfferings.findByTenantIdAndId(
                                tenantId,
                                markSheet.getClassOfferingId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Class offering not found"
                                )
                        );


        if (
                !Objects.equals(
                        classOffering.getAcademicYearId(),
                        enrollment.getAcademicYearId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Student enrollment academic year does not match Mark Sheet"
            );
        }


        if (
                !Objects.equals(
                        classOffering.getCampusId(),
                        enrollment.getCampusId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Student enrollment campus does not match Mark Sheet"
            );
        }


        if (
                !Objects.equals(
                        classOffering.getClassGradeId(),
                        enrollment.getClassGradeId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Student enrollment class grade does not match Mark Sheet"
            );
        }


        if (
                markSheet.getStreamId() != null
                && !Objects.equals(
                        markSheet.getStreamId(),
                        enrollment.getStreamId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Student enrollment stream does not match Mark Sheet"
            );
        }


        validateExaminationCandidate(
                tenantId,
                command.examinationCandidateId(),
                studentId,
                enrollmentId
        );


        validateCandidatePaperRegistration(
                tenantId,
                markSheet,
                command.examinationCandidateId(),
                command.candidatePaperRegistrationId()
        );


        if (
                repository.existsByTenantIdAndMarkSheetIdAndStudentId(
                        tenantId,
                        markSheetId,
                        studentId
                )
        ) {

            throw new IllegalArgumentException(
                    "Candidate score already exists for Mark Sheet and student"
            );
        }


        CandidateScore candidateScore =
                new CandidateScore(
                        markSheetId,
                        command.examinationCandidateId(),
                        command.candidatePaperRegistrationId(),
                        studentId,
                        enrollmentId
                );

        candidateScore.setTenantId(
                tenantId
        );

        return repository.save(
                candidateScore
        );
    }


    @Transactional(readOnly = true)
    public CandidateScore get(
            UUID tenantId,
            UUID id
    ) {

        requireTenant(tenantId);

        Objects.requireNonNull(
                id,
                "Candidate score ID is required"
        );

        return repository.findByTenantIdAndId(
                        tenantId,
                        id
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Candidate score not found"
                        )
                );
    }


    @Transactional(readOnly = true)
    public CandidateScore getByMarkSheetAndStudent(
            UUID tenantId,
            UUID markSheetId,
            UUID studentId
    ) {

        requireTenant(tenantId);

        return repository
                .findByTenantIdAndMarkSheetIdAndStudentId(
                        tenantId,
                        Objects.requireNonNull(
                                markSheetId,
                                "Mark sheet is required"
                        ),
                        Objects.requireNonNull(
                                studentId,
                                "Student is required"
                        )
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Candidate score not found"
                        )
                );
    }


    public CandidateScore enterScore(
            UUID tenantId,
            UUID id,
            BigDecimal score,
            UUID actorId
    ) {

        CandidateScore candidateScore =
                get(
                        tenantId,
                        id
                );

        MarkSheet markSheet =
                requireMarkSheet(
                        tenantId,
                        candidateScore.getMarkSheetId()
                );

        requireEntryOpen(
                markSheet
        );


        if (
                score == null
                || score.compareTo(BigDecimal.ZERO) < 0
        ) {

            throw new IllegalArgumentException(
                    "Score must be zero or greater"
            );
        }


        if (
                score.compareTo(
                        markSheet.getMaximumScore()
                ) > 0
        ) {

            throw new IllegalArgumentException(
                    "Score must not exceed Mark Sheet maximum score"
            );
        }


        candidateScore.enterScore(
                score,
                actorId
        );

        return candidateScore;
    }


    public CandidateScore markAbsent(
            UUID tenantId,
            UUID id,
            UUID actorId
    ) {

        CandidateScore candidateScore =
                get(
                        tenantId,
                        id
                );

        MarkSheet markSheet =
                requireMarkSheet(
                        tenantId,
                        candidateScore.getMarkSheetId()
                );

        requireEntryOpen(
                markSheet
        );

        candidateScore.markAbsent(
                actorId
        );

        return candidateScore;
    }


    public CandidateScore validate(
            UUID tenantId,
            UUID id,
            UUID actorId
    ) {

        CandidateScore candidateScore =
                get(
                        tenantId,
                        id
                );

        candidateScore.validate(
                actorId
        );

        return candidateScore;
    }


    public CandidateScore approve(
            UUID tenantId,
            UUID id,
            UUID actorId
    ) {

        CandidateScore candidateScore =
                get(
                        tenantId,
                        id
                );

        candidateScore.approve(
                actorId
        );

        return candidateScore;
    }


    private MarkSheet requireMarkSheet(
            UUID tenantId,
            UUID markSheetId
    ) {

        return markSheets.findByTenantIdAndId(
                        tenantId,
                        markSheetId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Mark Sheet not found"
                        )
                );
    }


    private void requireEntryOpen(
            MarkSheet markSheet
    ) {

        if (
                !"OPEN".equals(
                        markSheet.getMarkSheetStatus()
                )
        ) {

            throw new IllegalStateException(
                    "Candidate score entry requires an OPEN Mark Sheet"
            );
        }
    }


    private void validateExaminationCandidate(
            UUID tenantId,
            UUID examinationCandidateId,
            UUID studentId,
            UUID enrollmentId
    ) {

        if (examinationCandidateId == null) {
            return;
        }


        ExaminationCandidate candidate =
                examinationCandidates.findByTenantIdAndId(
                                tenantId,
                                examinationCandidateId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Examination candidate not found"
                                )
                        );


        if (
                !Objects.equals(
                        candidate.getStudentId(),
                        studentId
                )
        ) {

            throw new IllegalArgumentException(
                    "Examination candidate does not belong to student"
            );
        }


        if (
                !Objects.equals(
                        candidate.getStudentEnrollmentId(),
                        enrollmentId
                )
        ) {

            throw new IllegalArgumentException(
                    "Examination candidate enrollment does not match"
            );
        }
    }


    private void validateCandidatePaperRegistration(
            UUID tenantId,
            MarkSheet markSheet,
            UUID examinationCandidateId,
            UUID candidatePaperRegistrationId
    ) {

        if (candidatePaperRegistrationId == null) {
            return;
        }


        if (examinationCandidateId == null) {

            throw new IllegalArgumentException(
                    "Examination candidate is required when Candidate Paper Registration is supplied"
            );
        }


        CandidatePaperRegistration registration =
                candidatePaperRegistrations
                        .findByTenantIdAndId(
                                tenantId,
                                candidatePaperRegistrationId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Candidate Paper Registration not found"
                                )
                        );


        if (
                !Objects.equals(
                        registration.getExaminationCandidateId(),
                        examinationCandidateId
                )
        ) {

            throw new IllegalArgumentException(
                    "Candidate Paper Registration does not match Examination Candidate"
            );
        }


        if (
                markSheet.getAssessmentPaperId() == null
        ) {

            throw new IllegalArgumentException(
                    "Candidate Paper Registration requires a paper-based Mark Sheet"
            );
        }


        if (
                !Objects.equals(
                        registration.getAssessmentPaperId(),
                        markSheet.getAssessmentPaperId()
                )
        ) {

            throw new IllegalArgumentException(
                    "Candidate Paper Registration paper does not match Mark Sheet"
            );
        }


        if (
                !"VERIFIED".equals(
                        registration.getRegistrationStatus()
                )
        ) {

            throw new IllegalArgumentException(
                    "Candidate Paper Registration must be VERIFIED"
            );
        }
    }


    private void requireTenant(
            UUID tenantId
    ) {

        if (tenantId == null) {

            throw new IllegalArgumentException(
                    "Tenant is required"
            );
        }
    }
}
