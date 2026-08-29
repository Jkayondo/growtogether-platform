package africa.growtogether.platform.school.learner;

import africa.growtogether.platform.common.persistence.EntityStatus;

import africa.growtogether.platform.school.enrollment.StudentEnrollment;
import africa.growtogether.platform.school.enrollment.StudentEnrollmentRepository;

import africa.growtogether.platform.school.student.Student;
import africa.growtogether.platform.school.student.StudentRepository;

import africa.growtogether.platform.school.relationship.StudentGuardianRelationship;
import africa.growtogether.platform.school.relationship.StudentGuardianRelationshipRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Learner360Service {

    private final StudentRepository studentRepository;

    private final StudentGuardianRelationshipRepository relationshipRepository;

    private final StudentEnrollmentRepository enrollmentRepository;

    public Learner360Service(
            StudentRepository studentRepository,
            StudentGuardianRelationshipRepository relationshipRepository,
            StudentEnrollmentRepository enrollmentRepository
    ) {
        this.studentRepository = studentRepository;
        this.relationshipRepository = relationshipRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public Learner360View get(
            UUID tenantId,
            UUID learnerId
    ) {

        Student student =
                studentRepository
                        .findByTenantIdAndId(
                                tenantId,
                                learnerId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Learner not found"
                                )
                        );

        List<StudentGuardianRelationship> relationships =
                relationshipRepository
                        .findByTenantIdAndStudentId(
                                tenantId,
                                learnerId
                        );

        StudentEnrollment enrollment =
                enrollmentRepository
                        .findFirstByTenantIdAndStudentIdAndEnrollmentStatusAndStatusOrderByEnrollmentDateDesc(
                                tenantId,
                                learnerId,
                                "ACTIVE",
                                EntityStatus.ACTIVE
                        )
                        .orElse(null);

        Learner360View.GuardianSummary guardianSummary =
                new Learner360View.GuardianSummary(
                        relationships.size(),
                        null,
                        !relationships.isEmpty()
                );

        return new Learner360View(
                learnerId,
                student.getPermanentLearnerNumber(),
                student.getStudentNumber(),
                student.getFirstName(),
                student.getMiddleName(),
                student.getLastName(),
                student.getPreferredName(),
                student.getStudentStatus(),

                guardianSummary,

                new Learner360View.AcademicSummary(
                        null,
                        null,
                        null
                ),

                new Learner360View.AttendanceSummary(
                        0,
                        0,
                        0
                ),

                new Learner360View.WelfareSummary(
                        0,
                        false
                ),

                new Learner360View.FinanceSummary(
                        "UNKNOWN",
                        0
                ),

                new Learner360View.HealthSummary(
                        false,
                        false
                )
        );
    }
}
