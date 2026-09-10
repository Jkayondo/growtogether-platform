package africa.growtogether.platform.school.assessment.examination.candidate;

import africa.growtogether.platform.school.assessment.examination.ExaminationSession;
import africa.growtogether.platform.school.assessment.examination.ExaminationSessionRepository;
import africa.growtogether.platform.school.enrollment.StudentEnrollment;
import africa.growtogether.platform.school.enrollment.StudentEnrollmentRepository;
import africa.growtogether.platform.school.student.StudentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;


@Service
@Transactional
public class ExaminationCandidateService {


    private final ExaminationCandidateRepository repository;
    private final ExaminationSessionRepository sessions;
    private final StudentRepository students;
    private final StudentEnrollmentRepository enrollments;


    public ExaminationCandidateService(
            ExaminationCandidateRepository repository,
            ExaminationSessionRepository sessions,
            StudentRepository students,
            StudentEnrollmentRepository enrollments
    ) {

        this.repository = repository;
        this.sessions = sessions;
        this.students = students;
        this.enrollments = enrollments;
    }


    public ExaminationCandidate register(
            UUID tenantId,
            String candidateNumber,
            UUID examinationSessionId,
            UUID studentId,
            UUID studentEnrollmentId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        String normalizedCandidateNumber =
                requireText(
                        candidateNumber,
                        "candidateNumber"
                );

        requireNonNull(
                examinationSessionId,
                "examinationSessionId"
        );

        requireNonNull(
                studentId,
                "studentId"
        );

        requireNonNull(
                studentEnrollmentId,
                "studentEnrollmentId"
        );


        ExaminationSession session =
                sessions
                        .findByTenantIdAndId(
                                tenantId,
                                examinationSessionId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Examination session not found for tenant"
                                )
                        );


        if (!"REGISTRATION_OPEN".equals(
                session.getSessionStatus()
        )) {
            throw new IllegalStateException(
                    "Examination session is not open for registration"
            );
        }


        students
                .findByTenantIdAndId(
                        tenantId,
                        studentId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Student not found for tenant"
                        )
                );


        StudentEnrollment enrollment =
                enrollments
                        .findByTenantIdAndId(
                                tenantId,
                                studentEnrollmentId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Student enrollment not found for tenant"
                                )
                        );


        if (!Objects.equals(
                studentId,
                enrollment.getStudentId()
        )) {
            throw new IllegalArgumentException(
                    "Student enrollment does not belong to student"
            );
        }


        if (!Objects.equals(
                session.getAcademicYearId(),
                enrollment.getAcademicYearId()
        )) {
            throw new IllegalArgumentException(
                    "Student enrollment does not belong to examination academic year"
            );
        }


        if (!Objects.equals(
                session.getCampusId(),
                enrollment.getCampusId()
        )) {
            throw new IllegalArgumentException(
                    "Student enrollment does not belong to examination campus"
            );
        }


        if (repository.existsByTenantIdAndCandidateNumber(
                tenantId,
                normalizedCandidateNumber
        )) {
            throw new IllegalArgumentException(
                    "Candidate number already exists"
            );
        }


        if (
                repository
                        .existsByTenantIdAndExaminationSessionIdAndStudentId(
                                tenantId,
                                examinationSessionId,
                                studentId
                        )
        ) {
            throw new IllegalArgumentException(
                    "Student already registered for examination session"
            );
        }


        ExaminationCandidate candidate =
                new ExaminationCandidate(
                        normalizedCandidateNumber,
                        examinationSessionId,
                        studentId,
                        studentEnrollmentId
                );


        candidate.setTenantId(
                tenantId
        );


        return repository.save(
                candidate
        );
    }


    @Transactional(readOnly = true)
    public ExaminationCandidate get(
            UUID tenantId,
            String candidateNumber
    ) {

        return repository
                .findByTenantIdAndCandidateNumber(
                        tenantId,
                        candidateNumber
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Examination candidate not found"
                        )
                );
    }


    public ExaminationCandidate verify(
            UUID tenantId,
            String candidateNumber,
            UUID verifiedBy
    ) {

        ExaminationCandidate candidate =
                get(
                        tenantId,
                        candidateNumber
                );

        candidate.verify(
                verifiedBy
        );

        return candidate;
    }


    private String requireText(
            String value,
            String field
    ) {

        if (
                value == null
                || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }

        return value.trim();
    }


    private <T> T requireNonNull(
            T value,
            String field
    ) {

        if (value == null) {
            throw new IllegalArgumentException(
                    field + " must not be null"
            );
        }

        return value;
    }

}
